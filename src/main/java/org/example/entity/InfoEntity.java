package org.example.entity;

import java.util.List;

public class InfoEntity {
    private String login;
    private String email;
    private List<Repository> repositoryList;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Repository> getRepositoryList() {
        return repositoryList;
    }

    public void setRepositoryList(List<Repository> repositoryList) {
        this.repositoryList = repositoryList;
    }

    @Override
    public String toString() {
        return "InfoEntity{" +
                "login='" + login + '\'' +
                ", email='" + email + '\'' +
                ", repositoryList=" + repositoryList +
                '}';
    }
}
