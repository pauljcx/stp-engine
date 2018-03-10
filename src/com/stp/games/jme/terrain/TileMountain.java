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
import com.jme3.scene.Mesh;
import com.jme3.math.Vector3f;
// Java Dependencies

public class TileMountain implements VoxelSource {
	private IsoSurface isoSurface;

	private byte[][] values;
	private int xMin;
	private int xMax;
	private int zMin;
	private int zMax;
	
	public TileMountain(int width, int height) {
		values = new byte[width][height];
		//this.isoSurface = new IsoSurface(this);
	}
	public void setValue(int x, int z, byte value) {
		if (validateLocation(x, z)) {
			values[x][z] = value;
			if (x < xMin) {
				xMin = x;
			}
			if (x > xMax) {
				xMax = x;
			}
			if (z < zMin) {
				zMin = z;
			}
			if (z > zMax) {
				zMax = z;
			}
		}
	}
	public float getVoxel(Vector3f location) {
		return getVoxel(location.x, location.y, location.z);
	}
	public float getVoxel(float x, float y, float z) {
		if (validateLocation(x, z)) {
			return (values[(int)x][(int)z] != 0) ? 1f : -1f;
		}
		return -1f;
	}
	public Vector3f getGradient(Vector3f position, Vector3f store) {
		return Vector3f.UNIT_Y;
	}
    public Vector3f getGradient(float x, float y, float z, Vector3f store) {
		return Vector3f.UNIT_Y;
	}
	public boolean validateLocation(float x, float z) {
		return (x >= 0) && (x < values.length) && (z >= 0)  && (z < values[0].length);
	}
	public void reset() {
		xMin = 0;
		xMax = 0;
		zMin = 0;
		zMax = 0;
		for (int x = 0; x < values.length; x++) {
			for (int z = 0; z < values[0].length; z++) {
				values[x][z] = 0;
			}
		}
	}// Creates the mountain geometry using marching cubes algorithm
	public Mesh buildMesh() {
		for (int z = zMin; z < zMax; z++) {
			for (int x = xMin; x < xMax; x++) {
				
			}
		}
           return null;    
	}
	/*
	// Creates the mountain geometry using marching cubes algorithm
	public Mesh buildMesh() {
		MeshBuilder meshBuilder = new MeshBuilder();
		float height = 8;
		Vector3f loc = new Vector3f();
		Vector3f cubeSize = new Vector3f(1f, 1f, 1f);
        for (float i = xMin; i < xMax; i += cubeSize.x) {
            for (float j = 0; j < height; j += cubeSize.y) {
                for (float k = zMin; k < zMax; k += cubeSize.z) {
				
                    float[] values = {
						getVoxel(i, j, k),
                        getVoxel(i + cubeSize.x, j, k),
                        getVoxel(i + cubeSize.x, j, k + cubeSize.z),
                        getVoxel(i, j, k + cubeSize.z),
                        getVoxel(i, j + cubeSize.y, k),
                        getVoxel(i + cubeSize.x, j + cubeSize.y, k),
                        getVoxel(i + cubeSize.x, j + cubeSize.y, k + cubeSize.z),
                        getVoxel(i, j + cubeSize.y, k + cubeSize.z)
					};

                    Vector3f[] locations = {
                        new Vector3f(loc.x + i + 0, loc.y + j + 0, loc.z + k + 0),
                        new Vector3f(loc.x + i + cubeSize.x, loc.y  + j + 0, loc.z + k + 0),
                        new Vector3f(loc.x + i + cubeSize.x, loc.y  + j + 0, loc.z + k + cubeSize.z),
                        new Vector3f(loc.x + i + 0, loc.y  + j + 0, loc.z + k + cubeSize.z),
                        new Vector3f(loc.x + i + 0, loc.y  + j + cubeSize.y, loc.z + k + 0),
                        new Vector3f(loc.x + i + cubeSize.x, loc.y  + j + cubeSize.y, loc.z + k + 0),
                        new Vector3f(loc.x + i + cubeSize.x, loc.y  + j + cubeSize.y, loc.z + k + cubeSize.z),
                        new Vector3f(loc.x + i + 0, loc.y  + j + cubeSize.y, loc.z + k + cubeSize.z)
                    };
                    isoSurface.addMarchingCubesTriangles(locations, values, null, meshBuilder);
                }
            }
        }
		if (meshBuilder.countVertices() > 0) {
			return meshBuilder.generateMesh();
		}
		return null;
	}*/
}