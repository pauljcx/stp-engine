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
package com.stp.games.jme.hud;
// JME3 Dependencies
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.material.Material;
import com.jme3.scene.shape.Sphere;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;

import com.stp.games.jme.controls.GameControl;
/** @author Paul Collins
 *  @version v1.0 ~ 03/22/2017
 *  HISTORY: Version 1.0 HudIcon3D ~ 03/22/2017
 */
public class HudIcon3D extends HudComponent {
	protected GameControl control;
	protected Geometry shape;
	protected Vector3f extents;
	protected float scaleFactor;
	protected float rotationSpeed;
	protected float angleY;
	
	public HudIcon3D(String name, int width, int height) {
		super(name, width, height);
		//setBackground(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
		setRelative();
		extents = new Vector3f();
		scaleFactor = 10f;
		setX(width/2);
		setY(height);
		rotationSpeed = 15f;
	}
	public void setGameControl(GameControl control) {
		this.control = control;
		if (control != null) {
			if (control.hasSpatial()) {
				BoundingVolume bound = control.getSpatial().getWorldBound();
				if (bound.getType() == BoundingVolume.Type.AABB) {
					((BoundingBox)bound).getExtent(extents);
					if (extents.getX() > extents.getY()) {
						scaleFactor = getWidth()/(extents.getX()*2);
					} else {
						scaleFactor = getHeight()/(extents.getY()*2);
					}
				}
			}
		}
	}
	@Override
	public void update(float tpf) {
		if (visible) {
			angleY = angleY + (tpf*rotationSpeed);
			if (angleY > 360) {
				angleY = angleY - 360;
			}
			setRotationAngles(0, angleY, 0);
		}
	}
	@Override
	public Spatial getSpatial() {
		if (control != null) {
			if (control.hasSpatial()) {
				return control.getSpatial();
			}
		}
		return null;
	}
	// Returns the scaled width of this component
	@Override
	public int getScaledWidth() {
		return (int)(scaleFactor*getZoomLevel());
	}
	// Returns the scaled height of this component
	@Override
	public int getScaledHeight() {
		return (int)(scaleFactor*getZoomLevel());
	}
	// Returns the scaled depth of this component
	@Override
	public int getScaledDepth() {
		return (int)(scaleFactor*getZoomLevel());
	}
}