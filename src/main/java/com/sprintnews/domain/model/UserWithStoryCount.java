package com.sprintnews.domain.model;

import org.springframework.lang.NonNull;

public class UserWithStoryCount implements Comparable<UserWithStoryCount> {
    private final String user;
    private final Integer storyCount;

    public UserWithStoryCount(String user, Integer storyCount) {
        this.user = user;
        this.storyCount = storyCount;
    }

    public String getUser() {
        return user;
    }
    public Integer getStoryCount() {
        return storyCount;
    }

    @Override
    public int compareTo(@NonNull UserWithStoryCount o) {
        return this.user.compareTo(o.user);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserWithStoryCount that = (UserWithStoryCount) o;

        return user.equals(that.user);
    }

    @Override
    public int hashCode() {
        return user.hashCode();
    }
}
