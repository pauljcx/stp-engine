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
import com.jme3.scene.Spatial;
import com.jme3.scene.Node;
import com.jme3.math.Vector3f;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
// Internal Dependencies
import com.stp.games.jme.terrain.Volume;
import com.stp.games.jme.terrain.TileVolume;
import com.stp.games.jme.controls.GameControl;

public class TileMoveAction extends CharacterAction
{
	public static final int Z_POS = 0;
	public static final int Z_NEG = 1;
	public static final int X_POS = 2;
	public static final int X_NEG = 3;
	protected Vector3f target;
	protected Vector3f temp;
	protected Vector3f lastDirection;
	protected boolean targetSet;
	protected boolean targetAquired;
	protected int targetAxis;
	
	public TileMoveAction() {
		super();
		this.target = new Vector3f(0,0,0);
		this.temp = new Vector3f(0,0,0);
		this.lastDirection = new Vector3f(0,0,0);
		this.speed = 1.8f;
		this.targetSet = false;
		this.targetAquired = true;
	}
	protected boolean isPassable(float x, float z) {
		/*switch (getObjectTypeAt(x, z)) {
			case STRUCTURE: return false;
			case CONTAINER: return false;
			case PLANT: return false;
			case STONE: return false;
			default: return true;
		}*/
		return true;
	}
	protected GameControl.ObjectType getObjectTypeAt(float x, float z) {
		return GameControl.GENERIC_TYPE; //GameControl.Type.values()[(int)world.getVolume().getValue(x, TileVolume.TYPE, z)];
	}
	public float getGlobalX() {
		return getWorldTranslation().getX();
	}
	public float getGlobalZ() {
		return getWorldTranslation().getZ();
	}
	@Override
	protected Vector3f updateDirection() {
		if (Float.isNaN(targetCoords.x)) {
			direction.set(panning, 0, moving);
			// Check if the last movement was a left / right motion
			if (lastAxis == X_AXIS) {
				if (panning != 0) {
					direction.setZ(0);
				}
			} else {
				if (moving != 0) {
					direction.setX(0);
				}
			}
		} else {
			direction.set(targetCoords);
			direction.subtractLocal(getLocalTranslation());
			if (Math.abs(direction.x) > Math.abs(direction.z)) {
				direction.set((direction.x < 0) ? -1 : 1, 0, 0);
			} else {
				direction.set(0, 0, (direction.z < 0) ? -1 : 1);
			}
		}
		return direction;
	}
	@Override
	protected void calculate(float tpf) {
		if (!targetSet) {
			applyRotation(updateDirection());
			roundDirection(direction);
			//System.out.println("Direction: " + direction + " | " + creature + " | " + spatial.getClass() + " | " + speed);
			setTarget(direction);
		}
		if (!targetAquired) {
			float distance = speed*tpf;
			spatial.move(direction.x * distance, 0, direction.z * distance);
			Vector3f current = getLocalTranslation();
			switch (targetAxis) {
				case X_NEG:
					if (current.x <= target.x) {
						targetAquired = true;
						//setLocation(target.x, current.y, current.z);
					}
					break;
				case X_POS:
					if (current.x >= target.x) {
						targetAquired = true;
						//setLocation(target.x, current.y, current.z);
					}
					break;
				case Z_NEG:
					if (current.z <= target.z) {
						targetAquired = true;
						//setLocation(current.x, current.y, target.z);
					}
					break;
				case Z_POS:
					if (current.z >= target.z) {
						targetAquired = true;
						//setLocation(current.x, current.y, target.z);
					}
					break;
			}
		}
		if (targetAquired) {
			targetSet = false;
			//System.out.println("Current: " + getLocalTranslation());
			if (!isMoving()) {
				stop();
			}
		}
	}
	// Internal method to set the target given the specified heading
	private Vector3f setTarget(Vector3f heading) {
		target.zero();
		Vector3f current = getLocalTranslation();
		Vector3f halfStep = heading.mult(0.5f);
		if (heading.x < 0) {
			halfStep.x = current.x < 0 ? halfStep.x : -halfStep.x;
			target.setX((int)(current.x - 0.5f) + halfStep.x);
			targetAxis = X_NEG;
		} else if (heading.x > 0) {
			halfStep.x = current.x > 0 ? halfStep.x : -halfStep.x;
			target.setX((int)(current.x + 0.5f) + halfStep.x);
			targetAxis = X_POS;
		} else if (heading.z < 0) {
			halfStep.z = current.z < 0 ? halfStep.z : -halfStep.z;
			target.setZ((int)(current.z - 0.5f) + halfStep.z);
			targetAxis = Z_NEG;
		} else if (heading.z > 0) {
			halfStep.z = current.z > 0 ? halfStep.z : -halfStep.z;
			target.setZ((int)(current.z + 0.5f) + halfStep.z);
			targetAxis = Z_POS;
		}
		targetSet = !isZero(target);
		targetAquired = !targetSet;
		//System.out.println("Target Set: " + target);
		/*if (targetSet && !isPassable(target.x, target.z)) {
			targetSet = false;
			target.zero();
			System.out.println("Barrier Reached: ");
		}*/
		if (targetSet) {
			setCharRotation();
		}
		return target;
	}
	private void setCharRotation() {
		Spatial body = creature.getSpatial();
		if (body == null) {
			body = spatial;
		}
		switch (targetAxis) {
			case X_NEG:
				body.setLocalRotation(new Quaternion().fromAngles(0, -FastMath.PI/2, 0).multLocal(quaternion));
				break;
			case X_POS:
				body.setLocalRotation(new Quaternion().fromAngles(0, FastMath.PI/2, 0).multLocal(quaternion));
				break;
			case Z_NEG:
				body.setLocalRotation(new Quaternion().fromAngles(0, FastMath.PI, 0).multLocal(quaternion));
				break;
			case Z_POS:
				body.setLocalRotation(new Quaternion().fromAngles(0, 0, 0).multLocal(quaternion));
				break;
		}
		//System.out.println("CharRotation: " + spatial.getName() + " | " + spatial.getLocalRotation());
	}
	// Checks to see if a vector has all zero values
	private boolean isZero(Vector3f in) {
		return in.x == 0 && in.y == 0 && in.z == 0;
	}
	// Rounds direction perameters to lock them at -1, 0, or 1
	private Vector3f roundDirection(Vector3f in) {
		if (in.x > 0.5f) {
			in.x = 1f;
		} else if (in.x < -0.5f) {
			in.x = -1f;
		} else {
			in.x = 0;
		}
		if (in.z > 0.5f) {
			in.z = 1f;
		} else if (in.z < -0.5f) {
			in.z = -1f;
		} else {
			in.z = 0;
		}
		return in;
	}
}