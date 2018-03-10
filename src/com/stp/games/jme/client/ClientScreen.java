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
import com.jme3.asset.AssetManager;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.util.TempVars;
// Java Dependencies
import java.util.Iterator;
// Internal Dependencies
import com.stp.games.jme.GameRegistry;
import com.stp.games.jme.hud.HudManager;
import com.stp.games.jme.hud.HudContainer;
import com.stp.games.jme.hud.HudComponent;
import com.stp.games.jme.controls.GameControl;
import com.stp.games.jme.controls.ItemControl;
import com.stp.games.jme.controls.Player;

public class ClientScreen extends AbstractAppState {
	public final static String MoveForward = "MoveForward";
	public final static String MoveLeft = "MoveLeft";
	public final static String MoveBack = "MoveBack";
	public final static String MoveRight = "MoveRight";
	
	protected static ClientCamera camera = new ClientCamera();

	protected final Node screenNode;
	protected final Node targetNode;
	protected final GameRegistry registry;
	protected final HudManager hud;
	protected Player player;
	protected ClientMenu menu;
	protected boolean registered;

	private Vector3f targetDirection = new Vector3f();
	private float moveSpeed = 25f;
	private float relativeSpeed = moveSpeed*0.1f;

	public ClientScreen(Player player) {
		this.player = player;
		this.screenNode = new Node("scene");
		this.targetNode = new Node("target");
		this.registry = GameRegistry.getInstance();
		this.hud = HudManager.getInstance();
		this.registered = false;
	}
	public Node getNode() {
		return screenNode;
	}
	public Player getPlayer() {
		return player;
	}
	protected void registerAssets(AssetManager assetManager) {
		this.registered = true;
	}
	// Checks to see if the world assets have already been registered
	public boolean isRegistered() {
		return registered;
	}
	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);
		// Register any needed assets the first run
		if (!isRegistered()) {
			registerAssets(app.getAssetManager());
		}
		
		// Setup the camera
		camera.setCamera(app.getCamera());
		setupCamera();

		// Attach the main node to the scene graph
		setupScene(app.getAssetManager());
		screenNode.attachChild(targetNode);
		((SimpleApplication)app).getRootNode().attachChild(screenNode);
		
		// Enable the GUI components
		if (menu != null) {
			this.menu.setVisible(true);
		}
	}
	protected void setupCamera() {
		camera.initialize(45f, 1f, 1000f);
		camera.resetMotionStates();
		camera.setAllowMouseRotation(false);
		targetNode.setLocalTranslation(0, 0, 0);
		targetNode.addControl(camera);
		this.relativeSpeed = moveSpeed*0.1f;
	}
	protected void setupScene(AssetManager assetManager) {
		screenNode.detachAllChildren();
	}
	@Override
	public void cleanup() {
		super.cleanup();
		
		// Release camera control
		targetNode.removeControl(camera);
		
		// Detach the main node from the scene graph
		screenNode.removeFromParent();

		// Disable the GUI components
		if (menu != null) {
			this.menu.setVisible(false);
		}
	}
	@Override
	public void update(float tpf) {
		if (camera.isMoving()) {
			this.targetDirection.set(camera.getPanValue(), 0, camera.getMoveValue());
			targetDirection.normalizeLocal();
			//quaternion.multLocal(targetDirection);
			targetDirection.multLocal(relativeSpeed*tpf);
			targetDirection.setY(0);
			targetNode.move(targetDirection);
			this.relativeSpeed += (moveSpeed*tpf);
			if (relativeSpeed > moveSpeed) {
				this.relativeSpeed = moveSpeed;
			}
		}
	}
	public void destroy() {
	}
	public boolean isScreenActive() {
		return (screenNode.getParent() != null);
	}
	public boolean receiveInput(String name, boolean state, float tpf) {
		if (isScreenActive()) {
			if (name.equals(MoveForward)) {
				if (!camera.setMotionState(ClientCamera.FORWARD, state)) {
					this.relativeSpeed = moveSpeed*0.1f;
				}
				return true;
			}
			if (name.equals(MoveBack)) {
				if (!camera.setMotionState(ClientCamera.BACKWARD, state)) {
					this.relativeSpeed = moveSpeed*0.1f;
				}
				return true;
			}
			if (name.equals(MoveLeft)) {
				if (!camera.setMotionState(ClientCamera.LEFT, state)) {
					this.relativeSpeed = moveSpeed*0.1f;
				}
				return true;
			}
			if (name.equals(MoveRight)) {
				if (!camera.setMotionState(ClientCamera.RIGHT, state)) {
					this.relativeSpeed = moveSpeed*0.1f;
				}
				return true;
			}
		}
		return false;
	}
	public boolean doScroll(int x, int y, int axis, float value) {
		if (isScreenActive() && menu != null) {
			HudComponent result = menu.doScroll(x, y, axis, value);
			if (result == null) {
				if (axis == HudComponent.SCROLL_X) {
					this.targetDirection.set(value*0.01f, 0, 0);
					targetNode.move(targetDirection);
				} else if (axis == HudComponent.SCROLL_Y) {
					this.targetDirection.set(0, 0, value*0.01f);
					targetNode.move(targetDirection);
				} else if (axis == HudComponent.SCROLL_UP) {
					camera.zoomCamera(value);
				} else if (axis == HudComponent.SCROLL_DOWN) {
					camera.zoomCamera(value);
				}
			}
			return true;
		}
		return false;
	}
	public GameControl doClick(int eventX, int eventY, boolean isPressed) {
		return null;
	}
	/**
     * Finds and returns the nearest GameControl object from a given click location on screen if there is one
     *
     * @param screenX the incoming x coordinates in screen space to be evaluated
     * @param screenY the incoming y coordinates in screen space to be evaluated
     * @return control the GameControl object found at the specified coordinates or null if none was found
     */
	public GameControl getTargetControl(int screenX, int screenY) {
		TempVars temp = TempVars.get();
		// Convert screen click to 3d position
		temp.vect2d.set(screenX, screenY);
		camera.getWorldCoordinates(temp.vect2d, 0f, temp.vect1);
		camera.getWorldCoordinates(temp.vect2d, 1f, temp.vect2).subtractLocal(temp.vect1).normalizeLocal();
		// Collect intersections between ray and all nodes in results list.
		temp.collisionResults.clear();
		screenNode.collideWith(new Ray(temp.vect1, temp.vect2), temp.collisionResults);
		if (temp.collisionResults.size() > 0) {
			GameControl control = null;
			// Iterate over all collisions to find the first GameControl target
			for (Iterator<CollisionResult> collisions = temp.collisionResults.iterator(); collisions.hasNext();) {
				Spatial object = collisions.next().getGeometry();
				control = object.getControl(GameControl.class);
				// For each collisiion found check the object and all it's parents for a valid GameControl object
				while (object.getParent() != null && control == null) {
					object = object.getParent();
					control = object.getControl(GameControl.class);
					// Search neighboring children for a valid game control target if the collision occured with on screen text
					if (control == null && object instanceof BitmapText) {
						Node parent = object.getParent();
						if (parent != null) {
							for (Spatial child : parent.getChildren()) {
								control = child.getControl(GameControl.class);
								if (control != null) {
									break;
								}
							}
						}
					}
				}
				// If a valid GameControl was found return it
				if (control != null) {
					temp.release();
					return control;
				}
			}
		}
		temp.release();
		return null;
	}
	protected BitmapText getSpatialText(String textValue, float textSize, ColorRGBA textColor, float yOffset) {
		BitmapText text = text = new BitmapText(hud.loadFont("Font/profont-32-outline.fnt"));
		text.setQueueBucket(Bucket.Transparent);
		text.setText(textValue);
		text.setColor(textColor);
		text.setSize(textSize);
		text.updateLogicalState(0);
		text.setLocalTranslation((text.getLineWidth()/2), yOffset, 0);
		
		BillboardControl control = new BillboardControl();
		control.setAlignment(BillboardControl.Alignment.Screen);
		text.addControl(control);

		return text;
	}
	public static ClientCamera getClientCamera() {
		return camera;
	}
}