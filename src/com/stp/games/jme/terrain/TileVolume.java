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
import com.jme3.scene.Node;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Plane;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
// Java Dependencies
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
// Internal Dependencies
import com.stp.games.jme.controls.GameControl;

public class TileVolume extends Volume {
	public static int TILE_SIZE = 1;
	
	// Data Layers
	public static int SURFACE = 0;
	public static int OVERLAY = 1;
	public static int MASK = 2;
	public static int TYPE = 3;
	public static int EXPLORED = 4;
	public static int LOW_ADDRESS = 5;
	public static int HIGH_ADDRESS = 6;
	public static int SUB_SURFACE = 7;
	public static int SUB_OVERLAY = 8;
	public static int SUB_MASK = 9;
	public static int SUB_TYPE = 10;
	public static int SUB_EXPLORED = 11;
	public static int SUB_LOW_ADDRESS = 12;
	public static int SUB_HIGH_ADDRESS = 13;
	
	private boolean physicsInitialized = false;
	
	public TileVolume() {
		super ();
		//ChunkControl.CHUNK_SIZE_Y = 14;
	}
	// Gets the area that the tile takes up in world space
	public int getTileSize() {
		return TILE_SIZE;
	}
	@Override
	public boolean isPassable(float x, float y, float z) {
		/*switch (getObjectTypeAt(x, z)) {
			case CONTAINER: return false;
			case PLANT: return false;
			case STONE: return false;
			default: return true;
		}*/
		return true;
	}
	public GameControl.ObjectType getObjectTypeAt(float x, float z) {
		return GameControl.GENERIC_TYPE; //GameControl.Type.values()[(int)getValue(x, TYPE, z)];
	}
	@Override
	public ChunkControl createNewChunk(Vector3f location, Region region) {
		return new TileChunkControl(location, region);
	}
	@Override
	public void initializePhysics(ChunkControl chunk) {
		if (!physicsInitialized) {
			//world.addPhysicsObject(new RigidBodyControl(new PlaneCollisionShape(new Plane(new Vector3f(0, 1, 0), 0)), 0));
			physicsInitialized = true;
		}
	}
	@Override
	public Mesh buildMesh(ChunkControl chunk) {
		Mesh mesh = chunk.getMesh();
		FloatBuffer txCoords = BufferUtils.createFloatBuffer(chunk.getSizeX()*chunk.getSizeZ()*8);
		FloatBuffer txCoordsAlt = BufferUtils.createFloatBuffer(chunk.getSizeX()*chunk.getSizeZ()*8);
		FloatBuffer maskCoords = BufferUtils.createFloatBuffer(chunk.getSizeX()*chunk.getSizeZ()*8);
		FloatBuffer subCoords = BufferUtils.createFloatBuffer(chunk.getSizeX()*chunk.getSizeZ()*8);
		
		// Only build the mesh structure if it doesn't already exist
		if (mesh == null) {
			ArrayList<Vector2f> mtnVertsLow = new ArrayList<Vector2f>();
			ArrayList<Vector2f> mtnVertsHigh = new ArrayList<Vector2f>();
			FloatBuffer vertices = BufferUtils.createFloatBuffer(chunk.getSizeX()*chunk.getSizeZ()*12);
			IntBuffer indices = BufferUtils.createIntBuffer(chunk.getSizeX()*chunk.getSizeZ()*6);
			int count = 0;
			int zLast = -1;
			int xLast = -1;
			int xMin = 0;
			int xMax = 0;
			int zMin = 0;
			int zMax = 0;
			for (int x = 0; x < chunk.getSizeX(); x++) {
				zLast = -1;
				for (int z = 0; z < chunk.getSizeZ(); z++) {
					float y0 = 0;
					float y1 = 0;
					float y2 = 0;
					float y3 = 0;
					if (chunk.getValue(x, OVERLAY, z) == 0) {
						y0 = -100f;
						y1 = -100f;
						y2 = -100f;
						y3 = -100f;
					}
					/*if (x > 0 && chunk.getValue(x-1, OVERLAY, z) != 0) {
						y0 = 0;
						y2 = 0;
					}
					if (x < chunk.getSizeX()-1 && chunk.getValue(x+1, OVERLAY, z) != 0) {
						y1 = 0;
						y3 = 0;
					}
					if (z > 0 && chunk.getValue(x, OVERLAY, z-1) != 0) {
						y1 = 0;
						y3 = 0;
					}
					if (z < chunk.getSizeZ()-1 && chunk.getValue(x, OVERLAY, z+1) != 0) {
						y0 = 0;
						y2 = 0;
					}*/
					
					vertices.put(x);
					vertices.put(y0);
					vertices.put(z+1f);
					vertices.put(x+1f);
					vertices.put(y1);
					vertices.put(z+1f);
					vertices.put(x);
					vertices.put(y2);
					vertices.put(z);
					vertices.put(x+1f);
					vertices.put(y3);
					vertices.put(z);
					indices.put(count+2);
					indices.put(count+0);
					indices.put(count+1);
					indices.put(count+3);
					indices.put(count+2);
					indices.put(count+1);
					count = count + 4;
				
					addTxCoords(txCoords, 16, 0.0013f, 0.0625f, chunk.getValue(x, SURFACE, z));
					addTxCoords(txCoordsAlt, 16, 0.0013f, 0.0625f, chunk.getValue(x, OVERLAY, z));
					addTxCoords(maskCoords, 16, 0.0013f, 0.0625f, chunk.getValue(x, MASK, z));
					addTxCoords(subCoords, 16, 0.0013f, 0.0625f, chunk.getValue(x, SUB_SURFACE, z));
					
					if (chunk.getValue(x, OVERLAY, z) == 3) {
						if (zLast < 0 || x == chunk.getSizeX()-1) {
							mtnVertsLow.add(new Vector2f(x, z));
							if (x < xMin) { xMin = x; }
							if (x > xMax) { xMax = x; }
							if (z < zMin) { zMin = z; }
							if (z > zMax) { zMax = z; }
						}
						zLast = z;
					}
					
					if (chunk.getValue(x, TYPE, z) != 0) {
						ByteBuffer buffer = ByteBuffer.allocate(2);
						buffer.put(chunk.getValue(x, LOW_ADDRESS, z));
						buffer.put(chunk.getValue(x, HIGH_ADDRESS, z));
						buffer.rewind();
						/*GameControl object = chunk.getObject(buffer.getShort());
						if (object != null) {
							object.setWorldTranslation(x, 0, z);
						}*/
					}
				}
				zLast = -1;
				for (int z = chunk.getSizeZ()-1; z >= 0; z--) {
					if (chunk.getValue(x, OVERLAY, z) == 3) {
						if (zLast < 0 || x == 0 || z == chunk.getSizeZ()-1) {
							mtnVertsHigh.add(new Vector2f(x, z));
							if (x < xMin) { xMin = x; }
							if (x > xMax) { xMax = x; }
							if (z < zMin) { zMin = z; }
							if (z > zMax) { zMax = z; }
						}
						zLast = z;
					}
				}
			}
			vertices.rewind();
			indices.rewind();
			txCoords.rewind();
			txCoordsAlt.rewind();
			maskCoords.rewind();
			subCoords.rewind();
			mesh = new Mesh();
			mesh.setBuffer(VertexBuffer.Type.Position, 3, vertices);
			mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, txCoords);
			mesh.setBuffer(VertexBuffer.Type.TexCoord2, 2, txCoordsAlt);
			mesh.setBuffer(VertexBuffer.Type.TexCoord3, 2, maskCoords);
			mesh.setBuffer(VertexBuffer.Type.TexCoord4, 2, subCoords);
			mesh.setBuffer(VertexBuffer.Type.Index, 3, indices);
			mesh.updateBound();
			return mesh;
		} else {
			for (int x = 0; x < chunk.getSizeX(); x++) {
				for (int z = 0; z < chunk.getSizeZ(); z++) {
					addTxCoords(txCoords, 16, 0.0013f, 0.0625f, chunk.getValue(x, SURFACE, z));
					addTxCoords(txCoordsAlt, 16, 0.0013f, 0.0625f, chunk.getValue(x, OVERLAY, z));
					addTxCoords(maskCoords, 16, 0.0013f, 0.0625f, chunk.getValue(x, MASK, z));
					addTxCoords(subCoords, 16, 0.0013f, 0.0625f, chunk.getValue(x, SUB_SURFACE, z));
					if (chunk.getValue(x, TYPE, z) != 0) {
						ByteBuffer buffer = ByteBuffer.allocate(2);
						buffer.put(chunk.getValue(x, LOW_ADDRESS, z));
						buffer.put(chunk.getValue(x, HIGH_ADDRESS, z));
						buffer.rewind();
						/*GameControl object = chunk.getObject(buffer.getShort());
						if (object != null) {
							object.setWorldTranslation(x, 0, z);
						}*/
					}
				}
			}
			txCoords.rewind();
			txCoordsAlt.rewind();
			maskCoords.rewind();
			subCoords.rewind();
			mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, txCoords);
			mesh.setBuffer(VertexBuffer.Type.TexCoord2, 2, txCoordsAlt);
			mesh.setBuffer(VertexBuffer.Type.TexCoord3, 2, maskCoords);
			mesh.setBuffer(VertexBuffer.Type.TexCoord4, 2, subCoords);
			return mesh;
		}
	}
	private FloatBuffer addTxCoords(FloatBuffer buffer, int txCount, float txOffset, float txUnit, byte value) {
		int row = value/16;
		int column = value%16;
		float tx = column * txUnit;
		float ty = ((-row - 1) * txUnit) + 1;
		buffer.put(tx + txOffset);
		buffer.put(ty + txOffset);
		tx = (column + 1) * txUnit;
		ty = ((-row - 1) * txUnit) + 1;
		buffer.put(tx - txOffset);
		buffer.put(ty + txOffset);
		tx = column * txUnit;
		ty = (-row * txUnit) + 1;
		buffer.put(tx + txOffset);
		buffer.put(ty - txOffset);
		tx = (column + 1) * txUnit;
		ty = (-row * txUnit) + 1;
		buffer.put(tx - txOffset);
		buffer.put(ty - txOffset);
		return  buffer;
	}
	public Mesh buildMountainMesh(ChunkControl chunk) {
		int zLast = -1;
		int xMin = 0;
		int xMax = 0;
		int zMin = 0;
		int zMax = 0;
		ArrayList<Vector2f> lowVerts = new ArrayList<Vector2f>();
		ArrayList<Vector2f> highVerts = new ArrayList<Vector2f>();
		for (int x = 0; x < chunk.getSizeX(); x++) {
			zLast = -1;
			for (int z = 0; z < chunk.getSizeZ(); z++) {
				if (chunk.getValue(x, OVERLAY, z) == 3) {
					if (zLast < 0 || x == chunk.getSizeX()-1) {
						lowVerts.add(new Vector2f(x, z));
						if (x < xMin) { xMin = x; }
						if (x > xMax) { xMax = x; }
						if (z < zMin) { zMin = z; }
						if (z > zMax) { zMax = z; }
					}
					zLast = z;
				}
			}
			zLast = -1;
			for (int z = chunk.getSizeZ()-1; z >= 0; z--) {
				if (chunk.getValue(x, OVERLAY, z) == 3) {
					if (zLast < 0 || x == 0 || z == chunk.getSizeZ()-1) {
						highVerts.add(new Vector2f(x, z));
						if (x < xMin) { xMin = x; }
						if (x > xMax) { xMax = x; }
						if (z < zMin) { zMin = z; }
						if (z > zMax) { zMax = z; }
					}
					zLast = z;
				}
			}
		}
		int vCount = lowVerts.size() + highVerts.size();
		if (vCount == 0) {
			return null;
		}
		int xCenter = ((xMax - xMin)/2)+xMin;
		int zCenter = ((zMax - zMin)/2)+zMin;
		FloatBuffer vertices = BufferUtils.createFloatBuffer(vCount*6);
		IntBuffer indices = BufferUtils.createIntBuffer(vCount*6);
		for (int i = 0; i < lowVerts.size(); i++) {
			vertices.put(lowVerts.get(i).x);
			vertices.put(0);
			vertices.put(lowVerts.get(i).y);
		}
		for (int j = highVerts.size()-1; j >= 0; j--) {
			vertices.put(highVerts.get(j).x);
			vertices.put(0);
			vertices.put(highVerts.get(j).y);
		}
		for (int i = 0; i < lowVerts.size(); i++) {
			float nx = ((xCenter - lowVerts.get(i).x)*0.3f) + lowVerts.get(i).x;
			float nz = ((zCenter - lowVerts.get(i).y)*0.3f) + lowVerts.get(i).y;
			vertices.put(nx);
			vertices.put(3);
			vertices.put(nz);
		}
		for (int j = highVerts.size()-1; j >= 0; j--) {
			float nx = ((xCenter - highVerts.get(j).x)*0.3f) + highVerts.get(j).x;
			float nz = ((zCenter - highVerts.get(j).y)*0.3f) + highVerts.get(j).y;
			vertices.put(nx);
			vertices.put(0);
			vertices.put(nz);
		}
		for (int v = 0; v < vCount; v++) {
			int shift = (v == vCount - 1) ? -v : 1;
			indices.put(vCount + v);
			indices.put(vCount + v + shift);
			indices.put(v);
			indices.put(v + shift);
			indices.put(v);
			indices.put(vCount + v + shift);
		}
		vertices.rewind();
		indices.rewind();
		Mesh mesh = new Mesh();
		mesh.setBuffer(VertexBuffer.Type.Position, 3, vertices);
		mesh.setBuffer(VertexBuffer.Type.Index, 3, indices);
		mesh.updateBound();
		return mesh;
	}
}