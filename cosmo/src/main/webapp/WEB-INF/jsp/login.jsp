<%@ page language="java" contentType="text/html; charset=UTF-8" %>

<%--
/*
 * Copyright 2005-2006 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
--%>

<%@ include file="/WEB-INF/jsp/taglibs.jsp"  %>
<%@ include file="/WEB-INF/jsp/tagfiles.jsp" %>

<!DOCTYPE html
  PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title>
      <fmt:message key="Login.HeadTitle">
        <c:forEach var="p" items="${TitleParam}">
          <fmt:param value="${p}"/>
        </c:forEach>
      </fmt:message>
      
    </title>
	<cosmo:staticbaseurl var="staticBaseUrl"/>
	<cosmo:dojoBoilerplate/>
	
	<link rel="stylesheet" href="${staticBaseUrl}/templates/default/global.css"/>
    <link rel="self" type="text/html" href="${staticBaseUrl}/login"/>
    
    <fmt:setBundle basename="MessageResources"/>
    
    <%--
        Login and account-creation stuff
        Note: button.js still needed to do preloading of button images
    --%>
    <script type="text/javascript" src="${staticBaseUrl}/js/cosmo/ui/button.js"></script>
    <script type="text/javascript">

	    dojo.require('cosmo.ui.global_css');
        dojo.require("cosmo.app");
        dojo.require("cosmo.account.create");
        dojo.require("cosmo.util.popup");
        dojo.require("cosmo.convenience");
        dojo.require("cosmo.ui.widget.LoginDialog");
        dojo.require("cosmo.ui.widget.ModalDialog");
        dojo.require("cosmo.util.uri");
        
        dojo.event.browser.addListener(window, "onload", init, false);

        function init() {
            cosmo.util.cookie.destroy('JSESSIONID', '${staticBaseUrl}');
            cosmo.util.cookie.destroy('inputTimestamp');
            cosmo.util.cookie.destroy('username');
		    cosmo.util.auth.clearAuth();
		    
            cosmo.app.init();
            if (cosmo.util.uri.parseQueryString(location.search)['signup']
				== 'true'){
				cosmo.account.create.showForm();
			}
			            
        }
    </script>
  </head>
  <body>
    <div>
      <div dojoType="cosmo:LoginDialog" widgetId="loginDialog">
      </div>
      <div style="padding-top:24px; text-align:center">
        <fmt:message key="Login.CreateAccount">
        <fmt:param value="<a href=\"javascript:cosmo.account.create.showForm();\">"/>
        <fmt:param value="</a>"/>
        </fmt:message>
      </div>

      <div style="padding-top:4px; text-align:center;">
        <fmt:message key="Login.Forgot">
        <fmt:param value="<a href=\"${staticBaseUrl}/account/password/recover\">"/>
        <fmt:param value="</a>"/>
        </fmt:message>
	  </div>
      
      <div style="padding-top:36px; text-align:center;">
        <a href="javascript:cosmo.util.popup.open('${staticBaseUrl}/help/about', 360, 280);">
          <fmt:message key="Login.AboutCosmo"/>
        </a>
      </div>
    </div>
  </body>
</html>
