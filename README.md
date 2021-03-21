## SprintNews Jira add-on
A plugin to generate report with content tailored for stakeholders, with the goal of communicating the development of the product and managing expectations.
It highlights the stakeholders addressed in the selected sprint, and the value they will get after stories 
get implemented. Up to five roadmaps are summarized, as well as previous sprints.

#### Running the project locally and installing the add-on
1. Install ngrok and run `ngrok http 8080`
2. Copy the **https** address and update parameter 'base-url' on resources/application.yml file
3. Run `mvn spring-boot:run`
4. Install the add-on on Jira (menu Apps|Manage Apps, Upload button - first you have to enable Development mode using the Settings link on the same page)
5. Paste the `https` url you copied from ngrok
6. Go to one of your Jira project's backlog and you should see the "Get newspaper" button

#### Configurations
Environment variable SPRING_PROFILES_ACTIVE is set to `production` on Heroku <br>
Uses Liquibase for database migrations <br>
