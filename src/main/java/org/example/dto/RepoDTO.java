package org.example.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RepoDTO {
    private String name;
    private String url;
    @JsonAlias("contributors_url")
    private String contributorsUrl;

    public RepoDTO(String name, String url, String contributorsUrl) {
        this.name = name;
        this.url = url;
        this.contributorsUrl = contributorsUrl;
    }

    public RepoDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContributorsUrl() {
        return contributorsUrl;
    }

    public void setContributorsUrl(String contributorsUrl) {
        this.contributorsUrl = contributorsUrl;
    }
}
