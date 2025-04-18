package com.example.demo.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChildCategory {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("Url")
    private String url;

    @JsonProperty("Title")
    private Title title;

    @JsonProperty("_attributes")
    private CategoryAttributes attributes;
}
