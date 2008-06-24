/*
	Copyright (c) 2004-2008, The Dojo Foundation
	All Rights Reserved.

	Licensed under the Academic Free License version 2.1 or above OR the
	modified BSD license. For more information on Dojo licensing, see:

		http://dojotoolkit.org/book/dojo-book-0-9/introduction/licensing
*/


if(!dojo._hasResource["cosmo.testutils"]){dojo._hasResource["cosmo.testutils"]=true;dojo.provide("cosmo.testutils");dojo.require("dojox.uuid.generateRandomUuid");dojo.require("cosmo.cmp");dojo.require("cosmo.util.auth");cosmo.testutils={init:function initCosmoTests(_1){for(var i=0;i<_1.length;i++){var _3=_1[i];try{dojo.require(_3);var _4=dojo.getObject(_3);var _5=this.getFunctionNames(_4);var _6=[];for(var j in _5){var _8=_5[j];_6.push({name:_8,setUp:function(){},runTest:_4[_8],tearDown:function(){}});}}catch(error){doh.register(_3,[function failure(){throw (error);}]);continue;}doh.register(_3,_6);}jum=new JUM();},getFunctionNames:function getFunctionNames(_9){var _a=[];for(var _b in _9){if(_b.indexOf("test_")==0&&typeof _9[_b]=="function"){_a.push(_b);}}return _a;},createTestAccount:function(){return cosmo.testutils.createUser(dojox.uuid.generateRandomUuid().slice(0,8));},createUser:function(_c,_d){cosmo.util.auth.clearAuth();var _e={password:"testing",username:_c,firstName:_c,lastName:_c,email:_d||_c+"@cosmotesting.osafoundation.org"};var d=cosmo.cmp.signup(_e,{sync:true});d.addCallback(function(_10){cosmo.util.auth.setCred(_e.username,_e.password);});d.addErrback(function(e){console.log(e);});return this.defcon(d);},cleanupUser:function(_12){cosmo.util.auth.setCred("root","cosmo");cosmo.cmp.deleteUser(_12.username,{handle:function(){}},true);cosmo.util.auth.clearAuth();},defcon:function(_13){var d2=new doh.Deferred();_13.addCallbacks(dojo.hitch(d2,d2.callback),dojo.hitch(d2,d2.errback));return d2;}};}