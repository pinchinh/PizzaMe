package com.phuang.pizzame.model;

import java.util.List;

/**
 * Top level Java representation of JSON response.
 */
public class QueryResponse {
    public Query query;

    public static class Query {
        public int count;
        public String lang;
        public Results results;
    }

    public static class Results {
        public List<Store> Result;
    }
}
