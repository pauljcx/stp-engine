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

public class VoxelVolume extends Volume implements VoxelSource
{
	public static int VOXEL_SIZE = 1;
	
	private static final Vector3f[] LOD_SIZE = { new Vector3f(1f, 1f, 1f),
																		new Vector3f(2f, 2f, 2f),
																		new Vector3f(4f, 4f, 4f) };

	public VoxelVolume() {
		super (VolumeType.Voxel, 32, 32, 32);
	}
	// Gets the area that the tile takes up in world space
	public int getVoxelSize() {
		return VOXEL_SIZE;
	}
	// Gets the voxel value at the specified global location
	public float getVoxel(Vector3f globalLocation) {
		return getVoxel(globalLocation.x, globalLocation.y, globalLocation.z);
	}
	// Gets the voxel value at the specified global location
	public float getVoxel(float gx, float gy, float gz) {
		return (super.getValue(gx, gy, gz) != 0) ? 1f : -1f;
	}
	public Vector3f getGradient(Vector3f position, Vector3f store) {
		return store.set(0, 0, 0);
	}
    public Vector3f getGradient(float x, float y, float z, Vector3f store) {
		return store.set(0, 0, 0);
	}
	@Override
	public Mesh buildMesh(ChunkControl chunk) {
		if (chunk.isEmpty()) {
			return null;
		}
		return createMeshMarchingCubes(Vector3f.ZERO.subtract(chunk.getStartLocation()),
																chunk.getStartLocation(),
																new Vector3f(32f, 32f, 32f),
																LOD_SIZE[0]);
	}
	// Creates the chunk geometry using marching cubes algorithm
	private Mesh createMeshMarchingCubes(Vector3f loc, Vector3f start, Vector3f dim, Vector3f cubeSize) {
		MeshBuilder meshBuilder = new MeshBuilder();
		Vector3f end = new Vector3f(start.x + dim.x, start.y + dim.y, start.z + dim.z);
        for (float i = start.x; i < end.x; i += cubeSize.x) {
            for (float j = start.y; j < end.y; j += cubeSize.y) {
                for (float k = start.z; k < end.z; k += cubeSize.z) {
				
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
					IsoSurface.addMarchingCubesTriangles(this, locations, values, null, meshBuilder);
                }
            }
        }
		if (meshBuilder.countVertices() > 0) {
			return meshBuilder.generateMesh();
		}
		return null;
	}
}