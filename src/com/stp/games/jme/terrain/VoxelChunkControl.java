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
import com.jme3.math.Vector3f;

public class VoxelChunkControl extends ChunkControl {

	public static enum Face {
        Top, Bottom, Left, Right, Front, Back
    };
	
	public VoxelChunkControl() {
		this (0, 0, 0);
	}
	public VoxelChunkControl(int x, int y, int z) {
		this (new Vector3f(x, y, z));
	}
	public VoxelChunkControl(Vector3f location) {
		super (location);
	}
	// Gets the voxel value at the specified location
	public float getVoxel(Vector3f location) {
		return getVoxel(location.x, location.y, location.z);
	}
	// Gets the voxel value at the specified location
	public float getVoxel(float x, float y, float z) {
		return (super.getValue(x, y, z) != 0) ? 1f : -1f;
	}
	// Gets the voxel value at the specified global location
	public float getGlobalVoxel(float x, float y, float z) {
		return (super.getGlobalValue(x, y, z) != 0) ? 1f : -1f;
	}
	// Gets the voxel data that neighbors the specified location
	public float getNeighborVoxel(Vector3f location, Face face) {
		switch(face) {
			case Top: return getVoxel(location.x, location.y+1, location.z);
            case Bottom: return getVoxel(location.x, location.y-1, location.z);
            case Left: return getVoxel(location.x-1, location.y, location.z);
            case Right: return getVoxel(location.x+1, location.y, location.z);
            case Front: return getVoxel(location.x, location.y, location.z+1);
            case Back: return getVoxel(location.x, location.y, location.z-1);
            default: return getVoxel(location);
        }
    }
	// Gets the value of a neighboring voxel based on its global location
    public float getNeighborGlobalVoxel(Vector3f location, Face face) {
		switch(face) {
			case Top: return getGlobalVoxel(location.x, location.y+1, location.z);
            case Bottom: return getGlobalVoxel(location.x, location.y-1, location.z);
            case Left: return getGlobalVoxel(location.x-1, location.y, location.z);
            case Right: return getGlobalVoxel(location.x+1, location.y, location.z);
            case Front: return getGlobalVoxel(location.x, location.y, location.z+1);
            case Back: return getGlobalVoxel(location.x, location.y, location.z-1);
            default: return getGlobalVoxel(location.x, location.y, location.z);
        }
    }
}