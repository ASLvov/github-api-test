package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.ContributorsDTO;
import org.example.dto.RepoDTO;
import org.example.dto.UserDTO;
import org.example.entity.InfoEntity;
import org.example.entity.Repository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Main {
    private static final String BASE_API_URL = "https://api.github.com/users/";
    private static final int REPOS_COUNT = 10;
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();


    public static void main(String[] args) {
        String login = "pivotal";
        try {
            InfoEntity infoEntity = getInfoByLogin(login);
            System.out.println(infoEntity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static InfoEntity getInfoByLogin(String login) throws Exception {
        InfoEntity infoEntity = new InfoEntity();
        CompletableFuture<HttpResponse<String>> userCompletableFuture = sendGetRequestAsync(BASE_API_URL.concat(login));
        UserDTO userDTO = userCompletableFuture.thenApplyAsync(HttpResponse::body)
                .thenApplyAsync(body -> (UserDTO) mapResponseBodyToDTO(body, new TypeReference<UserDTO>() {
                }))
                .get();
        infoEntity.setLogin(userDTO.getLogin());
        infoEntity.setEmail(userDTO.getEmail());

        CompletableFuture<HttpResponse<String>> reposCompletableFuture = sendGetRequestAsync(userDTO.getReposUrl());
        List<RepoDTO> repoDTOList = reposCompletableFuture.thenApplyAsync(HttpResponse::body)
                .thenApplyAsync(body -> (List<RepoDTO>) mapResponseBodyToDTO(body, new TypeReference<List<RepoDTO>>() {
                }))
                .get();

        List<Repository> repositoryList = repoDTOList.stream()
                .parallel()
                .unordered()
                .limit(REPOS_COUNT)
                .map(Main::sendRequestAndProcess)
                .toList();

        infoEntity.setRepositoryList(repositoryList);

        return infoEntity;
    }

    private static Repository sendRequestAndProcess(RepoDTO repoDTO) {
        CompletableFuture<HttpResponse<String>> completableFuture = sendGetRequestAsync(repoDTO.getContributorsUrl());
        try {
            List<String> contributors = completableFuture.thenApplyAsync(HttpResponse::body)
                    .thenApplyAsync(body -> (List<ContributorsDTO>) mapResponseBodyToDTO(body,
                            new TypeReference<List<ContributorsDTO>>() {
                            }))
                    .get()
                    .stream()
                    .map(ContributorsDTO::getLogin)
                    .toList();
            Repository repository = new Repository();
            repository.setName(repoDTO.getName());
            repository.setUrl(repoDTO.getUrl());
            repository.setContributors(contributors);
            return repository;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object mapResponseBodyToDTO(String body, TypeReference<?> classInstance) {
        try {
            System.out.println(Thread.currentThread().getName() + " map response");
            return objectMapper.readValue(body, classInstance);
        } catch (Exception e) {
            String exceptionMessage = "Exception occurred! " + e.getMessage();
            System.out.println(exceptionMessage);
            throw new RuntimeException(exceptionMessage);
        }
    }

    private static CompletableFuture<HttpResponse<String>> sendGetRequestAsync(String stringUri) {
        try {
            System.out.println(Thread.currentThread().getName() + " send request to " + stringUri);
            URI uri = new URI(stringUri);
            HttpRequest httpRequest = HttpRequest.newBuilder(uri)
                    .GET()
                    .build();
            return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            String exceptionMessage = "Exception occurred! " + e.getMessage();
            System.out.println(exceptionMessage);
            throw new RuntimeException(exceptionMessage);
        }
    }
}