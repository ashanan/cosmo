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

dojo.provide("cosmotest.integration.test_atompub")

dojo.require("cosmotest.util");

dojo.require("cosmo.atompub");
dojo.require("cosmo.env");

cosmotest.integration.test_atompub = {
    test_Service: function(){
        try { 
            var user = cosmotest.util.createTestAccount();
            var service = cosmo.atompub.initializeService(cosmo.env.getBaseUrl() + "/atom/user/" + user.username);
            service.
        }
        finally{
            cosmotest.util.cleanup(user);            
        }

    }
}