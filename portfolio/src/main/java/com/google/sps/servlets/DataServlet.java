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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import com.google.gson.Gson;

/** Servlet that returns comment data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
    
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
     // Get the user input on how many comments to display.
     // If user input for max comments is greater than all comments, all comments will display.
     // If comments are empty, no comments will be displayed.
     
    int maxComments = getNumDisplayComments(request);
     // Retrieves comment data from datastore. 
     // Displays most recent comments first.
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
     // TODO (@zous): add option to see oldest/newest comments first.

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // Create arraylist of string to store comments.
    ArrayList<String> comments = new ArrayList<>();

    for (Entity entity : results.asIterable()) {
     
      if (comments.size() == maxComments){
          break;
      }
      String text = (String) entity.getProperty("text");
      comments.add(text);
      
      
    }

    Gson gson = new Gson();

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String text = getParameter(request, "text-input", "");
    long timestamp = System.currentTimeMillis();
    Entity CommentEntity = new Entity("Comment");
    CommentEntity.setProperty("text", text);
    CommentEntity.setProperty("timestamp", timestamp);
    // Store Comment.
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(CommentEntity);

    response.sendRedirect("/index.html");
  }

  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  /** Returns the integer entered by user on how many comments  */
  private int getNumDisplayComments(HttpServletRequest request) {
    // Get the input from the form on MaxComments and convert to integer to the
    // toGet function.
    String commentNumString = request.getParameter("max-comments");
    // Convert the input to an int.
    int commentNum;
    try {
      commentNum = Integer.parseInt(commentNumString);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
            "Expected integer number of comments, but got " +
            commentNumString);
      return -1;
    }

    return commentNum;
  }
}
