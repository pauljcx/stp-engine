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
package com.stp.games.jme.terrain;
// JME3 Dependencies
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.Geometry;
import com.jme3.material.Material;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
// Java Dependencies
import java.nio.ByteBuffer;
// Internal Dependencies
import com.stp.games.jme.controls.GameControl;

public class TileChunkControl extends ChunkControl {
	protected Geometry subShape;
	protected Material subMaterial;
	
	public TileChunkControl() {
		this (0, 0, 0);
	}
	public TileChunkControl(int x, int y, int z) {
		this (new Vector3f(x, y, z));
	}
	public TileChunkControl(Vector3f location, Region region) {
		super (location, region);
		start.setY(0);
	}
	public TileChunkControl(Vector3f location) {
		super (location);
		start.setY(0);
	}
	public void setSubMaterial(Material subMaterial) {
		this.subMaterial = subMaterial;
	}
	// Updates the geometry of this chunk
	@Override
	public void updateMesh(Mesh mesh, Material material) {
		if (subShape == null) {
			subShape = new Geometry("ChunkSub_" + getName());
			subShape.setMaterial(subMaterial);
			subShape.setShadowMode(ShadowMode.Off);
			subShape.setQueueBucket(Bucket.Opaque);
			subShape.setLocalTranslation(0, -3f, 0);
		}
		if (mesh != null) {
			subShape.setMesh(mesh);
			//node.attachChild(subShape);
		} else {
			node.detachChild(subShape);
		}
		super.updateMesh(mesh, material);
		if (shape != null) {
			//shape.setQueueBucket(Bucket.Transparent);
		}
	}
	// Adds objects to the scene graph if the area has been explored and they haven't been added yet
	public void updateObjects() {
		for (GameControl o : objects) {
			if (!o.isActive() && o.hasSpatial() && isAreaExplored(o.getWorldTranslation())) {
				objNode.attachChild(o.getSpatial());
				o.setActive(true);
			}
		}
	}
	// Adds an object to this chunk
	@Override
	public GameControl addObject(GameControl obj, float x, float y, float z) {
		if (obj != null && isValidLocation(x, 0, z)) {
			obj.setActive(false);
			obj.setWorldTranslation((int)(x+start.x), (int)(y+start.y), (int)(z+start.z));
			objects.add(obj);
			
			ByteBuffer buffer = ByteBuffer.allocate(2);
			buffer.putShort((short)nextAddress);
			buffer.rewind();
			
			setValue(x, TileVolume.TYPE, z, (byte)0);//obj.getType().index());
			setValue(x, TileVolume.LOW_ADDRESS, z, buffer.get());
			setValue(x, TileVolume.HIGH_ADDRESS, z, buffer.get());
			
			if (getValue(x, TileVolume.EXPLORED, z) > 0) {
				needsObjectUpdate = true;
			}
			nextAddress++;
		}
		return obj;
	}
	// Checks the explored data value at the specified coordinates
	public boolean isAreaExplored(Vector3f dataLocation) {
		return getValue(dataLocation.x, TileVolume.EXPLORED, dataLocation.z) > 0;
	}
	// Sets the voxel value at the specified local location
	@Override
	public void setValue(float x, float y, float z, byte value) {
		if (isValidLocation(x, y, z)) {
			data[(int)x][(int)y][(int)z] = value;
		}
		// Empty optimization check
		if (empty) {
			empty = (value == 0);
		}
		// Only update the mesh if visible components have changed
		if (y < 3) {
			needsMeshUpdate = true;
			updated = false;
		}
		// If the explored data value has changed and there is an object at the location then trigger an object update
		if ((int)y == TileVolume.EXPLORED && value > 0) {
			this.explored = true;
			if (getValue(x, TileVolume.TYPE, z) > 0) {
				needsObjectUpdate = true;
			}
		}
	}
}