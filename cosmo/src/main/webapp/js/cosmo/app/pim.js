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

dojo.provide("cosmo.app.pim");

dojo.require("dojo.html.common");

// -- Create global vars, do not remove despite lack of refs in code
dojo.require("cosmo.ui.conf");
dojo.require("cosmo.util.i18n");
dojo.require('cosmo.convenience');
// --

// -- FIXME: Weirdness that should be fixed
dojo.require("cosmo.legacy.cal_event");
// --

dojo.require("cosmo.model");
dojo.require("cosmo.datetime");
dojo.require("cosmo.datetime.Date");
dojo.require("cosmo.datetime.util");
dojo.require("cosmo.ui.button");
dojo.require("cosmo.ui.ContentBox");
dojo.require('cosmo.view.cal');
dojo.require('cosmo.account.create');
dojo.require('cosmo.service.conduits.common');
dojo.require('cosmo.app.pim.layout');

// Global variables for X and Y position for mouse
xPos = 0;
yPos = 0;

/**
 * @object The Cal singleton
 */
dojo.lang.mixin(cosmo.app.pim, new function () {
    var self = this;
    // Private variable for the list of any deleted subscriptions
    var deletedSubscriptions = [];

    // The Cosmo service -- used to talk to the backend
    this.serv = null;
    // The base layout for the PIM -- cosmo.ui.ContentBox obj
    this.baseLayout = null;

    // For calculating UI element positions
    this.top = 0;
    this.left = 0;
    this.width = 0;
    this.height = 0;
    // Vertical px pos of top of scrollable area
    // Used to cal positions for draggable elems
    // Changes when resizing all-day event area
    this.viewOffset = 0;
    // Width of the middle column of UI elements
    // All-day resizable area, scrolling area for normal events, detail form
    // Calculated based on client window size
    this.midColWidth = 0;
    // Current date for highlighting in the interface
    this.currDate = null;
    // The path to the currently selected collection
    this.currentCollection = null;
    //The list of calendars available to the current user
    this.currentCollections = [];
    // Anonymous ticket view, no client-side timeout
    this.authAccess = true;
    // Ticket passed, if any
    this.ticketKey = null;
    // Create the 'Welcome to Cosmo' event?
    this.createWelcomeItem = false;

    // ==========================
    // Init
    // ==========================
    /**
     * Main function
     */
    this.init = function (p) {
        var params = p || {};
        var collectionUid = params.collectionUid;

        this.ticketKey = params.ticketKey;
        this.authAccess = params.authAccess;
        this.currDate = new Date();

        // Do some setup
        // ===============================
        // Create and init the Cosmo service
        this.serv = cosmo.service.conduits.getAtomPlusEimConduit();
        // Localized date strings
        this.loadLocaleDateInfo();
        // Tell the calendar view what week we're on
        cosmo.view.cal.setQuerySpan(this.currDate)
        // Load collections for this user
        this.loadCollections(this.ticketKey, collectionUid);

        // Base layout
        // ===============================
        // FIXME: Safari -- Need to valign-middle the whole-screen mask
        this.baseLayout = cosmo.app.pim.layout.initBaseLayout({ domNode: $('baseLayout') });

        //cosmo.app.showHideSelectBoxes(false);

        // Load data -- successful load triggers render of UI widgets
        // ===============================
        cosmo.view.cal.loadEvents({ collection: this.currentCollection,
            viewStart: cosmo.view.cal.viewStart, viewEnd: cosmo.view.cal.viewEnd });

        // Show errors for deleted subscriptions -- deletedSubscriptions
        // is a private var populated in the loadCollections method
        if (deletedSubscriptions && deletedSubscriptions.length > 0){
            for (var x = 0; x < deletedSubscriptions.length; x++){
                cosmo.app.showErr(_("Main.Error.SubscribedCollectionDeleted",
                    deletedSubscriptions[x].displayName));
            }
        }
    };

    // ==========================
    // Localization
    // ==========================
    /**
     * Loads localized datetime info for the UI
     */
    this.loadLocaleDateInfo = function () {
        var keys = null;
        var newArr = null;
        // Use the default set of days as the keys to create an array of
        // localized versions -- e.g., 'Main.Wed' or 'Main.Thu'
        // Replace the default set with the localized set
        // --------------------
        // Weekday abbreviations array
        // ============
        keys = cosmo.datetime.abbrWeekday;
        newArr = [];
        for (var i = 0; i < keys.length; i++) {
            newArr.push(_('App.' + keys[i]));
        }
        cosmo.datetime.abbrWeekday = newArr;
        // Full month names array
        // ============
        keys = cosmo.datetime.fullMonth;
        newArr = [];
        for (var i = 0; i < keys.length; i++) {
            newArr.push(_('App.' + keys[i]));
        }
        cosmo.datetime.fullMonth = newArr;
        // AM/PM
        // ============
        newArr = [];
        newArr['AM'] = _('App.AM');
        newArr['PM'] = _('App.PM');
        Date.meridian = newArr;
        return true;
    };
    // ==========================
    // Timeout and keepalive
    // ==========================
    this.isTimedOut = function () {
        // Logged in or not, there's no client-side timeout for ticket view
        if (!this.authAccess) {
            return false;
        }
        else {
            var diff = 0;
            diff = new Date().getTime() - this.serv.getServiceAccessTime();
            if (diff > (60000*cosmo.env.getTimeoutMinutes())) {
                return true;
            }
            else {
                return false;
            }
        }
    };
    this.checkTimeout = function () {
        function isServerTimeoutSoon() {
            var ts = new Date();
            var diff = 0;
            ts = ts.getTime();
            diff = ts - self.serv.getServiceAccessTime();
            return !!(diff > (60000*(cosmo.env.getTimeoutMinutes()-2)));
        }

        // If user is client-side timed-out, kill the session cookie and redirect to login page
        if (this.isTimedOut()) {
            this.redirectTimeout();
            return true;
        }
        // Otherwise check for imminent server timeout and refresh local timing cookie
        else if (isServerTimeoutSoon()) {
            // If server-side session is about to time out, refresh it by hitting JSP page
            this.serv.refreshServerSession();
            // Reset local session timing cookie
            this.serv.resetServiceAccessTime();
            return false;
        }
    };
    this.redirectTimeout = function () {
        location = cosmo.env.getRedirectUrl();
    };

    // ==========================
    // Collections
    // ==========================
    this.loadCollections = function (ticketKey, collectionUid) {
        // Load/create calendar to view
        // --------------
        // If we received a ticket, just grab the specified collection
        if (ticketKey) {
            try {
               var collection = this.serv.getCollection(collectionUid, {ticketKey:ticketKey, sync:true}).results[0];
            }
            catch(e) {
                cosmo.app.showErr(_('Main.Error.LoadEventsFailed'), e);
                return false;
            }
            this.currentCollections.push(collection);
        }

        // Otherwise, get all collections for this user
        else {
            var userCollections = this.serv.getCollections({sync: true}).results[0];
            for (var i = 0; i < userCollections.length; i++){
                var collection = userCollections[i];
                this.currentCollections.push(collection);
            }

            // No collections for this user
            if (this.currentCollections.length == 0){
                 //XINT
                 throw new Error("No collections!")
            }
            var subscriptions = this.serv.getSubscriptions().results[0];
            //XINT make sure this still works!
            var result = this.filterOutDeletedSubscriptions(subscriptions);
            subscriptions = result[0];
            deletedSubscriptions = result[1];

            for (var i = 0; i < subscriptions.length; i++){
                var subscription = subscriptions[i];
                this.currentCollections.push(subscription);
            }
        }

        // Sort the collections
        var f = function (a, b) {
            var aName = a.getDisplayName().toLowerCase();
            var bName = b.getDisplayName().toLowerCase();
            var r = 0;
            if (aName == bName) {
                r = (a.collection.getUid() > b.collection.getUid()) ? 1 : -1;
            }
            else {
                r = (aName > bName) ? 1 : -1;
            }
            return r;
        };
        this.currentCollections.sort(f);

        // If we received a collectionUid, select that collection
        if (collectionUid){
            for (var i = 0; i < this.currentCollections.length; i++){
                if (this.currentCollections[i].getUid() == collectionUid){
                    this.currentCollection = this.currentCollections[i];
                    break;
                }
            }
        }
        // Otherwise, use the first collection
        else {
            this.currentCollection = this.currentCollections[0];
        }
    };
    this.handleCollectionUpdated = function(/*cosmo.topics.CollectionUpdatedMessage*/ message){
        var updatedCollection = message.collection;

        for (var x = 0; x < this.currentCollections.length; x++){
            var collection = this.currentCollections[x];
            if (collection instanceof cosmo.model.Collection
                   && collection.getUid() == updatedCollection.getUid()){
                this.currentCollections[x] = updatedCollection;
            }
        }
    };

    this.handleSubscriptionUpdated = function(/*cosmo.topics.SubscriptionUpdatedMessage*/ message){
        var updatedSubscription = message.subscription;
        for (var x = 0; x < this.currentCollections.length; x++){
            var subcription = this.currentCollections[x];
            if (subcription instanceof cosmo.model.Subscription
                   && subcription.getUid() == updatedSubscription.getUid()){
                this.currentCollections[x] = updatedSubscription;
            }
        }
    };

    //XINT
    this.filterOutDeletedSubscriptions = function(subscriptions){
        var deletedSubscriptions = [];
        var filteredSubscriptions = dojo.lang.filter(subscriptions,
            function(sub){
               if (!sub.calendar){
                   self.serv.deleteSubscription(sub.uid, sub.ticket.ticketKey);
                   deletedSubscriptions.push(sub);
                   return false;
               } else {
                   return true;
               }
        });
        return [filteredSubscriptions, deletedSubscriptions];
    };

    // ==========================
    // Cleanup
    // ==========================
    this.cleanup = function () {
        if (this.uiMask) {
            this.uiMask.cleanup();
        }
        if (this.allDayArea) {
            this.allDayArea.cleanup();
        }
        this.allDayArea = null;
    };
});

Cal = cosmo.app.pim;

