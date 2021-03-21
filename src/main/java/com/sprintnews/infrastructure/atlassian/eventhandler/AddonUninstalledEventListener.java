package com.sprintnews.infrastructure.atlassian.eventhandler;

import com.atlassian.connect.spring.AddonUninstalledEvent;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.sprintnews.infrastructure.atlassian.entities.AtlassianEvent;
import com.sprintnews.infrastructure.atlassian.repository.HostInformationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class AddonUninstalledEventListener implements ApplicationListener<AddonUninstalledEvent> {
    private final static Logger logger = Logger.getLogger(AddonUninstalledEventListener.class.getSimpleName());
    private final HostInformationRepository repository;
    private final AtlassianHostRepository hostRepository;

    public AddonUninstalledEventListener(@Autowired HostInformationRepository repository, @Autowired AtlassianHostRepository hostRepository) {
        this.repository = repository;
        this.hostRepository = hostRepository;
    }

    @Override
    public void onApplicationEvent(AddonUninstalledEvent addonUninstalledEvent) {
        try {
            hostRepository.findFirstByBaseUrlOrderByLastModifiedDateDesc(addonUninstalledEvent.getHost().getBaseUrl())
                    .ifPresent(hostRepository::delete);
        } catch (Exception e) {
            logger.severe("Failure deleting existing host after uninstall: " + e.getMessage());
        }
        try {
            this.repository.saveAndFlush(AtlassianEvent.uninstalledNow(addonUninstalledEvent.getHost().getBaseUrl()));
        } catch (Exception e) {
            logger.severe("Failure persisting host uninstalled event: " + e.getMessage());
        }

        // Submit metric independently of storing the event.
        logger.info("count#host.uninstalled=1");
    }
}
