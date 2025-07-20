package com.retry.vuga.model;

public class Language {

    String engName;
    String id;
    String name;

    public Language(String engName, String name, String id) {
        this.engName = engName;
        this.name = name;
        this.id = id;
    }

    public String getEngName() {
        return engName;
    }

    public void setEngName(String engName) {
        this.engName = engName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}