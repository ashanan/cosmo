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
package org.osaf.cosmo.dav.caldav.report;

import java.text.ParseException;

import net.fortuna.ical4j.model.component.VTimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.xml.DomUtil;

import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.calendar.query.UnsupportedCollationException;
import org.osaf.cosmo.dav.BadRequestException;
import org.osaf.cosmo.dav.DavCollection;
import org.osaf.cosmo.dav.DavContent;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavResource;
import org.osaf.cosmo.dav.UnprocessableEntityException;
import org.osaf.cosmo.dav.caldav.CaldavConstants;
import org.osaf.cosmo.dav.caldav.SupportedCollationException;
import org.osaf.cosmo.dav.caldav.TimeZoneExtractor;
import org.osaf.cosmo.dav.impl.DavCalendarCollection;
import org.osaf.cosmo.dav.impl.DavCalendarResource;

import org.w3c.dom.Element;

/**
 * <p>
 * Represents the <code>CALDAV:calendar-query</code> report that
 * provides a mechanism for finding calendar resources matching
 * specified criteria.
 * </p>
 */
public class QueryReport extends CaldavMultiStatusReport
    implements CaldavConstants {
    private static final Log log = LogFactory.getLog(QueryReport.class);

    public static final ReportType REPORT_TYPE_CALDAV_QUERY =
        ReportType.register(ELEMENT_CALDAV_CALENDAR_QUERY,
                            NAMESPACE_CALDAV, QueryReport.class);

    private VTimeZone tz;
    private CalendarFilter queryFilter;

    // Report methods

    public ReportType getType() {
        return REPORT_TYPE_CALDAV_QUERY;
    }

    // ReportBase methods

    /**
     * Parses the report info, extracting the properties, filters and time
     * zone.
     */
    protected void parseReport(ReportInfo info)
        throws DavException {
        if (! getType().isRequestedReportType(info))
            throw new DavException("Report not of type " + getType());

        setPropFindProps(info.getPropertyNameSet());
        if (info.containsContentElement(XML_ALLPROP, NAMESPACE)) {
            setPropFindType(PROPFIND_ALL_PROP);
        } else if (info.containsContentElement(XML_PROPNAME, NAMESPACE)) {
            setPropFindType(PROPFIND_PROPERTY_NAMES);
        } else {
            setPropFindType(PROPFIND_BY_PROPERTY);
            setOutputFilter(findOutputFilter(info));
        }

        tz = findTimeZone(info);
        if (tz == null) {
            if (getResource() instanceof DavCalendarCollection)
                tz = ((DavCalendarCollection) getResource()).getTimeZone();
        }

        queryFilter = findQueryFilter(info, tz);
    }

    /**
     * <p>
     * Runs the report query against the given resource. If the query
     * succeeds, the resource is added to the results list.
     * </p>
     * <p>
     * If the resource is a calendar resource, attempts to match the query
     * filter using {@link DavCalendarResource#matches(CalendarFilter)}.
     * If a non-calendar resource, throws an exception. If a collection,
     * does nothing, as query reports only match non-collection resources.
     * </p>
     *
     * @throws UnprocessableEntityException if the resource is not a
     * collection and is not a calendar resource.
     */
    protected void doQuerySelf(DavResource resource)
        throws DavException {
        if (resource instanceof DavCalendarResource) {
            DavCalendarResource dcr = (DavCalendarResource) resource;
            if (dcr.matches(queryFilter))
                getResults().add(dcr);
            return;
        }
        if (resource instanceof DavContent)
            throw new UnprocessableEntityException(getType() + " report not supported for non-calendar resources");
        // if the resource is a collection, it will not match a calendar
        // query, which only matches calendar resource, so we can ignore it
    }

    /**
    * <p>
    * Runs the report query against the members of the collection. All
    * resulting members are added to the results list.
    * </p>
    * <p>
    * If the collection is a calendar collection, attempts to match the query
    * filter using {@link DavCalendarCollection#findMembers(CalendarFilter)}.
    * Otherwise does nothing, as only calendar resources can match the query,
    * and regular collections cannot contain calendar resources.
    * </p>
     */
    protected void doQueryChildren(DavCollection collection)
        throws DavException {
        if (collection instanceof DavCalendarCollection) {
            DavCalendarCollection dcc = (DavCalendarCollection) collection;
            getResults().addAll(dcc.findMembers(queryFilter));
            return;
        }
        // if it's a regular collection, there won't be any calendar resources
        // within it to match the query
    }

    // our methods

    public CalendarFilter getQueryFilter() {
        return queryFilter;
    }

    private static VTimeZone findTimeZone(ReportInfo info)
        throws DavException {
        Element propdata =
            DomUtil.getChildElement(info.getReportElement(),
                                    XML_PROP, NAMESPACE);
        if (propdata == null)
            return null;

        Element tzdata =
            DomUtil.getChildElement(propdata, ELEMENT_CALDAV_TIMEZONE,
                                    NAMESPACE_CALDAV);
        if (tzdata == null)
            return null;

        String icaltz = DomUtil.getTextTrim(tzdata);
        if (icaltz == null)
            throw new UnprocessableEntityException("Expected text content for " + QN_CALDAV_TIMEZONE);

        return TimeZoneExtractor.extract(icaltz);
    }

    private static CalendarFilter findQueryFilter(ReportInfo info,
                                                  VTimeZone tz)
        throws DavException {
        Element filterdata =
            DomUtil.getChildElement(info.getReportElement(),
                                    ELEMENT_CALDAV_FILTER, NAMESPACE_CALDAV);
        if (filterdata == null)
            return null;

        try {
            CalendarFilter filter = new CalendarFilter(filterdata, tz);
            filter.validate();
            return filter;
        } catch (ParseException e) {
            throw new InvalidFilterException(e);
        } catch (UnsupportedCollationException e) {
            throw new SupportedCollationException();
        }
    }
}
