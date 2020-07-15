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

  // Runtime: O(n^2) where n is number of events passed in. 
  // Ideally this problem could be dealt in O(n) time with the events sorted before 
  // finding timeslots. 
  
  /** Given a collection of events and a meeting request, will return a collection of TimeRange that can accommodate Optional + Mandatory
  Attendees or just Mandatory Attendees if no solutions exist for Optional + Mandatory */
  // query uses query helper which returns TimeRange solutions for events and Mandatory attendees
  public Collection<TimeRange> query (Collection<Event> events, MeetingRequest request){
      // For optional attendees, just run queryhelper twice. First with optional  attendees included, if that is 
      // not empty then return. If empty, then queryhelper just with mandatory attendees. 
      Collection<String> allAttendees = new ArrayList<>(request.getAttendees());
      allAttendees.addAll(request.getOptionalAttendees());
      MeetingRequest requestAll = new MeetingRequest (allAttendees, request.getDuration()); 
      Collection<TimeRange> solutionsAll = queryHelper(events, requestAll);
      // Return query with optional guests, if there are options
      // or if there are no mandatory attendees.
      if (!(solutionsAll.isEmpty()) || request.getAttendees().isEmpty()){
        return solutionsAll;
      }
      else{
          return queryHelper(events, request);
      }
  }

  //Runtime: O(n^2) where n is the size of the events. 
  /** Given a collection of events and a meeting request, returns a Collection of 
  TimeRanges that can accommodate the request.*/
  // Given a possible solution of WHOLE_DAY it takes out portions of time where events conflict.
  private Collection<TimeRange> queryHelper(Collection<Event> events, MeetingRequest request) {
    // Want to return a collection of time ranges that work for the given request's Attendees.
    ArrayList<TimeRange> solutions = new ArrayList<>();
    // No options for request longer than a day. Will return no solutions.
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
        if (!overlappingAttendees(request, event)){
            continue;
        }
        // Take out conflicting time from current answer of TimeRanges.
        solutions = takeOutConflicts(solutions, event, request.getDuration());
    }
    return solutions;

  }

  /**Splits a TimeRange into two TimeRanges if conflicting event is contained in TimeRange
   or shotens one TimeRange due to conflict with a given event */
  // Runtime: O(n) where n is the size of the Old Solutions.
  private ArrayList<TimeRange> takeOutConflicts(ArrayList<TimeRange> oldSolutions, Event event, long duration) {
      ArrayList<TimeRange> solutions = new ArrayList<>();
      for (TimeRange slot: oldSolutions){
          if (slot.contains(event.getWhen())){
              // Need to split along contained event.
              TimeRange slotA = TimeRange.fromStartEnd(slot.start(), event.getWhen().start(), false);
              TimeRange slotB = TimeRange.fromStartEnd(event.getWhen().end(), slot.end(), false);
              //check if duration is still good for the resulting slots.
              if (checkTimeRangeisLongEnough(duration, slotA)){
                  solutions.add(slotA);
              }
              if (checkTimeRangeisLongEnough(duration, slotB)){
                  solutions.add(slotB);
              }
          }
          else if (slot.overlaps(event.getWhen())){ // Need to take out slot and event overlap.
              // First make sure that conflicting event does not contian slot
              // if it does, no longer want to add slot back into solutions.
              if (event.getWhen().contains(slot)){
                  continue;
              }
              //shorten slot in the front
              else if (event.getWhen().end() > slot.start()){
                   TimeRange shortenedSlot = TimeRange.fromStartEnd(event.getWhen().end(), slot.end(), false);
                   if (checkTimeRangeisLongEnough(duration, shortenedSlot)){
                       solutions.add(shortenedSlot);
                   }
              }
              else{
                  TimeRange shortenedSlot = TimeRange.fromStartEnd(slot.start(), event.getWhen().start(), false);
                  if (checkTimeRangeisLongEnough(duration, shortenedSlot)){
                      solutions.add(shortenedSlot);
                  }
              }
              
          }
          else{ //If there is no conflict between event and slot, add unmodified slot back into solutions.
              solutions.add(slot);
          }
      }
      return solutions;
    
  }

  /**Returns true if at least one attendee in Meeting Request is an attendee in the Event */
  // Runtime: addAll and retainAll are O(n) time (I could be wrong on that). 
  private boolean overlappingAttendees(MeetingRequest request, Event event){
      // Since request.getAttendees returns an inmuttable set. Need to copy it.
      ArrayList<String> requestedAttendees = new ArrayList<>(request.getAttendees());
      requestedAttendees.retainAll(event.getAttendees());
      return !requestedAttendees.isEmpty();
  }

/**Checks if timerange can accomodate the duration in the meeting request and returns true or false. */
// Runtime: constant. 
  private boolean checkTimeRangeisLongEnough (long duration, TimeRange timerange){
      return (timerange.duration() >= duration);
  }
}
