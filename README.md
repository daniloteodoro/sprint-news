[![Build Status](https://github.com/daniloteodoro/sprint-news/actions/workflows/pr-check.yml/badge.svg)](https://github.com/daniloteodoro/sprint-news/actions)

## SprintNews Jira add-on
A plugin to generate a report with content tailored for stakeholders, with the goal of communicating the benefits stakeholders will get and managing their expectations on the product development.
It highlights the stakeholders addressed in the selected sprint, and the value they will get after stories 
get implemented. Up to five roadmaps are summarized, as well as previous sprints. You can visit the plugin on Jira's Market place [using this link](https://marketplace.atlassian.com/apps/1223324/sprint-news-sprint-report-for-jira?hosting=cloud&tab=overview&utm_source=github)

[Sample report generated using the plugin](https://github.com/daniloteodoro/sprint-news/blob/main/docs/sample-newspaper-2021-03-28.pdf): <br>
![Sample report generated using the plugin](https://github.com/daniloteodoro/sprint-news/blob/main/docs/sample-newspaper-2021-03-28.png?raw=true)

## Text generation algorithm
The text generation algorithm tries to adapt the request and benefit inside the user story into a text with the intention of describing requests and benefits for a given user.
To accomplish this there is an issue of linking these 2 parts: the text of the story and the text describing the request or benefit for a given user. 
Since the request / benefit text is already limited to 2 or 3 sentences, the solution was to limit the "variations" of the text extracted from the stories by analyzing their part of speech (PoS). 
This task was done using Apache OpenNLP. The last part was to build the block of text that will be used by the final report, which includes adding, updating and removing part of the text (tokens).
To make these changes more maintainable, each individual operation was implemented as a discrete rule.

![Text generation algorithm](https://github.com/daniloteodoro/sprint-news/blob/main/docs/generate-text-bpm.png?raw=true)

### Running the project locally and installing the add-on
1. Install ngrok and run `ngrok http 8080`
2. Copy the **https** address from ngrok and update parameter 'base-url' on the resources/application.yml file
3. Run `mvn spring-boot:run`
4. Install the add-on on Jira (menu Apps|Manage Apps, Upload button - first you have to enable Development mode using the Settings link on the same page)
5. Paste the `https` url you copied from ngrok
6. Go to one of your Jira project's backlog and you should see the "Get newspaper" button

#### Configurations
Environment variable SPRING_PROFILES_ACTIVE is set to `production` on Heroku <br>
Uses Liquibase for database migrations <br>
