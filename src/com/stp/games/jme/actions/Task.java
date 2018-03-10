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
import com.jme3.math.Vector2f;
import com.jme3.scene.Spatial;
import com.jme3.collision.CollisionResult;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.material.Material;
import com.jme3.texture.Texture;
import com.jme3.material.RenderState.BlendMode;
// Internal Dependencies
import com.stp.games.jme.hud.HudManager;
import com.stp.games.jme.terrain.World;
import com.stp.games.jme.terrain.ChunkControl;
import com.stp.games.jme.terrain.TileVolume;
import com.stp.games.jme.controls.GameControl;

/**
 * The Task class...full description here.
 *
 */
public abstract class Task {
	protected static final ColorRGBA ALPHA_GREEN = new ColorRGBA(0, 1f, 0, 0.375f);
	protected static final ColorRGBA ALPHA_RED = new ColorRGBA(1f, 0, 0, 0.375f);	
	
	protected HudManager hud;
	protected World world;
	protected boolean enabled;
	protected boolean sourceIsSet;
	protected GameControl source;
	protected GameControl target;
	
	protected Node node;
	protected AssetManager assetManager;	
	protected Material targetMaterial;
	protected Texture alphaMap;
	protected boolean updated;
	protected boolean placeTarget;
	
	public Task(HudManager hud, World world, AssetManager assetManager) {
		this.hud = hud;
		this.world = world;
		this.enabled = false;
		this.sourceIsSet = false;
		this.placeTarget = false;
		this.updated = false;
		
		this.assetManager = assetManager;
		node = new Node("TempObject");
		alphaMap = assetManager.loadTexture("Textures/alpha96.png");
	}
	public void update(float tpf) {
		if (enabled && target != null) {
			CollisionResult result = world.getChunkCollision(new Vector2f(hud.getMouseX(), hud.getScreenHeight()-hud.getMouseY()));
			if (result != null) {
				Vector3f point = result.getContactPoint();
				point.setX(Math.round(point.x));
				point.setZ(Math.round(point.z));
				target.setWorldTranslation(point);
				if (targetMaterial != null) {
					if (validate(point)) {
						targetMaterial.setColor("Diffuse", ALPHA_GREEN);
					} else {
						targetMaterial.setColor("Diffuse", ALPHA_RED);
					}
				}
			}
		}
		if (updated) {
			if (node.getParent() == null) {
				world.getNode().attachChild(node);
			} else {
				world.getNode().detachChild(node);
			}
			updated = false;
		}
		if (placeTarget && target != null) {
			world.getVolume().addObject(target, target.getStoredLocation());
			placeTarget = false;
		}
	}
	public boolean validate(Vector3f point) {
		return false;
	}
	protected void setTempObject(GameControl object) {
		if (node.getParent() == null) {
			node.detachAllChildren();
			/*if (!object.hasSpatial()) {
				object.initialize(assetManager);
			}*/
			this.targetMaterial = object.getMaterial();
			if (targetMaterial != null) {
				targetMaterial.setBoolean("UseMaterialColors", true);
				targetMaterial.setColor("Diffuse", ALPHA_GREEN);
			}
			node.attachChild(object.getSpatial());
			updated = true;
		}
	}
	public boolean isEnabled() {
		return enabled;
	}
	public abstract void initiate(GameControl source, String action);
	public abstract boolean receiveInput(String name);
}