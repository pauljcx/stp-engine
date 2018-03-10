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
import com.jme3.texture.Image;

import java.nio.ByteBuffer;

import com.stp.games.jme.util.RawImageReader;

public class WorldGen {

	public static void generateRegion(Image image, Volume volume, int rx, int ry) {
		image = RawImageReader.getImageData("/Textures/Terrain/terrain_hm.rpl");
		int width = image.getWidth();
		int height = image.getHeight();
		System.out.println("Generating region data: " + width + " x " + height + " Format: " + image.getFormat() + " Buffer Count: " + image.getData(0).capacity());
		ByteBuffer data = image.getData(0);

		int columns = (width/volume.getChunkSizeX()) + 2;
		int rows = (height/volume.getChunkSizeZ()) + 2;
		int radius = columns/2;
		int halfChunkX = volume.getChunkSizeX()/2;
		int halfChunkZ = volume.getChunkSizeZ()/2;
		Vector3f chunkLocation = new Vector3f();
		for (int cx = -radius; cx <= radius; cx++) {
			for (int cz = -radius; cz <= radius; cz++) {
				chunkLocation.set(cx, 0, cz);
				HeightChunkControl chunk = new HeightChunkControl(chunkLocation, null);
				int startX = (width/2) + (cx*(volume.getChunkSizeX()-1)) - halfChunkX;
				int startZ = (height/2) + (cz*(volume.getChunkSizeZ()-1)) - halfChunkZ;
				System.out.println("Generating chunk: " + chunkLocation + " (" + startX + ", " + startZ + ")");
				for (int z = 0; z < volume.getChunkSizeZ(); z++) {
					int offset = ((z+startZ)*width*2)+startX; // calculate the offset into the buffer
					if (offset >= 0 && offset < data.capacity()-volume.getChunkSizeX()) {
						data.position(offset);
						for (int x = 0; x < volume.getChunkSizeX(); x++) {
							// Read two bytes of data for each value
							chunk.setShortValue(x, 0, z, data.getShort());
						}
					}
				}
			}
		}
	}
}