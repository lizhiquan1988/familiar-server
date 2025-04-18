package com.example.demo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Current {

    @JsonProperty("Id")
    private String id;

    @JsonProperty("ParentId")
    private String parentId;

    @JsonProperty("Url")
    private String url;

    @JsonProperty("Title")
    private Title title;

    @JsonProperty("Path")
    private Path path;

    public Current() {}

    public Current(String id, String parentId, String url, Title title, Path path) {
        this.id = id;
        this.parentId = parentId;
        this.url = url;
        this.title = title;
        this.path = path;
    }

    @Override
    public String toString() {
        return "Current{" +
                "id='" + id + '\'' +
                ", parentId='" + parentId + '\'' +
                ", url='" + url + '\'' +
                ", title=" + title +
                ", path=" + path +
                '}';
    }
}

