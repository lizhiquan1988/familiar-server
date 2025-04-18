package com.example.demo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZeroElement {
    @JsonProperty("Result")
    private Result result;

    // getter和setter
}
