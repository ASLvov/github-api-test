package org.example.entity;

import java.util.List;

public class Repository {
    private String name;
    private String url;
    private List<String> contributors;

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

    public List<String> getContributors() {
        return contributors;
    }

    public void setContributors(List<String> contributors) {
        this.contributors = contributors;
    }

    @Override
    public String toString() {
        return "Repository{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", contributors=" + contributors +
                '}';
    }
}
