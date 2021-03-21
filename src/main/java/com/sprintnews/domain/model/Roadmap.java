package com.sprintnews.domain.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Roadmap {
    public static final Roadmap CANNOT_LOAD = EmptyRoadmap("(Could not get epic information)");

    private final String title;
    private final LocalDate dueDate;
    private final String originalDescription;
    private final List<SimpleStoryList.SimpleStory> storiesInTheSprint;
    private final List<SimpleStoryList.SimpleStory> storiesOutsideTheSprint;
    private String description;

    public Roadmap(String title, LocalDate dueDate, String originalDescription,
                   List<SimpleStoryList.SimpleStory> storiesInTheSprint, List<SimpleStoryList.SimpleStory> storiesOutsideTheSprint) {
        this.title = title;
        this.dueDate = dueDate;
        this.originalDescription = originalDescription;
        this.storiesInTheSprint = storiesInTheSprint;
        this.storiesOutsideTheSprint = storiesOutsideTheSprint;
    }

    public static Roadmap EmptyRoadmap(String title) {
        return new Roadmap(title, LocalDate.MIN, "", Collections.emptyList(), Collections.emptyList());
    }

    public boolean isEmpty() {
        return (dueDate != null &&
                dueDate.isEqual(LocalDate.MIN) &&
                "".equals(originalDescription) &&
                getIssueCount() == 0);
    }

    public long getIssueCount() {
        return storiesInTheSprint.size() + storiesOutsideTheSprint.size();
    }
    public long getIssuesInCurrentSprint() {
        return storiesInTheSprint.size();
    }
    public long getIssuesOutsideCurrentSprint() {
        return storiesOutsideTheSprint.size();
    }

    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public LocalDate getDueDate() {
        return dueDate;
    }
    public String getOriginalDescription() {
        return originalDescription;
    }
    public List<SimpleStoryList.SimpleStory> getStoriesInTheSprint() {
        return storiesInTheSprint;
    }
    public List<SimpleStoryList.SimpleStory> getStoriesOutsideTheSprint() {
        return storiesOutsideTheSprint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Roadmap roadmap = (Roadmap) o;

        if (!title.equals(roadmap.title)) return false;
        if (!Objects.equals(dueDate, roadmap.dueDate)) return false;
        return Objects.equals(originalDescription, roadmap.originalDescription);
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + (dueDate != null ? dueDate.hashCode() : 0);
        result = 31 * result + (originalDescription != null ? originalDescription.hashCode() : 0);
        return result;
    }
}
