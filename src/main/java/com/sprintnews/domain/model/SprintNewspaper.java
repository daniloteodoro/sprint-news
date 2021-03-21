package com.sprintnews.domain.model;

import java.util.List;
import java.util.stream.Collectors;

public class SprintNewspaper {
    private final String teamName;
    private final String cityAndDate;
    private final String sprintGoal;
    private final String sprintBenefits;
    private final String sprintExplanation;
    private final List<StructuredUserStory> stories;
    private final String projectName;
    private final String sprintName;
    private final MainImageInfo mainImage;
    private final List<CompleteStory> completeStories;
    private final List<RequestFromUser> requests;
    private final List<Roadmap> roadmaps;
    private final String infoOnPastSprints;
    private final String currentSprintInfo;
    private final String productOwnerName;
    private final List<UserWithStoryCount> userWithStories;

    // TODO: Create builder
    public SprintNewspaper(String projectName, String sprintName, String teamName, String cityAndDate, String sprintGoal, String sprintBenefits, String sprintExplanation,
                           List<StructuredUserStory> stories, MainImageInfo mainImage, List<CompleteStory> completeStories,
                           List<RequestFromUser> requests, List<Roadmap> roadmaps, String infoOnPastSprints, String currentSprintInfo,
                           String productOwnerName) {
        this.teamName = teamName;
        this.cityAndDate = cityAndDate;
        this.sprintGoal = sprintGoal;
        this.sprintBenefits = sprintBenefits;
        this.sprintExplanation = sprintExplanation;
        this.stories = stories;
        this.projectName = projectName;
        this.sprintName = sprintName;
        this.mainImage = mainImage;
        this.completeStories = completeStories;
        this.requests = requests;
        this.roadmaps = roadmaps;
        this.infoOnPastSprints = infoOnPastSprints;
        this.currentSprintInfo = currentSprintInfo;
        this.productOwnerName = productOwnerName;
        this.userWithStories = buildListOfUserWithStories(stories);
    }

    public String getTeamName() {
        return teamName;
    }
    public String getCityAndDate() {
        return cityAndDate;
    }
    public String getSprintGoal() {
        return sprintGoal;
    }
    public String getSprintBenefits() {
        return sprintBenefits;
    }
    public String getSprintExplanation() {
        return sprintExplanation;
    }
    public List<StructuredUserStory> getStories() {
        return stories;
    }
    public String getProjectName() {
        return projectName;
    }
    public String getSprintName() {
        return sprintName;
    }
    public MainImageInfo getMainImage() {
        return mainImage;
    }
    public List<CompleteStory> getCompleteStories() {
        return completeStories;
    }
    public List<RequestFromUser> getRequests() {
        return requests;
    }
    public List<Roadmap> getRoadmaps() {
        return roadmaps;
    }
    public String getInfoOnPastSprints() {
        return infoOnPastSprints;
    }
    public String getCurrentSprintInfo() {
        return currentSprintInfo;
    }
    public String getProductOwnerName() { return productOwnerName; }
    public List<UserWithStoryCount> getUserWithStories() { return userWithStories; }

    private List<UserWithStoryCount> buildListOfUserWithStories(List<StructuredUserStory> origin) {
        return origin.stream()
                .collect(Collectors.groupingBy(StructuredUserStory::getUserGroup))
                .entrySet().stream()
                .map(item -> new UserWithStoryCount(item.getKey(), item.getValue().size()))
                .collect(Collectors.toList());
    }
}
