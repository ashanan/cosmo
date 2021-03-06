/*
 * Copyright 2006-2007 Open Source Applications Foundation
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
package org.osaf.cosmo.dav.impl;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.server.io.IOUtil;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.osaf.cosmo.dav.DavCollection;
import org.osaf.cosmo.dav.DavContent;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavResource;
import org.osaf.cosmo.dav.DavResourceFactory;
import org.osaf.cosmo.dav.DavResourceLocator;
import org.osaf.cosmo.dav.LockedException;
import org.osaf.cosmo.dav.UnprocessableEntityException;
import org.osaf.cosmo.dav.acl.report.PrincipalMatchReport;
import org.osaf.cosmo.dav.acl.report.PrincipalPropertySearchReport;
import org.osaf.cosmo.dav.caldav.report.FreeBusyReport;
import org.osaf.cosmo.dav.caldav.report.MultigetReport;
import org.osaf.cosmo.dav.caldav.report.QueryReport;
import org.osaf.cosmo.dav.property.DavProperty;
import org.osaf.cosmo.dav.property.ExcludeFreeBusyRollup;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionLockedException;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.xml.DomWriter;
import org.w3c.dom.Element;

/**
 * Extends <code>DavResourceBase</code> to adapt the Cosmo
 * <code>CollectionItem</code> to the DAV resource model.
 *
 * This class defines the following live properties:
 *
 * <ul>
 * <li><code>DAV:supported-report-set</code> (protected)</li>
 * <li><code>cosmo:exclude-free-busy-rollup</code></li>
 * </ul>
 *
 * @see DavResourceBase
 * @see CollectionItem
 */
public class DavCollectionBase extends DavItemResourceBase
    implements DavItemCollection {
    private static final Log log = LogFactory.getLog(DavCollectionBase.class);
    private static final Set<String> DEAD_PROPERTY_FILTER =
        new HashSet<String>();
    private static final Set<ReportType> REPORT_TYPES =
        new HashSet<ReportType>();

    private ArrayList members;

    static {
        registerLiveProperty(EXCLUDEFREEBUSYROLLUP);

        REPORT_TYPES.add(FreeBusyReport.REPORT_TYPE_CALDAV_FREEBUSY);
        REPORT_TYPES.add(MultigetReport.REPORT_TYPE_CALDAV_MULTIGET);
        REPORT_TYPES.add(QueryReport.REPORT_TYPE_CALDAV_QUERY);
        REPORT_TYPES.add(PrincipalMatchReport.REPORT_TYPE_PRINCIPAL_MATCH);
        REPORT_TYPES.add(PrincipalPropertySearchReport.
                         REPORT_TYPE_PRINCIPAL_PROPERTY_SEARCH);

        DEAD_PROPERTY_FILTER.add(CollectionItem.class.getName());
    }

    public DavCollectionBase(CollectionItem collection,
                             DavResourceLocator locator,
                             DavResourceFactory factory,
                             EntityFactory entityFactory)
        throws DavException {
        super(collection, locator, factory, entityFactory);
        members = new ArrayList();
    }

    public DavCollectionBase(DavResourceLocator locator,
                             DavResourceFactory factory,
                             EntityFactory entityFactory)
        throws DavException {
        this(entityFactory.createCollection(), locator, factory, entityFactory);
    }

    // Jackrabbit DavResource

    public String getSupportedMethods() {
        // If resource doesn't exist, then options are limited
        if(!exists())
            return "OPTIONS, TRACE, PUT, MKCOL, MKCALENDAR";
        else
            return "OPTIONS, GET, HEAD, PROPFIND, PROPPATCH, TRACE, COPY, DELETE, MOVE, MKTICKET, DELTICKET, REPORT";
    }

    public boolean isCollection() {
        return true;
    }

    public void spool(OutputContext outputContext)
        throws IOException {
        throw new UnsupportedOperationException();
    }

    public void addMember(org.apache.jackrabbit.webdav.DavResource member,
                          InputContext inputContext)
        throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public DavResourceIterator getMembers() {
        try {
            for (Item memberItem : ((CollectionItem)getItem()).getChildren()) {
                DavResource resource = memberToResource(memberItem);
                if(resource!=null)
                    members.add(resource);
            }
                
            return new DavResourceIteratorImpl(members);
        } catch (DavException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeMember(org.apache.jackrabbit.webdav.DavResource member)
        throws org.apache.jackrabbit.webdav.DavException {
        if (log.isDebugEnabled())
            log.debug("removing resource '" + member.getDisplayName() +
                      "' from '" + getDisplayName() + "'");

        CollectionItem collection = (CollectionItem) getItem();
        Item item = ((DavItemResource)member).getItem();

        try {
            if(item instanceof CollectionItem)
                getContentService().removeCollection((CollectionItem) item);
            else
                getContentService().removeItemFromCollection(item, collection);
        } catch (CollectionLockedException e) {
            throw new LockedException();
        }

        members.remove(member);
    }

    // DavResource

    public void writeTo(OutputContext out)
        throws DavException, IOException {
        writeHtmlDirectoryIndex(out);
    }

    // DavCollection

    public void addContent(DavContent content,
                           InputContext context)
        throws DavException {
        DavContentBase base = (DavContentBase) content;
        base.populateItem(context);
        saveContent(base);
        members.add(base);
    }

    public MultiStatusResponse addCollection(DavCollection collection,
                                             DavPropertySet properties)
        throws DavException {
        DavCollectionBase base = (DavCollectionBase) collection;
        base.populateItem(null);
        MultiStatusResponse msr = base.populateAttributes(properties);
        if (! msr.hasNonOk()) {
            saveSubcollection(base);
            members.add(base);
        }
        return msr;
    }

    public DavResource findMember(String href)
        throws DavException {
        return memberToResource(href);
    }

    // DavItemCollection

    public boolean isCalendarCollection() {
        return false;
    }

    public boolean isHomeCollection() {
        return false;
    }

    public boolean isExcludedFromFreeBusyRollups() {
        return ((CollectionItem) getItem()).isExcludeFreeBusyRollup();
    }

    // our methods

    protected Set<QName> getResourceTypes() {
        HashSet<QName> rt = new HashSet<QName>(1);
        rt.add(RESOURCE_TYPE_COLLECTION);
        return rt;
    }

    public Set<ReportType> getReportTypes() {
        return REPORT_TYPES;
    }

    /** */
    protected void loadLiveProperties(DavPropertySet properties) {
        super.loadLiveProperties(properties);

        CollectionItem cc = (CollectionItem) getItem();
        if (cc == null)
            return;

        properties.add(new ExcludeFreeBusyRollup(cc.isExcludeFreeBusyRollup()));
    }

    /** */
    protected void setLiveProperty(DavProperty property)
        throws DavException {
        super.setLiveProperty(property);

        CollectionItem cc = (CollectionItem) getItem();
        if (cc == null)
            return;

        DavPropertyName name = property.getName();
        if (property.getValue() == null)
            throw new UnprocessableEntityException("Property " + name + " requires a value");

        if (name.equals(EXCLUDEFREEBUSYROLLUP)) {
            Boolean flag = Boolean.valueOf(property.getValueText());
            cc.setExcludeFreeBusyRollup(flag);
        }
    }

    /** */
    protected void removeLiveProperty(DavPropertyName name)
        throws DavException {
        super.removeLiveProperty(name);

        CollectionItem cc = (CollectionItem) getItem();
        if (cc == null)
            return;

        if (name.equals(EXCLUDEFREEBUSYROLLUP))
            cc.setExcludeFreeBusyRollup(false);
    }

    /** */
    protected Set<String> getDeadPropertyFilter() {
        return DEAD_PROPERTY_FILTER;
    }

    /**
     * Saves the given collection resource to storage.
     */
    protected void saveSubcollection(DavItemCollection member)
        throws DavException {
        CollectionItem collection = (CollectionItem) getItem();
        CollectionItem subcollection = (CollectionItem) member.getItem();

        if (log.isDebugEnabled())
            log.debug("creating collection " + member.getResourcePath());

        try {
            subcollection = getContentService().
                createCollection(collection, subcollection);
            member.setItem(subcollection);
        } catch (CollectionLockedException e) {
            throw new LockedException();
        }
    }

    /**
     * Saves the given content resource to storage.
     */
    protected void saveContent(DavItemContent member)
        throws DavException {
        CollectionItem collection = (CollectionItem) getItem();
        ContentItem content = (ContentItem) member.getItem();

        try {
            if (content.getCreationDate() != null) {
                if (log.isDebugEnabled())
                    log.debug("updating member " + member.getResourcePath());

                content = getContentService().updateContent(content);
            } else {
                if (log.isDebugEnabled())
                    log.debug("creating member " + member.getResourcePath());

                content =
                    getContentService().createContent(collection, content);
            }
        } catch (CollectionLockedException e) {
            throw new LockedException();
        }

        member.setItem(content);
    }

    protected DavResource memberToResource(Item item)
        throws DavException {
        String path = getResourcePath() + "/" + item.getName();
        DavResourceLocator locator = getResourceLocator().getFactory().
            createResourceLocatorByPath(getResourceLocator().getContext(),
                                        path);
        return getResourceFactory().createResource(locator, item);
    }    

    protected DavResource memberToResource(String uri)
        throws DavException {
        DavResourceLocator locator = getResourceLocator().getFactory().
            createResourceLocatorByUri(getResourceLocator().getContext(), 
                                       uri);
        return getResourceFactory().resolve(locator);
    }

    private void writeHtmlDirectoryIndex(OutputContext context)
        throws DavException, IOException {
        if (log.isDebugEnabled())
            log.debug("writing html directory index for  " +
                      getDisplayName());

        context.setContentType(IOUtil.buildContentType("text/html", "UTF-8"));
        context.setModificationTime(getModificationTime());
        context.setETag(getETag());

        if (! context.hasStream())
            return;

        PrintWriter writer =
            new PrintWriter(new OutputStreamWriter(context.getOutputStream(),
                                                   "utf8"));

        writer.write("<html>\n<head><title>");
        writer.write(StringEscapeUtils.escapeHtml(getDisplayName()));
        writer.write("</title></head>\n");
        writer.write("<body>\n");
        writer.write("<h1>");
        writer.write(StringEscapeUtils.escapeHtml(getDisplayName()));
        writer.write("</h1>\n");
        
        DavResource parent = getParent();
        if (parent.exists()) {
            writer.write("Parent: <a href=\"");
            writer.write(parent.getResourceLocator().getHref(true));
            writer.write("\">");
            writer.write(StringEscapeUtils.escapeHtml(parent.getDisplayName()));
            writer.write("</a></li>\n");
        }

        writer.write("<h2>Members</h2>\n");
        writer.write("<ul>\n");
        for (DavResourceIterator i=getMembers(); i.hasNext();) {
            DavResource child = (DavResource) i.nextResource();
            writer.write("<li><a href=\"");
            writer.write(child.getResourceLocator().getHref(child.isCollection()));
            writer.write("\">");
            writer.write(StringEscapeUtils.escapeHtml(child.getDisplayName()));
            writer.write("</a></li>\n");
        }
        writer.write("</ul>\n");

        writer.write("<h2>Properties</h2>\n");
        writer.write("<dl>\n");
        for (DavPropertyIterator i=getProperties().iterator(); i.hasNext();) {
            DavProperty prop = (DavProperty) i.nextProperty();
            Object value = prop.getValue();
            String text = null;
            if (value instanceof Element) {
                try {
                    text = DomWriter.write((Element)value);
                } catch (Exception e) {
                    log.warn("Error serializing value for property " + prop.getName());
                }
            }
            if (text == null)
                text = prop.getValueText();
            if (text == null)
                text = "-- no value --";
            writer.write("<dt>");
            writer.write(StringEscapeUtils.escapeHtml(prop.getName().toString()));
            writer.write("</dt><dd>");
            writer.write(StringEscapeUtils.escapeHtml(text));
            writer.write("</dd>\n");
        }
        writer.write("</dl>\n");

        User user = getSecurityManager().getSecurityContext().getUser();
        if (user != null) {
            writer.write("<p>\n");
            if (! isHomeCollection()) {
                DavResourceLocator homeLocator =
                    getResourceLocator().getFactory().
                    createHomeLocator(getResourceLocator().getContext(),
                                      user);
                writer.write("<a href=\"");
                writer.write(homeLocator.getHref(true));
                writer.write("\">");
                writer.write("Home collection");
                writer.write("</a><br>\n");
            }

            DavResourceLocator principalLocator = 
                getResourceLocator().getFactory().
                createPrincipalLocator(getResourceLocator().getContext(),
                                       user);
            writer.write("<a href=\"");
            writer.write(principalLocator.getHref(false));
            writer.write("\">");
            writer.write("Principal resource");
            writer.write("</a><br>\n");
        }

        writer.write("</body>");
        writer.write("</html>\n");
        writer.close();
    }
    
    @Override
    protected void updateItem() throws DavException {
        try {
            getContentService().updateCollection((CollectionItem) getItem());
        } catch (CollectionLockedException e) {
            throw new LockedException();
        }
    }
}
