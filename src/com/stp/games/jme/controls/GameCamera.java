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
package com.stp.games.jme.controls;
// JME3 Dependencies
import com.jme3.app.SimpleApplication;
import com.jme3.app.FlyCamAppState;
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
import com.jme3.input.controls.InputListener;
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

public class GameCamera extends AbstractControl {
	
	public static final String ZOOM = "ZoomCamera";
	public static final String ZOOM_IN = "ZoomIn";
	public static final String ZOOM_OUT = "ZoomOut";
	
	public static final String MOVE_FORWARD = "MoveForward";
	public static final String MOVE_LEFT = "MoveLeft";
	public static final String MOVE_BACK= "MoveBack";
	public static final String MOVE_RIGHT = "MoveRight";
	public static final String MOVE_NONE = "MoveNone";

	protected Camera camera;
	protected int[] states = new int[] { 0, 0, 0, 0 };
	
	protected boolean invertYaxis = false;
    protected boolean invertXaxis = false;
	protected Vector3f lookAtOffset = new Vector3f(0, 0, 0);
	
	// Zooming Variables
	protected float zoomSpeed = 0.6f;
	protected float minZoom = 8.0f;
    protected float maxZoom = 64.0f;
	protected float targetZoom = 16.0f;
    protected float currentZoom = 16.0f;
	protected boolean zooming = false;
	protected Vector3f targetDirection = new Vector3f();
	
	protected float moveSpeed = 30f;
	protected float relativeSpeed = 3f;
	protected boolean moving = false;
	protected Vector3f moveDirection = new Vector3f();
	
    protected float rotationSpeed = 0.8f;

	protected boolean directCamControl;
	
	public GameCamera(SimpleApplication app) {
		this.camera = app.getCamera();
		app.getFlyByCamera().unregisterInput();
		app.getStateManager().detach(app.getStateManager().getState(FlyCamAppState.class));
		directCamControl = true;
	}

	@Override
	public void setSpatial(Spatial spatial) {
		super.setSpatial(spatial);
	}
	@Override
    protected void controlUpdate(float tpf) {
		if (directCamControl) {
			if (moving) {
				moveDirection.set(states[2] + states[3], 0, states[0] + states[1]);
				moveDirection.normalizeLocal();
				moveDirection.multLocal(relativeSpeed*tpf);
				moveDirection.setY(0);
				moveDirection.addLocal(camera.getLocation());
				camera.setLocation(moveDirection);
				relativeSpeed += (moveSpeed*tpf);
				if (relativeSpeed > moveSpeed) {
					relativeSpeed = moveSpeed;
				}
			} else {
				relativeSpeed = moveSpeed*0.1f;
			}
		}
	}
	@Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
	}
	public final void registerWithInput(InputManager inputManager, InputListener inputListener) {
		inputManager.addMapping(ZOOM_IN, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping(ZOOM_OUT, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
		inputManager.addMapping(MOVE_FORWARD, new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping(MOVE_LEFT, new KeyTrigger(KeyInput.KEY_A));
		inputManager.addMapping(MOVE_BACK, new KeyTrigger(KeyInput.KEY_S));
		inputManager.addMapping(MOVE_RIGHT, new KeyTrigger(KeyInput.KEY_D));
		inputManager.addListener(inputListener, ZOOM_IN, ZOOM_OUT, MOVE_FORWARD, MOVE_LEFT, MOVE_BACK, MOVE_RIGHT);
	}
	public boolean receiveInput(String name, boolean state, float value, float tpf) {
		if (name.equals(ZOOM_IN)) {
			if (directCamControl) {
				zoomDirect(-value);
			} else {
				zoomCamera(-value);
			}
			return true;
		}
		if (name.equals(ZOOM_OUT)) {
			if (directCamControl) {
				zoomDirect(value);
			} else {
				zoomCamera(value);
			}
			return true;
		}
		if (name.equals(MOVE_FORWARD)) {
			states[0] = state ? 1 : 0;
			updateMovingState();
			return true;
		}
		if (name.equals(MOVE_BACK)) {
			states[1] = state ? -1 : 0;
			updateMovingState();
			return true;
		}
		if (name.equals(MOVE_LEFT)) {
			states[2] = state ? 1 : 0;
			updateMovingState();
			return true;
		}
		if (name.equals(MOVE_RIGHT)) {
			states[3] = state ? -1 : 0;
			updateMovingState();
			return true;
		} 
		if (name.equals(MOVE_NONE)) {
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
	public void setZoomParams(float minZoom, float maxZoom, float zoomSpeed) {
		this.minZoom = minZoom;
		this.maxZoom = maxZoom;
		this.zoomSpeed = zoomSpeed;
	}
	// Directly moves the camera in the direction that it is facing, unbounded
	protected void zoomDirect(float value) {
		if (!enabled || camera == null) {
            return;
        }
		camera.getDirection(targetDirection);
		targetDirection.multLocal(value*zoomSpeed);
		targetDirection.set(camera.getLocation().x - targetDirection.x,
									camera.getLocation().y - targetDirection.y,
									camera.getLocation().z - targetDirection.z);
		camera.setLocation(targetDirection);
    }
	// Move the camera toward or away the target by the specified amount
    protected void zoomCamera(float value) {
        if (!enabled) {
            return;
        }
		zooming = true;
        targetZoom += (value * zoomSpeed);
        if (targetZoom > maxZoom) {
            targetZoom = maxZoom;
        }
        if (targetZoom < minZoom) {
            targetZoom = minZoom;
        }
    }
	public void setMoveSpeed(float moveSpeed) {
		this.moveSpeed = moveSpeed;
		this.relativeSpeed = moveSpeed*0.1f;
	}
	// FPS style camera moves the camera in the x and z plane
    protected void moveDirect(float value, boolean sideways){
		if (!enabled) {
            return;
        }
		if (sideways){
            camera.getLeft(moveDirection);
        }else{
            camera.getDirection(moveDirection);
        }
        moveDirection.multLocal(value * moveSpeed);
		moveDirection.setY(0);
		moveDirection.addLocal(camera.getLocation());
		camera.setLocation(moveDirection);
    }
}