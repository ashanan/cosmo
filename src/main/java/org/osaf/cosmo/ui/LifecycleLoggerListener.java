/*
 * Copyright 2005 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osaf.cosmo.ui;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.log4j.LogManager;

/**
 * A {@link javax.servlet.ServletContextListener} that logs
 * information about Cosmo servlet context lifecycle events.
 */
public class LifecycleLoggerListener implements ServletContextListener {
    private static final Log log =
        LogFactory.getLog(LifecycleLoggerListener.class);

    /**
     * Logs an info message when the servlet context is starting.
     */
    public void contextInitialized(ServletContextEvent sce) {
        log.info("Cosmo webapp starting");
    }

    /**
     * Logs an info message when the servlet context is shutting
     * down.
     */
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Cosmo webapp stopping");
        LogManager.shutdown();
        java.beans.Introspector.flushCaches();
        LogFactory.releaseAll();
    }
}
