package com.sprintnews.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.awt.image.BufferedImage;

public class MainImageInfo {
    private final String title;
    private final BufferedImage image;

    @JsonCreator
    public MainImageInfo(@JsonProperty("title") String title, @JsonProperty("image") BufferedImage image) {
        this.title = title;
        this.image = image;
    }

    public static MainImageInfo of(String storySummary, String reporter, BufferedImage image) {
        return new MainImageInfo(String.format("Picture of story \"%s\" (by %s)", storySummary, reporter), image);
    }

    public String getTitle() {
        return title;
    }
    public BufferedImage getImage() {
        return image;
    }
}
