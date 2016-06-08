/*
 * Copyright (C) 2011 LINUXTEK, Inc.  All Rights Reserved.
 */
package com.linuxtek.kona.analytics;


/**
 * KAnalyticsService.
 */

public interface KAnalyticsService {
    public String getJavascriptTag(String siteId) 
            throws KAnalyticsException;

    public String addSite(String siteName, String... siteUrl)
            throws KAnalyticsException;

    public void deleteSite(String siteId)
            throws KAnalyticsException;

    public void updateSite(String siteId, String siteName, String siteUrl)
            throws KAnalyticsException;

    /*
    public Integer getSiteIdByUrl(String siteUrl)
            throws KAnalyticsException;
    */
}
