package com.sprintnews.infrastructure.reports;

import com.sprintnews.domain.model.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Used for testing report directly in Jaspersoft Studio
 */
public class SampleData {
    public static java.util.Collection<SprintNewspaper> generateCollection() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"));
        String cityAndDate = String.format("%s, %s", "Rotterdam", today);
        String sprintBenefits = "This is a long text that is supposed to cover the first part of the report even some line breaks in order to occupy a big part of the newspaper. \n"+
                "Now this is a text that will be repeated a lot of times, even when we don't notice it will be there, being repeated, even when we don't notice it will be there, being repeated, "+
                "even when we don't notice it will be there, being repeated, even when we don't notice.  \nIt will be there, being repeated, even when we don't notice it will be there, being repeated, "+
                "even when we don't notice it will be there, being repeated, even when we don't notice it will be there, being repeated, even when we don't notice it will be there, being repeated, "+
                "even when we don't notice it will be there, being repeated, even when we don't notice it will be there, being repeated, even when we don't notice it will be there, being repeated, "+
                "even when we don't notice it will be there, being repeated, even when we don't notice. \nIt will be there, being repeated, even when we don't notice it will be there, being repeated, "+
                "even when we don't notice it will be there, being repeated, even when we don't notice it will be there, being repeated, even when we don't notice it will be there, being repeated, "+
                "even when we don't notice it will be there, being repeated, even when we don't notice. \nIt will be there, being repeated, even when we don't notice it will be there, being repeated, "+
                "even when we don't notice it will be there, being repeated, even when we don't notice it will be there, being repeated, even when we don't notice it will be there, being repeated, "+
                "even when we don't notice it will be there, being repeated, even when we don't notice. \nIt will be there, being repeated, even when we don't notice it will be there, being repeated, "+
                "even when we don't notice it will be there, being repeated, even when we don't notice it will be there, being repeated, even when we don't notice it will be there, being repeated, ";
        String sprintExplanation = "About Sprints: A sprint is a short, time-boxed period when a scrum team works to complete a set amount of work. Sprints help teams follow the agile principle of \"delivering working software frequently,\" as well as live the agile value of \"responding to change over following a plan.\" ¹";

        StructuredUserStory cra5 = new StructuredUserStory("CRA-5", "Welcome page", "Selected Partners", "Have a welcome page",
                "start immediately to look for the cars I want");
        StructuredUserStory cra4 = new StructuredUserStory("CRA-4", "Search for cars", "Selected Partners","request","benefit");
        StructuredUserStory cra9 = new StructuredUserStory("CRA-9", "Add child seat", "Selected Partners","request","benefit");
        List<StructuredUserStory> stories = Arrays.asList(cra5, cra4, cra9);

        CompleteStory cra5Complete = new CompleteStory(cra5.getKey(), cra5.getTitle(), "As a Selected Partner, I want to see a Welcome page with a search feature, so that I can start immediately to look for the car I want to rent.",
                new MainImageInfo("", null), String.format("https://sprintnews.atlassian.net/browse/%s", cra5.getKey()));
        CompleteStory cra4Complete = new CompleteStory(cra4.getKey(), cra4.getTitle(), "As a Selected Partner, I want to be able to search for the car I want, including category, so that I can choose the best car for my needs.\n" +
                "\n" +
                "Acceptance criteria: I enter the welcome page and see an option to enter a category and search for cars. After clicking Find car I can see the search results, that should only include cars in the category I specified, or all of them in case I left the category untouched.",
                new MainImageInfo("", null), String.format("https://sprintnews.atlassian.net/browse/%s", cra4.getKey()));
        CompleteStory cra9Complete = new CompleteStory(cra9.getKey(), cra9.getTitle(), "As a Selected Partner, I want to add extras to my reservation, including a Child seat, so that I can bring convenience to my associates.\n" +
                "\n" +
                "Acceptance criteria: Inside an in-progress reservation, I can see a link to “Extras” and see that page. On that page I can see among the extras the item “Child seat” and add it to my current reservation, updating the total amount due into EUR X.",
                new MainImageInfo("", null), String.format("https://sprintnews.atlassian.net/browse/%s", cra9.getKey()));
        List<CompleteStory> completeStories = Arrays.asList(cra5Complete, cra4Complete, cra9Complete);

        RequestFromUser req1 = new RequestFromUser("Selected Partners", "Sprint Rent it! includes requests from Selected Partners, that indicated in 2 stories that they want to have a Welcome page, be able to find cars they need, and add Child seat as extra.");
        RequestFromUser req2 = new RequestFromUser("Development Teams", "Sprint Rent it! includes requests from Development team, that expressed in 1 story the need for finding their favorite model. More on stories details on page 2.");
        RequestFromUser req3 = new RequestFromUser("Testers", "Sprint Rent it! includes requests from Testers, that expressed in 1 story the need for finding their favorite model. More on stories details on page 2.");
        RequestFromUser req4 = new RequestFromUser("PM", "Sprint Rent it! includes requests from PM, that expressed in 1 story the need for finding their favorite model. More on stories details on page 2.");
        List<RequestFromUser> requestFromUsers = Arrays.asList(req1, req2, req3, req4);

        Roadmap r1 = new Roadmap("Allow clients to reserve a car", LocalDate.now(),
                "This epic \"It makes possible to not only search for cars, but also reserve them online\" contains 6 stories, 3 of them in this sprint. The other 3 stories are: CRA-6: Select a specific car category, CRA-7: Enter client details, and CRA-8: Reserve a car and pay at checkout. The planned date to fully implement this epic is July 17th, 2020 (Friday).",
                Collections.emptyList(), Collections.emptyList());
        Roadmap r2 = new Roadmap("Selected partners", LocalDate.now(), "Extra features for the special group of partners",
                Collections.emptyList(), Collections.emptyList());

        List<Roadmap> epics = Arrays.asList(r1, r2);

        SprintNewspaper newspaper = new SprintNewspaper("Car Reservation App", "Rent it!",
                "Apples & Oranges", cityAndDate, "Allow clients to book their holiday cars", sprintBenefits, sprintExplanation, stories,
                new MainImageInfo("Search for cars (by John Doe)", null), completeStories, requestFromUsers, epics,
                "This is information about past sprints.",
                "This sprint is set to start in 2 day(s) and should take 11 day(s) to finish - August 14 (Friday). Get in touch with team Your team name... for instructions on how to use the product and attend the Sprint Review (demo), which is planned for Friday - August 21.",
                "Mariah");

        return Collections.singletonList(newspaper);
    }

}
