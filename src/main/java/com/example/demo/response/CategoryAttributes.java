package com.example.demo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryAttributes {
    @JsonProperty("sortOrder")
    private String sortOrder;

    @JsonProperty("depth")
    private int depth;

}
