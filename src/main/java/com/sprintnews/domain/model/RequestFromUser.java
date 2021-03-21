package com.sprintnews.domain.model;

public class RequestFromUser {
    private final String title;
    private final String description;

    public RequestFromUser(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
}
