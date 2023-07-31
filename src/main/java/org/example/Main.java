package org.example;

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
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Main {
    private static final String BASE_API_URL = "https://api.github.com/users/";
    private static final int REPOS_COUNT = 5;
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();


    public static void main(String[] args) {
        String login = "apache";
        try {
            Instant start = Instant.now();
            InfoEntity infoEntity = getInfoByLogin(login);
            System.out.println("Duration - " + Duration.between(start, Instant.now()));
            System.out.println(infoEntity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static InfoEntity getInfoByLogin(String login) throws InterruptedException {
        CompletableFuture<UserDTO> userDTOCompletableFuture =
                sendGetRequestAsync(BASE_API_URL.concat(login))
                        .thenApply(HttpResponse::body)
                        .thenApply(body -> (UserDTO) mapResponseBodyToDTO(body, UserDTO.class));
        return userDTOCompletableFuture
                .thenApplyAsync(userDTO -> sendGetRequestAsync(userDTO.getReposUrl())
                        .thenApply(HttpResponse::body)
                        .thenApply(body -> mapResponseBodyToListDTO(body, RepoDTO.class)))
                .thenApply(listCompletableFuture ->
                        listCompletableFuture
                                .join()
                                .stream()
                                .map(o -> (RepoDTO) o)
                                .parallel()
                                .unordered()
                                .limit(REPOS_COUNT)
                                .map(Main::sendRequestAndProcess)
                                .toList())
                .thenCombine(userDTOCompletableFuture, (repos, user) ->
                        new InfoEntity(user.getLogin(), user.getEmail(), repos))
                .join();
    }

    private static Repository sendRequestAndProcess(RepoDTO repoDTO) {
        try {
            List<String> contributors =
                    sendGetRequestAsync(repoDTO.getContributorsUrl())
                            .thenApply(HttpResponse::body)
                            .thenApply(body -> mapResponseBodyToListDTO(body, ContributorsDTO.class))
                            .join()
                            .stream()
                            .map(o -> (ContributorsDTO) o)
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

    private static <T> Object mapResponseBodyToDTO(String body, Class<T> elementType) {
        try {
            System.out.println(Instant.now() + " - " + Thread.currentThread().getName() + " map response to DTO");
            return objectMapper.readValue(body, elementType);
        } catch (Exception e) {
            String exceptionMessage = "Exception occurred! " + e.getMessage();
            System.out.println(exceptionMessage);
            throw new RuntimeException(exceptionMessage);
        }
    }

    private static <T> List<?> mapResponseBodyToListDTO(String body,
                                                        Class<T> elementType) {
        try {
            System.out.println(Instant.now() + " - " + Thread.currentThread().getName() + " map response to List DTO");
            return objectMapper.readValue(body, objectMapper.getTypeFactory().constructCollectionType(List.class, elementType));
        } catch (Exception e) {
            String exceptionMessage = "Exception occurred! " + e.getMessage();
            System.out.println(exceptionMessage);
            throw new RuntimeException(exceptionMessage);
        }
    }

    private static CompletableFuture<HttpResponse<String>> sendGetRequestAsync(String stringUri) {
        try {
            System.out.println(Instant.now() + " - " + Thread.currentThread().getName() + " send request to " + stringUri);
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