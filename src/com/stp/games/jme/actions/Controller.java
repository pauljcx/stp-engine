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

import com.stp.games.jme.util.MathUtils;
import com.stp.games.jme.terrain.World;
import com.stp.games.jme.controls.CreatureControl;

public class Controller extends AbstractControl implements ActionListener,AnalogListener
{
	protected final static String CamDown = "CamDown";
    protected final static String CamUp = "CamUp";
    protected final static String CamZoomIn = "CamZoomIn";
    protected final static String CamZoomOut = "CamZoomOut";
    protected final static String CamMoveLeft = "CamMoveLeft";
    protected final static String CamMoveRight = "CamMoveRight";
    protected final static String CamToggleRotate = "CamToggleRotate";
	
	public final static String MoveForward = "MoveForward";
	public final static String MoveLeft = "MoveLeft";
	public final static String MoveBack = "MoveBack";
	public final static String MoveRight = "MoveRight";
	public final static String MoveNone = "MoveNone";
	public final static String Jump = "jump";
	
	protected Camera camera;
	protected Movable receiver;
	protected Vector3f offset;
	private Quaternion quaternion;
	private Vector3f rotationVector;
	private Vector3f eye;
	private Vector3f center;
	private float boundEdgeWidth = 24f;
	private float rxSpeed;
	private float rySpeed;
	private float rzSpeed;
	protected boolean invertYaxis = false;
    protected boolean invertXaxis = false;
	private boolean firstPerson;
	private boolean directControl;
	private volatile boolean moving;
	private int[] states;
	
	protected Node imposter = new Node();
	protected Spatial target = null;
    protected float minVerticalRotation = -FastMath.PI / 32;//-FastMath.PI / 5;
    protected float maxVerticalRotation = FastMath.PI / 3.5f;
    protected float minDistance = 6.0f;
    protected float maxDistance = 64.0f;
    protected float distance = 16.0f;    
    protected float rotationSpeed = 0.8f;
	protected float moveSpeed = 30f;
	protected float zoomSpeed = 0.6f;
    protected float rotation = -FastMath.PI / 2;
    protected float trailingRotationInertia = 0.05f;
    protected float zoomSensitivity = 2f;
    protected float rotationSensitivity = 5f;
    protected float chasingSensitivity = 5f;
    protected float trailingSensitivity = 0.5f;
    protected float vRotation = FastMath.PI / 8;
    protected boolean smoothMotion = true;
    protected boolean trailingEnabled = false;
    protected float rotationLerpFactor = 0;
    protected float trailingLerpFactor = 0;
    protected boolean rotating = false;
    protected boolean vRotating = false;
    protected float targetRotation = rotation;
    protected InputManager inputManager;
    protected Vector3f initialUpVec;
    protected float targetVRotation = vRotation;
    protected float vRotationLerpFactor = 0;
    protected float targetDistance = distance;
    protected float distanceLerpFactor = 0;
	protected float relativeSpeed;
    protected boolean zooming = false;
    protected boolean trailing = false;
    protected boolean chasing = false;
    protected boolean veryCloseRotation = false;
    protected boolean canRotate;
    protected float offsetDistance = 0.002f;
    protected Vector3f prevPos;
    protected boolean targetMoves = false;
    protected boolean enabled = true;
    protected final Vector3f targetDir = new Vector3f();
    protected float previousTargetRotation;
    protected final Vector3f pos = new Vector3f();
    protected Vector3f targetLocation = new Vector3f(0, 0, 0);
    protected boolean dragToRotate = true;
    protected Vector3f lookAtOffset = new Vector3f(0, 0, 0);
    protected Vector3f temp = new Vector3f(0, 0, 0);
	protected boolean zoomin;
    protected boolean hideCursorOnRotate = false;
	protected World world;
	
	public Controller(Camera camera, Vector3f offset, World world) {
		this.camera = camera;
		this.offset = offset;
		this.world = world;
		this.rotationSpeed = 1f;
		this.distance = 1.0f;
		this.rxSpeed = 0.25f;
		this.rySpeed = 0.35f;
		this.rzSpeed = 0.25f;
		this.prevPos = new Vector3f();
		this.rotationVector = new Vector3f();
		this.initialUpVec = camera.getUp().clone();
		this.lookAtOffset.set(0, 1.8f, 0);
		this.eye = new Vector3f();
		this.center = new Vector3f();
		imposter.setLocalTranslation(272f, 0f, -72f);
		camera.setFrustumPerspective(45f, (float)camera.getWidth()/camera.getHeight(), 1f, 256f);
		pos.set(272, 5, -62);
		camera.setLocation(pos);
		camera.lookAt(new Vector3f(272f, 1f, -72f), initialUpVec);
		this.quaternion = new Quaternion(camera.getRotation());
		
		directControl = true;
		firstPerson = false;
		moving = false;
		states = new int[] { 0, 0, 0, 0 };
		canRotate = false;
		dragToRotate = true;
	}
	public Camera getCamera() {
		return camera;
	}
	public boolean setTargetCoords(Vector3f targetCoords, float threshold) {
		if (hasReceiver()) {
			return receiver.moveTo(targetCoords, threshold);
		}
		return false;
	}
	public void setMovable(Movable object) {
		if (object != null) {
			if (receiver == null) {
				object.addControl(this);
				this.receiver = object;
			} else if (!object.equals(receiver)) {
				receiver.removeControl(this);
				object.addControl(this);
				this.receiver = object;
			}
		}
	}
	@Override
	public void setSpatial(Spatial spatial) {
		super.setSpatial(spatial);
		if (directControl) {
			this.target = imposter;
		} else if (spatial != null){
			this.target = spatial;
		} else {
			this.receiver = null;
			this.target = imposter;
		}
		computePosition();
		prevPos.set(target.getWorldTranslation());
		camera.setLocation(pos);
	}
	@Override
    protected void controlUpdate(float tpf) {
        if (spatial != null && camera != null) {
			updateCamera(tpf);
			if (!directControl) {				
				spatial.setLocalRotation(quaternion);
				if (hasReceiver()) {
					receiver.updateRotation(quaternion);
					if (receiver.setMotionStates(states)) {
						eye.set(receiver.getWorldTranslation());
						eye.addLocal(camera.getDirection());
						receiver.lookAt(eye);
					}
				}
			} else {
				if (moving) {
					targetDir.set(states[2] + states[3], 0, states[0] + states[1]);
					targetDir.normalizeLocal();
					quaternion.multLocal(targetDir);
					targetDir.multLocal(relativeSpeed*tpf);
					targetDir.setY(0);
					target.move(targetDir);
					temp.set(target.getLocalTranslation());
					float h = world.getHeight(temp)+1f;
					float delta = h - temp.y;
					if (delta > 0) {
						temp.setY(temp.y + Math.min(delta, 0.1f));
					} else {
						temp.setY(temp.y + Math.max(delta, -0.1f));
					}
					target.setLocalTranslation(temp);
					//Vector3f pos = camera.getLocation().clone();
					//pos.addLocal(targetDir);
					//camera.setLocation(pos);
					relativeSpeed += (moveSpeed*tpf);
					if (relativeSpeed > moveSpeed) {
						relativeSpeed = moveSpeed;
					}
				} else {
					relativeSpeed = moveSpeed*0.1f;
				}
			}
			/*if (receiver.setMotionStates(states)) {
					eye.set(receiver.getTranslation());
					eye.addLocal(camera.getDirection());
				}*/
        }
	}
	@Override
    protected void controlRender(RenderManager rm, ViewPort vp) {}

    @Override
    public Control cloneForSpatial(Spatial newSpatial) {
        Controller control = new Controller(camera, offset, world);
        control.setSpatial(newSpatial);
        control.setEnabled(isEnabled());
        return control;
    }
	public void setDragToRotate(boolean dragToRotate) {
		this.dragToRotate = dragToRotate;
		this.canRotate = !dragToRotate;
	}
	public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }
	public boolean hasReceiver() {
		return receiver != null;
	}
	public void processRotation(Vector3f rotation, boolean rx, boolean ry, boolean rz) {
		if (receiver != null) {
			receiver.setRotation(rx ? rotationVector.x : 0,
								 ry ? rotationVector.y : 0,
								 rz ? rotationVector.z : 0);
		}
	}
	public void rotate(float value, Vector3f axis) {
        Matrix3f mat = new Matrix3f();
        mat.fromAngleNormalAxis(rotationSpeed * value, axis);

        Vector3f up = camera.getUp();
        Vector3f left = camera.getLeft();
        Vector3f dir = camera.getDirection();

        mat.mult(up, up);
        mat.mult(left, left);
        mat.mult(dir, dir);

        Quaternion q = new Quaternion();
        q.fromAxes(left, up, dir);
        q.normalizeLocal();
		
		center.set(dir.x, 0, dir.z);
		center.normalizeLocal();
		
		quaternion.lookAt(center, Vector3f.UNIT_Y);
        //quaternion.normalizeLocal();
        camera.setAxes(q);
    }
	// FPS style camera alters the vertical position
	protected void riseCamera(float value){
		Vector3f vel = new Vector3f(0, value * moveSpeed, 0);
		Vector3f pos = camera.getLocation().clone();
		pos.addLocal(vel);
		camera.setLocation(pos);
    }
	// FPS style camera moves the camera in the x and z plane
    protected void moveCamera(float value, boolean sideways){
		Vector3f vel = new Vector3f();
		Vector3f pos = camera.getLocation().clone();
		if (sideways){
            camera.getLeft(vel);
        }else{
            camera.getDirection(vel);
        }
        vel.multLocal(value * moveSpeed);
		vel.setY(0);
		pos.addLocal(vel);
		camera.setLocation(pos);
    }
	public Vector3f rotate(float x, float y, float z) {
		if (x == 0 && y == 0)
		{ return null; }
		if (firstPerson)
		{ rotationVector.addLocal(x*rxSpeed, y*rySpeed, z*rzSpeed); }
		else
		{ rotationVector.addLocal(-x*rxSpeed, y*rySpeed, z*rzSpeed); }
		MathUtils.constrainX(rotationVector, -85, 85);
		return rotationVector;
	}
	public void zoom(int dx) {
		distance -= dx;
		distance = Math.max(1, distance);
		distance = Math.min(25, distance);
	}
	//rotate the camera around the target on the horizontal plane
   public void rotateCamera(float value) {
        if (!canRotate || !enabled) {
            return;
        }
        rotating = true;
        targetRotation += value * rotationSpeed;
    }

    //move the camera toward or away the target
    protected void zoomCamera(float value) {
        if (!enabled) {
            return;
        }
		zooming = true;
        targetDistance += value * zoomSensitivity;
        if (targetDistance > maxDistance) {
            targetDistance = maxDistance;
        }
        if (targetDistance < minDistance) {
            targetDistance = minDistance;
        }
		vRotateCamera(value*0.03f);
        if (veryCloseRotation) {
            if ((targetVRotation < minVerticalRotation) && (targetDistance > (minDistance + 1.0f))) {
                targetVRotation = minVerticalRotation;
            }
        }
    }
	protected void zoomFPSCamera(float value) {
		camera.getDirection(targetDir);
		//targetDir.set(0, 0, value);
		//quaternion.multLocal(targetDir);
		targetDir.multLocal(value*zoomSpeed);
		Vector3f pos = camera.getLocation().clone();
		pos.subtractLocal(targetDir);
		camera.setLocation(pos);
		/*if (pos.getY() > 3f) {
			
		}*/
		
        /*// derive fovY value
        float h = camera.getFrustumTop();
        float w = camera.getFrustumRight();
        float aspect = w / h;

        float near = camera.getFrustumNear();

        float fovY = FastMath.atan(h / near)
                  / (FastMath.DEG_TO_RAD * .5f);
        float newFovY = fovY + value * 0.1f * zoomSpeed;
        if (newFovY > 0f) {
            // Don't let the FOV go zero or negative.
            fovY = newFovY;
        }

        h = FastMath.tan( fovY * FastMath.DEG_TO_RAD * .5f) * near;
        w = h * aspect;

        camera.setFrustumTop(h);
        camera.setFrustumBottom(-h);
        camera.setFrustumLeft(-w);
        camera.setFrustumRight(w);*/
    }

    //rotate the camera around the target on the vertical plane
    public void vRotateCamera(float value) {
        if (!canRotate || !enabled) {
            return;
        }
        vRotating = true;
        float lastGoodRot = targetVRotation;
        targetVRotation += value * rotationSpeed;
        if (targetVRotation > maxVerticalRotation)
		{
            targetVRotation = lastGoodRot;
        }
        if (veryCloseRotation)
		{
            if ((targetVRotation < minVerticalRotation) && (targetDistance > (minDistance + 1.0f)))
			{
                targetVRotation = minVerticalRotation;
            }
			else if (targetVRotation < -FastMath.DEG_TO_RAD * 90)
			{
                targetVRotation = lastGoodRot;
            }
        }
		else
		{
            if ((targetVRotation < minVerticalRotation))
			{
                targetVRotation = lastGoodRot;
            }
        }
    }
	public final void registerWithInput(InputManager inputManager)
	{
        String[] inputs = { CamToggleRotate,
            CamDown,
            CamUp,
            CamMoveLeft,
            CamMoveRight,
            CamZoomIn,
            CamZoomOut };
			
			//MoveForward,
		//MoveLeft,
		//	MoveBack,
		//	MoveRight,
        this.inputManager = inputManager;
		//inputManager.addMapping(MoveForward, new KeyTrigger(KeyInput.KEY_W));
	//	inputManager.addMapping(MoveLeft, new KeyTrigger(KeyInput.KEY_A));
	//	inputManager.addMapping(MoveBack, new KeyTrigger(KeyInput.KEY_S));
	//	inputManager.addMapping(MoveRight, new KeyTrigger(KeyInput.KEY_D));
        if (!invertYaxis)
		{
            inputManager.addMapping(CamDown, new MouseAxisTrigger(MouseInput.AXIS_Y, true));
            inputManager.addMapping(CamUp, new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        }
		else
		{
            inputManager.addMapping(CamDown, new MouseAxisTrigger(MouseInput.AXIS_Y, false));
            inputManager.addMapping(CamUp, new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        }
        inputManager.addMapping(CamZoomIn, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping(CamZoomOut, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        if (!invertXaxis)
		{
            inputManager.addMapping(CamMoveLeft, new MouseAxisTrigger(MouseInput.AXIS_X, true));
            inputManager.addMapping(CamMoveRight, new MouseAxisTrigger(MouseInput.AXIS_X, false));
        }
		else
		{
            inputManager.addMapping(CamMoveLeft, new MouseAxisTrigger(MouseInput.AXIS_X, false));
            inputManager.addMapping(CamMoveRight, new MouseAxisTrigger(MouseInput.AXIS_X, true));
        }
        inputManager.addMapping(CamToggleRotate, new KeyTrigger(KeyInput.KEY_Y));
        // inputManager.addMapping(CamToggleRotate, new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));

        inputManager.addListener(this, inputs);
    }
	public boolean receiveInput(String name, boolean state, float tpf) {
		if (state && name.equals("jump")) {
			receiver.processMovement(name, true);
			System.out.println("Conroller Jump");
			return true;
		}
		if (name.equals(MoveForward)) {
			states[0] = state ? 1 : 0;
			updateMovingState();
			return true;
		}
		if (name.equals(MoveBack)) {
			states[1] = state ? -1 : 0;
			updateMovingState();
			return true;
		}
		if (name.equals(MoveLeft)) {
			states[2] = state ? 1 : 0;
			updateMovingState();
			return true;
		}
		if (name.equals(MoveRight)) {
			states[3] = state ? -1 : 0;
			updateMovingState();
			return true;
		} 
		if (name.equals(MoveNone)) {
			states[0] = 0;
			states[1] = 0;
			states[2] = 0;
			states[3] = 0;
			updateMovingState();
			return true;
		}
		return false;
	}
	private void updateMovingState() {
		this.moving = (states[0] != 0) || (states[1] != 0) || (states[2] != 0) || (states[3] != 0);
	}
	public void onAction(String command, boolean state, float tpf)
	{
		if (dragToRotate)
		{
            if (command.equals(CamToggleRotate) && enabled)
			{
                if (state)
				{
                    canRotate = true;
                    if (hideCursorOnRotate)
					{
                        inputManager.setCursorVisible(false);
                    }
                }
				else
				{
                    canRotate = false;
                    if (hideCursorOnRotate)
					{
                        inputManager.setCursorVisible(true);
                    }
                }
				return;
            }
        }
	}
	public void onAnalog(String name, float value, float tpf)
	{
       /*if (name.equals(CamMoveLeft))
		{
			mouse.subtractLocal(value*mouseMoveSpeed, 0);
			if (mouse.x < (mouseBoundMin.x+boundEdgeWidth))
			{
				mouse.setX(Math.max(mouse.x, mouseBoundMin.x));
				rotateCamera(-value);
				//rotate(value*5, Vector3f.UNIT_Y);
			}
			return;
        }
		if (name.equals(CamMoveRight))
		{
			mouse.addLocal(value*mouseMoveSpeed, 0);
			if (mouse.x > (mouseBoundMax.x-boundEdgeWidth))
			{
				mouse.setX(Math.min(mouse.x, mouseBoundMax.x));
				rotateCamera(value);
				//rotate(-value*5, Vector3f.UNIT_Y);
			}
			return;
        }
		if (name.equals(CamUp))
		{
			mouse.subtractLocal(0, value*mouseMoveSpeed);
			if (mouse.y < (mouseBoundMin.y+boundEdgeWidth))
			{
				mouse.setY(Math.max(mouse.y, mouseBoundMin.y));
				vRotateCamera(value);
				//rotate(value * (invertY ? -2 : 2), cam.getLeft());
			}
			return;
        }
		if (name.equals(CamDown))
		{
			mouse.addLocal(0, value*mouseMoveSpeed);
			if (mouse.y > (mouseBoundMax.y-boundEdgeWidth))
			{
				mouse.setY(Math.min(mouse.y, mouseBoundMax.y));
				vRotateCamera(-value);
				//rotate(-value * (invertY ? -2 : 2), cam.getLeft());
			}
			return;
        }*/
		if (name.equals(CamZoomIn)) {
			/*if (directControl) {
				zoomFPSCamera(-value);
				return;
			}*/
            zoomCamera(-value);
            if (zoomin == false) {
                distanceLerpFactor = 0;
            }
            zoomin = true;
			return;
        }
		if (name.equals(CamZoomOut)) {
			/*if (directControl) {
				zoomFPSCamera(+value);
				return;
			}*/
            zoomCamera(+value);
            if (zoomin == true) {
                distanceLerpFactor = 0;
            }
            zoomin = false;
			return;
        }
    }
	public void mouseUpTrigger(float value) {
		/*if (directControl) {
			rotate(value * (invertYaxis ? -0.8f : 0.8f), camera.getLeft());
		} else {*/
			//vRotateCamera(value);
		//}
		/*if (directControl) {
			targetDir.set(0, 0, 1);
			quaternion.multLocal(targetDir);
			targetDir.multLocal(value);
			targetDir.setY(0);
			target.move(targetDir);
		}*/
	}
	public void mouseDownTrigger(float value) {
		/*if (directControl) {
			rotate(-value * (invertYaxis ? -0.8f : 0.8f), camera.getLeft());
		} else {*/
			//vRotateCamera(-value);
		//}
	}
	public void mouseLeftTrigger(float value) {
		/*if (directControl) {
			rotate(value, Vector3f.UNIT_Y);
		} else {*/
			//rotateCamera(-value);
		//}
	}
	public void mouseRightTrigger(float value) {
		/*if (directControl) {
			rotate(-value, Vector3f.UNIT_Y);
		} else {*/
			//rotateCamera(value);
		//}
	}
	public Vector3f getLocation() {
		return (target != null) ? target.getWorldTranslation() : Vector3f.ZERO;
	}
	protected void computePosition() {
		if (target != null) {
			float hDistance = (distance) * FastMath.sin((FastMath.PI / 2) - vRotation);
			pos.set(hDistance * FastMath.cos(rotation), (distance) * FastMath.sin(vRotation), hDistance * FastMath.sin(rotation));
			pos.addLocal(target.getWorldTranslation());
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
                targetDir.set(targetLocation).subtractLocal(prevPos);
                float dist = targetDir.length();

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
                        Vector3f a = targetDir.negate().normalizeLocal();
                        //the x unit vector
                        Vector3f b = Vector3f.UNIT_X;
                        //2d is good enough
                        a.y = 0;
                        //computation of the rotation angle between the x axis and the trail
                        if (targetDir.z > 0) {
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
                camera.setLocation(pos.addLocal(lookAtOffset));
            } else {
                //easy no smooth motion
                vRotation = targetVRotation;
                rotation = targetRotation;
                distance = targetDistance;
                computePosition();
                camera.setLocation(pos.addLocal(lookAtOffset));
            }
            //keeping track on the previous position of the target
            prevPos.set(targetLocation);

            //the cam looks at the target
            camera.lookAt(targetLocation, initialUpVec);
			
			center.set(camera.getDirection());
			center.setY(0);
			center.normalizeLocal();
		
			quaternion.lookAt(center, Vector3f.UNIT_Y);
        }
    }
}