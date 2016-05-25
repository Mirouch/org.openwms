/*
 * openwms.org, the Open Warehouse Management System.
 * Copyright (C) 2014 Heiko Scherrer
 *
 * This file is part of openwms.org.
 *
 * openwms.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * openwms.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software. If not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.openwms.core.configuration.file;

import javax.annotation.PostConstruct;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ameba.exception.IntegrationLayerException;
import org.openwms.core.event.ReloadFilePreferencesEvent;
import org.openwms.core.exception.NoUniqueResultException;
import org.openwms.core.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * A XMLPreferenceDaoImpl reads a XML file of preferences and keeps them internally in a Map. An initial preferences file can be configured
 * with a property <i>openwms.core.config.initial-properties</i> in the application.properties file. <p> On a {@link
 * ReloadFilePreferencesEvent} the internal Map is cleared and reloaded. </p>
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 * @version 0.2
 * @see org.openwms.core.event.ReloadFilePreferencesEvent
 * @since 0.1
 */
@Transactional(propagation = Propagation.MANDATORY)
@Repository
public class XMLPreferenceDaoImpl implements PreferenceDao, ApplicationListener<ReloadFilePreferencesEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLPreferenceDaoImpl.class);
    @Autowired
    private ApplicationContext ctx;
    @Autowired
    private Unmarshaller unmarshaller;
    @Value("${openwms.core.config.initial-properties:}")
    private String fileName;
    private volatile Resource fileResource;
    private volatile Preferences preferences;
    private Map<PreferenceKey, AbstractPreference> prefs = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     *
     * @see PreferenceDao#findAll()
     */
    @Override
    public List<AbstractPreference> findAll() {
        return preferences == null ? Collections.emptyList() : preferences.getAll();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    @Override
    public void onApplicationEvent(ReloadFilePreferencesEvent event) {
        reloadResources();
    }

    /**
     * On bean initialization load all preferences into a Map.
     */
    @PostConstruct
    private void loadResources() {
        if (initialPropertiesExist()) {
            try {
                preferences = (Preferences) unmarshaller.unmarshal(new StreamSource(fileResource.getInputStream()));
                for (AbstractPreference pref : preferences.getAll()) {
                    if (prefs.containsKey(pref.getPrefKey())) {
                        throw new NoUniqueResultException("Preference with key " + pref.getPrefKey() + " already loaded.");
                    }
                    prefs.put(pref.getPrefKey(), pref);
                }
                LOGGER.debug("Loaded {} properties into cache", preferences.getAll().size());
            } catch (XmlMappingException xme) {
                throw new IntegrationLayerException("Exception while unmarshalling from " + fileName, xme);
            } catch (IOException ioe) {
                throw new ResourceNotFoundException("Exception while accessing the resource with name " + fileName, ioe);
            }
        }
    }

    private boolean initialPropertiesExist() {
        if (fileName != null && !fileName.isEmpty()) {
            fileResource = ctx.getResource(fileName);
        }
        if (fileResource == null || !fileResource.exists()) {
            LOGGER.debug("File to load initial preferences does not exist or is not preset. Filename [{}]", fileName);
            return false;
        }
        return true;
    }

    private void reloadResources() {
        preferences = null;
        prefs.clear();
        loadResources();
    }
}