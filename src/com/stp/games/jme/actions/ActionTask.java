/* MIT License
 *
 * Copyright (c) 2018 Paul Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.stp.games.jme.actions;
// JME3 Dependencies
import com.jme3.cinematic.MotionPath;
// Java Dependencies
import java.util.ArrayList;
import java.util.concurrent.Future;
// Internal Dependencies
import com.stp.games.jme.controls.GameControl;

/**
 * The ActionTask class, stores all the information about the actions required to complete a given task. To run the task
 * 	assign it the the GameAction that is attached to a Creature. The step keeps track of which action in the actions list
 *		is currently being processed. The count and yield are used to help calculate how long an action takes. Use the looping
 *		parameter to have the task run in a continuous loop cycling through the list of actions repeatedly.
 *
 */
public class ActionTask {
	/* INSTANCE VARIABLES */
	protected final ArrayList<GameAction.Param> actions = new ArrayList<GameAction.Param>(5);
	protected int step;
	protected int count;
	protected float yield;
	protected boolean looping;
	
	public ActionTask() {
		this.step = 0;
		this.count = 0;
		this.yield = 0;
		this.looping = false;
	}
	// Add another set of action parameters to be completed as part of this task
	public void queueAction(GameControl source, GameControl target, GameAction.Type type, int quantity, String value) {
		actions.add(new GameAction.Param(source, target, type, quantity, value));
	}
	// Clear all parameters from the action list
	public void clear() {
		actions.clear();
		step = 0;
		count = 0;
		yield = 0;
		looping = false;
	}
	public void setLooping(boolean looping) {
		this.looping = looping;
	}
	// A boolean check to see if the task is complete
	public boolean isComplete() {
		return step >= actions.size();
	}
	// Increments the step and returns if the task is now complete
	public boolean incrementStep() {
		step++;
		if (looping) {
			if (step >= actions.size()) {
				step = 0;
			}
		}
		return isComplete();
	}
	// Returns the current step parameters if there are any
	public GameAction.Param getCurrentStep() {
		if (!isComplete()) {
			return actions.get(step);
		}
		return null;
	}
	// Increments the step and returns the next step parameters if there is one
	public GameAction.Param getNextStep() {
		if (!incrementStep()) {
			return actions.get(step);
		} 
		return null;
	}
	// Add to the count
	public void addCount(int amount) {
		count = count + amount;
	}
	// Sets the count to a specific value
	public void setCount(int count) {
		this.count = count;
	}
	// Retrieve the current count value
	public int getCount() {
		return count;
	}
	// Add to the yield
	public void addYield(float amount) {
		yield = yield + amount;
	}
	// Set the yield to a specific value
	public void setYield(float yield) {
		this.yield = yield;
	}
	// Retrieves the current yield value
	public float getYield() {
		return yield;
	}
	
}