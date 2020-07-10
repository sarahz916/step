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

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class FindMeetingQuery {


  public Collection<TimeRange> query (Collection<Event> events, MeetingRequest request){
        // For optional attendees, just run query twice. First with optional  attendees included, if that is 
        // not empty then return. If empty, then query just with mandatory attendees. 
      Collection<String> allAttendees = new ArrayList<>();
      allAttendees.addAll(request.getAttendees());
      allAttendees.addAll(request.getOptionalAttendees());
      MeetingRequest requestAll = new MeetingRequest (allAttendees, request.getDuration()); 
      Collection<TimeRange> solutionsAll = queryHelper(events, requestAll);
      // Return query of with optional guests, if there are options
      // or if there are no mandatory attendees.
      if (!(solutionsAll.isEmpty()) || request.getAttendees().isEmpty()){
        return solutionsAll;
      }
      else{
          return queryHelper(events, request);
      }
  }

  private Collection<TimeRange> queryHelper(Collection<Event> events, MeetingRequest request) {
    // Want to return a collection of time ranges that work for the given request.
    ArrayList<TimeRange> solutions = new ArrayList<>();
    // No options for too long of a requests.
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()){
      return solutions;
    }
    solutions.add(TimeRange.WHOLE_DAY);
    // Process case where duration == 0. Would also return whole day. 
    // If there are no attendees then return WHOLE_DAY.
    if (request.getAttendees().isEmpty() || request.getDuration() == 0){
      return solutions;
    }
    for (Event event: events){
        // Check if event attendees are in the request. If not continue to next event.
        boolean relevantEvent = overlappingAttendees(request, event);
        if (!relevantEvent){
            continue;
        }
        // Take out conflicting time from current answer of TimeRanges.
        solutions = takeOutConflicts(solutions, event, request.getDuration());
    }
    return solutions;
    //cannot conflict with the collection of events given.
    //make sure to check end point of time range is inclusive. 
  }

  /**Splits a TimeRange into two time or shotens one TimeRange due to conflict with a given event */
  private ArrayList<TimeRange> takeOutConflicts(ArrayList<TimeRange> oldSolutions, Event event, long duration) {
      ArrayList<TimeRange> solutions = new ArrayList<>();
      for (TimeRange slot: oldSolutions){
          if (slot.contains(event.getWhen())){
              //need to split slot into two
              TimeRange slotA = TimeRange.fromStartEnd(slot.start(), event.getWhen().start(), false);
              TimeRange slotB = TimeRange.fromStartEnd(event.getWhen().end(), slot.end(), false);
              //check if duration is still good for both
              if (durationCheck(duration, slotA)){
                  solutions.add(slotA);
              }
              if (durationCheck(duration, slotB)){
                  solutions.add(slotB);
              }
          }
          else if (slot.overlaps(event.getWhen())){
              // if event contains slot continue in the loop
              if (event.getWhen().contains(slot)){
                  continue;
              }
              //shorten slot in the front
              else if (event.getWhen().end() > slot.start()){
                   TimeRange shortenedSlot = TimeRange.fromStartEnd(event.getWhen().end(), slot.end(), false);
                   if (durationCheck(duration, shortenedSlot)){
                       solutions.add(shortenedSlot);
                   }
              }
              else{
                  TimeRange shortenedSlot = TimeRange.fromStartEnd(slot.start(), event.getWhen().start(), false);
                  if (durationCheck(duration, shortenedSlot)){
                      solutions.add(shortenedSlot);
                  }
              }
              
          }
          else{
              solutions.add(slot);
          }
      }
      return solutions;
    
  }

  /**Returns true if at least one attendee in Meeting Request is an attendee in the Event */
  private boolean overlappingAttendees(MeetingRequest request, Event event){
      for (String attendee : event.getAttendees()){
          if (request.getAttendees().contains(attendee))
            return true;
        }
      return false;
  }

  private boolean durationCheck (long duration, TimeRange timerange){
      return (timerange.duration() >= duration);
  }
}
