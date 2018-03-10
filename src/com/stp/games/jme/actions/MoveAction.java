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
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.math.Vector3f;
import com.jme3.math.Quaternion;
import com.jme3.scene.Spatial;
import com.jme3.cinematic.MotionPath;
// Java Dependencies
import java.util.Iterator;
import java.util.concurrent.Future;
// Internal Dependencies
import com.stp.games.jme.terrain.Volume;

public class MoveAction extends Action {
	public static final int X_AXIS = 0;
	public static final int Z_AXIS = 1;
	
	public enum PhysicsType {
		NoPhysics,
		BasicPhysics,
		CharacterPhysics,
		VehiclePhysics;
	}
	
	protected final MotionPath path = new MotionPath();;
	protected final Vector3f targetCoords = new Vector3f(Float.NaN, Float.NaN, Float.NaN);;
	protected final Vector3f direction = new Vector3f();
	protected final Quaternion quaternion = new Quaternion();
	protected volatile int[] states = new int[4];
	
	protected PhysicsControl physicsControl;
	protected PhysicsType physicsType;
	protected float speed;
	protected float threshold;
	protected int lastAxis;
	protected int panning;
	protected int moving;
	protected int pathIndex;
	protected double lastTime;
	
	protected volatile boolean stop;
	
	public MoveAction() {
		super (0, -1);
		this.speed = 10.0f;
		this.threshold = 1f;
		this.enabled = false;
		this.stop = false;
		this.pathIndex = 0;
	}
	@Override
	public void setSpatial(Spatial spatial) {
		super.setSpatial(spatial);
		if (spatial != null) {
			// Detect the physics control for applying movement
			physicsControl = spatial.getControl(PhysicsControl.class);
			if (physicsControl == null) {
				physicsType = PhysicsType.NoPhysics;
			} else if (physicsControl instanceof RigidBodyControl) {
				physicsType = PhysicsType.BasicPhysics;
			} else  if (physicsControl instanceof BetterCharacterControl) {
				physicsType = PhysicsType.CharacterPhysics;
			} else  if (physicsControl instanceof BetterCharacterControl) {
				physicsType = PhysicsType.VehiclePhysics;
			}
		} else {
			physicsType = PhysicsType.NoPhysics;
		}
	}
	public synchronized void setPath(MotionPath newPath, double time) {
		if (time >= lastTime) {
			lastTime = time;
			clearPath();
			for (int w = newPath.getNbWayPoints()-1; w >= 0; w--) {
				addWayPoint(newPath.getWayPoint(w));
			}
			startPath(false);
		}
	}
	public void addWayPoint(Vector3f waypoint) {
		path.addWayPoint(waypoint);
	}
	public void startPath(boolean cycle) {
		if (path.getNbWayPoints() > 0) {
			path.setCycle(cycle);
			this.pathIndex = 0;
			setTargetCoords(path.getWayPoint(pathIndex), threshold);
		}
	}
	public void resumePath() {
		if (path.getNbWayPoints() > 0) {
			setTargetCoords(path.getWayPoint(pathIndex), threshold);
		}
	}
	public void clearPath() {
		targetCoords.set(Float.NaN, Float.NaN, Float.NaN);
		this.pathIndex = 0;
		path.clearWayPoints();
	}
	public boolean setTargetCoords(Vector3f in, float threshold) {
		this.threshold = threshold;
		this.targetCoords.set(in);
		if (!Float.isNaN(targetCoords.x)) {
			if (isAtTarget(in)) {
				return true;
			}
			start();
		}
		return false;
	}
	public boolean setCommandStates(int[] motionStates) {
		if (motionStates.length >= 4) {
			boolean changed = false;
			if (states[0] != motionStates[0] || states[1] != motionStates[1]) {
				lastAxis = motionStates[0] + motionStates[1] != 0 ? Z_AXIS : lastAxis;
				changed = true;
			}
			if (states[2] != motionStates[2] || states[3] != motionStates[3]) {
				lastAxis = motionStates[2] + motionStates[3] != 0 ? X_AXIS : lastAxis;
				changed = true;
			}
			if (changed) {
				states[0] = motionStates[0];
				states[1] = motionStates[1];
				states[2] = motionStates[2];
				states[3] = motionStates[3];
				
				// Update moving and panning values
				this.moving = states[0] + states[1];
				this.panning = states[2] + states[3];
				if (checkStates()) {
					targetCoords.set(Float.NaN, Float.NaN, Float.NaN);
					start();
					return true;
				}
			}
		}
		return checkStates();
	}
	public void axisChanged(int axis) {
	}
	public boolean setCommandState(String command, boolean state) {
		if (command.equals("MoveLeft")) {
			moveLeft(state);
		} else if (command.equals("MoveRight"))	{
			moveRight(state);
		} else if (command.equals("MoveForward")) {
			moveForward(state);
		} else if (command.equals("MoveBack"))	{
			moveBack(state);
		} else if (command.equals("jump"))	{
			jump();
		}
		return isMoving();
	}
	public void moveForward(boolean state) {
		states[0] = state ? 1 : 0;
		lastAxis = state ? Z_AXIS : lastAxis;
		this.moving = states[0] + states[1];
		start();
	}
	public void moveBack(boolean state) {
		states[1] = state ? -1 : 0;
		lastAxis = state ? Z_AXIS : lastAxis;
		this.moving = states[0] + states[1];
		start();
	}
	public void moveLeft(boolean state) {
		states[2] = state ? 1 : 0;
		lastAxis = state ? X_AXIS : lastAxis;
		this.panning = states[2] + states[3];
		start();
	}
	public void moveRight(boolean state) {
		states[3] = state ? -1 : 0;
		lastAxis = state ? X_AXIS : lastAxis;
		this.panning = states[2] + states[3];
		start();
	}
	public void jump() {
	}
	protected Vector3f applyRotation(Vector3f in) {
		return quaternion.mult(in);
	}
	public void updateRotation(Quaternion in) {
		quaternion.set(in);
	}
	public void updateRotation(float x, float y, float z, float w) {
		quaternion.set(x, y, z, w);
	}
	public void updateSpatialDirection() {
		if (spatial != null && !Float.isNaN(targetCoords.x)) {
			spatial.lookAt(targetCoords, Vector3f.UNIT_Y);
		}
	}
	public void setRotation(float rx, float ry, float rz) {
		if (spatial != null) {
			spatial.setLocalRotation(spatial.getLocalRotation().fromAngles(rx, ry, rz));
		}
	}
	public void setLocation(float x, float y, float z) {
		if (spatial != null) {
			spatial.setLocalTranslation(x, y, z);
		}
	}
	public void setTransform(float x, float y, float z, float sx, float sy, float sz, float qx, float qy, float qz, float qw) {
		if (spatial != null) {
			spatial.setLocalTranslation(x, y, z);
			spatial.setLocalScale(sx, sy, sz);
			spatial.setLocalRotation(spatial.getLocalRotation().set(qx, qy, qz, qw));
		}
	}
	// Gets the 3D coordintes of the underlying spatial
	public Vector3f getWorldTranslation() {
		return (spatial != null) ? spatial.getWorldTranslation() : Vector3f.ZERO;
	}
	// Gets the 3D coordintes of the underlying spatial
	public Vector3f getLocalTranslation() {
		return (spatial != null) ? spatial.getLocalTranslation() : Vector3f.ZERO;
	}
	public boolean isAtPathEnd() {
		if (path != null && path.getNbWayPoints() > 0) {
			return isAtTarget(path.getWayPoint(path.getNbWayPoints()-1));
		}
		return false;
	}
	public boolean isAtTarget(Vector3f in) {
		return in.distance(getLocalTranslation()) < threshold;
	}
	protected boolean checkStates() {
		return states[0] != 0 || states[1] != 0 || states[2] != 0 || states[3] != 0;
	}	
	// Checks to see if any of the states has a value not equal to zero indicating movement is occuring
	protected boolean isMoving() {
		// First check to see if their are target coordinates to move to
		if (!Float.isNaN(targetCoords.x)) {
			// Next check to see if the we are already at the the target coordinates
			if (isAtTarget(targetCoords)) {
				if (path != null) {
					pathIndex++;
					if (path.getNbWayPoints() > pathIndex) {
						setTargetCoords(path.getWayPoint(pathIndex), threshold);
						return true;
					} else if (path.isCycle()) {
						pathIndex = 0;
						setTargetCoords(path.getWayPoint(pathIndex), threshold);
						return true;
					}
				}
				targetCoords.set(Float.NaN, Float.NaN, Float.NaN);
				return false;
			}
			return true;
		}
		return checkStates();
	}
	protected Vector3f updateDirection() {
		if (Float.isNaN(targetCoords.x)) {
			direction.set(panning, 0, moving);
			direction.normalizeLocal();
			applyRotation(direction);
			
		} else {
			direction.set(targetCoords);
			direction.subtractLocal(getLocalTranslation());
			direction.normalizeLocal();
			updateSpatialDirection();
		}
		return direction;
	}
	// Called internally by the calculate method to move the underlying spatial
	protected void setMovement(float tpf) {
		// Recalculate the directon of teh movement
		updateDirection();
		// Set the speed of the movement
		direction.multLocal(speed*tpf);
		switch (physicsType) {
			case NoPhysics: 
				spatial.move(direction);
				return;
			case BasicPhysics:
				((RigidBodyControl)physicsControl).applyCentralForce(direction);
				return;
			case CharacterPhysics:
				((BetterCharacterControl)physicsControl).setWalkDirection(direction);
				return;
			case VehiclePhysics:
				return;
			default: return;
		}
	}
	// This method is called between frames to update position of the underlying spatial
	@Override
	protected void calculate(float tpf) {
		if (isProcessing()) {
			setMovement(tpf);
			if (!isMoving()) {
				stop();
			}
		}
	}
	@Override
	public void start() {
		super.start();
		System.out.println("Started Moving");
	}
	@Override
	public boolean stop() {
		states[0] = 0;
		states[1] = 0;
		states[2] = 0;
		states[3] = 0;
		panning = 0;
		moving = 0;
		targetCoords.set(Float.NaN, Float.NaN, Float.NaN);
		switch (physicsType) {
			case BasicPhysics:
				((RigidBodyControl)physicsControl).clearForces();
				break;
			case CharacterPhysics:
				((BetterCharacterControl)physicsControl).setWalkDirection(Vector3f.ZERO);
				break;
			default: break;
		}
		System.out.println("Stopped Moving");
		return super.stop();
	}
}