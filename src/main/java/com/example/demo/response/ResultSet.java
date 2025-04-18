package com.example.demo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultSet {
    @JsonProperty("0")
    private ZeroElement zero;

    @JsonProperty("totalResultsReturned")
    private String totalResultsReturned;

    // 无参构造函数
    public ResultSet() {}
}

