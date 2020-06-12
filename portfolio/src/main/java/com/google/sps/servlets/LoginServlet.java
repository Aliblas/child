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
            String userId = userService.getCurrentUser().getUserId();
            String nickname = getUserNickname(userId);
            if (nickname == null) nickname = "Anonymous";
            json.addProperty("nickname", nickname);
            json.addProperty("userLoggedIn", true);
        }

        response.getWriter().println(json.toString());
    }

    @Override //Implements settings post request
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
    }

    private String getUserNickname(String id) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query =
            new Query("UserInfo")
                .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
        PreparedQuery results = datastore.prepare(query);
        Entity entity = results.asSingleEntity();
        if (entity == null) {
            return null;
        }
        String nickname = (String) entity.getProperty("nickname");
        return nickname;
    }
}