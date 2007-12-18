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

dojo.provide("cosmotest.testutils");

/*dojo.require("cosmo.cmp");
dojo.require("cosmo.util.auth");
*/
cosmotest.testutils = {
    init: function initCosmoTests(/*Array*/ testModules){

        for (var i = 0; i < testModules.length; i++){
            var moduleName = testModules[i];
            try {
                dojo.require(moduleName);
                var module = dojo.getObject(moduleName);
                var functionNames = this.getFunctionNames(module);
                
                var testFunctions = [];
                for (var i in functionNames){
                    var name = functionNames[i];
                    testFunctions.push(
                        {
                            name: name,
			    setUp: function(){
			    },
			    runTest: module[name],
			    tearDown: function(){
			    }
                        }
                    );
                }
            } catch (error){
                doh.register(moduleName, 
                             [function failure(){
                                 throw(error);
                             }]);
                continue;
            }

            doh.register(moduleName, testFunctions);
        }

        dojo.global.jum = doh;
    },
    
    getFunctionNames: function getFunctionNames(scope){
        var fNames = [];
        for (var name in scope){
            if (name.indexOf("test_") == 0 && typeof scope[name] == "function"){
                fNames.push(name);
            }
        }
        return fNames; 
    },

    createTestAccount: function(){
        return cosmotest.testutils.createUser("User0");
    },

    createUser: function(username, email){
        cosmo.util.auth.clearAuth();
        var user = {
            password: "testing",
            username: username,
            firstName: username,
            lastName: username,
            email: (email || username) + "@cosmotesting.osafoundation.org"
        };
        cosmo.cmp.signup(user, {
            load: function(type, data, evt){
                cosmo.util.auth.setCred(user.username, user.password);
            },
            error: function(e){
                dojo.debug(e);
            }}, true);
        return user;
    },
    
    cleanupUser: function(user){
        cosmo.util.auth.setCred("root", "cosmo");
        cosmo.cmp.deleteUser(user.username, {handle: function(){}}, true);
        cosmo.util.auth.clearAuth();
    }
}
