package com.example.demo.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Title {
    @JsonProperty("Short")
    private String shortTitle;

    @JsonProperty("Medium")
    private String mediumTitle;

    @JsonProperty("Long")
    private String longTitle;

    @JsonProperty("Name")
    private String name;

}
