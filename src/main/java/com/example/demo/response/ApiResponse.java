package com.example.demo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiResponse {
    // Getter 和 Setter
    @JsonProperty("ResultSet")
    private ResultSet resultSet;

}

