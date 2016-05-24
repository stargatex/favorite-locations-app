package com.example.lkj.mylocator.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LKJ on 5/18/2016.
 */
public class User {
    @JsonIgnore
    public G g;
    @JsonProperty("l")
    private List<Double> l = new ArrayList<Double>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public User() {
    }

    /**
     * @return The l
     */
    @JsonProperty("l")
    public List<Double> getL() {
        return l;
    }

    /**
     * @param l The l
     */
    @JsonProperty("l")
    public void setL(List<Double> l) {
        this.l = l;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return "User{" +
                "g=" + g +
                ", l=" + l +
                ", additionalProperties=" + additionalProperties +
                '}';
    }
}
