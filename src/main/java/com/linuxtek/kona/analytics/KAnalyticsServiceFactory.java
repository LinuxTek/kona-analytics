/*
 * Copyright (C) 2011 LINUXTEK, Inc.  All Rights Reserved.
 */
package com.linuxtek.kona.analytics;

import org.apache.log4j.Logger;

import com.linuxtek.kona.http.KHttpClientException;

/**
 * KAnalyticsService.
 */

public class KAnalyticsServiceFactory {
    private static Logger logger = 
            Logger.getLogger(KAnalyticsServiceFactory.class);

    public static KAnalyticsService getPiwikService(
            String baseUrl, String authToken) throws KAnalyticsException {
        try {
            logger.debug("Piwik service requested");
            KAnalyticsService piwik = new PiwikServiceImpl(baseUrl, authToken);
            return piwik;
        } catch (KHttpClientException e) {
            throw new KAnalyticsException(e);
        }

    }
}
