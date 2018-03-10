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
// Java Dependencies
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class VoxelShape implements VoxelSource {
	public static enum Face {
        Top, Bottom, Left, Right, Front, Back
    };
	
	protected byte[][][] data;
	private boolean empty;
	
	public VoxelShape() {
	}
	public VoxelShape(int width, int height, int length) {
		this.data = new byte[width][height][length];
		this.empty = true;
	}
	// Gets the bounding width of this shape
	public int getWidth() {
		return data.length;
	}
	// Gets the bounding height of this shape
	public int getHeight() {
		return data[0].length;
	}
	// Gets the bounding length of this shape
	public int getLength() {
		return data[0][0].length;
	}
	// Gets the total number of data bytes this shape holds
	public int getDataCount() {
		return getWidth() * getHeight() * getLength();
	}
	// Gets the voxel value at the specified location
	public float getVoxel(Vector3f location) {
		return getVoxel(location.x, location.y, location.z);
	}
	// Gets the voxel value at the specified location
	public float getVoxel(float x, float y, float z) {
		if (isValidLocation(x, y, z)) {
			return (data[(int)x][(int)y][(int)z] != 0) ? 1f : -1f;
		}
		return -1f;
	}
	public Vector3f getGradient(Vector3f position, Vector3f store) {
		return getGradient(position.getX(), position.getY(), position.getZ(), store);
	}
	public Vector3f getGradient(float x, float y, float z, Vector3f store) {
		return store.set(x- (getWidth()/2f), y - (getHeight()/2f), z - (getLength()/2f));
	}
	// Gets the voxel data that neighbors the specified location
	public float getNeighborVoxel(Vector3f location, Face face) {
		return getNeighborVoxel(location.x, location.y, location.z, face);
	}
	// Gets the voxel data that neighbors the specified location
	public float getNeighborVoxel(float x, float y, float z, Face face) {
		switch(face) {
			case Top: return getVoxel(x, y+1, z);
            case Bottom: return getVoxel(x, y-1,z);
            case Left: return getVoxel(x-1, y, z);
            case Right: return getVoxel(x+1, y, z);
            case Front: return getVoxel(x, y, z+1);
            case Back: return getVoxel(x, y, z-1);
            default: return getVoxel(x, y, z);
        }
    }
	public int getVolume() {
		int volume = 0;
		for (int x = 0; x < getWidth(); x++) {
			for (int y = 0; y < getHeight(); y++) {
				for (int z = 0; z < getLength(); z++) {
					if (data[x][y][z] != 0) {
						volume++;
					}
				}
			}
		}
		return volume;
	}
	// Sets the voxel value at the specified location
	public void setVoxel(Vector3f location, float value) {
		setVoxel(location.x, location.y, location.z, value);
		
	}
	// Sets the voxel value at the specified location
	public void setVoxel(float x, float y, float z, float value) {
		if (isValidLocation(x, y, z)) {
			data[(int)x][(int)y][(int)z] = (value != 0f) ? (byte)1 : (byte)0;
		}
		if (empty) {
			empty = (value == 0);
		}
	}
	// Clears all voxel data from this shape
	public void clear() {
		for (int x = 0; x < getWidth(); x++) {
			for (int y = 0; y < getHeight(); y++) {
				for (int z = 0; z < getLength(); z++) {
					data[x][y][z] = (byte)0;
				}
			}
		}
		this.empty = true;
	}
	public int decimate(Vector3f direction, float force) {
		if (direction.x > 0) {
			for (int x = 0; x < getWidth(); x++) {
				if (direction.z > 0) {
					for (int z = 0; z < getLength(); z++) {
						for (int y = 0; y < getHeight(); y++) {
							if (data[x][y][z] != 0) {
								setVoxel(x, y, z, 0f);
								return 1;
							}
						}
					}
				} else {
					for (int z =  getLength()-1; z >= 0; z--) {
						for (int y = 0; y < getHeight(); y++) {
							if (data[x][y][z] != 0) {
								setVoxel(x, y, z, 0f);
								return 1;
							}
						}
					}
				}
			}
		} else {
			for (int x = getWidth()-1; x >= 0; x--) {
				if (direction.z > 0) {
					for (int z = 0; z < getLength(); z++) {
						for (int y = 0; y < getHeight(); y++) {
							if (data[x][y][z] != 0) {
								setVoxel(x, y, z, 0f);
								return 1;
							}
						}
					}
				} else {
					for (int z =  getLength()-1; z >= 0; z--) {
						for (int y = 0; y < getHeight(); y++) {
							if (data[x][y][z] != 0) {
								setVoxel(x, y, z, 0f);
								return 1;
							}
						}
					}
				}
			}
		}
		return 0;
	}
	// Check whether the shape contains any data
	public boolean isEmpty() {
		return empty;
	}
	// Internal function to check if location is in this chunk
	protected boolean isValidLocation(float x, float y, float z) {
		int px = (int)x;
		int py = (int)y;
		int pz = (int)z;
		return (px >= 0) && (px < getWidth())
			&& (py >= 0) && (py < getHeight())
			&& (pz >= 0) && (pz < getLength());
    }
	public void generateCubeShape() {
		for (int x = 0; x < getWidth(); x++) {
			for (int y = 0; y < getHeight(); y++) {
				for (int z = 0; z < getLength(); z++) {
					setVoxel(x, y, z, 1f);
				}
			}
		}
	}
	public void generatePyramidShape() {
		int xRadius = getWidth()/2;
		int yRadius = getHeight()/2;
		int zRadius = getLength()/2;
		float radius;
		for (int x = 0; x < getWidth(); x++) {
			for (int y = 0; y < getHeight(); y++) {
				for (int z = 0; z < getLength(); z++) {
					radius = (y < yRadius) ? y : getHeight()-y;
					if (Math.abs(x-xRadius) < radius && Math.abs(z-zRadius) < radius) {
						setVoxel(x, y, z, 1f);
					}
				}
			}
		}
	}
	// Fills the shape with an oval pattern using sine waves
	public void generateOvalShape() {
		double xRadius = getWidth()/2;
		double yRadius = getHeight()/2;
		double zRadius = getLength()/2;
		for (int x = 0; x < getWidth(); x++) {
			for (int y = 0; y < getHeight(); y++) {
				for (int z = 0; z < getLength(); z++) {
					/*if ((((x-xRadius)*(x-xRadius))/(xRadius*xRadius) + ((y-yRadius)*(y-yRadius))/(yRadius*yRadius)) <= 1.0) {
						setVoxel(x, y, z, 1f);
					}*/
					double tx = (x - xRadius) / xRadius;
					double ty = (y - yRadius) / yRadius;
					double tz = (z - zRadius) / zRadius;
					if ((tx * tx + ty * ty) < 1.0 && (tx * tx + tz * tz) < 1.0 && (ty * ty + tz * tz) < 1.0) {
						setVoxel(x, y, z, 1f);
					}
				}
			}
		}
	}
	public void generateSphereShape() {
		double radius = getWidth()/2.0;
		double tolerance = 0.001;
		for (int x = 0; x < getWidth(); x++) {
			for (int y = 0; y < getHeight(); y++) {
				for (int z = 0; z < getLength(); z++) {
					double dx = x - radius;
					double dy = y - radius;
					double dz = z - radius;
					double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
					if ((distance + tolerance) <= radius) { // && (Math.abs(dx) > 8 || Math.abs(dy) > 8)) {
						setVoxel(x, y, z, 1f);
					}
				}
			}
		}
	}
	public void generateRockShape() {
		float y = 1f;
		setVoxel(5, y, 9, 1f);
		setVoxel(6, y, 9, 1f);
		
		setVoxel(4, y, 8, 1f);
		setVoxel(5, y, 8, 1f);
		setVoxel(6, y, 8, 1f);
		setVoxel(7, y, 8, 1f);
		
		setVoxel(3, y, 7, 1f);
		setVoxel(4, y, 7, 1f);
		setVoxel(5, y, 7, 1f);
		setVoxel(6, y, 7, 1f);
		setVoxel(7, y, 7, 1f);
		
		setVoxel(2, y, 6, 1f);
		setVoxel(3, y, 6, 1f);
		setVoxel(4, y, 6, 1f);
		setVoxel(5, y, 6, 1f);
		setVoxel(6, y, 6, 1f);
		setVoxel(7, y, 6, 1f);
		setVoxel(8, y, 6, 1f);
		
		setVoxel(1, y, 5, 1f);
		setVoxel(2, y, 5, 1f);
		setVoxel(3, y, 5, 1f);
		setVoxel(4, y, 5, 1f);
		setVoxel(5, y, 5, 1f);
		setVoxel(6, y, 5, 1f);
		setVoxel(7, y, 5, 1f);
		setVoxel(8, y, 5, 1f);
		setVoxel(9, y, 5, 1f);
		
		setVoxel(1, y, 4, 1f);
		setVoxel(2, y, 4, 1f);
		setVoxel(3, y, 4, 1f);
		setVoxel(4, y, 4, 1f);
		setVoxel(5, y, 4, 1f);
		setVoxel(6, y, 4, 1f);
		setVoxel(7, y, 4, 1f);
		setVoxel(8, y, 4, 1f);
		setVoxel(9, y, 4, 1f);
		
		setVoxel(0, y, 3, 1f);
		setVoxel(1, y, 3, 1f);
		setVoxel(2, y, 3, 1f);
		setVoxel(3, y, 3, 1f);
		setVoxel(4, y, 3, 1f);
		setVoxel(5, y, 3, 1f);
		setVoxel(6, y, 3, 1f);
		setVoxel(7, y, 3, 1f);
		setVoxel(8, y, 3, 1f);
		setVoxel(9, y, 3, 1f);
		
		setVoxel(0, y, 2, 1f);
		setVoxel(1, y, 2, 1f);
		setVoxel(2, y, 2, 1f);
		setVoxel(3, y, 2, 1f);
		setVoxel(4, y, 2, 1f);
		setVoxel(5, y, 2, 1f);
		setVoxel(6, y, 2, 1f);
		setVoxel(7, y, 2, 1f);
		setVoxel(8, y, 2, 1f);
		
		setVoxel(1, y, 1, 1f);
		setVoxel(2, y, 1, 1f);
		setVoxel(3, y, 1, 1f);
		setVoxel(4, y, 1, 1f);
		setVoxel(5, y, 1, 1f);
		setVoxel(6, y, 1, 1f);
		setVoxel(7, y, 1, 1f);
		setVoxel(8, y, 1, 1f);
		
		setVoxel(3, y, 0, 1f);
		setVoxel(4, y, 0, 1f);
		setVoxel(5, y, 0, 1f);
		setVoxel(6, y, 0, 1f);
		setVoxel(7, y, 0, 1f);
		
		// Layer 1
		setVoxel(4, 2, 9, 1f);
		setVoxel(5, 2, 9, 1f);
		setVoxel(6, 2, 9, 1f);
		
		setVoxel(3, 2, 8, 1f);
		setVoxel(4, 2, 8, 1f);
		setVoxel(5, 2, 8, 1f);
		setVoxel(6, 2, 8, 1f);
		setVoxel(7, 2, 8, 1f);
		
		setVoxel(3, 2, 7, 1f);
		setVoxel(4, 2, 7, 1f);
		setVoxel(5, 2, 7, 1f);
		setVoxel(6, 2, 7, 1f);
		setVoxel(7, 2, 7, 1f);
		
		setVoxel(2, 2, 6, 1f);
		setVoxel(3, 2, 6, 1f);
		setVoxel(4, 2, 6, 1f);
		setVoxel(5, 2, 6, 1f);
		setVoxel(6, 2, 6, 1f);
		setVoxel(7, 2, 6, 1f);
		setVoxel(8, 2, 6, 1f);
		
		setVoxel(1, 2, 5, 1f);
		setVoxel(2, 2, 5, 1f);
		setVoxel(3, 2, 5, 1f);
		setVoxel(4, 2, 5, 1f);
		setVoxel(5, 2, 5, 1f);
		setVoxel(6, 2, 5, 1f);
		setVoxel(7, 2, 5, 1f);
		setVoxel(8, 2, 5, 1f);
		setVoxel(9, 2, 5, 1f);
		
		setVoxel(1, 2, 4, 1f);
		setVoxel(2, 2, 4, 1f);
		setVoxel(3, 2, 4, 1f);
		setVoxel(4, 2, 4, 1f);
		setVoxel(5, 2, 4, 1f);
		setVoxel(6, 2, 4, 1f);
		setVoxel(7, 2, 4, 1f);
		setVoxel(8, 2, 4, 1f);
		setVoxel(9, 2, 4, 1f);
		
		setVoxel(0, 2, 3, 1f);
		setVoxel(1, 2, 3, 1f);
		setVoxel(2, 2, 3, 1f);
		setVoxel(3, 2, 3, 1f);
		setVoxel(4, 2, 3, 1f);
		setVoxel(5, 2, 3, 1f);
		setVoxel(6, 2, 3, 1f);
		setVoxel(7, 2, 3, 1f);
		setVoxel(8, 2, 3, 1f);
		setVoxel(9, 2, 3, 1f);
		
		setVoxel(0, 2, 2, 1f);
		setVoxel(1, 2, 2, 1f);
		setVoxel(2, 2, 2, 1f);
		setVoxel(3, 2, 2, 1f);
		setVoxel(4, 2, 2, 1f);
		setVoxel(5, 2, 2, 1f);
		setVoxel(6, 2, 2, 1f);
		setVoxel(7, 2, 2, 1f);
		setVoxel(8, 2, 2, 1f);
		
		setVoxel(1, 2, 1, 1f);
		setVoxel(2, 2, 1, 1f);
		setVoxel(3, 2, 1, 1f);
		setVoxel(4, 2, 1, 1f);
		setVoxel(5, 2, 1, 1f);
		setVoxel(6, 2, 1, 1f);
		setVoxel(7, 2, 1, 1f);
		setVoxel(8, 2, 1, 1f);
		
		setVoxel(3, 2, 0, 1f);
		setVoxel(4, 2, 0, 1f);
		setVoxel(5, 2, 0, 1f);
		setVoxel(6, 2, 0, 1f);
		setVoxel(7, 2, 0, 1f);
		
		// Layer 2
		setVoxel(5, 3, 8, 1f);
		
		setVoxel(4, 3, 7, 1f);
		setVoxel(5, 3, 7, 1f);
		setVoxel(6, 3, 7, 1f);
		
		setVoxel(4, 3, 6, 1f);
		setVoxel(5, 3, 6, 1f);
		setVoxel(6, 3, 6, 1f);
		
		setVoxel(3, 3, 5, 1f);
		setVoxel(4, 3, 5, 1f);
		setVoxel(5, 3, 5, 1f);
		setVoxel(6, 3, 5, 1f);
		setVoxel(7, 3, 5, 1f);
		
		setVoxel(2, 3, 4, 1f);
		setVoxel(3, 3, 4, 1f);
		setVoxel(4, 3, 4, 1f);
		setVoxel(5, 3, 4, 1f);
		setVoxel(6, 3, 4, 1f);
		setVoxel(7, 3, 4, 1f);
		
		setVoxel(2, 3, 3, 1f);
		setVoxel(3, 3, 3, 1f);
		setVoxel(4, 3, 3, 1f);
		setVoxel(5, 3, 3, 1f);
		setVoxel(6, 3, 3, 1f);
		setVoxel(7, 3, 3, 1f);
		
		setVoxel(2, 3, 2, 1f);
		setVoxel(3, 3, 2, 1f);
		setVoxel(4, 3, 2, 1f);
		setVoxel(5, 3, 2, 1f);
		setVoxel(6, 3, 2, 1f);
		setVoxel(7, 3, 2, 1f);
		
		setVoxel(4, 3, 1, 1f);
		setVoxel(5, 3, 1, 1f);
		setVoxel(6, 3, 1, 1f);
		
		//Layer 3
		setVoxel(5, 4, 5, 1f);
		
		setVoxel(4, 4, 4, 1f);
		setVoxel(5, 4, 4, 1f);
		setVoxel(6, 4, 4, 1f);
		
		setVoxel(4, 4, 3, 1f);
		setVoxel(5, 4, 3, 1f);
		setVoxel(6, 4, 3, 1f);
		
		setVoxel(5, 4, 2, 1f);
	}
	public void generateRockShapeAlt() {
		double xRadius = getWidth()/2;
		double yRadius = getHeight()/2;
		double zRadius = getLength()/2;
		int d = (int)(Math.random()*xRadius);
		int sx = (int)((xRadius-d)*Math.random());
		for (int y = 2; y < 3; y++) {
			for (int z = 0; z < getLength(); z++) {
				if (Math.random() < 0.8f) {
					d = (z < zRadius) ? d + 1 : d - 1;
				}
				if (Math.random() < 0.4f) {
					sx = (z < zRadius) ? sx - 1 : sx + 1;
				}
				for (int x = sx; x < sx+d; x++) {
					setVoxel(x, y, z, 1f);
				}
			}
		}
	}
	
	/***************** I/0 Functions *******************/	
	// Gets the amount of memory in bytes required to store this shapes data
	public int getBufferSize() {
		return 4 + ((empty) ? 0 : getDataCount());
	}
	// Writes the shapes data to the specified buffer
	public void fillBuffer(ByteBuffer buffer) throws IOException {
		buffer.put((byte)getWidth());
		buffer.put((byte)getHeight());
		buffer.put((byte)getLength());
		// Optimize saving for shapes that have no data
		buffer.put(empty ? (byte)1 : (byte)0);
		if (!empty) {
			for (int x = 0; x < getWidth(); x++) {
				for (int y = 0; y < getHeight(); y++) {
					buffer.put(data[x][y], 0, getLength());
				}
			}
		}
	}
	// Reads the shapes data from the specified buffer
	public void readBuffer(ByteBuffer buffer) throws IOException {
		if (buffer == null)	{
			return;
		}
		int width = (buffer.get() & 0xFF);
		int height = (buffer.get() & 0xFF);
		int length = (buffer.get() & 0xFF);
		this.data = new byte[width][height][length];
		this.empty = (buffer.get() != 0);
		if (!empty) {
			for (int x = 0; x < getWidth(); x++) {
				for (int y = 0; y < getHeight(); y++) {
					buffer.get(data[x][y]);
				}
			}
		}
	}
}