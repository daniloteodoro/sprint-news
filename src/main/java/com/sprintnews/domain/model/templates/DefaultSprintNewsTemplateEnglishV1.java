package com.sprintnews.domain.model.templates;

import com.sprintnews.domain.model.NewspaperInput;
import com.sprintnews.domain.model.*;
import com.sprintnews.domain.model.exception.CannotGenerateReport;
import com.sprintnews.domain.model.exception.UserStoriesAreInvalid;
import com.sprintnews.domain.model.textgenerator.BenefitGenerator;
import com.sprintnews.domain.model.textgenerator.RequestGenerator;
import com.sprintnews.domain.model.utils.TextUtils;
import opennlp.tools.postag.POSModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.DetokenizationDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.sprintnews.domain.model.utils.TextUtils.generateHumanReadableList;
import static com.sprintnews.domain.model.utils.TextUtils.isBlank;

/**
 * This class builds the report in English using the default SprintNews template.
 * @author daniloteodoro
 */
public class DefaultSprintNewsTemplateEnglishV1 {
    private static final Logger logger = LoggerFactory.getLogger(DefaultSprintNewsTemplateEnglishV1.class);

    private final SentenceModel sentenceModel = generateSentenceModel();
    private final POSModel posModel = getPosModel();
    private final DetokenizationDictionary detokenizerDict = getDetokenizerDictionary();

    private SentenceModel generateSentenceModel() {
        try (InputStream is = getClass().getResourceAsStream("/models/en-sent.bin")) {
            return new SentenceModel(is);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failure loading sentence detector model");
        }
    }

    private POSModel getPosModel() {
        try (InputStream is = getClass().getResourceAsStream("/models/en-pos-maxent.bin")) {
            return new POSModel(is);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failure loading PoS tagger model");
        }
    }

    private DetokenizationDictionary getDetokenizerDictionary() {
        try (InputStream is = getClass().getResourceAsStream("/models/en-detokenizer.xml")) {
            return new DetokenizationDictionary(is);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failure loading Detokenizer Dictionary");
        }
    }

    public SprintNewspaper generate(SimpleStoryList storyList, NewspaperInput input, SprintList.Sprint selectedSprint,
                                    List<Roadmap> roadmaps, List<SprintList.Sprint> pastSprints, MainImageInfo mainImage) {
        long t0 = System.currentTimeMillis();
        SimpleStoryList.StoryProject project = storyList.getFirstProjectFromStories();
        if (project == null) {
            logger.info("count#newspaper.get_project.failure=1");
            throw new CannotGenerateReport("Could not get project based on stories to generate the newspaper");
        }

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"));
        String cityAndDate = String.format("%s, %s", input.getTeamCity(), today);
        String sprintExplanation = "About Sprints: A sprint is a short, time-boxed period when a scrum team works to complete a set amount of work. Sprints help teams follow the agile principle of \"delivering working software frequently,\" as well as live the agile value of \"responding to change over following a plan.\" ¹";

        List<StructuredUserStory> stories;
        try {
            stories = storyList.getIssues()
                    .stream()
                    .parallel()
                    .map(this::parseFromSimpleStory)
                    .filter(StructuredUserStory::isComplete)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("count#newspaper.parsing.failure=1");
            throw new CannotGenerateReport("Could not structure stories to generate report.");
        }

        Map<String, List<StructuredUserStory>> userStories = mapToUsers(stories);
        if (userStories.keySet().isEmpty()) {
            logger.info("count#newspaper.prediction_empty=1");
            throw new UserStoriesAreInvalid("Could not extract users. Does the sprint's stories follow the agile story template 'As a User, I want.. so that..' ? ");
        }

        logger.info("count#newspaper.prediction.success=1");

        SprintNewspaper result = new SprintNewspaper(project.getName(), selectedSprint.getName(), input.getTeamName(), cityAndDate,
                selectedSprint.getGoal(), getSprintBenefits(storyList, selectedSprint, userStories), sprintExplanation,
                stories, mainImage, Collections.emptyList(), getRequestFromUsers(userStories), describeRoadmaps(roadmaps),
                getInformationOnPastSprints(pastSprints, selectedSprint.getName()), generateSprintDetails(input), input.getProductOwner());

        System.out.println("Generating newspaper: " + (System.currentTimeMillis() - t0) + " ms");

        return result;
    }

    private List<RequestFromUser> getRequestFromUsers(Map<String, List<StructuredUserStory>> userStories) {
        try {
            RequestGenerator requestGenerator = new RequestGenerator(this.posModel, this.detokenizerDict);
            return userStories.entrySet().stream()
                    .map(mapping -> {

                        String rewrittenSentence = requestGenerator.generate(mapping.getKey(), mapping.getValue());

                        if (isBlank(rewrittenSentence))
                            return null;

                        return new RequestFromUser(mapping.getKey(), rewrittenSentence);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("count#newspaper.get_requests.failure=1");
            throw new CannotGenerateReport("Could not get requests from stories to generate report.");
        }
    }

    private String getSprintBenefits(Map<String, List<StructuredUserStory>> userStories) {
        List<String> referStory = Arrays.asList(
                " through story ",
                " with story ",
                " with story ",
                " after implementation of story ",
                " through story "
        );
        List<String> modalVerbList = Arrays.asList(
                " can ",
                " can ",
                " will ",
                " can ",
                " can ",
                " will "
        );

        BenefitGenerator benefitGenerator = new BenefitGenerator(this.posModel, this.detokenizerDict);

        return userStories.entrySet().stream()
                .map(userWithStories -> benefitGenerator.generate(userWithStories.getKey(), userWithStories.getValue()))
                .filter(TextUtils::isNotBlank)
                .collect(Collectors.joining("\n\n"));
    }

    private List<Roadmap> describeRoadmaps(List<Roadmap> roadmaps) {
        return roadmaps.stream()
                .peek(roadmap -> {

                    if (roadmap.isEmpty())
                        return;

                    String roadmapDescription = TextUtils.removeFinalPunctuation(roadmap.getOriginalDescription());

                    StringBuilder sb = new StringBuilder();
                    boolean multipleStories = roadmap.getIssueCount() > 1;
                    sb.append("This epic ")
                            .append(roadmapDescription.isEmpty() ? "" : TextUtils.addDoubleQuotes(roadmapDescription) + " ")
                            .append("contains ")
                            .append(roadmap.getIssueCount())
                            .append(multipleStories ? " stories, " : roadmap.getIssuesInCurrentSprint() == 1 ? " story, " : " story.")
                            .append(roadmap.getIssuesInCurrentSprint() > 0 ? roadmap.getIssuesInCurrentSprint() : "")
                            .append(multipleStories ? " of them in this sprint." : roadmap.getIssuesInCurrentSprint() == 1 ? " of which in this sprint." : "");

                    if (!roadmap.getStoriesOutsideTheSprint().isEmpty()) {
                        sb.append(" The other ")
                                .append(roadmap.getIssuesOutsideCurrentSprint())
                                .append(roadmap.getIssuesOutsideCurrentSprint() > 1 ? " stories are " : " story is ");
                        boolean manyStories = roadmap.getIssuesOutsideCurrentSprint() > 4;
                        sb.append(generateHumanReadableList(manyStories ? roadmap.getStoriesOutsideTheSprint().subList(0, 3) : roadmap.getStoriesOutsideTheSprint(),
                                SimpleStoryList.SimpleStory::getKeyAndSummary));
                        if (manyStories) {
                            sb.append(" (")
                                    .append(roadmap.getIssuesOutsideCurrentSprint() - 4)
                                    .append(" story(ies) hidden)");
                        }
                        sb.append(".");
                    }

                    if (roadmap.getDueDate() != null) {
                        sb.append(" The planned date to fully implement this epic is ")
                                .append(roadmap.getDueDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")))
                                .append(".");
                    } else {
                        sb.append(" There is no planned date to fully implement this specific roadmap.");
                    }

                    roadmap.setDescription(sb.toString());
                })
                .collect(Collectors.toList());
    }

    private String getSprintBenefits(SimpleStoryList storyList, SprintList.Sprint selectedSprint, Map<String, List<StructuredUserStory>> userStories) {
        StringBuilder benefits = new StringBuilder();
        if (storyList.getIssues().size() == 1) {
            benefits.append("Sprint ")
                    .append(selectedSprint.getName())
                    .append(" brings benefits for ");
        } else {
            benefits.append("The ")
                    .append(storyList.getIssues().size())
                    .append(" stories of sprint \"")
                    .append(selectedSprint.getName())
                    .append("\" bring benefits for ");
        }
        if (userStories.keySet().size() == 1) {
            benefits.append("one group of users: ");
        } else {
            benefits.append(userStories.keySet().size())
                    .append(" groups of users: ");
        }
        benefits.append(generateHumanReadableList(new ArrayList<>(userStories.keySet()), String::toString))
                .append(storyList.getIssues().size() > 1 ? ". See the reasons why these stories are " : ". See the reason why this story is ")
                .append("important to each stakeholder below.\n\n")
                .append(getSprintBenefits(userStories));

        return benefits.toString();
    }

    private Map<String, List<StructuredUserStory>> mapToUsers(List<StructuredUserStory> stories) {
        Map<String, List<StructuredUserStory>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        stories.forEach(story -> {
            List<StructuredUserStory> userStories;
            if (map.containsKey(story.getUserGroup())) {
                userStories = map.get(story.getUserGroup());
            } else {
                userStories = new ArrayList<>();
                map.put(story.getUserGroup(), userStories);
            }
            userStories.add(story);
        });
        return map;
    }

    private StructuredUserStory parseFromSimpleStory(SimpleStoryList.SimpleStory simpleStory) {
        final String END_OF_SENTENCE = "\n\n";

        if (simpleStory.getFields().getDescription() == null || simpleStory.getFields().getDescription().trim().isEmpty())
            return StructuredUserStory.emptyUserStory(simpleStory.getKey(), simpleStory.getFields().getSummary());

        try {
            SentenceDetectorME sdetector = new SentenceDetectorME(sentenceModel);
            // Clean up sentences containing 2 line breaks
            String[] detectedSentences = sdetector.sentDetect(simpleStory.getFields().getDescription());
            for (int i = 0; i < detectedSentences.length; i++) {
                if (detectedSentences[i] == null || detectedSentences[i].isEmpty())
                    continue;
                if (detectedSentences[i].contains(END_OF_SENTENCE)) {
                    detectedSentences[i] = detectedSentences[i].substring(0, detectedSentences[i].indexOf(END_OF_SENTENCE));
                }
            }
            return StructuredUserStory.parse(simpleStory.getKey(), simpleStory.getFields().getSummary(), detectedSentences );
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("count#newspaper.parsing.failure=1");
            logger.error("Failure parsing this story: \n" + simpleStory.getFields().getDescription());
            return StructuredUserStory.emptyUserStory(simpleStory.getKey(), simpleStory.getFields().getSummary());
        }
    }

    private String generateSprintDetails(NewspaperInput input) {
        StringBuilder sb = new StringBuilder();

        if (input.hasStartDate()) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), input.tryToParseStartDate());
            if (days < 0) {
                sb.append("This sprint started ")
                        .append(Math.abs(days))
                        .append(" day(s) ago");
            } else if (days == 0) {
                sb.append("This sprint started today");
            } else {
                sb.append("This sprint is set to start in ")
                        .append(days)
                        .append(" day(s)");
            }
        }

        if (input.hasEndDate()) {
            long daysToFinish = ChronoUnit.DAYS.between(LocalDate.now(), input.tryToParseEndDate());
            if (input.hasStartDate()) {
                long days = ChronoUnit.DAYS.between(input.tryToParseStartDate(), input.tryToParseEndDate());

                if (daysToFinish > 0) {
                    sb.append(" and should take ")
                            .append(days)
                            .append(" day(s) to finish, on ");
                } else if (daysToFinish == 0) {
                    sb.append(" and should be finished today, ");
                } else {
                    sb.append(" and finished ")
                            .append(Math.abs(daysToFinish))
                            .append(" day(s) ago, on ");
                }
            } else {
                if (daysToFinish < 0)
                    sb.append("This sprint is estimated to be finished on ");
                else if (daysToFinish == 0)
                    sb.append("This sprint should be finished today, ");
                else
                    sb.append("This sprint finished ")
                            .append(daysToFinish)
                            .append(" day(s) ago, on ");
            }
            sb.append(input.tryToParseEndDate().format(DateTimeFormatter.ofPattern("MMMM dd (EEEE)")))
                    .append(".");
        }

        sb.append(" Get in touch with team ")
                .append(input.getTeamName())
                .append(" for instructions on how to use the product");
        if (input.hasSprintReviewDate())
            sb.append(" and attend the Sprint Review (demo), which is planned for ")
                    .append(input.tryToParseSprintReviewDate().format(DateTimeFormatter.ofPattern("EEEE - MMMM dd")));
        sb.append(".");
        return sb.toString();
    }

    private String getInformationOnPastSprints(List<SprintList.Sprint> pastSprints, String sprintName) {
        try {

            if (pastSprints == null || pastSprints.isEmpty()) {
                return String.format("It looks like there were no sprints before sprint \"%s\".", sprintName);
            }

            StringBuilder sb = new StringBuilder();

            SprintList.Sprint firstSprint = pastSprints.get(0);
            sb.append("Before this sprint we had sprint ")
                    .append(firstSprint.getName())
                    .append(", whose goal was \"")
                    .append(firstSprint.getGoal())
                    .append("\".");
            if (firstSprint.tryParsingCompleteDate() != null) {
                sb.append(" It was completed on ")
                        .append(firstSprint.tryParsingCompleteDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")))
                        .append(".");
            }
            if (pastSprints.size() > 1) {
                pastSprints = pastSprints.subList(1, pastSprints.size());
                firstSprint = pastSprints.get(0);
                sb.append(" There was also sprint ")
                        .append(firstSprint.getName())
                        .append(firstSprint.hasGoal() ? " with goal \"" : "")
                        .append(firstSprint.hasGoal() ? firstSprint.getGoal() : "")
                        .append(firstSprint.hasGoal() ? "\"" : "");

                if (pastSprints.size() > 1) {
                    sb.append(" and ")
                            .append(pastSprints.size() - 1)
                            .append(" more past sprint(s) completed for this project");
                }
                sb.append(".");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("count#newspaper.past_sprints.failure=1");
            return "";
        }
    }

}
