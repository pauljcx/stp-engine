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
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
// Java Dependencies
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
// Internal Dependencies

public class Region {
	// Biome Types
	public static final int OCEAN = 0;
	public static final int DESERT = 1;
	public static final int TROPICS = 2;
	public static final int JUNGLE = 3;
	public static final int MESA = 4;
	public static final int SAVANA = 5;
	public static final int PLAINS = 6;
	public static final int FOREST = 7;
	public static final int MOUNTAINS = 8;
	public static final int TAIGA = 9;
	public static final int TUNDRA = 10;
	public static final int GLACIAL = 11;

	// Hemisphere Types
	public static final int ARCTIC_NORTH = 3;
	public static final int TEMPERATE_NORTH = 2;
	public static final int TROPICS_NORTH = 1;
	public static final int EQUATOR = 0;
	public static final int TROPICS_SOUTH = -1;
	public static final int TEMPERATE_SOUTH = -2;
	public static final int ARCTIC_SOUTH = -3;
	
	// Rainfall Types
	public static final int ARID = 0;
	public static final int LIGHT = 1;
	public static final int AVERAGE = 2;
	public static final int MODERATE = 3;
	public static final int HEAVY = 4;
	public static final int SEVERE = 5;
	
	public static int DIMENSION_X = 65;
	public static int DIMENSION_Z = 65;

	//protected ArrayList<ChunkControl> loaded = new ArrayList<ChunkControl>();
	protected final List<ChunkControl> loaded = Collections.synchronizedList(new ArrayList<ChunkControl>());
	protected final Vector2f location = new Vector2f();
	
	protected File file;
	
	protected int biome;
	protected int hemisphere;
	protected int rainfall;
	protected long seed;
	protected int sizeX;
	protected int sizeZ;

	public Region(File file, int x, int z, int width, int length) {
		this.file = file;
		this.location.set(x, z);
		this.seed = System.nanoTime();
		this.sizeX = width*DIMENSION_X;
		this.sizeZ = length*DIMENSION_Z;
	}
	public ChunkControl createNewChunk(Vector3f chunkLocation) {
		return new ChunkControl(chunkLocation, this);
	}
	public ChunkControl getChunk(Vector3f chunkLocation) {
		return this.getChunk(chunkLocation.x, chunkLocation.y, chunkLocation.z);
	}
	public ChunkControl getChunk(float x, float y, float z) {
		synchronized(loaded) {
			Iterator<ChunkControl> i = loaded.iterator(); // Must be in synchronized block
			while (i.hasNext()) {
				ChunkControl chunk = i.next();
				if (chunk.matchesLocation(x, y, z)) {
					return chunk;
				}
			}
		}
		return null;
	}
	public ChunkControl addLoadedChunk(ChunkControl chunk) {
		loaded.add(chunk);
		return chunk;
	}
	public int getSizeX() {
		return sizeX;
	}
	public int getSizeZ() {
		return sizeZ;
	}
	public int getBiome() {
		return biome;
	}
	public int getHemisphere() {
		return hemisphere;
	}
	public int getRainfall() {
		return rainfall;
	}
	public Vector2f getLocation() {
		return location;
	}
	public long getSeed() {
		return seed;
	}
	public void setBiome(int biome) {
		this.biome = biome;
	}
	public void setHemisphere(int hemisphere) {
		this.hemisphere = hemisphere;
	}
	public void setRainfall(int rainfall) {
		this.rainfall = rainfall;
	}
	public boolean matchesLocation(Vector2f regionLocation) {
		return matchesLocation(regionLocation.x, regionLocation.y);
	}
	public boolean matchesLocation(float x, float z) {
		return (x == location.x) && (z == location.y);
	}
	public String getRegionName() {
		return "r" + (int)location.x + "_" + (int)location.y;
	}
	@Override
	public boolean equals(Object other) {
		if (other instanceof Region) {
			return ((Region)other).getLocation().equals(location);
		}
		return false;
	}
	@Override
	public int hashCode() {
		return 13 + location.hashCode();
	}
	public boolean loadChunk(ChunkControl chunk) {
		// Fast fail if file is not valid
		if (file == null || !file.exists()) {
			return false;
		}
		ZipFile zip = null;
		try {
			zip = new ZipFile(file);
			ZipEntry entry = zip.getEntry(chunk.getName());
			if (entry != null) {
				chunk.read(zip.getInputStream(entry));
				chunk.setLoaded(true);
				loaded.add(chunk);
			}
			zip.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try { zip.close(); }
			catch (Exception ex) {}
		}
		return chunk.isLoaded();
	}
	public void save(File directory) {
		// Fast fail if file is not valid or there are no chunks to save
		if (directory == null || loaded.size() == 0) {
			return;
		}
		File saveFile = new File(directory, getRegionName());
		ZipInputStream zin = null;
		ZipOutputStream out = null;
		boolean renameOk = false;
		File tempFile = null;
		try {
			// get a temp file
			tempFile = File.createTempFile(saveFile.getName(), null);
			// delete it, otherwise you cannot rename your existing zip to it.
			tempFile.delete();
			
			boolean saveExists = saveFile.exists();
			if (saveExists) {
				renameOk = saveFile.renameTo(tempFile);
				if (!renameOk) {
					throw new RuntimeException("could not rename the file " + saveFile.getAbsolutePath() + " to " + tempFile.getAbsolutePath());
				}
			}

			FileOutputStream dest = new FileOutputStream(file);
			out = new ZipOutputStream(new BufferedOutputStream(dest));

			// Write all loaded chunks to the output file
			for (ChunkControl chunk : loaded) {
				ZipEntry entry = new ZipEntry(chunk.getName());
				out.putNextEntry(entry);
				chunk.write(out);
			}
			
			if (saveExists) {
				byte[] buf = new byte[16384];
				zin = new ZipInputStream(new FileInputStream(tempFile));
				ZipEntry entry = zin.getNextEntry();
				while (entry != null) {
					// Copy all unloaded chunks from the original file
					if (!isLoaded(entry.getName())) {
						// Add ZIP entry to output stream.
						out.putNextEntry(new ZipEntry(entry.getName()));
						// Transfer bytes from the ZIP file to the output file
						int len;
						while ((len = zin.read(buf)) > 0) {
							out.write(buf, 0, len);
						}
					}
					entry = zin.getNextEntry();
				}
				zin.close();
				tempFile.delete();
			}
			out.closeEntry();
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			if (renameOk) {
				System.out.println("Attempting to restore original file: " + tempFile);
				try {
					// Restore the original file if there was a problem saving
					saveFile.delete();
					tempFile.renameTo(saveFile);
				} catch (Exception exct) {}
			}
		} finally {
			try {
				out.close();
				zin.close();
			} catch (Exception ex) {}
		}
	}
	private boolean isLoaded(String chunkName) {
		for (ChunkControl c : loaded) {
			if (c.getName().equals(chunkName)) {
				return true;
			}
		}
		return false;
	}
}