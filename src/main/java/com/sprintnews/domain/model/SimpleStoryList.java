package com.sprintnews.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class SimpleStoryList {
    private static final Logger logger = LoggerFactory.getLogger(SimpleStoryList.class);

    private final List<SimpleStory> issues;

    @JsonCreator
    public SimpleStoryList(@JsonProperty("issues") List<SimpleStory> issues) {
        this.issues = issues != null ? issues : new ArrayList<>();
    }

    public List<SimpleStory> getIssues() {
        return issues;
    }

    /**
     * Get image from the main story. Main story is based on priority.
     * Use the first story in case all stories share the same priority.
     * @return The image content (link) and author.
     */
    public Optional<StoryImage> findMainImage() {
        if (issues.isEmpty())
            return Optional.empty();

        SimpleStory mainStory = issues.stream()
                .min(Comparator.comparingInt(story -> story.fields.getPriority().getId()))
                .get();
        List<StoryAttachment> images = mainStory.getImages();
        if (images.isEmpty())
            return Optional.empty();
        StoryAttachment firstImage = images.get(0);

        return Optional.of(new StoryImage(mainStory.fields.getSummary(), firstImage.author.getDisplayName(), firstImage.content));
    }

    public StoryProject getFirstProjectFromStories() {
        if (issues.isEmpty())
            return null;
        return issues.get(0).fields.project;
    }

    public static class StoryImage {
        private final String name;
        private final String author;
        private final String imageUrl;

        public StoryImage(String name, String author, String imageUrl) {
            this.name = name;
            this.author = author;
            this.imageUrl = imageUrl;
        }

        public String getName() {
            return name;
        }
        public String getAuthor() {
            return author;
        }
        public String getImageUrl() {
            return imageUrl;
        }
    }

    public static class SimpleStory implements Comparable<SimpleStory> {
        private final String id;
        private final String key;
        private final SimpleStoryFields fields;

        @JsonCreator
        public SimpleStory(@JsonProperty("id") String id, @JsonProperty("key") String key, @JsonProperty("fields") SimpleStoryFields fields) {
            this.id = id;
            this.key = key;
            this.fields = fields;
        }

        public List<StoryAttachment> getImages() {
            return fields.attachment.stream()
                    .filter(StoryAttachment::isImage)
                    .collect(Collectors.toList());
        }

        public StoryAttachment getFirstImage() {
            List<StoryAttachment> images = getImages();
            return !images.isEmpty() ? images.get(0) : null;
        }

        public String getKeyAndSummary() {
            return String.format("%s %s", getKey(), getFields().getSummary());
        }

        @Override
        public int compareTo(SimpleStory another) {
            if (getLocalDateOrMax().isAfter(another.getLocalDateOrMax()))
                return 1;
            else if (getLocalDateOrMax().isBefore(another.getLocalDateOrMax()))
                return -1;

            return 0;
        }

        public LocalDate getLocalDateOrMax() {
            return fields.tryParsingDueDate(LocalDate.MAX);
        }

        public String getId() {
            return id;
        }
        public String getKey() {
            return key;
        }
        public SimpleStoryFields getFields() {
            return fields;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SimpleStory that = (SimpleStory) o;
            return id.equals(that.id);
        }
        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    public static class SimpleStoryFields {
        private final String summary;
        private final String description;
        private final StoryAuthor reporter;
        private final StoryPriority priority;
        private final List<StoryAttachment> attachment;
        private final StoryProject project;
        private final SimpleStoryParent parent;
        private final String dueDate;
        private final IssueType issuetype;

        @JsonCreator
        public SimpleStoryFields(@JsonProperty("summary") String summary, @JsonProperty("description") String description,
                                 @JsonProperty("reporter") StoryAuthor reporter, @JsonProperty("priority") StoryPriority priority,
                                 @JsonProperty("attachment") List<StoryAttachment> attachment, @JsonProperty("project") StoryProject project,
                                 @JsonProperty("parent") SimpleStoryParent parent, @JsonProperty("duedate") String dueDate,
                                 @JsonProperty("issuetype") IssueType issuetype) {
            this.summary = summary;
            this.description = description;
            this.reporter = reporter;
            this.priority = priority;
            this.attachment = attachment;
            this.project = project;
            this.parent = parent;
            this.dueDate = dueDate;
            this.issuetype = issuetype;
        }

        public LocalDate parseDueDate() {
            return LocalDate.parse(this.dueDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        public LocalDate tryParsingDueDate(LocalDate defaultValue) {
            try {
                return parseDueDate();
            } catch (Exception ignored) {
                return defaultValue;
            }
        }

        public String getSummary() {
            return summary;
        }
        public String getDescription() {
            return description;
        }
        public StoryAuthor getReporter() {
            return reporter;
        }
        public StoryPriority getPriority() {
            return priority;
        }
        public List<StoryAttachment> getAttachment() {
            return attachment;
        }
        public StoryProject getProject() {
            return project;
        }
        public SimpleStoryParent getParent() {
            return parent;
        }
        public String getDueDate() {
            return dueDate;
        }
        public IssueType getIssueType() {
            return issuetype;
        }
    }

    public static class IssueType {
        private final String name;

        @JsonCreator
        public IssueType(@JsonProperty("name") String name) {
            this.name = name;
        }

        public boolean isEpic() {
            return this.name != null &&
                    this.name.equalsIgnoreCase("Epic");
        }

        public String getName() {
            return name;
        }
    }

    public static class SimpleStoryParent implements Comparable<SimpleStoryParent> {
        private final Long id;
        private final String key;
        private final String self;
        private final SimpleStoryFields fields;

        @JsonCreator
        public SimpleStoryParent(@JsonProperty("id") Long id, @JsonProperty("key") String key,
                                 @JsonProperty("self") String self, @JsonProperty("fields") SimpleStoryFields fields) {
            this.id = id;
            this.key = key;
            this.self = self;
            this.fields = fields;
        }

        public Long getId() {
            return id;
        }
        public String getKey() {
            return key;
        }
        public String getSelf() {
            return self;
        }
        public SimpleStoryFields getFields() {
            return fields;
        }

        public LocalDate getLocalDateOrMax() {
            return fields.tryParsingDueDate(LocalDate.MAX);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SimpleStoryParent that = (SimpleStoryParent) o;
            return id.equals(that.id);
        }
        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public int compareTo(SimpleStoryParent another) {
            if (getLocalDateOrMax().isAfter(another.getLocalDateOrMax()))
                return 1;
            else if (getLocalDateOrMax().isBefore(another.getLocalDateOrMax()))
                return -1;

            return 0;
        }
    }

    public static class StoryProject  {
        private final String name;

        @JsonCreator
        public StoryProject(@JsonProperty("name") String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class StoryAttachment {
        private final String filename;
        private final StoryAuthor author;
        private final Long size;
        private final String mimeType;
        private final String content;

        @JsonCreator
        public StoryAttachment(@JsonProperty("filename") String filename, @JsonProperty("author") StoryAuthor author,
                               @JsonProperty("size") Long size, @JsonProperty("mimeType") String mimeType,
                               @JsonProperty("content") String content) {
            this.filename = filename;
            this.author = author;
            this.size = size;
            this.mimeType = mimeType;
            this.content = content;
        }

        public boolean isImage() {
            return (content != null && !content.isEmpty()) &&
                    (getMimeType().equalsIgnoreCase("image/jpeg") ||
                    getMimeType().equalsIgnoreCase("image/png"));
        }

        public String getFilename() {
            return filename;
        }
        public StoryAuthor getAuthor() {
            return author;
        }
        public Long getSize() {
            return size;
        }
        public String getMimeType() {
            return mimeType;
        }
        public String getContent() {
            return content;
        }
    }

    public static class StoryAuthor {
        private final String displayName;

        @JsonCreator
        public StoryAuthor(@JsonProperty("displayName") String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public static class StoryPriority {
        private final Integer id;
        private final String name;

        @JsonCreator
        public StoryPriority(@JsonProperty("id") Integer id, @JsonProperty("name") String name) {
            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }
        public String getName() {
            return name;
        }
    }
}
