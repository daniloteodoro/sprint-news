package com.sprintnews.domain.model;

import com.google.gson.Gson;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class NewspaperInput {
    private final String teamName;
    private final String teamCity;
    private final SelectedSprint selectedSprint;
    private final String startDate;
    private final String endDate;
    private final String reviewDate;
    private final String productOwner;

    public NewspaperInput(String teamName, String teamCity, SelectedSprint selectedSprint, String startDate, String endDate, String reviewDate, String productOwner) {
        this.teamName = teamName;
        this.teamCity = teamCity;
        this.selectedSprint = selectedSprint;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reviewDate = reviewDate;
        this.productOwner = productOwner;
    }

    public NewspaperInput() {
        this.teamName = "";
        this.teamCity = "";
        this.selectedSprint = SelectedSprint.none();
        this.startDate = "";
        this.endDate = "";
        this.reviewDate = "";
        this.productOwner = "";
    }

    public LocalDate tryToParseStartDate() {
        try {
            return LocalDate.parse(this.startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            return null;
        }
    }
    public LocalDate tryToParseEndDate() {
        try {
            return LocalDate.parse(this.endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            return null;
        }
    }
    public LocalDate tryToParseSprintReviewDate() {
        try {
            return LocalDate.parse(this.reviewDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            return null;
        }
    }

    public boolean hasStartDate() {
        return tryToParseStartDate() != null;
    }
    public boolean hasEndDate() {
        return tryToParseEndDate() != null;
    }
    public boolean hasSprintReviewDate() {
        return tryToParseSprintReviewDate() != null;
    }

    public String getTeamName() {
        return teamName;
    }
    public String getTeamCity() {
        return teamCity;
    }
    public SelectedSprint getSelectedSprint() {
        return selectedSprint;
    }
    public String getStartDate() {
        return startDate;
    }
    public String getEndDate() {
        return endDate;
    }
    public String getReviewDate() {
        return reviewDate;
    }
    public String getProductOwner() { return productOwner; }

    @Override
    public String toString() {
        return String.format("TeamName: %s, TeamCity: %s, selectedSprint: %s", teamName, teamCity, selectedSprint);
    }

    public static class SelectedSprint {
        private final static SelectedSprint EMPTY = new SelectedSprint("", "");
        private final String label;
        private final String value;

        public SelectedSprint(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }
        public String getValue() {
            return value;
        }

        public static SelectedSprint none() {
            return EMPTY;
        }

        @Override
        public String toString() {
            return "id: " + value;
        }
    }

    public static NewspaperInput valueOf(String json) {
        return new Gson().fromJson(json, NewspaperInput.class);
    }

}
