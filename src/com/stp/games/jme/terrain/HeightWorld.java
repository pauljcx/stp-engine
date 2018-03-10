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
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
// Java Dependencies
import java.io.File;

import com.stp.games.jme.hud.HudManager;
import com.stp.games.jme.controls.CreatureControl ;

public class HeightWorld extends World {
	public HeightWorld(AssetManager assetManager, HudManager hud, File saveFile, int r) {
		super (assetManager, hud, saveFile, r, 1, r);
	}
	// Generates new tile data for the chunk
	// TODO: implement terrain generation
	/*@Override
	public void generateChunk(ChunkControl chunk) {
		Vector3f temp = new Vector3f();		
		int startX =(terrainRaster.getWidth()/2) - (chunk.getX()*(chunk.getSizeX()-1)) + (chunk.getSizeX()-1)/2;
		int startZ = (terrainRaster.getHeight()/2) + (chunk.getZ()*(chunk.getSizeZ()-1)) - (chunk.getSizeZ()-1)/2;
		for (int x = 0; x < chunk.getSizeX(); x++) {
			for (int z = 0; z < chunk.getSizeZ(); z++) {
				byte val = getRasterValue(startX-x, startZ+z);
				//byte val = (byte)Math.floor(islandHm.getTrueHeightAtPoint(startX-x, startZ+z));
				
				chunk.setValue(x, TileVolume.SURFACE, z, val);
				float h = ((val & 0xFF)*0.5f)-72.5f;
				if (h > 1.5f) {
					if ((Math.random()*100.0) < 3) {
						chunk.addObject(createObject("CoconutTree"), (x*2) + chunk.getStartX(), h, (z*2)+ chunk.getStartZ());
					} 
				}
			}
		}
		System.out.println("Chunk Generated: " + chunk + " startX=" + startX + "startZ=" + startZ);
		chunk.setLoaded(true);
	}*/
	// Activates all chunks within a given radius of the player
	@Override
	protected int activateChunks(CreatureControl creature) {
		int unloaded = 0;
		Vector3f temp = new Vector3f();
		for (int x = 0; x < dimensions.getX(); x++) {
			for (int z = 0; z < dimensions.getZ(); z++) {
				temp.set(currentChunk.x - radius.getX() + x, 0, currentChunk.z - radius.getZ() + z);
				// Count the number of chunks that need to be loaded
				ChunkControl chunk = volume.activateChunk(temp);
				if (!chunk.isLoaded()) {
					unloaded++;
				}
			}
		}
		return unloaded;
	}
}