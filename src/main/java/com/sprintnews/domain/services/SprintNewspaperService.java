package com.sprintnews.domain.services;

import com.atlassian.connect.spring.AtlassianHostRestClients;
import com.sprintnews.domain.model.*;
import com.sprintnews.domain.model.exception.CannotGenerateReport;
import com.sprintnews.domain.model.templates.DefaultSprintNewsTemplateEnglishV1;
import com.sprintnews.infrastructure.utils.ProjectDirectory;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author daniloteodoro
 */

@Service
@SuppressWarnings("deprecation")
public class SprintNewspaperService {
    private static final Logger logger = LoggerFactory.getLogger(SprintNewspaperService.class);
    private static final String IMAGE_NOT_AVAILABLE = "/images/no-image-available.png";
    private static final String REPORT_LOCATION = "/target/reports/SprintNewsHomeV2.jasper";
    private static final int MAX_EPIC_COUNT = 5;

    @Value("${addon.base-url}")
    protected String baseUrl;

    public byte[] generate(AtlassianHostRestClients atlassianHostRestClient, NewspaperInput input) {

        OAuth2RestTemplate clientRestTemplate = atlassianHostRestClient.authenticatedAsHostActor();
        RestTemplate addOnRestTemplate = atlassianHostRestClient.authenticatedAsAddon();

        long t0 = System.currentTimeMillis();
        CompletableFuture<MainImageInfo> unavailableImageFuture =
                CompletableFuture.supplyAsync(() -> this.getUnavailableStoryImage(addOnRestTemplate));
        CompletableFuture<SprintList.Sprint> selectSprintFuture =
                CompletableFuture.supplyAsync(() -> getSprintDetails(clientRestTemplate, input.getSelectedSprint().getValue()));
        CompletableFuture<SimpleStoryList> storyListFuture =
                CompletableFuture.supplyAsync(() -> getStoryList(clientRestTemplate, input));

        try {
            CompletableFuture.allOf(selectSprintFuture, storyListFuture, unavailableImageFuture)
                    .get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("count#newspaper.get_sprint_stories.failure=1");
            throw new CannotGenerateReport("Could not get sprint / stories information to generate the newspaper. Try again in a few minutes");
        }

        // Mandatory items
        SprintList.Sprint selectedSprint = selectSprintFuture.getNow(null);
        if (selectedSprint == null) {
            logger.info("count#newspaper.get_sprint.failure=1");
            throw new CannotGenerateReport("Could not get sprint information to generate the newspaper");
        }
        SimpleStoryList storyList = storyListFuture.getNow(null);
        if (storyList == null || storyList.getIssues().isEmpty()) {
            logger.info("count#newspaper.no_stories.failure=1");
            throw new CannotGenerateReport("There are no stories to generate the newspaper");
        }
        MainImageInfo unavailableImage = unavailableImageFuture.getNow(null);
        if (unavailableImage == null) {
            logger.info("count#newspaper.unavailable_image.failure=1");
            throw new CannotGenerateReport("Failure preparing the report");
        }
        logger.debug(String.format("Fetch stories: %d ms", (System.currentTimeMillis() - t0)));

        long t1 = System.currentTimeMillis();
        CompletableFuture<List<SprintList.Sprint>> pastSprintsFuture =
                CompletableFuture.supplyAsync(() -> findPastSprints(clientRestTemplate, selectedSprint.getOriginBoardId()));

        CompletableFuture<List<Roadmap>> epicsFuture =
                CompletableFuture.supplyAsync(() -> findEpicsAssociatedWithStories(clientRestTemplate, storyList.getIssues()));

        Optional<SimpleStoryList.StoryImage> mainStoryImage = storyList.findMainImage();

        CompletableFuture<MainImageInfo> mainImageFuture = mainStoryImage.map(storyImage -> CompletableFuture.supplyAsync(() -> fetchStoryImage(clientRestTemplate, storyImage)))
                        .orElseGet(() -> CompletableFuture.supplyAsync(() -> unavailableImage));

        try {
            CompletableFuture.allOf(pastSprintsFuture, epicsFuture, mainImageFuture)
                    .get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("count#newspaper.additional_items.failure=1");
        }

        // Optional items
        List<SprintList.Sprint> pastSprints = pastSprintsFuture.getNow(new ArrayList<>());
        List<Roadmap> epics = epicsFuture.getNow(Collections.singletonList(Roadmap.CANNOT_LOAD));
        MainImageInfo mainImage = mainImageFuture.getNow(unavailableImage);

        logger.debug(String.format("Fetch roadmaps and main image: %d ms", (System.currentTimeMillis() - t1)));

        SimpleStoryList.StoryProject project = storyList.getFirstProjectFromStories();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"));
        String cityAndDate = String.format("%s, %s", input.getTeamCity(), today);

        long t2 = System.currentTimeMillis();
        // The only available template so far: DefaultSprintNewsTemplateEnglishV1
        Object newspaper = new DefaultSprintNewsTemplateEnglishV1().generate(storyList, input, selectedSprint, epics, pastSprints, mainImage);
        logger.debug(String.format("Generate newspaper content: %d ms", (System.currentTimeMillis() - t2)));

        long t3 = System.currentTimeMillis();
        JRBeanCollectionDataSource reportDataSource = new JRBeanCollectionDataSource(Collections.singletonList(newspaper));
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("project", project.getName());
            params.put("city_date", cityAndDate);

            JasperPrint finalReport = JasperFillManager.fillReport(
                    ProjectDirectory.getRoot() + REPORT_LOCATION,
                    params, reportDataSource);

            logger.debug(String.format("Fill out report with content: %d ms", (System.currentTimeMillis() - t3)));

            long t4 = System.currentTimeMillis();
            byte[] result = JasperExportManager.exportReportToPdf(finalReport);
            logger.debug(String.format("Export report as PDF: %d ms", (System.currentTimeMillis() - t4)));

            return result;

        } catch (JRException e) {
            logger.info("count#newspaper.report.failure=1");
            e.printStackTrace();
            throw new CannotGenerateReport("Failure generating the newspaper. The system admin has been notified.");
        }
    }

    private SimpleStoryList getStoryList(OAuth2RestTemplate clientRestTemplate, NewspaperInput input) {
        try {
            // Get only issues of type STORY from the sprint. Update: issueType can vary per project and raises an error, removing...
            String url = String.format("/rest/agile/1.0/sprint/%s/issue", input.getSelectedSprint().getValue());
            return clientRestTemplate.getForObject(url, SimpleStoryList.class);
        } catch (Exception e) {
            logger.info("count#newspaper.get_story_list.failure=1");
            throw new CannotGenerateReport("Failure getting stories for sprint " + input.getSelectedSprint().getValue(), e);
        }
    }

    private SprintList.Sprint getSprintDetails(OAuth2RestTemplate restTemplate, String id) {
        try {
            String url = String.format("/rest/agile/1.0/sprint/%s", id);
            return restTemplate.getForObject(url, SprintList.Sprint.class);
        } catch (Exception e) {
            logger.info("count#newspaper.get_sprint.failure=1");
            throw new CannotGenerateReport("Failure getting information for sprint " + id, e);
        }
    }

    private List<SprintList.Sprint> findPastSprints(OAuth2RestTemplate restTemplate, String boardId) {
        try {
            String url = String.format("/rest/agile/1.0/board/%s/sprint?state=active,closed", boardId);
            SprintList pastSprints = restTemplate.getForObject(url, SprintList.class);

            if (pastSprints == null || pastSprints.isEmpty()) {
                return new ArrayList<>();
            }

            // Display active sprints first. Then display the past sprints completed recently first.
            pastSprints.getValues().sort(Comparator.comparing(this::getCompletedDate).reversed());

            return pastSprints.getValues();

        } catch (RestClientException e) {
            e.printStackTrace();
            logger.info("count#newspaper.past_sprints.failure=1");
            return new ArrayList<>();
        }
    }

    private LocalDate getCompletedDate(SprintList.Sprint curr) {
        // Give prio for active sprints, if any.
        LocalDate replacement = curr.isActive() ? LocalDate.now() : LocalDate.of(2000, 1, 1);
        LocalDate completed = curr.tryParsingCompleteDate();
        return completed != null ? completed : replacement;
    }

    private boolean storyIsEpic(SimpleStoryList.SimpleStoryParent story) {
        return story != null && story.getFields().getIssueType().isEpic();
    }

    private SimpleStoryList.SimpleStory getEpic(OAuth2RestTemplate restTemplate, String epicUrl) {
        try {
            return restTemplate.getForObject(epicUrl, SimpleStoryList.SimpleStory.class);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("count#newspaper.get_epic.failure=1");
            return null;
        }
    }

    private Roadmap getRoadmap(OAuth2RestTemplate restTemplate, SimpleStoryList.SimpleStory epic, List<SimpleStoryList.SimpleStory> issues) {
        try {
            // Get a list of children issues that are not done.
            String url = String.format("/rest/api/2/search?jql=parent=%s and statuscategory!=Done&fields=summary", epic.getId());
            SimpleStoryList children = restTemplate.getForObject(url, SimpleStoryList.class);

            if (children == null || children.getIssues().isEmpty())
                return Roadmap.EmptyRoadmap(epic.getFields().getSummary());

            List<SimpleStoryList.SimpleStory> issueInThisSprint = children.getIssues().stream()
                    .filter(issues::contains)
                    .collect(Collectors.toList());
            List<SimpleStoryList.SimpleStory> issueOutsideThisSprint = children.getIssues().stream()
                    .filter(issue -> !issues.contains(issue))
                    .collect(Collectors.toList());

            return new Roadmap(epic.getFields().getSummary(), epic.getFields().tryParsingDueDate(null),
                    epic.getFields().getDescription(), issueInThisSprint, issueOutsideThisSprint);

        } catch (Exception e) {
            e.printStackTrace();
            logger.info("count#newspaper.get_epic.failure=1");
            return null;
        }
    }

    /**
     * Find epics associated with given stories. This can give a view of which part of the roadmap this sprint is completing,
     * although there could be other epics that simply are not listed because they don't have any stories in this sprint.
     */
    private List<Roadmap> findEpicsAssociatedWithStories(OAuth2RestTemplate restTemplate, List<SimpleStoryList.SimpleStory> issues) {
        try {
            return issues.stream()
                    .parallel()
                    .map(SimpleStoryList.SimpleStory::getFields)
                    .map(SimpleStoryList.SimpleStoryFields::getParent)
                    .filter(this::storyIsEpic)
                    .distinct() // it might be the same epic
                    .map(epic -> getEpic(restTemplate, epic.getSelf()))
                    .filter(Objects::nonNull)
                    .sorted()   // Most recent first
                    .limit(MAX_EPIC_COUNT)   // Only 2 roadmaps for now...
                    .map(epic -> getRoadmap(restTemplate, epic, issues))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("count#newspaper.get_epics.failure=1");
            return new ArrayList<>();
        }
    }

    private String getUnavailableImageUrl() {
        return baseUrl + IMAGE_NOT_AVAILABLE;
    }

    private MainImageInfo fetchStoryImage(OAuth2RestTemplate restTemplate, SimpleStoryList.StoryImage storyImage) {
        try {
            byte[] response = restTemplate.getForObject(storyImage.getImageUrl(), byte[].class);
            assert response != null;

            return MainImageInfo.of(storyImage.getName(), storyImage.getAuthor(), ImageIO.read(new ByteArrayInputStream(response)));
        } catch (Exception e) {
            logger.error("Failure getting image " + storyImage.getImageUrl());
            e.printStackTrace();
            throw new CannotGenerateReport("Failure generating images for the report");
        }
    }

    private MainImageInfo getUnavailableStoryImage(RestTemplate addOnRestTemplate) {
        try {
            byte[] response = addOnRestTemplate.getForObject(getUnavailableImageUrl(), byte[].class);
            assert response != null;

            return new MainImageInfo("(Please add an image to your highest priority story)", ImageIO.read(new ByteArrayInputStream(response)));
        } catch (Exception e) {
            logger.error("Failure getting 'unavailable' image " + getUnavailableImageUrl());
            e.printStackTrace();
            throw new CannotGenerateReport("Failure generating images for the report");
        }
    }

}
