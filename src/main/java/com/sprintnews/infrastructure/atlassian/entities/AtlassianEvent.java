package com.sprintnews.infrastructure.atlassian.entities;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Entity(name = "atlassian_event")
public class AtlassianEvent {
    private final static String EVENT_UNINSTALLED = "uninstalled";
    private final static String EVENT_INSTALLED = "installed";

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "host")
    private final String host;
    @Column(name = "date_time")
    private final LocalDateTime dateTime;
    @Column(name = "event")
    private final String event;

    private AtlassianEvent(String host, LocalDateTime dateTime, String event) {
        this.host = Objects.requireNonNull(host, "Host is mandatory");
        this.dateTime = Objects.requireNonNull(dateTime, "Date/time is mandatory");
        this.event = Objects.requireNonNull(event, "Event is mandatory");
    }

    public static AtlassianEvent installedNow(String host) {
        return new AtlassianEvent(host != null && !host.isEmpty() ? host : "<unknown>",
                LocalDateTime.now(ZoneOffset.UTC),
                EVENT_INSTALLED);
    }

    public static AtlassianEvent uninstalledNow(String host) {
        return new AtlassianEvent(host != null && !host.isEmpty() ? host : "<unknown>",
                LocalDateTime.now(ZoneOffset.UTC),
                EVENT_UNINSTALLED);
    }

    // JPA
    protected AtlassianEvent() {
        this.host = "";
        this.dateTime = LocalDateTime.now(ZoneOffset.UTC);
        this.event = "";
    }

    public String getHost() {
        return host;
    }
    public LocalDateTime getDateTime() {
        return dateTime;
    }
    public String getEvent() {
        return event;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AtlassianEvent that = (AtlassianEvent) o;

        if (!host.equals(that.host)) return false;
        if (!dateTime.equals(that.dateTime)) return false;
        return event.equals(that.event);
    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + dateTime.hashCode();
        result = 31 * result + event.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s - %s", host, event);
    }
}
