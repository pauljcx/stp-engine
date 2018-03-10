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
// Internal Dependencies
import com.stp.games.jme.controls.GameControl;
import com.stp.games.jme.controls.TreeControl;
import com.stp.games.jme.controls.CreatureControl;
import com.stp.games.jme.controls.ContainerControl;
import com.stp.games.jme.terrain.World;
import com.stp.games.jme.terrain.ChunkControl;

/**
 * The GameAction class...define purpose here.
 *
 */
public class GameAction extends Action {
	// Enumeration of action types a player can perform
	public enum Type {
		MOVE("move"),
		STAB("stab"),
		SLASH("slash"),
		CHOP("chop"),
		PICK("pick"),
		BASH("bash"),
		WHIP("whip"),
		LASSO("lasso"),
		SPLASH("splash"),
		ILLUMINATE("illuminate"),
		IGNITE("ignite"),
		ACTIVATE("activate"),
		HARVEST("harvest"),
		THROW("throw"),
		BLOCK("block"),
		PUNCH("punch"),
		KICK("kick"),
		BUILD("build"),
		PATROL("patrol"),
		GRAZE("graze"),
		PICKUP("grab"),
		HUNT("hunt"),
		SLEEP("sleep"),
		EAT("eat"),
		DRINK("drink"),
		NONE("none");
		private String name;
		private Type(String name)
		{ this.name = name; }
		public byte index() { return (byte)ordinal(); }
		public String value() { return name; }
	}
	// Static inner class to store action parameter references
	public static class Param {
		public GameControl source;
		public GameControl target;
		public Type type;
		public int quantity;
		public String value;
		public Param(GameControl source, GameControl target, Type type, int quantity, String value) {
			this.source = source;
			this.target = target;
			this.type = type;
			this.quantity = quantity;
			this.value = value;
		}
	}
	
	/* INSTANCE VARIABLES */
	protected CreatureControl creature;
	protected GameControl target;
	protected World world;
	protected TaskAI ai;
	protected ActionTask task;
	protected Type type;
	
	/**
	 * TBD Description here.
	 *
	 * @param creature a reference to the creature that this action being performed by.
	 * @param world a reference to the world this action is being performed in.
	 * @param ai the underlying task manager responsible for managing all actions.
	 */
	public GameAction(CreatureControl creature, World world, TaskAI ai) {
		super (0, -1f);
		this.creature = creature;
		this.world = world;
		this.ai = ai;
		reset();
	}
	/**
	 * Entry point for initiating a new task, if the task isn't already complete it will start executing
	 * @param task, a reference to the ActionTask is to be executed
	 */
	public void setTask(ActionTask task) {
		this.task = task;
		if (!task.isComplete()) {
			start();
		}
	}
	// Pauses the execution of the current task / action
	public void pause() {
		this.enabled = false;
	}
	// Used to manually start executing the current task if there is one, this will resume the task if it was paused or stopped
	@Override
	public void start() {
		if (task != null) {
			super.start();
		}
	}
	// Manually stop the currently executing task, this resets the action parameters and should be used only when a task is completed
	@Override
	public boolean stop() {
		// Stop all player animations when the action has stopped
		creature.processMovement("idle", true);
		// TODO: player.setAnimation(null);
		return super.stop();
	}
	// // Called once per frame to execute the current step of the active task
	@Override
	protected void calculate(float tpf) {
		// If the task is not complete do the current step
		if (!task.isComplete()) {
			int result = doStep(task.getCurrentStep(), tpf);
			if (result > 0) {
				// Once the step is done increment to the next step, if there are no more steps then stop
				if (task.incrementStep()) {
					task.clear();
					stop();
				}
			// Stop the action if the step indeicated to do so
			} else if (result < 0) {
				stop();
			}
		// Stop the action if the task is complete
		} else {
			stop();
		}
	}
	// Convenience method to look up the Type for a particular input String
	public static Type getType(String actionType) {
		for (Type t : Type.values()) {
			if (t.value().equals(actionType)) {
				return t;
			}
		}
		return Type.NONE;
	}
	/**
	 * Runs each step by calling the designated handle methods
	 * @param params, a reference to the Param values stored for the task
	 * @param tpf, a float indicating the elapse time in seconds per frame
	 * @return a int indicating whether the step is still in progress 0, completed 1, or cancelled -1
	 */
	protected int doStep(Param params, float tpf) {
		return ai.handleTask(task, creature, params, tpf);
	}
}