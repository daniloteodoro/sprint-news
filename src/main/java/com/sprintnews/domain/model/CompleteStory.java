package com.sprintnews.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CompleteStory {
    private final String key;
    private final String title;
    private final String description;
    private final MainImageInfo image;
    private final String url;

    @JsonCreator
    public CompleteStory(@JsonProperty("key") String key, @JsonProperty("title") String title, @JsonProperty("description") String description,
                         @JsonProperty("image") MainImageInfo image, @JsonProperty("url") String url) {
        this.key = key;
        this.title = title;
        this.description = description;
        this.image = image;
        this.url = url;
    }

    public String getKey() {
        return key;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public MainImageInfo getImage() {
        return image;
    }
    public String getUrl() {
        return url;
    }
}
