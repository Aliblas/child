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

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;
import com.google.gson.Gson;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Key;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);

    String queryString = request.getParameter("comment-capacity");
    int commentCapacity = Integer.parseInt(queryString);

    System.out.println("Preparing " + commentCapacity + " comments.");

    List<String> commentsList = new ArrayList<>();
    Iterator<Entity> entityIterator = results.asIterator();
    for (int i = 0; i < commentCapacity; i++) {
        if (!entityIterator.hasNext()) break;
        Entity entity = entityIterator.next();

        String commentString = (String)entity.getProperty("commentString");
        String nickname = (String)entity.getProperty("nickname");

        String commentDatum = createJsonComment(nickname, commentString);
        commentsList.add(commentDatum);
    }

    String commentsJson = new Gson().toJson(commentsList);
    response.setContentType("application/json;");
    response.getWriter().println(commentsJson);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      String commentString = request.getParameter("comment-input");
      commentString = escapeSpecialChars(commentString);

      if (!commentString.isEmpty()) {
          Entity commentEntity = createCommentEntity(commentString);

          DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
          datastore.put(commentEntity);
      }

      response.sendRedirect("/index.html");
  }

  private Entity createCommentEntity(String text) {
      Entity commentEntity = new Entity("Comment");
      commentEntity.setProperty("timestamp", System.currentTimeMillis());
      commentEntity.setProperty("commentString", text);

      UserService userService = UserServiceFactory.getUserService();
      String userEmail = userService.getCurrentUser().getEmail();
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Key userKey = KeyFactory.createKey("userPublicProfile", userEmail);
      
      try {
        Entity userEntity = datastore.get(userKey);
        String nickname = (String)userEntity.getProperty("nickname");
        commentEntity.setProperty("nickname", nickname);
      } catch(Exception e) {
        commentEntity.setProperty("nickname", "Nameless One");
      }
      
      return commentEntity;
  }

  private String createJsonComment(String nickname, String comment) {
      String json = 
      "{ \"name\" : \"" + nickname + "\", \"comment\" : \"" + comment + "\" }";
      return json;
  }

// https://stackoverflow.com/questions/3844595/

  private String escapeSpecialChars(String str) {
    StringBuilder builder = new StringBuilder();
    for (char c : str.toCharArray()) {
        if (c == '\'') {
            builder.append("\\'");
        } else if (c == '\"') {
            builder.append("\\\"");
        } else if(c == '\r') {
            builder.append("\\r");
        } else if(c == '\n') {
            builder.append("\\n");
        } else if(c == '\t') {
            builder.append("\\t");
        } else if(c < 32 || c >= 127 ) {
            // Format special characters (determine by ascii value)
            builder.append(String.format("\\u%04x", (int)c));
        } else {
            builder.append(c);
        }
    }
    return builder.toString();
  }
}

