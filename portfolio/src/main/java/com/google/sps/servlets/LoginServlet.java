// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/user-data")
public class LoginServlet extends HttpServlet {

    // doGet is used by comment sumbission area.
    // if user is logged in: Provides user-data for sending comments.
    // if user is logged out: provides login link.
    @Override
    public void doGet (HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        UserService userService = UserServiceFactory.getUserService();

        JsonObject json = new JsonObject();

        if (!userService.isUserLoggedIn()) {
            String loginUrl = userService.createLoginURL("/");
            json.addProperty("loginUrl", loginUrl);
            json.addProperty("userLoggedIn", false);
        } else {
            String nickname = getCurrentUserNickname();
            if (nickname == null) {
                nickname = "Nameless-individual";
            }
            json.addProperty("nickname", nickname);
            json.addProperty("userLoggedIn", true);
        }

        response.getWriter().println(json.toString());
    }

    // doPost implements settings.html post request.
    // Associates settings input with user account entity in datastore.
    // Relevant entities contain public profile information only.
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String nickname = request.getParameter("nickname-input");

        if (nickname == null) {
            System.out.println("invalid nickname requested");
            return;
        }

        nickname = escapeSpecialChars(nickname);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        UserService userService = UserServiceFactory.getUserService();
        String userEmail = userService.getCurrentUser().getEmail();

        Entity entity = new Entity("userPublicProfile", userEmail);
        entity.setProperty("nickname", nickname);

        datastore.put(entity);

        response.sendRedirect("/settings.html");
    }

    // https://stackoverflow.com/questions/3844595/

  private String escapeSpecialChars(String str) {
    StringBuilder builder = new StringBuilder();
    for( char c : str.toCharArray() )
    {
        if( c == '\'' )
            builder.append( "\\'" );
        else if ( c == '\"' )
            builder.append( "\\\"" );
        else if( c == '\r' )
            builder.append( "\\r" );
        else if( c == '\n' )
            builder.append( "\\n" );
        else if( c == '\t' )
            builder.append( "\\t" );
        else if( c < 32 || c >= 127 )
            builder.append( String.format( "\\u%04x", (int)c ) );
        else
            builder.append( c );
    }
    return builder.toString();
  }

  private String getCurrentUserNickname() {
    UserService userService = UserServiceFactory.getUserService();
    String userEmail = userService.getCurrentUser().getEmail();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key userKey = KeyFactory.createKey("userPublicProfile", userEmail);
    String nickname;

    try {
        Entity userEntity = datastore.get(userKey);
        nickname = (String)userEntity.getProperty("nickname");
    } catch(Exception e) {
        return null;
    }

    return nickname;
  }
}