package com.project.insajang.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class InstagramInsightResponse {
    private List<Data> data;

    @Getter @Setter
    public static class Data {
        private String name;
        private List<Value> values;
    }

    @Getter @Setter
    public static class Value {
        private Integer value;
    }
}