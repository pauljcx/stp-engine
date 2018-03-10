/*
 Copyright (c) 2013-2014, Stephen Gold
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 * Stephen Gold's name may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL STEPHEN GOLD BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.stp.games.jme.util;
import com.jme3.app.state.AbstractAppState;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple app state to track the time of day in a game.
 *
 * @author Stephen Gold <sgold@sonic.net>
 */
public class TimeOfDay extends AbstractAppState {
    // *************************************************************************
    // constants

    /**
     * duration of a full day (in hours)
     */
    final public static int hoursPerDay = 24;
    /**
     * number of minutes in an hour
     */
    final public static int minutesPerHour = 60;
    /**
     * number of seconds in a minute - declared early due to a dependency
     */
    final public static int secondsPerMinute = 60;
    /**
     * number of seconds in an hour - declared early due to a dependency
     */
    final public static int secondsPerHour = secondsPerMinute * minutesPerHour;
    /**
     * number of seconds in a day
     */
    final public static int secondsPerDay = secondsPerHour * hoursPerDay;
    /**
     * message logger for this class
     */
    final private static Logger logger = Logger.getLogger(TimeOfDay.class.getName());
    // *************************************************************************
    // fields
    /**
     * simulated time of day (seconds since midnight, &lt;86400, &ge;0)
     * <p>
     * The simulated time is stored in double precision because it is
     * incremented by a small amount for each frame.
     */
    private double timeOfDay = 0.0;
    /**
     * simulation rate relative to real time
     */
    private float rate = 60f;
	// total elapsed simulated time in seconds
	private volatile double time = 0.0;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a new simulation clock, specifying the start time.
     *
     * @param startHour hours since midnight (&lt;24, &ge;0)
     */
    public TimeOfDay(double startTime) {
		this.time = startTime;
    }
    // *************************************************************************
    // new methods exposed
	// Gets the total amount of simulated time in seconds
	public double getTime() {
		return time;
	}
	// Returns the total amount of simulated time rounded to the nearest second
	public long getNearestSecond() {
		return Math.round(time);
	}
	// Initializes the simulated time value
	public synchronized void setTime(double value) {
		this.time = value;
	}
	// Return the number of days that have been simulated
	public int getDay() {
		return (int)Math.floor(time / secondsPerDay);
	}

    /**
     * Read the simulated time of day.
     *
     * @return hours since midnight (&lt;24, &ge;0)
     */
    public float getHour() {
        float result = (float) timeOfDay / secondsPerHour;
        if (result == hoursPerDay) {
            result = 0f;
        }

        assert result >= 0f : result;
        assert result <= hoursPerDay : result;
        return result;
    }

    /**
     * Read the simulated time of day.
     *
     * @return seconds since midnight (&lt;86400, &ge;0)
     */
    public int getSecond() {
        int result = (int) Math.round(timeOfDay);
        if (result == secondsPerDay) {
            result = 0;
        }

        assert result >= 0 : result;
        assert result < secondsPerDay : result;
        return result;
    }

    /**
     * Write the simulation rate.
     *
     * @param newRate simulation rate relative to real time
     */
    public void setRate(float newRate) {
        rate = newRate;
    }
    // *************************************************************************
    // Object methods

    /**
     * Format the time of day as text.
     */
    @Override
    public String toString() {
        int second = getSecond();
        int ss = second % secondsPerMinute;
        int minute = second / secondsPerMinute;
        int mm = minute % minutesPerHour;
        int hh = minute / minutesPerHour;
		int d = getDay();
        String result = String.format("%d %02d:%02d:%02d", d, hh, mm, ss);
        return result;
    }
    // *************************************************************************
    // SimpleAppState methods

    /**
     * Callback to update the time of day.
     *
     * @param interval elapsed since the previous update (in seconds, &ge;0)
     */
    @Override
    public void update(float interval) {
        super.update(interval);

        double simulatedSeconds = rate * interval;
		// Increment the simulated time
        setTime(time + simulatedSeconds);
		// Calculate the current time of day
        timeOfDay = time % secondsPerDay;
    }
}