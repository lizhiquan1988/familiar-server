package com.example.demo.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Categories {
    @JsonProperty("Current")
    private Current current;

    @JsonProperty("Children")
    private Children children = new Children(); // 默认值避免null

    public Categories() {}

}

