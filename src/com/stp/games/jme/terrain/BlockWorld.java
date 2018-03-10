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
import com.jme3.math.Vector3f;
import com.jme3.math.Vector2f;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.texture.Texture;
import com.jme3.texture.Image;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
// Java Dependencies
import java.io.File;

import com.stp.games.jme.controls.SentientControl;
import com.stp.games.jme.hud.HudManager;

public class BlockWorld extends World {

	private String heightMapPath;
	private ImageBasedHeightMap heightMap;
	private Vector2f area;

	public BlockWorld(AssetManager assetManager, HudManager hud, File saveFile, String path, int x, int y, int z) {
		super (assetManager, hud, saveFile, x, y, z);
		this.volume = new BlockVolume();
		this.heightMapPath = path;
		this.area = new Vector2f();
	}
	@Override
	public void registerAssets(AssetManager assetManager) {
		/* Register The Block Material */
		material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md"); //"Common/MatDefs/Misc/Unshaded.j3md");
		Texture texture = assetManager.loadTexture("Textures/cubes/terrain2.png");
        texture.setMagFilter(Texture.MagFilter.Nearest);
        texture.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        material.setTexture("DiffuseMap", texture);
        material.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		
		if (heightMapPath.length() > 0)
		{
			try {
				Texture heightMapTx = assetManager.loadTexture(heightMapPath);
				Image heightMapImage = heightMapTx.getImage();
				heightMap = new ImageBasedHeightMap(heightMapImage, 1f);
				heightMap.load();
				area.set(heightMapImage.getWidth(), heightMapImage.getHeight());
			} catch(Exception ex) {
				heightMap = null;
				System.out.println("Error while loading heightmap: " + heightMapPath);
			}
		}
		
		BlockVolume blockVolume = (BlockVolume)volume;
		blockVolume.registerBlock(0, 0, false);
		blockVolume.registerBlock(1, 0, false);
		this.registered = true;
	}
	// Generates new tile data for the chunk
	// TODO: implement terrain generation
	@Override
	public void generateChunk(ChunkControl chunk) {
		if (heightMap != null) {
			Vector3f coords = new Vector3f();
			Vector3f location = chunk.getStartLocation();
			float sx = location.getX() + (int)(area.getX()/2);
			float sz = location.getZ() + (int)(area.getY()/2);
			for (int x = 0; x < chunk.getSizeX(); x++) {
				float mx = sx + x;
				for (int z = 0; z < chunk.getSizeZ(); z++) {
					float mz = sz + z;
					if (mx > 0 && mx < area.getX() && mz > 0 && mz < area.getY()) {
						int height = Math.round(heightMap.getTrueHeightAtPoint((int)mx, (int)mz))-64;
						//System.out.println("Height: " + height + " Local_Y: " + location.getY());
						for (int y = 0; y < chunk.getSizeY(); y++) {
							float my = location.getY() + y;
							if (my <= height) {
								coords.set(x, y, z);
								if (height > 0) {
									chunk.setValue(coords, (byte)1);
								} else {
									chunk.setValue(coords, (byte)1);
								}
							}
						}
					}
				}
			}
			System.out.println("Generated Chunk: " + chunk);
		} else {
			if (chunk.getY() < 0) {
				chunk.setValues(new Vector3f(0, 0, 0), new Vector3f(chunk.getSizeX(), chunk.getSizeY(), chunk.getSizeZ()), (byte)1);
			} else if (chunk.getY() == 0) {
				Vector3f start = new Vector3f();
				Vector3f end = new Vector3f();
				for (int x = 0; x < chunk.getSizeX(); x++) {
					for (int z = 0; z < chunk.getSizeX(); z++) {
						start.set(x, 0, z);
						end.set(x+1, (int)(Math.random()*2)+2, z+1);
						chunk.setValues(start, end, (byte)1);
					}
				}
				//chunk.setValues(new Vector3f(0, 0, 0), new Vector3f(chunk.getSizeX(), chunk.getSizeY()/4, chunk.getSizeZ()), (byte)1);
			}
		}
		chunk.setLoaded(true);
	}
}