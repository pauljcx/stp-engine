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
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector2f;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.material.Material;
import com.jme3.bullet.collision.shapes.CollisionShape;
// Java Dependencies
// Internal Dependencies

public class SpaceChunkControl extends ChunkControl {
	
	public SpaceChunkControl() {
		this (0, 0, 0);
	}
	public SpaceChunkControl(int x, int y, int z) {
		this (x, y, z, null);
	}
	public SpaceChunkControl(Vector3f location) {
		this (location, null);
	}
	public SpaceChunkControl(Vector3f location, Region region) {
		this ((int)location.x, (int)location.y, (int)location.z, region);
	}
	public SpaceChunkControl(int x, int y, int z, Region region) {
		super (x, y, z);
		setRegion(region);
		this.position.set(location.getX()*(getSizeX()-1)*2, 0, location.getZ()*(getSizeZ()-1)*2);
		this.start.set(position);
		start.subtractLocal((getSizeX()-1), 0, (getSizeZ()-1));
	}
	// Updates the geometry of this chunk
	@Override
	public void updateMesh(Mesh mesh, Material material) {
		updated = true;
	}
	@Override
	public float getHeight(float gx, float gz) {
		return 0;
	}
	@Override
	public CollisionShape getCollisionShape() {
		return null;
	}
	// Checks for collisions with the chunks shape
	@Override
	public int collideWith(Collidable other, CollisionResults results){
		return 0;
    }
	// Gets the voxel value at the specified location
	public byte getValue(float x, float y, float z) {
		return 0;
	}
	// Sets the voxel value at the specified local location
	public void setValue(float x, float y, float z, byte value) {
		empty = true;
	}
}