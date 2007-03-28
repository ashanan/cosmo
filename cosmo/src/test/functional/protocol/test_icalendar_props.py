#   Copyright (c) 2006-2007 Open Source Applications Foundation
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

import cosmoclient
import random
import os, sys
from xml.etree import ElementTree

import cosmo_test_lib
from cosmo_test_lib import *

CALENDAR = 'calendar'
FILES_DIR =  os.path.dirname(os.path.abspath(sys.modules[__name__].__file__))+'/files/'

def test_attachment():
    ics = """BEGIN:VCALENDAR
VERSION:2.0
CALSCALE:GREGORIAN
PRODID:-//Apple Computer\, Inc//iCal 3.0//EN
BEGIN:VTIMEZONE
LAST-MODIFIED:20060710T225223Z
TZID:America/Vancouver
BEGIN:DAYLIGHT
TZOFFSETFROM:+0000
DTSTART:20060402T100000
TZNAME:PDT
TZOFFSETTO:-0700
END:DAYLIGHT
BEGIN:STANDARD
TZOFFSETFROM:-0700
DTSTART:20061029T020000
TZNAME:PST
TZOFFSETTO:-0800
END:STANDARD
END:VTIMEZONE
BEGIN:VEVENT
SUMMARY:New Event
DTEND;TZID=America/Vancouver:20060709T143000
DTSTART;TZID=America/Vancouver:20060709T133000
DTSTAMP:20060710T225223Z
UID:95C0F1E7-F691-42DD-8889-4E0B700B9778-3
ATTACHMENT:http://www.example.com/test.jpg
END:VEVENT
END:VCALENDAR"""

    client.put('%s/%s/1.ics' % (PRINCIPAL_DAV_PATH, CALENDAR), body=ics)
    assert client.response.status == 201
    
def test_vavailability():
    ics = """BEGIN:VCALENDAR
CALSCALE:GREGORIAN
PRODID:-//example.com//iCalendar 2.0//EN
VERSION:2.0
BEGIN:VTIMEZONE
LAST-MODIFIED:20040110T032845Z
TZID:America/Montreal
BEGIN:DAYLIGHT
DTSTART:20000404T020000
RRULE:FREQ=YEARLY;BYDAY=1SU;BYMONTH=4
TZNAME:EDT
TZOFFSETFROM:-0500
TZOFFSETTO:-0400
END:DAYLIGHT
BEGIN:STANDARD
DTSTART:20001026T020000
RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10
TZNAME:EST
TZOFFSETFROM:-0400
TZOFFSETTO:-0500
END:STANDARD
END:VTIMEZONE
BEGIN:VAVAILABILITY
ORGANIZER:mailto:bernard@example.com
UID:20061005T133225Z-00001-availability@example.com
DTSTAMP:20061005T133225Z
DTSTART;TZID=America/Montreal:20060101T000000
DTEND;TZID=America/Montreal:20060108T000000
BEGIN:AVAILABLE
UID:20061005T133225Z-00001-A-availability@example.com
DTSTAMP:20061005T133225Z
SUMMARY:Monday, Wednesday and Friday from 9:00 to 12:00
DTSTART;TZID=America/Montreal:20060102T090000
DTEND;TZID=America/Montreal:20060102T120000
RRULE:FREQ=WEEKLY;BYDAY=MO,WE,FR
END:AVAILABLE
BEGIN:AVAILABLE
UID:20061005T133225Z-00001-A-availability@example.com
RECURRENCE-ID;TZID=America/Montreal:20060106T090000
DTSTAMP:20061005T133225Z
SUMMARY:Friday override from 12:00 to 17:00
DTSTART;TZID=America/Montreal:20060106T120000
DTEND;TZID=America/Montreal:20060106T170000
END:AVAILABLE
END:VAVAILABILITY
END:VCALENDAR"""

    client.put('%s/%s/2.ics' % (PRINCIPAL_DAV_PATH, CALENDAR), body=ics)
    assert client.response.status == 201
    
    
def test_status():
    
    ics = """BEGIN:VCALENDAR
VERSION:2.0
CALSCALE:GREGORIAN
PRODID:-//Apple Computer\, Inc//iCal 3.0//EN
BEGIN:VTIMEZONE
LAST-MODIFIED:20060710T225223Z
TZID:America/Vancouver
BEGIN:DAYLIGHT
TZOFFSETFROM:+0000
DTSTART:20060402T100000
TZNAME:PDT
TZOFFSETTO:-0700
END:DAYLIGHT
BEGIN:STANDARD
TZOFFSETFROM:-0700
DTSTART:20061029T020000
TZNAME:PST
TZOFFSETTO:-0800
END:STANDARD
END:VTIMEZONE
BEGIN:VEVENT
SUMMARY:New Event
STATUS:NEEDS-ACTION
DTEND;TZID=America/Vancouver:20060709T143000
DTSTART;TZID=America/Vancouver:20060709T133000
DTSTAMP:20060710T225223Z
UID:95C0F1E7-F691-42DD-8889-4E0B800B9778-3
END:VEVENT
END:VCALENDAR"""
    
    client.put('%s/%s/5.ics' % (PRINCIPAL_DAV_PATH, CALENDAR), body=ics)
    assert client.response.status == 201
    
    ics = """BEGIN:VCALENDAR
CALSCALE:GREGORIAN
PRODID:-//Cyrusoft International\, Inc.//Mulberry v4.0//EN
VERSION:2.0
BEGIN:VTODO
DTSTAMP:20060313T145240Z
STATUS:NEEDS-ACTION
SUMMARY:Task #1
UID:961FBB7850529AAA6195464A--2345@Cyrus-Daboo.local
END:VTODO
END:VCALENDAR"""

    client.put('%s/%s/3.ics' % (PRINCIPAL_DAV_PATH, CALENDAR), body=ics)
    assert client.response.status == 201
    
    
    
    
    
    
    
    
