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
package com.stp.games.jme.client;
// JME3 Dependencies
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.util.TempVars;
import com.jme3.scene.Spatial;
import com.jme3.scene.Node;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
// Internal Dependencies
import com.stp.games.jme.util.MathUtils;
import com.stp.games.jme.controls.GameControl;

public class ClientCamera extends AbstractControl implements AnalogListener {
    public final static String CamToggleRotate = "CamToggleRotate";
	
	public final static int FORWARD = 0;
	public final static int BACKWARD = 1;
	public final static int LEFT = 2;
	public final static int RIGHT = 3;
	
	protected Camera camera;
	protected Spatial target = null;

	protected float moveSpeed = 35f;
    protected float chasingSensitivity = 15f;
    protected float trailingSensitivity = 0.5f;
	protected float trailingLerpFactor = 0;
	
	protected float distance = 12.0f;
	protected float minDistance = 1.0f;
    protected float maxDistance = 64.0f;
	protected float offsetDistance = 0.002f;
	protected float zoomVerticalRotation = 0.01f;
	protected float zoomSpeed = 2f;
	protected float zoomSensitivity = 2f;
	protected float distanceLerpFactor = 0;
	
    protected float rotation = -FastMath.PI / 2;
	protected float vRotation = FastMath.PI / 8;
	protected float minVerticalRotation = FastMath.PI / 24;//-FastMath.PI / 5;
    protected float maxVerticalRotation = FastMath.PI / 3.5f;
	protected float rotationSpeed = 1.8f;
	protected float rotationSensitivity = 5f;
	protected float rotationLerpFactor = 0;
	protected float vRotationLerpFactor = 0;
    protected float trailingRotationInertia = 0.05f;

	protected boolean invertYaxis = true;
    protected boolean invertXaxis = false;
	protected boolean smoothMotion = false;
    protected boolean trailingEnabled = true;
    protected boolean enabled = true;
	protected boolean veryCloseRotation = false;
	protected boolean canRotate = true;
	protected boolean canZoom = true;
	protected boolean allowMouseRotation = false;
	
    protected boolean rotating = false;
    protected boolean vRotating = false;
	protected boolean zooming = false;
    protected boolean trailing = false;
    protected boolean chasing = false;
	protected boolean targetMoves = false;
	
    protected float targetRotation = rotation;
	protected float targetVRotation = vRotation;
	protected float targetDistance = distance;
	protected float relativeSpeed;
	protected float previousTargetRotation;

	protected final Vector3f position = new Vector3f();
    protected final Vector3f previousPosition = new Vector3f();
    protected final Vector3f targetDirection = new Vector3f();
    protected final Vector3f targetLocation = new Vector3f();
    protected final Vector3f lookAtOffset = new Vector3f();
	protected final Vector3f initialUpVec = new Vector3f();
    protected final Vector3f temp = new Vector3f();
	
	protected int[] motionStates = new int[4];
	protected boolean moving;

	public ClientCamera() {
	}
	public void setLookAtOffset(float x, float y, float z) {
		this.lookAtOffset.set(x, y, z);
	}
	public void initialize(float fovY, float near, float far) {
		camera.setFrustumPerspective(fovY, (float)camera.getWidth()/camera.getHeight(), near, far);
		position.set(0f, 1f, 12f);
		camera.setLocation(position);
		camera.lookAt(Vector3f.ZERO, initialUpVec);
		this.distance = 12.0f;
		this.rotation = -FastMath.PI / 2;
		this.vRotation = FastMath.PI / 8;
		this.targetDistance = distance;
		this.targetRotation = rotation;
		this.targetVRotation = vRotation;
		this.lookAtOffset.set(0, 0, 0);
	}
	public Camera getCamera() {
		return camera;
	}
	public void setCamera(Camera camera) {
		this.camera = camera;
		this.initialUpVec.set(camera.getUp());
	}
	public void attachTo(GameControl obj) {
		if (obj != null) {
			if (spatial != null && spatial != obj.getSpatial()) {
				spatial.removeControl(this);
			}
			obj.addControl(this);
		} else if (spatial != null) {
			spatial.removeControl(this);
		}
	}
	public boolean hasSpatial() {
		return (spatial != null);
	}
	@Override
	public void setSpatial(Spatial spatial) {
		super.setSpatial(spatial);
		this.target = spatial;
		enabled = (target != null);
		computePosition();
	}
	@Override
	protected void controlUpdate(float tpf) {
		if (spatial != null && camera != null) {
			updateCamera(tpf);
		}
	}
	@Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
	}
	public boolean isMoving() {
		return moving;
	}
	public int getMoveValue() {
		return motionStates[0] + motionStates[1];
	}
	public int getPanValue() {
		return motionStates[2] + motionStates[3];
	}
	public boolean setMotionState(int index, boolean state) {
		this.motionStates[index] = state ? 1 : 0;
		updateMovingState();
		return moving;
	}
	public void resetMotionStates() {
		this.motionStates[0] = 0;
		this.motionStates[1] = 0;
		this.motionStates[2] = 0;
		this.motionStates[3] = 0;
		this.moving = false;
	}
	private void updateMovingState() {
		this.moving = (motionStates[0] != 0) || (motionStates[1] != 0) || (motionStates[2] != 0) || (motionStates[3] != 0);
	}
	public void setZoomEnabled(boolean canZoom) {
		this.canZoom = canZoom;
	}
	public void setRotationEnabled(boolean canRotate) {
		this.canRotate = canRotate;
	}
	public int getWidth() {
		return camera.getWidth();
	}
	public int getHeight() {
		return camera.getHeight();
	}
	public Vector3f getLocation() {
		return camera.getLocation();
	}
	public Vector3f getScreenCoordinates(Vector3f worldPosition) {
		return camera.getScreenCoordinates(worldPosition);
	}
	public Vector3f getScreenCoordinates(Vector3f worldPosition, Vector3f store) {
		return camera.getScreenCoordinates(worldPosition, store);
	}
	public Vector3f getWorldCoordinates(Vector2f screenPosition, float projectionZPos) {
		return camera.getWorldCoordinates(screenPosition, projectionZPos);
	}
	public Vector3f getWorldCoordinates(Vector2f screenPosition, float projectionZPos, Vector3f store) {
		return camera.getWorldCoordinates(screenPosition, projectionZPos, store);
	}
	//rotate the camera around the target on the horizontal plane
	public void rotateCamera(float value) {
		if (!canRotate || !enabled) {
			return;
        }
        rotating = true;
		targetRotation += value * rotationSpeed;
    }
	public void setZoomConstraints(float min, float max) {
		this.minDistance = min;
		this.maxDistance = max;
	}
	public void setZoomDistance(float value) {
		if (!enabled) {
            return;
        }
		zooming = true;
        targetDistance = value;
        if (targetDistance > maxDistance) {
            targetDistance = maxDistance;
        }
        if (targetDistance < minDistance) {
            targetDistance = minDistance;
        }
	}
	public float getZoomDistance() {
		return targetDistance;
	}
	//move the camera toward or away the target
	public void zoomCamera(float value) {
		if (!enabled || !canZoom) {
            return;
        }
		zooming = true;
		targetDistance += value * zoomSensitivity;
		if (targetDistance > maxDistance) {
			targetDistance = maxDistance;
		} else if (targetDistance < minDistance) {
			targetDistance = minDistance;
		} else {
			vRotateCamera(value*zoomVerticalRotation);
		}
	}
	public void setVerticalConstraints(float min, float max) {
		this.minVerticalRotation = min;
		this.maxVerticalRotation = max;
	}
	public float getVerticalRotation() {
		return targetVRotation;
	}
	public void setVerticalRotation(float value) {
		vRotating = true;
        float lastGoodRot = targetVRotation;
        targetVRotation = value;
		if (targetVRotation > maxVerticalRotation) {
            targetVRotation = maxVerticalRotation;
        }
        if (veryCloseRotation) {
            if ((targetVRotation < minVerticalRotation) && (targetDistance > (minDistance + 1.0f))) {
                targetVRotation = minVerticalRotation;
            } else if (targetVRotation < -FastMath.DEG_TO_RAD * 90) {
                targetVRotation = lastGoodRot;
            }
        } else {
            if ((targetVRotation < minVerticalRotation)) {
                targetVRotation = minVerticalRotation;
            }
        }
	}
	 //rotate the camera around the target on the vertical plane
    public void vRotateCamera(float value) {
        if (!canRotate || !enabled) {
            return;
        }
        vRotating = true;
        float lastGoodRot = targetVRotation;
        targetVRotation += value * rotationSpeed;
        if (targetVRotation > maxVerticalRotation) {
            targetVRotation = lastGoodRot;
        }
        if (veryCloseRotation) {
            if ((targetVRotation < minVerticalRotation) && (targetDistance > (minDistance + 1.0f))) {
                targetVRotation = minVerticalRotation;
            } else if (targetVRotation < -FastMath.DEG_TO_RAD * 90) {
                targetVRotation = lastGoodRot;
            }
        } else {
            if ((targetVRotation < minVerticalRotation)) {
                targetVRotation = lastGoodRot;
            }
        }
    }
	public Vector3f getTargetLocation() {
		return (target != null) ? target.getWorldTranslation() : Vector3f.ZERO;
	}
	protected void computePosition() {
		if (target != null) {
			float hDistance = (distance) * FastMath.sin((FastMath.PI / 2) - vRotation);
			position.set(hDistance * FastMath.cos(rotation), (distance) * FastMath.sin(vRotation), hDistance * FastMath.sin(rotation));
			position.addLocal(target.getWorldTranslation());
		}
    }
	/**
     * Updates the camera, should only be called internally
     */
    protected void updateCamera(float tpf) {
        if (enabled) {
            targetLocation.set(target.getWorldTranslation()).addLocal(lookAtOffset);
            if (smoothMotion) {

                //computation of target direction
                targetDirection.set(targetLocation).subtractLocal(previousPosition);
                float dist = targetDirection.length();

                //Low pass filtering on the target postition to avoid shaking when physics are enabled.
                if (offsetDistance < dist) {
                    //target moves, start chasing.
                    chasing = true;
                    //target moves, start trailing if it has to.
                    if (trailingEnabled) {
                        trailing = true;
                    }
                    //target moves...
                    targetMoves = true;
                } else {
                    //if target was moving, we compute a slight offset in rotation to avoid a rought stop of the cam
                    //We do not if the player is rotationg the cam
                    if (targetMoves && !canRotate) {
                        if (targetRotation - rotation > trailingRotationInertia) {
                            targetRotation = rotation + trailingRotationInertia;
                        } else if (targetRotation - rotation < -trailingRotationInertia) {
                            targetRotation = rotation - trailingRotationInertia;
                        }
                    }
                    //Target stops
                    targetMoves = false;
                }

                //the user is rotating the cam by dragging the mouse
                if (canRotate) {
                    //reseting the trailing lerp factor
                    trailingLerpFactor = 0;
                    //stop trailing user has the control
                    trailing = false;
                }


                if (trailingEnabled && trailing) {
                    if (targetMoves) {
                        //computation if the inverted direction of the target
                        Vector3f a = targetDirection.negate().normalizeLocal();
                        //the x unit vector
                        Vector3f b = Vector3f.UNIT_X;
                        //2d is good enough
                        a.y = 0;
                        //computation of the rotation angle between the x axis and the trail
                        if (targetDirection.z > 0) {
                            targetRotation = FastMath.TWO_PI - FastMath.acos(a.dot(b));
                        } else {
                            targetRotation = FastMath.acos(a.dot(b));
                        }
                        if (targetRotation - rotation > FastMath.PI || targetRotation - rotation < -FastMath.PI) {
                            targetRotation -= FastMath.TWO_PI;
                        }

                        //if there is an important change in the direction while trailing reset of the lerp factor to avoid jumpy movements
                        if (targetRotation != previousTargetRotation && FastMath.abs(targetRotation - previousTargetRotation) > FastMath.PI / 8) {
                            trailingLerpFactor = 0;
                        }
                        previousTargetRotation = targetRotation;
                    }
                    //computing lerp factor
                    trailingLerpFactor = Math.min(trailingLerpFactor + tpf * tpf * trailingSensitivity, 1);
                    //computing rotation by linear interpolation
                    rotation = FastMath.interpolateLinear(trailingLerpFactor, rotation, targetRotation);

                    //if the rotation is near the target rotation we're good, that's over
                    if (targetRotation + 0.01f >= rotation && targetRotation - 0.01f <= rotation) {
                        trailing = false;
                        trailingLerpFactor = 0;
                    }
                }

                //linear interpolation of the distance while chasing
                if (chasing) {
                    distance = temp.set(targetLocation).subtractLocal(camera.getLocation()).length();
                    distanceLerpFactor = Math.min(distanceLerpFactor + (tpf * tpf * chasingSensitivity * 0.05f), 1);
                    distance = FastMath.interpolateLinear(distanceLerpFactor, distance, targetDistance);
                    if (targetDistance + 0.01f >= distance && targetDistance - 0.01f <= distance) {
                        distanceLerpFactor = 0;
                        chasing = false;
                    }
                }

                //linear interpolation of the distance while zooming
                if (zooming) {
                    distanceLerpFactor = Math.min(distanceLerpFactor + (tpf * tpf * zoomSensitivity), 1);
                    distance = FastMath.interpolateLinear(distanceLerpFactor, distance, targetDistance);
                    if (targetDistance + 0.1f >= distance && targetDistance - 0.1f <= distance) {
                        zooming = false;
                        distanceLerpFactor = 0;
                    }
                }

                //linear interpolation of the rotation while rotating horizontally
                if (rotating) {
                    rotationLerpFactor = Math.min(rotationLerpFactor + tpf * tpf * rotationSensitivity, 1);
                    rotation = FastMath.interpolateLinear(rotationLerpFactor, rotation, targetRotation);
                    if (targetRotation + 0.01f >= rotation && targetRotation - 0.01f <= rotation) {
                        rotating = false;
                        rotationLerpFactor = 0;
                    }
                }

                //linear interpolation of the rotation while rotating vertically
                if (vRotating) {
                    vRotationLerpFactor = Math.min(vRotationLerpFactor + tpf * tpf * rotationSensitivity, 1);
                    vRotation = FastMath.interpolateLinear(vRotationLerpFactor, vRotation, targetVRotation);
                    if (targetVRotation + 0.01f >= vRotation && targetVRotation - 0.01f <= vRotation) {
                        vRotating = false;
                        vRotationLerpFactor = 0;
                    }
                }
                //computing the position
                computePosition();
                //setting the position at last
                camera.setLocation(position.addLocal(lookAtOffset));
            } else {
                //easy no smooth motion
                vRotation = targetVRotation;
                rotation = targetRotation;
                distance = targetDistance;
                computePosition();
                camera.setLocation(position.addLocal(lookAtOffset));
            }
            //keeping track on the previous position of the target
            previousPosition.set(targetLocation);

            //the cam looks at the target
            camera.lookAt(targetLocation, initialUpVec);
        }
    }
	public void onAnalog(String name, float value, float tpf) {
		if (name.equals("MouseMotionXNeg")) {
			if (allowMouseRotation) {
				rotateCamera(invertXaxis ? value : -value);
			}
			return;
        }
		if (name.equals("MouseMotionXPos")) {
			if (allowMouseRotation) {
				rotateCamera(invertXaxis ? -value : value);
			}
			return;
        }
		if (name.equals("MouseMotionYPos")) {
			if (allowMouseRotation) {
				vRotateCamera(invertYaxis ? -value : value);
			}
			return;
        }
		if (name.equals("MouseMotionYNeg")) {
			if (allowMouseRotation) {
				vRotateCamera(invertYaxis ? value : -value);
			}
			return;
		}
	}
	public void setAllowMouseRotation(boolean value) {
		this.allowMouseRotation = value;
	}
}