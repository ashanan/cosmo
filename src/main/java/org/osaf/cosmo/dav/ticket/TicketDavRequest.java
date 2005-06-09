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
package org.osaf.cosmo.dav.ticket;

import org.osaf.cosmo.model.Ticket;

/**
 * Provides request functionality related to "Ticket-Based Access
 * Control Extension to WebDAV".
 *
 * @see http://www.sharemation.com/%7Emilele/public/dav/draft-ito-dav-ticket-00.txt
 */
public interface TicketDavRequest {

    /**
     * Return a {@link Ticket} representing the information about a
     * ticket contained in the request.
     *
     * @throws IllegalArgumentException if ticket information exists
     * but is invalid
     */
    public Ticket getTicket();
}
