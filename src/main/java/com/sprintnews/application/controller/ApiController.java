package com.sprintnews.application.controller;

import com.atlassian.connect.spring.AtlassianHostRestClients;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.ContextJwt;
import com.sprintnews.domain.model.NewspaperInput;
import com.sprintnews.domain.model.exception.CannotGenerateReport;
import com.sprintnews.domain.model.exception.InvalidUserInput;
import com.sprintnews.domain.model.exception.SprintNewsError;
import com.sprintnews.domain.services.SprintNewspaperService;
import com.sprintnews.domain.model.utils.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author daniloteodoro
 */

@ContextJwt
@CrossOrigin()
@RestController
public class ApiController {
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    private final SprintNewspaperService newspaperService;
    private final AtlassianHostRestClients atlassianHostRestClient;

    @Autowired
    public ApiController(SprintNewspaperService newspaperService, AtlassianHostRestClients atlassianHostRestClient) {
        super();
        this.newspaperService = Objects.requireNonNull(newspaperService, "SprintNewspaperService should not be null");
        this.atlassianHostRestClient = Objects.requireNonNull(atlassianHostRestClient, "AtlassianHostRestClients should not be null!");
    }

    @GetMapping(value = "generate", produces = "application/pdf")
    public ResponseEntity<byte[]> generatePdf(@AuthenticationPrincipal AtlassianHostUser hostUser, @RequestParam NewspaperInput data) {
        long t1 = System.currentTimeMillis();
        try {
            logger.info("count#newspaper.requested=1");

            if (hostUser == null || hostUser.getHost() == null)
                throw new CannotGenerateReport("Cannot generate report for an unknown host");

            logger.info("PDF being generated with data: \n\n" + data + "\n\n from host: " + hostUser.getHost().getBaseUrl());
            try {
                List<String> errors = new ArrayList<>();
                if (data.getTeamName() == null || data.getTeamName().trim().isEmpty()) {
                    errors.add("Team name");
                }
                if (data.getTeamCity() == null || data.getTeamCity().trim().isEmpty()) {
                    errors.add("City");
                }
                if (data.getSelectedSprint() == null || data.getSelectedSprint().getValue() == null || data.getSelectedSprint().getValue().trim().isEmpty()) {
                    errors.add("Selected sprint");
                }
                if (data.tryToParseStartDate() != null && data.tryToParseEndDate() != null) {
                    if (data.tryToParseStartDate().isAfter(data.tryToParseEndDate())) {
                        errors.add("Start date should happen before End date");
                    }
                }
                if (data.tryToParseSprintReviewDate() != null && data.tryToParseEndDate() != null) {
                    if (data.tryToParseSprintReviewDate().isBefore(data.tryToParseEndDate())) {
                        errors.add("Sprint Review date should happen after the Sprint's end date");
                    }
                }
                if (!errors.isEmpty()) {
                    throw new InvalidUserInput(TextUtils.generateHumanReadableList(errors, String::toString));
                }
                logger.info("count#newspaper.validation.success=1");
            } catch (Exception e) {
                logger.error("Failure processing: \n" + data);
                if ((e instanceof InvalidUserInput)) {
                    logger.info("count#newspaper.validation.invalid=1");
                    throw e;
                } else {
                    logger.info("count#newspaper.validation.failure=1");
                    e.printStackTrace();
                    throw new CannotGenerateReport("Failure parsing input data. The system admin was notified.");
                }
            }

            byte[] newspaperContent = newspaperService.generate(atlassianHostRestClient, data);

            ResponseEntity<byte[]> result = ResponseEntity.ok()
                    .header("Content-Disposition", "inline; filename=\"newspaper.pdf\"")
                    .body(newspaperContent);

            logger.info("count#newspaper.success=1");
            logger.info("measure#newspaper.processing.time={}ms", System.currentTimeMillis() - t1);

            printMemoryInfo();

            return result;

        } catch (Exception e) {
            if (e instanceof SprintNewsError)
                throw e;

            e.printStackTrace();
            logger.info("count#newspaper.failure=1");
            throw new CannotGenerateReport("Unknown error generating the report, please try again later. The system admin has been notified.");
        }
    }

    private void printMemoryInfo() {
        logger.debug("Max: "  + Runtime.getRuntime().maxMemory() / 1024 / 1024);
        logger.debug("Total: "  + Runtime.getRuntime().totalMemory() / 1024 / 1024);
        logger.debug("Free: "  + Runtime.getRuntime().freeMemory() / 1024 / 1024);
    }

}
