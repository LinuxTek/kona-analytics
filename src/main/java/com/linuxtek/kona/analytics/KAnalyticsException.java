/*
 * Copyright (C) 2011 LINUXTEK, Inc.  All Rights Reserved.
 */
package com.linuxtek.kona.analytics;


/**
 * KAnalyticsException.
 */

public class KAnalyticsException extends Exception {
	private static final long serialVersionUID = 1L;

	public KAnalyticsException(String message) {
        super(message);
    }

    public KAnalyticsException(String message, Throwable cause) {
        super(message, cause);
    }

    public KAnalyticsException(Throwable cause) {
        super(cause);
    }
}
