/*
 * Copyright 2006-2008 Open Source Applications Foundation
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
package org.osaf.cosmo.model;

/**
 * An exception that indicates that the uid chosen for an item is
 * already in use by another item.
 */
public class UidInUseException extends RuntimeException {

    String uid = null;
    
    /** */
    public UidInUseException(String uid, String message) {
        super(message);
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }
}
