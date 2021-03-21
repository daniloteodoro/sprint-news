package com.sprintnews.infrastructure.atlassian.eventhandler;

import com.atlassian.connect.spring.AddonInstalledEvent;
import com.sprintnews.infrastructure.atlassian.entities.AtlassianEvent;
import com.sprintnews.infrastructure.atlassian.repository.HostInformationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class AddonInstalledEventListener implements ApplicationListener<AddonInstalledEvent> {
    private final static Logger logger = Logger.getLogger(AddonInstalledEventListener.class.getSimpleName());
    private final HostInformationRepository repository;

    public AddonInstalledEventListener(@Autowired HostInformationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void onApplicationEvent(AddonInstalledEvent addonInstalledEvent) {
        try {
            this.repository.saveAndFlush(AtlassianEvent.installedNow(addonInstalledEvent.getHost().getBaseUrl()));
        } catch (Exception e) {
            logger.severe("Failure persisting host installed event: " + e.getMessage());
        }

        // Export metric independently of saving object
        logger.info("count#host.installed=1");
    }
}
