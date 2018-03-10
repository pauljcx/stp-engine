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
import com.jme3.scene.control.AbstractControl;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;

/**
 * The Action class provides basic functionality for controlling and managing queued actions. The parameters
 * allow full control over when and how frequently the action is processed as well as providing outside access
 * to check on the current status. Although the class can be used stand alone it is nothing more than a timing
 * mechanism unless it has been sub-classed. Sub-classes should override the initialize() and calculate() methods
 * and optionally override the stop() and complete() methods when needed.
 *
 */
public class Action extends AbstractControl
{
	public enum Type {
		CHOP("chop"),
		SLASH("slash"),
		THRUST("thrust"),
		THROW("throw"),
		BLOCK("block"),
		BASH("bash"),
		VIEW("view"),
		OPEN("open"),
		GATHER("gather"),
		PUNCH("punch"),
		PICK("pick");
		private String name;
		private Type(String name)
		{ this.name = name; }
		public byte index() { return (byte)ordinal(); }
		public String value() { return name; }
	}
	/* INSTANCE VARIABLES */
	protected volatile boolean interupted;
	protected volatile boolean completed;
	protected float elapse;
	protected float totalTime;
	protected float interval;
	protected float maximum;
	protected int count;
	
	// Initializes the time an execution variables, by default the action can only be executed once
	public Action() {
		this (0, 0);
	}
	public Action(float interval, float maximum) {
		this.interval = interval;
		this.maximum = maximum;
		this.elapse = 0;
		this.totalTime = 0;
		this.count = 0;
		this.interupted = false;
		this.completed = false;
	}
	// A boolean tests to see if the action is in process by evaluating the elapse time and completed flag
	public final boolean isProcessing() {
		return (elapse > 0) && !completed;
	}
	// A boolean check to see if the action has been completed
	public final boolean hasCompleted() {
		return completed;
	}
	// Sets the interrupt flag to true which will terminate the action in the next update
	public final boolean interupt() {
		this.interupted = true;
		return true;
	}
	// Triggers the control to start processing by enabling the control (only necessary when in a rendering queue)
	public void start() {
		this.enabled = true;
	}
	// Provides the same functionality of interrupt but can be overridden in subclass
	public boolean stop() {
		this.interupted = true;
		return true;
	}
	// Called when the action has completed execution subclasses should call this using super or set the flag manually when overriding
	protected void complete() {
		this.completed = true;
	}
	// Resets all time and execution values in preparation for the next call to start, should not be called during execution
	protected void reset() {
		this.elapse = 0;
		this.totalTime = 0;
		this.interupted = false;
		this.completed = false;
		this.enabled = false;
	}
	// Called during the first pass of each execution and can be overridden in subclass 
	protected void initialize() {
	}
	/**
	 * Called continually during execution, when in a rendering thread it is called once per frame in the
	 * update method, can also be called from outside in background process queues
	 *
	 * @param tpf a float that references the amount of time that has elapsed between each call
	 * @return a boolean indicating when all processing has been completed (to stop future execution)
	 */
	public final boolean process(float tpf) {
		// First pass only call the init() method prior to any calculations
		if (totalTime == 0) {
			totalTime = tpf;
			initialize();
			calculate(tpf);
		} else {
			// Increment the elapse time
			elapse+=tpf;
			// Calls the calculate method at fixed time intervals, set to zero for immediate execution
			if (elapse > interval) {
				calculate(tpf);
				elapse = 0;
			}
			// Increment the total time
			totalTime+=tpf;
		}
		// A check for triggered interruptions
		if (interupted) {
			reset();
			return true;
		// A check for maximum allowed time the action can execute
		} else if (maximum >= 0 && totalTime >= maximum) {
			complete();
			return true;
		} else {
			return false;
		}
	}
	// Called during each pass during execution and is the primary mechanism for subclasses to implement all calculations
	protected void calculate(float tpf) {
		count++;
	}
	// Necessary for abstraction, not currently utilized
	protected void controlRender(RenderManager rm, ViewPort vp) {
	}
	// Calls the internal process method once each frame during active rendering
	protected final void controlUpdate(float tpf) {
		process(tpf);
	}          
}