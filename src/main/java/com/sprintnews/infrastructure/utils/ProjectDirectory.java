package com.sprintnews.infrastructure.utils;

import java.nio.file.Paths;

public class ProjectDirectory {
    public static String getRoot() {
        return Paths.get("")
                .toAbsolutePath()
                .toString();
    }

}
