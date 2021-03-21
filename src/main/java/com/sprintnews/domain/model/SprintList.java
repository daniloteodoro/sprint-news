package com.sprintnews.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sprintnews.domain.model.utils.TextUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class SprintList {
    private final List<Sprint> values;

    @JsonCreator
    public SprintList(@JsonProperty("values") List<Sprint> values) {
        this.values = values;
    }

    public List<Sprint> getValues() {
        return values;
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public static class Sprint {
        private final Long id;
        private final String self;
        private final String state;
        private final String name;
        private final String goal;
        private final String completeDate;
        private final String originBoardId;

        @JsonCreator
        public Sprint(@JsonProperty("id") Long id, @JsonProperty("self") String self, @JsonProperty("state") String state,
                      @JsonProperty("name") String name, @JsonProperty("goal") String goal, @JsonProperty("completeDate") String completeDate,
                      @JsonProperty("originBoardId") String originBoardId) {
            this.id = id;
            this.self = self;
            this.state = state;
            this.name = name;
            this.goal = TextUtils.removeFinalPunctuation(goal);
            this.completeDate = completeDate;
            this.originBoardId = originBoardId;
        }

        public LocalDate tryParsingCompleteDate() {
            try {
                return LocalDate.parse(completeDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX"));
            } catch (Exception e) {
                return null;
            }
        }

        public boolean isActive() {
            return this.state != null && this.state.equalsIgnoreCase("active");
        }
        public boolean hasGoal() {
            return this.goal != null && this.goal.trim().length() > 0;
        }

        public Long getId() {
            return id;
        }
        public String getSelf() {
            return self;
        }
        public String getState() {
            return state;
        }
        public String getName() {
            return name;
        }
        public String getGoal() {
            return goal;
        }
        public String getCompleteDate() {
            return completeDate;
        }
        public String getOriginBoardId() {
            return originBoardId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Sprint sprint = (Sprint) o;
            return id.equals(sprint.id);
        }
        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

}
