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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public final class FindMeetingQuery {
    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
        // Create full (mandatory + optional) attendee list
        Collection<String> fullAttendees = new ArrayList<>();
        fullAttendees.addAll(request.getAttendees());
        fullAttendees.addAll(request.getOptionalAttendees());

        MeetingRequest fullRequest = new MeetingRequest(fullAttendees, request.getDuration());
        Collection<TimeRange> resultWithOptionals = queryHelper(events, fullRequest);

        // If any TimeRanges are available with optionals include, return those,
        // Otherwise exculde optionals
        // Notable case: When there are no mandatory attendees, optionals are treated as mandatory and returned.
        if (request.getAttendees().isEmpty() || !resultWithOptionals.isEmpty()) {
            return resultWithOptionals;
        } else {
            return queryHelper(events, request);
        }
    }


    public Collection<TimeRange> queryHelper(Collection<Event> events, MeetingRequest request) {

        // Remove events that do not share in required attendees.
        ArrayList<Event> eventList = new ArrayList<>(events);
        AttendeeHelper attendees = new AttendeeHelper(request.getAttendees());
        
        Iterator<Event> eventIt = eventList.iterator();
        while (eventIt.hasNext()) {
            if (!attendees.eventHasAttendees(eventIt.next())) {
                eventIt.remove();
            }
        }
        ArrayList<TimeRange> eventTimeRanges = new ArrayList<>();
        for (Event event : eventList) {
            eventTimeRanges.add(event.getWhen());
        }
        
        ArrayList<TimeRange> occupiedTimeRanges = sortCombineOverlappingTimeRanges(eventTimeRanges);
        ArrayList<TimeRange> result = new ArrayList<>();
        
        int startTime = TimeRange.START_OF_DAY;
        // For each event, check for time between current event start and end of previous event (initially 0)
        // Time between end of final event and end of day must be checked outside of loop.
        for (TimeRange occupiedTimeRange : occupiedTimeRanges) {
            if (occupiedTimeRange.start() - startTime >= request.getDuration()) {
                result.add(TimeRange.fromStartEnd(startTime, occupiedTimeRange.start(), false));
            }
            startTime = occupiedTimeRange.end();
        }

        // Check for availability between last event and end of day.
        if ((TimeRange.END_OF_DAY + 1 - startTime) >= request.getDuration()) {
            result.add(TimeRange.fromStartEnd(startTime, TimeRange.END_OF_DAY, true));
        }

        return result;
    }

    private ArrayList<TimeRange> sortCombineOverlappingTimeRanges(ArrayList<TimeRange> eventRanges) {
        Collections.sort(eventRanges, TimeRange.ORDER_BY_START);
        if (eventRanges.isEmpty()) {
            return new ArrayList<TimeRange>();
        }

        ArrayList<TimeRange> result = new ArrayList<>();
        Iterator<TimeRange> timeRangeIt = eventRanges.iterator();
        TimeRange nextRange = timeRangeIt.next();
        TimeRange builder = nextRange;

        /*  Build non-overlapping ranges by iteration, beginning with initial event (builder), sorted by start.
            append to results range list based on the overlap with the next event (nextRange).

            // Case 1: |---| |---|    ->  push builder to result, builder := next_range
            //
            // Case 2: |---|          ->  update builder.end := next_range.end
            //            |---|
            //
            // Case 3: |---------|    -> do nothing and continue looping
            //            |---|
        */

        while (timeRangeIt.hasNext()) {
            nextRange = timeRangeIt.next();

            if (!builder.overlaps(nextRange)) {
                result.add(builder);
                builder = nextRange;
            } else if (!builder.contains(nextRange)) {
                builder = TimeRange.fromStartEnd(builder.start(), nextRange.end(), false);
            }
        }

        // Last builder range will not be added in loop.
        // Also if there is only one event, loop will not be entered.
        // So add last builder to the result list after loop

        result.add(builder);
        return result;
    }

    private class AttendeeHelper {
        Collection<String> attendees;

        public AttendeeHelper(Collection<String> attendees_in) {
            attendees = attendees_in;
        }

        // For use in removeIf predicate.
        // returns whether any of our required attendees are also attending a given event.
        public boolean eventHasAttendees(Event event) {
            Set<String> eventAttendees = event.getAttendees();
            for (String person : attendees) {
                if (eventAttendees.contains(person)) {
                    return true;
                }
            }
            return false;
        }
    }

}

