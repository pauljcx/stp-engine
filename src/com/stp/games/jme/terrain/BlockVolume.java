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
import com.jme3.math.Vector3f;
import com.jme3.math.Vector2f;
import com.jme3.scene.VertexBuffer.Type;
// Java Dependencies
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class BlockVolume extends Volume {
	// Static structure to hold texture location
	public static class TextureLocation	{
		public int column;
		public int row;
		public TextureLocation(int column, int row) {
			this.column = column;
			this.row = row;
		}
		public int getColumn() {
			return column;
		}
		public int getRow() {
			return row;
		}
	}
	
	public static class BlockType {
		private byte type;
		private TextureLocation[] txLocations;
		private boolean isTransparent;
		
		public BlockType(byte type, TextureLocation txLocation, boolean transparent)	{
			this (type, new TextureLocation[]{ txLocation }, transparent);
		}
		public BlockType(byte type, TextureLocation[] txLocations, boolean transparent)	{
			this.type = type;
			this.txLocations = txLocations;
			this.isTransparent = transparent;
		}
		public byte getType() {
			return type;
		}
		// Retrieves the appropriate texture location given the specified face
		public TextureLocation getTextureLocation(Face face) {
			int index = (txLocations.length == 6) ? face.ordinal() : 0;
			return txLocations[index];
		}
		// Returns the transparency value
		public boolean isTransparent() {
			return isTransparent;
		}
	}
	public static int BLOCK_SIZE = 1;
	
	protected ArrayList<BlockType> types;
	
	public BlockVolume() {
		super ();
		types = new ArrayList<BlockType>();
	}
	public int getBlockSize() {
		return BLOCK_SIZE;
	}
	// Gets the block at the specified global location
	public BlockType getBlock(Vector3f globalLocation) {
		return getBlock(globalLocation.x, globalLocation.y, globalLocation.z);
	}
	// Gets the block at the specified global location
	public BlockType getBlock(float gx, float gy, float gz) {
		return getBlock(super.getValue(gx, gy, gz));
	}
	public BlockType getBlock(byte type) {
		if (type == 0) {
			return null;
		}
		int index = (int)type - 1;
		return (index < types.size()) ? types.get(index) : null;
	}
	// Gets the block that neighbors the specified location
	public BlockType getNeighborBlock(Vector3f location, Face face) {
		return getNeighborBlock(location.x, location.y, location.z, face);
	}
	public BlockType getNeighborBlock(float gx, float gy, float gz, Face face) {
		switch(face) {
			case Top: return getBlock(gx, gy+1, gz);
            case Bottom: return getBlock(gx, gy-1, gz);
            case Left: return getBlock(gx-1, gy, gz);
            case Right: return getBlock(gx+1, gy, gz);
            case Front: return getBlock(gx, gy, gz+1);
            case Back: return getBlock(gx, gy, gz-1);
            default: return getBlock(gx, gy, gz);
        }
    }
	public void registerBlock(int column, int row, boolean transparent) {
		BlockType type = new BlockType((byte)types.size(), new TextureLocation(column, row), transparent);
		types.add(type);
	}
	public void registerBlock(int[] columns, int[] rows, boolean transparent) {
		if (columns.length == 6 && rows.length == 6) {
			TextureLocation[] locations = { new TextureLocation(columns[0], rows[0]),
															new TextureLocation(columns[1], rows[1]),
															new TextureLocation(columns[2], rows[2]),
															new TextureLocation(columns[3], rows[3]),
															new TextureLocation(columns[4], rows[4]),
															new TextureLocation(columns[5], rows[5]) };
			BlockType type = new BlockType((byte)types.size(), locations, transparent);
			types.add(type);
		}
	}
	@Override
	public Mesh buildMesh(ChunkControl chunk) {
		if (chunk.isEmpty()) {
			return null;
		}
		ArrayList<Vector3f> verticeList = new ArrayList<Vector3f>();
        ArrayList<Vector2f> textureCoordinateList = new ArrayList<Vector2f>();
        ArrayList<Integer> indicesList = new ArrayList<Integer>();
        ArrayList<Float> normalsList = new ArrayList<Float>();
        Vector3f tmpLocation = new Vector3f();
		boolean transparent = false;
        for (int x = 0; x < chunk.getSizeX(); x++) {
            for (int y = 0; y < chunk.getSizeY(); y++) {
                for(int z = 0; z < chunk.getSizeZ(); z++) {
					BlockType block = getBlock(chunk.getValue(x, y, z));
                    if(block != null) {
						Vector3f bottomBackLeft = new Vector3f(x, y, z).multLocal(getBlockSize());
						Vector3f bottomBackRight = new Vector3f(x+1, y, z).multLocal(getBlockSize());
						Vector3f bottomFrontLeft = new Vector3f(x, y, z+1).multLocal(getBlockSize());
						Vector3f bottomFrontRight = new Vector3f(x+1, y, z+1).multLocal(getBlockSize());
						
						Vector3f topBackLeft = new Vector3f(x, y+1, z).multLocal(getBlockSize());
						Vector3f topBackRight = new Vector3f(x+1, y+1, z).multLocal(getBlockSize());
						Vector3f topFrontLeft = new Vector3f(x, y+1, z+1).multLocal(getBlockSize());
						Vector3f topFrontRight = new Vector3f(x+1, y+1, z+1).multLocal(getBlockSize());
						
						tmpLocation.set(chunk.getGlobalX()+x, chunk.getGlobalY()+y, chunk.getGlobalZ()+z);
						if (shouldFaceBeAdded(block, getNeighborBlock(tmpLocation, Face.Top), transparent)) {
                            addVerticeIndexes(verticeList, indicesList);
                            verticeList.add(topFrontLeft);
                            verticeList.add(topFrontRight);
                            verticeList.add(topBackLeft);
                            verticeList.add(topBackRight);
                            addBlockTextureCoordinates(textureCoordinateList, block.getTextureLocation(Face.Top));
                            addSquareNormals(normalsList, new float[] { 0, 1, 0 });
                        }
						 if (shouldFaceBeAdded(block, getNeighborBlock(tmpLocation, Face.Bottom), transparent)) {
                            addVerticeIndexes(verticeList, indicesList);
                            verticeList.add(bottomFrontRight);
                            verticeList.add(bottomFrontLeft);
                            verticeList.add(bottomBackRight);
                            verticeList.add(bottomBackLeft);
                            addBlockTextureCoordinates(textureCoordinateList, block.getTextureLocation(Face.Bottom));
                            addSquareNormals(normalsList, new float[] { 0, -1, 0 });
                        }
						if (shouldFaceBeAdded(block, getNeighborBlock(tmpLocation, Face.Left), transparent)) {
                            addVerticeIndexes(verticeList, indicesList);
                            verticeList.add(bottomBackLeft);
                            verticeList.add(bottomFrontLeft);
                            verticeList.add(topBackLeft);
                            verticeList.add(topFrontLeft);
                            addBlockTextureCoordinates(textureCoordinateList, block.getTextureLocation(Face.Left));
                            addSquareNormals(normalsList, new float[] { -1, 0, 0 });
                        }
						if (shouldFaceBeAdded(block, getNeighborBlock(tmpLocation, Face.Right), transparent)) {
                            addVerticeIndexes(verticeList, indicesList);
                            verticeList.add(bottomFrontRight);
                            verticeList.add(bottomBackRight);
                            verticeList.add(topFrontRight);
                            verticeList.add(topBackRight);
                            addBlockTextureCoordinates(textureCoordinateList, block.getTextureLocation(Face.Right));
                            addSquareNormals(normalsList, new float[] { 1, 0, 0 });
                        }
						if (shouldFaceBeAdded(block, getNeighborBlock(tmpLocation, Face.Front), transparent)) {
                            addVerticeIndexes(verticeList, indicesList);
                            verticeList.add(bottomFrontLeft);
                            verticeList.add(bottomFrontRight);
                            verticeList.add(topFrontLeft);
                            verticeList.add(topFrontRight);
                            addBlockTextureCoordinates(textureCoordinateList, block.getTextureLocation(Face.Front));
                            addSquareNormals(normalsList, new float[] { 0, 0, 1 });
                        }
						if (shouldFaceBeAdded(block, getNeighborBlock(tmpLocation, Face.Back), transparent)) {
                            addVerticeIndexes(verticeList, indicesList);
                            verticeList.add(bottomBackRight);
                            verticeList.add(bottomBackLeft);
                            verticeList.add(topBackRight);
                            verticeList.add(topBackLeft);
                            addBlockTextureCoordinates(textureCoordinateList, block.getTextureLocation(Face.Back));
                            addSquareNormals(normalsList, new float[] { 0, 0, -1 });
                        }
					}
				}
			}
		}
		IntBuffer indices = BufferUtils.createIntBuffer(indicesList.size());
        for (int i = 0; i < indicesList.size(); i++) {
            indices.put(indicesList.get(i));
        }
		FloatBuffer vertices = BufferUtils.createFloatBuffer(verticeList.toArray(new Vector3f[0]));		
		FloatBuffer txCoords =  BufferUtils.createFloatBuffer(textureCoordinateList.toArray(new Vector2f[0]));		
		FloatBuffer normals = BufferUtils.createFloatBuffer(normalsList.size());
        for (int i = 0; i < normalsList.size(); i++) {
            normals.put(normalsList.get(i));
        }
		vertices.rewind();
		indices.rewind();
		normals.rewind();
		txCoords.rewind();
		
		Mesh mesh = new Mesh();
        mesh.setBuffer(Type.Position, 3, vertices);
        mesh.setBuffer(Type.TexCoord, 2, txCoords);
        mesh.setBuffer(Type.Index, 1, indices);
        mesh.setBuffer(Type.Normal, 3, normals);
        mesh.updateBound();
        return mesh;
	}
    private boolean shouldFaceBeAdded(BlockType block, BlockType neighborBlock, boolean transparent)	{
        if (block.isTransparent() == transparent) {
            if (neighborBlock != null) {
                if (block.isTransparent() != neighborBlock.isTransparent()) {
                    return true;
                }
                return false;
            }
            return true;
        }
        return false;
    }
	private static void addVerticeIndexes(ArrayList<Vector3f> verticeList, ArrayList<Integer> indexesList) {
        int verticesCount = verticeList.size();
        indexesList.add(verticesCount + 2);
        indexesList.add(verticesCount + 0);
        indexesList.add(verticesCount + 1);
        indexesList.add(verticesCount + 1);
        indexesList.add(verticesCount + 3);
        indexesList.add(verticesCount + 2);
    }
	private static void addSquareNormals(ArrayList<Float> normalsList, float[] normal) {
        for(int i=0; i<4; i++) {
            normalsList.add(normal[0]);
            normalsList.add(normal[1]);
            normalsList.add(normal[2]);
        }
    }
	private static void addBlockTextureCoordinates(ArrayList<Vector2f> textureCoordinatesList, TextureLocation textureLocation) {
        textureCoordinatesList.add(getTextureCoordinates(textureLocation, 0, 0));
        textureCoordinatesList.add(getTextureCoordinates(textureLocation, 1, 0));
        textureCoordinatesList.add(getTextureCoordinates(textureLocation, 0, 1));
        textureCoordinatesList.add(getTextureCoordinates(textureLocation, 1, 1));
    }
    private static Vector2f getTextureCoordinates(TextureLocation textureLocation, int xUnitsToAdd, int yUnitsToAdd) {
        float textureCount = 16;
        float textureUnit = 1f / textureCount;
        float x = (((textureLocation.getColumn() + xUnitsToAdd) * textureUnit));
        float y = ((((-1 * textureLocation.getRow()) + (yUnitsToAdd - 1)) * textureUnit) + 1);
        return new Vector2f(x, y);
    }
}