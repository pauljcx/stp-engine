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
import com.jme3.texture.Texture;
import com.jme3.texture.Image;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
// Java Dependencies
import java.io.File;

import com.stp.games.jme.controls.CreatureControl;
import com.stp.games.jme.hud.HudManager;

public class VoxelWorld extends World {

	private String heightMapPath;
	private ImageBasedHeightMap heightMap;
	private Vector2f area;

	public VoxelWorld(AssetManager assetManager, HudManager hud, File saveFile, String path, int x, int y, int z) {
		super (assetManager, hud, saveFile, x, y, z);
		this.volume = new VoxelVolume();
		this.heightMapPath = path;
		this.area = new Vector2f();
	}
	@Override
	public void registerAssets(AssetManager assetManager) {
		float grassScale = 64;
		float dirtScale = 16;
		float rockScale = 128;
		
		if (heightMapPath.length() > 0) {
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

		this.material = new Material(assetManager, "Materials/TerrainLighting.j3md");
		material.setBoolean("useTriPlanarMapping", true);
		material.setFloat("Shininess", 0.0f);

        //   ALPHA map (for splat textures)
		material.setTexture("AlphaMap", assetManager.loadTexture("Textures/Terrain/splat/alpha1.png"));
        //   matTerrain.setTexture("AlphaMap_1", assetManager.loadTexture("Textures/Terrain/splat/alpha2.png"));
        // this material also supports 'AlphaMap_2', so you can get up to 12 diffuse textures

        // HEIGHTMAP image (for the terrain heightmap)
        //  Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");

        // DIRT texture, Diffuse textures 0 to 3 use the first AlphaMap
		Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
		dirt.setWrap(Texture.WrapMode.Repeat);
		material.setTexture("DiffuseMap", dirt);
        //  matTerrain.setFloat("DiffuseMap_0_scale", dirtScale);

        // GRASS texture
		Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
		grass.setWrap(Texture.WrapMode.Repeat);
		material.setTexture("DiffuseMap_1", grass);
        //   matTerrain.setFloat("DiffuseMap_1_scale", grassScale);

        // ROCK texture
		Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
		rock.setWrap(Texture.WrapMode.Repeat);
		material.setTexture("DiffuseMap_2", rock);
        //  matTerrain.setFloat("DiffuseMap_2_scale", rockScale);

		material.setFloat("DiffuseMap_0_scale", 1f / (128f / grassScale));
		material.setFloat("DiffuseMap_1_scale", 1f / (128f / dirtScale));
		material.setFloat("DiffuseMap_2_scale", 1f / (128f / rockScale));
		this.registered = true;
	}
	// Generates new tile data for the chunk
	// TODO: implement terrain generation
	@Override
	public void generateChunk(ChunkControl chunk) {
		if (heightMap != null) {
			Vector3f coords = new Vector3f();
			Vector3f location = chunk.getStartLocation();
			float sx = location.x + (int)(area.x/2);
			float sz = location.z + (int)(area.y/2);
			for (int x = 0; x < chunk.getSizeX(); x++) {
				float mx = sx + x;
				for (int z = 0; z < chunk.getSizeZ(); z++) {
					float mz = sz + z;
					if (mx > 0 && mx < area.x && mz > 0 && mz < area.y) {
						int height = Math.round(heightMap.getTrueHeightAtPoint((int)mx, (int)mz))-64;
						//System.out.println("Height: " + height + " Local_Y: " + location.getY());
						for (int y = 0; y < chunk.getSizeY(); y++) {
							float my = location.y + y;
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
		} else {
			if (chunk.getY() < 0) {
				chunk.setValues(new Vector3f(0, 0, 0), new Vector3f(chunk.getSizeX(), chunk.getSizeY(), chunk.getSizeZ()), (byte)1);
			} else if (chunk.getY() == 0) {
				Vector3f temp = new Vector3f();
				Vector3f start = new Vector3f();
				Vector3f end = new Vector3f();
				for (int x = 0; x < chunk.getSizeX(); x++) {
					for (int z = 0; z < chunk.getSizeZ(); z++) {
						temp.set(chunk.getGlobalX()+x, 0, chunk.getGlobalZ()+z);
						if (temp.distance(Vector3f.ZERO) < 54f) {
							start.set(x, 0, z);
							end.set(x+1, 4, z+1);
							chunk.setValues(start, end, (byte)1);
						} else if (temp.distance(Vector3f.ZERO) < 56f) {
							start.set(x, 0, z);
							end.set(x+1, 3, z+1);
							chunk.setValues(start, end, (byte)1);
						} else if (temp.distance(Vector3f.ZERO) < 64f) {
							start.set(x, 0, z);
							end.set(x+1, 2, z+1);
							chunk.setValues(start, end, (byte)1);
						} else {
							start.set(x, 0, z);
							end.set(x+1, 1, z+1);
							chunk.setValues(start, end, (byte)1);
						}
					}
				}
				/*for (int x = 0; x < chunk.getSizeX(); x++) {
					for (int z = 0; z < chunk.getSizeX(); z++) {
						start.set(x, 0, z);
						end.set(x+1, (int)(Math.random()*2)+2, z+1);
						chunk.setValues(start, end, (byte)1);
					}
				}*/
				//chunk.setValues(new Vector3f(0, 0, 0), new Vector3f(chunk.getSizeX(), chunk.getSizeY()/4, chunk.getSizeZ()), (byte)1);
			}
		}
		System.out.println("Chunk Generated: " + chunk);
		chunk.setLoaded(true);
	}
	// Activates all chunks within a given radius of the player
	@Override
	protected int activateChunks(CreatureControl creature)	{
		int unloaded = 0;
		Vector3f temp = new Vector3f();
		for (int x = 0; x <= dimensions.getX(); x++) {
			for (int y = 0; y <= dimensions.getY(); y++) {
				for (int z = 0; z <= dimensions.getZ(); z++) {
					temp.set(creature.getChunkX() - radius.getX() + x, creature.getChunkY() - radius.getY() + y, creature.getChunkZ() - radius.getZ() + z);
					// Count the number of chunks that need to be loaded
					ChunkControl chunk = volume.activateChunk(temp);
					// Restrict chunks on the outside edge of the active area so they can be loaded but aren't updated or drawn
					// This allows data from neighboring chunks to be sampled when building the meshes
					if (x == dimensions.getX() || y == dimensions.getY() || z == dimensions.getZ()) {
						chunk.setRestricted(true);
						chunk.setVisible(false);
					}
					if (!chunk.isLoaded()) {
						unloaded++;
					}
				}
			}
		}
		return unloaded;
	}
	public float getHeightmapHeight(Vector2f coords) {
		if (heightMap != null) {
			float mx = (area.x/2) + coords.x;
			float mz = (area.y/2) + coords.y;
			if (mx > 0 && mx < area.x && mz > 0 && mz < area.y) {
				return heightMap.getTrueHeightAtPoint((int)mx, (int)mz)-64f;
			}
		}
		return 0;
	}
}