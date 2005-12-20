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
package org.osaf.cosmo.io;

import java.io.IOException;
import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.server.io.DefaultHandler;
import org.apache.jackrabbit.server.io.ExportContext;
import org.apache.jackrabbit.server.io.ImportContext;
import org.apache.jackrabbit.server.io.IOManager;
import org.apache.jackrabbit.server.io.IOUtil;
import org.apache.jackrabbit.webdav.DavResource;

import org.osaf.cosmo.dao.UnsupportedCalendarObjectException;
import org.osaf.cosmo.dao.jcr.JcrConstants;
import org.osaf.cosmo.dao.jcr.JcrEscapist;
import org.osaf.cosmo.icalendar.DuplicateUidException;

/**
 * Extends {@link org.apache.jackrabbit.server.io.DefaultHandler}
 * to provide custom logic for importing and exporting Cosmo
 * resources.
 */
public class CosmoHandler extends DefaultHandler implements JcrConstants {
    private static final Log log = LogFactory.getLog(CosmoHandler.class);

    /**
     */
    public CosmoHandler(IOManager ioManager) {
        super(ioManager);
    }

    /**
     */
    public CosmoHandler(IOManager ioManager,
                        String collectionNodetype,
                        String defaultNodetype,
                        String contentNodetype) {
        super(ioManager, collectionNodetype, defaultNodetype, contentNodetype);
    }

    /**
     * Extends the superclass method with the following logic:
     *
     * <ol>
     * <li> Adds the <code>dav:resource</code> and
     * <code>mix:ticketable</code> mixin type to the resource node if
     * it does not already have them.</li>
     * <li> If importing a calendar resource into a caldav collection:
     * <ol>
     * <li> Ensures that the calendar object contains at least one
     * event.</li>
     * <li> Ensures that uid of the calendar object is unique within
     * the calendar collection.</li>
     * <li> Adds the <code>caldav:resource</code> mixin type to the
     * resource node if it does not already have that type.</li>
     * </ol>
     * </ol>
     */
    public boolean importData(ImportContext context,
                              boolean isCollection,
                              Node contentNode)
        throws IOException, RepositoryException {
        if (! super.importData(context, isCollection, contentNode)) {
            return false;
        }

        CosmoImportContext cosmoContext = (CosmoImportContext) context;
        Node resourceNode = contentNode;
        if (! isCollection) {
            resourceNode = contentNode.getParent();
        }

        // add dav resource and ticketable mixin types for all resources
        if (! resourceNode.isNodeType(NT_DAV_RESOURCE)) {
            resourceNode.addMixin(NT_DAV_RESOURCE);
        }
        if (! resourceNode.isNodeType(NT_TICKETABLE)) {
            resourceNode.addMixin(NT_TICKETABLE);
        }

        if (cosmoContext.isCalendarContent() &&
            resourceNode.getParent().isNodeType(NT_CALDAV_COLLECTION)) {
            Calendar calendar = cosmoContext.getCalendar();

            // since we are importing a calendar resource into a
            // calendar collection, we have to:

            // 1) make sure that the calendar object contains at least
            // one event
            if (calendar.getComponents().getComponents(Component.VEVENT).
                isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("Calendar contains no events");
                }
                throw new UnsupportedCalendarObjectException("No events");
            }

            // 2) make sure that the calendar object's uid is
            // unique with in the calendar collection
            Component event = (Component)
                calendar.getComponents().getComponents(Component.VEVENT).
                get(0);
            Property uid = (Property)
                event.getProperties().getProperty(Property.UID);
            if (! isUidUnique(resourceNode, uid.getValue())) {
                throw new DuplicateUidException(uid.getValue());
            }

            // 3) add caldav resource mixin type
            if (! resourceNode.isNodeType(NT_CALDAV_RESOURCE)) {
                resourceNode.addMixin(NT_CALDAV_RESOURCE);
            }
        }

        return true;
    }

    /**
     * Extends the superclass method with the following logic:
     *
     * <ol>
     * <li> The (JCR-escaped) system id is used to set the resource
     * node's <code>dav:displayname</code> property.</li>
     * <li> The resource node's <code>dav:contentlanguage</code>
     * property is set from the import context.</li>
     * <li> If importing a calendar resource into a caldav collection,
     * set the resource node's <code>caldav:uid</code> property.</li>
     *</ol>
     */
    protected boolean importProperties(ImportContext context,
                                       boolean isCollection,
                                       Node contentNode) {
        if (! super.importProperties(context, isCollection, contentNode)) {
            return false;
        }

        CosmoImportContext cosmoContext = (CosmoImportContext) context;
        Node resourceNode = contentNode;

        try {
            if (! isCollection) {
                resourceNode = contentNode.getParent();
            }

            String displayName = 
                JcrEscapist.hexUnescapeJcrNames(context.getSystemId());
            resourceNode.setProperty(NP_DAV_DISPLAYNAME, displayName);
            resourceNode.setProperty(NP_DAV_CONTENTLANGUAGE,
                                     context.getContentLanguage());

            if (resourceNode.isNodeType(NT_CALDAV_RESOURCE)) {
                // set the uid property on caldav resources
                Calendar calendar = cosmoContext.getCalendar();
                Component event = (Component)
                    calendar.getComponents().getComponents(Component.VEVENT).
                    get(0);
                Property uid = (Property)
                    event.getProperties().getProperty(Property.UID);
                resourceNode.setProperty(NP_CALDAV_UID, uid.getValue());
            }
        } catch (IOException e) {
            // XXX ugh swallowing
            log.error("error reading calendar stream", e);
            return false;
        } catch (RepositoryException e) {
            // XXX ugh swallowing
            log.error("error importing dav properties", e);
            return false;
        }

        return true;
    }

    /**
     * Returns true if the given uid value is not already in use by
     * any calendar resource node within the parent calendar
     * collection node of the given resource node.
     */
    protected boolean isUidUnique(Node resourceNode, String uid)
        throws RepositoryException {
        // look for nodes anywhere below the parent calendar
        // collection that have this same uid 
        StringBuffer stmt = new StringBuffer();
        stmt.append("/jcr:root");
        if (! resourceNode.getParent().getPath().equals("/")) {
            stmt.append(JcrEscapist.xmlEscapeJcrPath(resourceNode.getParent().
                                                     getPath()));
        }
        stmt.append("//element(*, ").
            append(NT_CALDAV_RESOURCE).
            append(")").
            append("[@").
            append(NP_CALDAV_UID).
            append(" = '").
            append(uid).
            append("']");

        QueryManager qm =
            resourceNode.getSession().getWorkspace().getQueryManager();
        QueryResult qr =
            qm.createQuery(stmt.toString(), Query.XPATH).execute();

        // if we are updating this node, then we expect it to show up
        // in the result, but nothing else
        for (NodeIterator i=qr.getNodes(); i.hasNext();) {
            Node n = (Node) i.next();
            if (! n.getPath().equals(resourceNode.getPath())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Extends the superclass method with the following logic:
     *
     * <ol>
     * <li> If the resource node is a <code>dav:resource</code>, its
     * <code>dav:contentlanguage</code> property is used to set the
     * export context's content language.
     *</ol>
     */
    protected void exportProperties(ExportContext context,
                                    boolean isCollection,
                                    Node contentNode)
        throws IOException {
        super.exportProperties(context, isCollection, contentNode);

        try {
            Node resourceNode = isCollection ?
                contentNode : contentNode.getParent();
            // get content language
            if (resourceNode.hasProperty(NP_DAV_CONTENTLANGUAGE)) {
                String contentLanguage =
                    resourceNode.getProperty(NP_DAV_CONTENTLANGUAGE).
                    getString();
                context.setContentLanguage(contentLanguage);
            }
        } catch (RepositoryException e) {
            log.error("error exporting dav properties", e);
            throw new IOException(e.getMessage());
        }
    }
}
