/*
 * Copyright 2008 Open Source Applications Foundation
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
dojo.provide("cosmo.ui.widget.AdvancedSearch");
dojo.require("dojo.fx");
dojo.require("cosmo.util.html");
dojo.require("dijit.form.TextBox");
dojo.require("dijit.form.DateTextBox");
dojo.require("dijit.form.TimeTextBox");
dojo.require("dijit.form.CheckBox");
dojo.require("dijit.form.Button");

dojo.declare("cosmo.ui.widget.AdvancedSearch", [dijit._Widget, dijit._Templated],
{

    // Processing lock to avoid duplicate items created
    isProcessing: false,

    parent: null,
    textBox: null,
    button: null,

    l10n: null,

    widgetsInTemplate: true,
    templateString: '<div class="advancedSearchContainer">'
        + '<input type="text" id="titleSearchEntry" class="searchTextInput inputText" dojoAttachPoint="textBox" dojoAttachEvent="onkeyup: onKeyUp" dojoAttachEvent="onmouseover: testAttach"></input>'
        + '<button id="searchSubmit" class="advancedSearchButton" dojoType="dijit.form.Button" dojoAttachPoint="button" label="Search!" dojoAttachEvent="onclick: onSubmit"></button>'  
        + '</div>', //label="${l10n.submit}"

    constructor: function(){
	//remember to add i18n
        //this.l10n = dojo.i18n.getLocalization("cosmo.ui.widget", "AwesomeBox"); 
    },

    testAttach: function(){
        console.log("test");
    },

    getWriteable: function(){
        return cosmo.app.pim.getSelectedCollectionWriteable();
    },

    getValue: function() {
        return this.textBox.value;
    },

    setValue: function(value) {
        this.textBox.value = value;
    },

    updateHint: function() {
        cosmo.util.html.setTextInput(this.textBox, this.l10n.hintText, true);
    },

    disableButton: function () {
        this.button.setAttribute("disabled", true);
    },

    enableButton: function () {
        this.button.setAttribute("disabled", false);
    },

    disableText: function () {
        this.textBox.disabled = true;
    },

    enableText: function () {
        this.textBox.disabled = false;
    },

    onSubmit: function () {
        console.log("onSubmit");
/*
        // Only submit one action at a time
	
        if (this.isProcessing) { return false; }
        this.isProcessing = true;
        var title = this.getValue();
        this.disableButton();
        this.disableText();
        this.setValue(_('App.Status.Processing'));
	if(title.slice(0,3)=="/f ") {
		console.log("In searchParse...");
		console.log(title.slice(0,3));
		console.log(title.substr(3));
		/*if(title.charAt(3) == "\"" && title.charAt(title.length-1) == "\""){
			//exact search
			cosmo.view.list.loadItems({noDashboard:true,searchCrit:{query:title.substr(3), searchType:"exactSearch"}});
		}
		else *//*
		cosmo.view.list.loadItems({noDashboard:true,searchCrit:{query:title.substr(3), searchType:"basicSearch"}});
		//dojo.byId("listViewContainer").style.backgroundColor="red";//#f7fbff
	}
        else { 
		console.log("Creating note...");
		console.log(title.slice(0,3));
		console.log(title.substr(3));
		cosmo.view.list.createNoteItem(title); 
	}*/
    },

    onKeyUp: function(e){
        if (this.getWriteable() && e.keyCode == 13) {
            this.onSubmit();
            e.stopPropagation();
        }
    },

    disable: function(){
        this.disableButton();
        this.disableText();
    },

    enable: function(){
        this.enableButton();
        this.enableText();
    },

    reset: function(){
        this.isProcessing = false;
        this.updateHint();
        if (this.getWriteable()){
            this.enable();
            dojo.connect(this.textBox, 'onfocus', cosmo.util.html.handleTextInputFocus);
            dojo.connect(this.textBox, 'onblur', this, 'updateHint');
        } else {
            this.disable();
        }
    },

    render: function(){
        this.reset();
        console.log("cosmo.ui.widget.AwesomeBox.render deprecated");
    }
});

