/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.calendar.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;

/**
 * Utility methods for parsing icalendar data.
 */
public class CalendarUtils {
    
    
    /**
     * Convert Calendar object to String
     * @param calendar
     * @return string representation of calendar
     */
    public static String outputCalendar(Calendar calendar) 
        throws ValidationException, IOException {
        if (calendar == null)
            return null;
        CalendarOutputter outputter = new CalendarOutputter();
        StringWriter sw = new StringWriter();
        outputter.output(calendar, sw);
        return sw.toString();
    }

    /**
     * Parse icalendar string into Calendar object.
     * @param calendar icalendar string
     * @return Calendar object
     */
    public static Calendar parseCalendar(String calendar) 
        throws ParserException, IOException {
        if (calendar == null)
            return null;
        CalendarBuilder builder = CalendarBuilderDispenser.getCalendarBuilder();
        StringReader sr = new StringReader(calendar);
        return builder.build(sr);
    }

    /**
     * Parse icalendar data from Reader into Calendar object.
     * @param reader icalendar data reader
     * @return Calendar object
     */
    public static Calendar parseCalendar(Reader reader)
        throws ParserException, IOException {
        if (reader == null)
            return null;
        CalendarBuilder builder = CalendarBuilderDispenser.getCalendarBuilder();
        return builder.build(reader);
    }

    /**
     * Parse icalendar data from byte[] into Calendar object.
     * @param content icalendar data
     * @return Calendar object
     * @throws Exception
     */
    public static Calendar parseCalendar(byte[] content) 
        throws ParserException, IOException {
        Calendar calendar = CalendarBuilderDispenser.getCalendarBuilder()
                .build(new ByteArrayInputStream(content));
        return calendar;
    }

    /**
     * Parse icalendar data from InputStream
     * @param is icalendar data inputstream
     * @return Calendar object
     * @throws Exception
     */
    public static Calendar parseCalendar(InputStream is) 
        throws ParserException, IOException {
        Calendar calendar = CalendarBuilderDispenser.getCalendarBuilder()
                .build(is);
        return calendar;
    }
}
