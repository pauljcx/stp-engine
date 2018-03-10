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
package com.stp.games.jme;
// JME3 Dependencies
import com.jme3.asset.AssetManager;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.collision.CollisionResult;
import com.jme3.scene.Node;
import com.stp.games.jme.controls.GameControl;

public class GameScene extends AbstractAppState {
	protected final Node sceneNode = new Node("scene");
	protected final GameRegistry registry;
	protected SimpleApplication app;
	protected boolean registered;
	
	public GameScene() {
		registry = GameRegistry.getInstance();
		this.registered = false;
	}
	public Node getNode() {
		return sceneNode;
	}
	
	// Note that update is only called while the state is both attached and enabled
	@Override
	public void update(float tpf) {
	}
	public void loadComplete() {
	}
	public void registerAssets(AssetManager assetManager) {
		this.registered = true;
	}
	// Checks to see if the world assets have already been registered
	public boolean isRegistered() {
		return registered;
	}
	// Gets the root node of the application
	public Node getRootNode() {
		return app.getRootNode();
	}
	// Creates a new object from the schematic that matches the specified name
	public GameControl createObject(String name) {
		return registry.createObject(name);
	}
	public void destroy() {
	}
}