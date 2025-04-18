package com.example.demo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Result {
    @JsonProperty("Categories")
    private Categories categories;

    public Result() {}
}

