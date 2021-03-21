package com.sprintnews.infrastructure.atlassian.repository;

import com.sprintnews.infrastructure.atlassian.entities.AtlassianEvent;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persist Atlassian events to the database. These events are usually not related to the core domain of the app.
 * @author daniloteodoro
 */
public interface HostInformationRepository extends JpaRepository<AtlassianEvent, Long> {}
