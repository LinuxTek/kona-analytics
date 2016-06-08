/*
 * Copyright (C) 2011 LINUXTEK, Inc.  All Rights Reserved.
 */
package com.linuxtek.kona.analytics;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.linuxtek.kona.http.KHttpClientException;
import com.linuxtek.kona.http.KHttpClientRequest;

/**
 * PiwikServiceImpl.
 */

public class PiwikServiceImpl extends KHttpClientRequest
        implements KAnalyticsService {

    private static Logger logger = Logger.getLogger(PiwikServiceImpl.class);

    private String baseUrl = null;
    private String authToken = null;
    Type listMapType = null;
    Type mapType = null;
    Gson gson = null;

    public PiwikServiceImpl(String baseUrl, String authToken) 
            throws KHttpClientException {
        super(baseUrl);
        this.baseUrl = baseUrl;
        this.authToken = authToken;
        gson = new Gson();
        mapType = new TypeToken<Map<String,String>>() {}.getType();
        listMapType = new TypeToken<List<Map<String,String>>>() {}.getType();

        logger.debug("PiwikService initialized");
    }
    
    private boolean isDemo() {
    	if (baseUrl == null || baseUrl.equalsIgnoreCase("demo")) {
    		return true;
    	}
        return false;
    }

    /*
     * expected response:
        {"value":"<!-- Piwik --> \n<script type=\"text\/javascript\">\nvar pkBaseURL = ((\"https:\" == document.location.protocol) ? \"https:\/\/piwik.linuxtek.com\/\" : \"http:\/\/piwik.linuxtek.com\/\");\ndocument.write(unescape(\"%3Cscript src='\" + pkBaseURL + \"piwik.js' type='text\/javascript'%3E%3C\/script%3E\"));\n<\/script><script type=\"text\/javascript\">\ntry {\n  var piwikTracker = Piwik.getTracker(pkBaseURL + \"piwik.php\", 2);\n  piwikTracker.trackPageView();\n  piwikTracker.enableLinkTracking();\n} catch( err ) {}\n<\/script><noscript><p><img src=\"http:\/\/piwik.linuxtek.com\/piwik.php?idsite=2\" style=\"border:0\" alt=\"\" \/><\/p><\/noscript>\n<!-- End Piwik Tracking Code -->\n"}
    */
    public String getJavascriptTag(String siteId) 
            throws KAnalyticsException {
        
        if (isDemo()) return "<!-- DEMO_JS_TAG -->";
        
        String tag = null;

        StringBuffer buffer = new StringBuffer();
        buffer.append("&method=SitesManager.getJavascriptTag");
        buffer.append("&idSite=" + encode(siteId));

        String result = doRequest(buffer.toString());
        Map<String,String> map =  gson.fromJson(result, mapType);
        tag = map.get("value");

        return tag;
    }

    // expected response: {"value":3}
    public String addSite(String siteName, String... siteUrls) 
            throws KAnalyticsException {
        if (isDemo()) return "-1";
        
        String siteId = null;

        StringBuffer buffer = new StringBuffer();
        buffer.append("&method=SitesManager.addSite");
        buffer.append("&siteName=" + encode(siteName));

        for (int i=0; i<siteUrls.length; i++ ) {
            String siteUrl = siteUrls[i];
            buffer.append("&urls["+i+"]=" + encode(siteUrl));
        }

        String result = doRequest(buffer.toString());
        Map<String,String> map =  gson.fromJson(result, mapType);
        siteId = map.get("value");

        return siteId;
    }

    // expected response:  {"result":"success", "message":"ok"}
    public void deleteSite(String siteId) 
            throws KAnalyticsException {
        if (isDemo()) return;
        
        StringBuffer buffer = new StringBuffer();
        buffer.append("&method=SitesManager.deleteSite");
        buffer.append("&idSite=" + encode(siteId));
        String result = doRequest(buffer.toString());
        logger.debug("deleteSite: siteId:" + siteId + "\n" + result);
    }

    public void updateSite(String siteId, String siteName, String siteUrl)
            throws KAnalyticsException {
        if (isDemo()) return;
        
        StringBuffer buffer = new StringBuffer();
        buffer.append("&method=SitesManager.updateSite");
        buffer.append("&idSite=" + encode(siteId));
        buffer.append("&siteName=" + encode(siteName));
        buffer.append("&urls[0]=" + encode(siteUrl));
        String result = doRequest(buffer.toString());
        logger.debug("updateSite: siteId:" + siteId + "\n" + result);
    }

    // expected response: [{"idsite":"3"}]
    protected String getSiteIdByUrl(String siteUrl) 
            throws KAnalyticsException {
        if (isDemo()) return "-1";
        
        String siteId = null;

        StringBuffer buffer = new StringBuffer();
        buffer.append("&method=SitesManager.getSitesIdFromSiteUrl");
        buffer.append("&url=" + encode(siteUrl));

        String result = doRequest(buffer.toString());
        List<Map<String,String>> list =  gson.fromJson(result, listMapType);
        if (list != null && list.size() > 0) {
            siteId = list.get(0).get("idsite");
        } else {
            throw new KAnalyticsException("getSiteIdByUrl() [siteUrl: "
                + siteUrl + "] Invalid server response:\n" + result);
        }

        return siteId;
    }

    // FIXME: need to correctly parse the result and determine
    // if the call succeeded or not (response status code = 200).
    // If the call is successful, check to make sure the response
    // is not an error message.
    /*
    private List<Map<String,String>> doRequest(String params) {
        List<Map<String,String>> result = null;
        try {
            params = "module=API&format=json&token_auth=" + authToken + params;
            String s = super.doRequest("", params);
            if (s != null) {
                result =  gson.fromJson(s, listMapType);
                if (result != null && result.size() == 0) {
                    result = null;
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return result;
    }
    */

    private String doRequest(String params) throws KAnalyticsException {
        String result = null;
        try {
            params = "module=API&format=json&token_auth=" + authToken + params;
            result = super.doRequest("", params);
            checkResult(params, result);
        } catch (Exception e) {
            throw new KAnalyticsException(e);
        }
        return result;
    }


    /*
     * NOTE: Error message format:
     *      "{  "result":"error", 
     *          "message":"error message goes here."}"
     */
    private void checkResult(String params, String response) 
            throws KAnalyticsException {
        String request = getBaseUrl() + "?" + params;

        if (response == null) {
            throw new KAnalyticsException(
                "Null server response. Check server request: " + request);
        }

        // see if we can parse this response as a map
        Map<String,String> map =  gson.fromJson(response, mapType);

        // will the map be null if the json response is some other type?
        if (map == null) return;

        String result = map.get("result");
        if (result != null && result.equalsIgnoreCase("error")) {
            String message = map.get("message");
            throw new KAnalyticsException(message);
        }
    }
}
