package com.forgerock.authenticator.utils;

import android.app.Application;

import roboguice.RoboGuice;

/**
 * Used to disable annotation database for RoboGuice.
 */
public class FRAuthApplication extends Application {
    static {
        RoboGuice.setUseAnnotationDatabases(false);
    }
}
