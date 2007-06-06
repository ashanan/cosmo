/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.osaf.cosmo.atom.provider;

import org.apache.abdera.protocol.server.provider.RequestContext;
import org.apache.abdera.protocol.server.provider.TargetType;

import org.osaf.cosmo.model.Preference;
import org.osaf.cosmo.model.User;

/**
 * <p>
 * A target that identifies a particular user preference.
 * </p>
 */
public class PreferenceTarget extends UserTarget {

    private Preference preference;

    public PreferenceTarget(RequestContext request,
                            User user,
                            Preference preference) {
        super(TargetType.TYPE_ENTRY, request, user);
        this.preference = preference;
    }

    public Preference getPreference() {
        return preference;
    }
}
