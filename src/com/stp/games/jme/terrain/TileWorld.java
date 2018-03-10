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
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;
import com.jme3.scene.Mesh;
import com.jme3.scene.Geometry;
import com.jme3.material.Material;
import com.jme3.texture.Texture;
import com.jme3.texture.Image;
import com.jme3.texture.image.ImageRaster;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
// Java Dependencies
import java.io.File;

import com.stp.games.jme.hud.HudManager;
import com.stp.games.jme.controls.*;
import com.stp.games.jme.actions.TileMoveAction;
import com.stp.games.jme.actions.CharacterAction;

public class TileWorld extends World {
	protected Material subMaterial;
	protected ImageRaster terrainRaster;
	
	public TileWorld(AssetManager assetManager, HudManager hud, File saveFile, int r) {
		super (assetManager, hud, saveFile, r, 1, r);
	}
	@Override
	public void registerAssets(AssetManager assetManager) {
		this.volume = new TileVolume();
		worldNode.attachChild(volume.getNode());
		this.material = new Material(assetManager, "MatDefs/Terrain/Tiles.j3md");
		Texture texture = assetManager.loadTexture("Textures/Terrain/tiles.png");
		texture.setMagFilter(Texture.MagFilter.Nearest);
        texture.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
		texture.setWrap(Texture.WrapMode.EdgeClamp);
		material.setTexture("Texture", texture);
		Texture normals = assetManager.loadTexture("Textures/Terrain/normals.png");
		normals.setMagFilter(Texture.MagFilter.Nearest);
        normals.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
		normals.setWrap(Texture.WrapMode.EdgeClamp);
		material.setTexture("NormalMap", normals);
		Texture masks = assetManager.loadTexture("Textures/Terrain/masks.png");
		masks.setMagFilter(Texture.MagFilter.Nearest);
        masks.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
		masks.setWrap(Texture.WrapMode.EdgeClamp);
		material.setTexture("Mask", masks);
		Texture overlay = assetManager.loadTexture("Interface/underground.png");
		overlay.setMagFilter(Texture.MagFilter.Nearest);
        overlay.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
		overlay.setWrap(Texture.WrapMode.EdgeClamp);
		material.setTexture("Overlay", overlay);
		//material.setTransparent(true);
		//material.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		
		Texture terrainMap = assetManager.loadTexture("Textures/Terrain/terrain.png");
		terrainRaster = ImageRaster.create(terrainMap.getImage());
		
		this.subMaterial = new Material(assetManager, "MatDefs/Terrain/SubTiles.j3md");
		subMaterial.setTexture("Texture", texture.clone());
		subMaterial.setTexture("NormalMap", normals.clone());
		subMaterial.setTexture("Mask", masks.clone());
		//registry.add(TreeControl.OAK_TREE);
		this.registered = true;
	}
	public byte getRasterValue(int x, int y) {
		if (x >= 0 && x < terrainRaster.getWidth() && y >= 0 && y < terrainRaster.getHeight()) {
			ColorRGBA pixel = terrainRaster.getPixel(x, y);
			//System.out.println("Pixel: " + x + ", " + y + " | " + pixel);
			if (pixel.r < 0.12f) {
				return 0;
			} else if (pixel.r < 0.3f) {
				return (byte)3;
			} else if (pixel.r < 0.5f) {
				return (byte)6;
			} else if (pixel.r < 0.6f) {
				return (byte)2;
			} else {
				return (byte)1;
			}
		} else {
			return 0;
		}
	}
	public byte getMaskValue(int x, int y) {
		if (x > 0 && x < terrainRaster.getWidth()-1 && y > 0 && y < terrainRaster.getHeight()-1) {
			byte[][] samples = new byte[3][3];
			samples[0][0] = getRasterValue(x-1, y-1);
			samples[0][1] = getRasterValue(x, y-1);
			samples[0][2] = getRasterValue(x+1, y-1);
			samples[1][0] = getRasterValue(x-1, y);
			
			samples[1][1] = getRasterValue(x, y);
			
			samples[1][2] = getRasterValue(x+1, y);
			samples[2][0] = getRasterValue(x-1, y+1);
			samples[2][1] = getRasterValue(x, y+1);
			samples[2][2] = getRasterValue(x+1, y+1);

			if (samples[1][1] == samples[0][1] && samples[1][1] != samples[2][1]) {
				if (samples[1][1] == samples[2][0]) {
					return (byte)3;
				} else {
					return (byte)6;
				}
			} else if (samples[1][1] == samples[2][1] && samples[1][1] != samples[0][1]) {
				if (samples[1][1] != samples[1][0]) {
					return (byte)35;
				} else {
					return (byte)5;
				}
			} else {
				return (byte)0;
			}
		} else {
			return 48;
		}
	}
	// Generates new tile data for the chunk
	// TODO: implement terrain generation
	@Override
	public void generateChunk(ChunkControl chunk) {
		try {
			((TileChunkControl)chunk).setSubMaterial(subMaterial);
		} catch (Exception ex) {}
		Vector3f temp = new Vector3f();
		int dx = (terrainRaster.getWidth()/chunk.getSizeX())*-chunk.getX();
		int startX = (terrainRaster.getWidth()/2) + dx;
		int dz = (terrainRaster.getHeight()/chunk.getSizeZ())*chunk.getZ();
		int startZ = (terrainRaster.getHeight()/2) + dz;
		//System.out.println("StartX: " + startX + " StartZ: " + startZ);
		for (int x = 0; x < chunk.getSizeX(); x++) {
			for (int z = 0; z < chunk.getSizeZ(); z++) {
				byte val = getRasterValue(startX-x, startZ+z);
				chunk.setValue(x, TileVolume.OVERLAY, z, val);
				if (val < 1) {
					chunk.setValue(x, TileVolume.MASK, z, (byte)48);
				} else {
					chunk.setValue(x, TileVolume.MASK, z, getMaskValue(startX-x, startZ+z));
					//chunk.setValue(x, TileVolume.MASK, z, (byte)0);
				}
				chunk.setValue(x, TileVolume.EXPLORED, z, (byte)1);
				if (val == 2) {
					if ((Math.random()*100.0) < 2) {
						chunk.addObject(createControl("PalmTree"), x, 0, z);
					} else if ((Math.random()*600.0) < 2) {
						chunk.addObject(createControl("Stone Pile"), x, 0, z);
					}
				}
				/*if (val == 6) {
					chunk.addObject(createControl("Mountain"), chunk.getSizeX()/2, 0, chunk.getSizeZ()/2);
				}*/
			}
		}

		/*for (int x = 0; x < chunk.getSizeX(); x++) {
			for (int z = 0; z < chunk.getSizeZ(); z++) {
				temp.set(chunk.getGlobalX()+x, 0, chunk.getGlobalZ()+z);
				if (temp.distance(Vector3f.ZERO) < 54f) {
					double var = Math.random()*250.0;
					if (var < 2) {
						chunk.addObject(createControl("Tree1a"), x, 0, z);
					}
					chunk.setValue(x, TileVolume.OVERLAY, z, (byte)2);
					chunk.setValue(x, TileVolume.MASK, z, (byte)0);
				} else if (temp.distance(Vector3f.ZERO) < 56f) {
					chunk.setValue(x, TileVolume.OVERLAY, z, (byte)3);
					chunk.setValue(x, TileVolume.MASK, z, (byte)0);
				} else if (temp.distance(Vector3f.ZERO) < 64f) {
					chunk.setValue(x, TileVolume.OVERLAY, z, (byte)1);
					chunk.setValue(x, TileVolume.MASK, z, (byte)0);
				} else {
					chunk.setValue(x, 0, z, (byte)0);
				}
				if (x == 0 || z == 0) {
					chunk.setValue(x, TileVolume.SUB_SURFACE, z, (byte)8);
				} else {
					chunk.setValue(x, TileVolume.SUB_SURFACE, z, (byte)7);
				}
			}
		}*/
		/*int m = 8;
		for (int x = 0; x < 8; x++){
			chunk.setValue(x, TileVolume.MASK, 0, (byte)m);
			m = TileGen.getRandomMatch(m, false);
			System.out.println("New Mask: " + m);
		}*/
		if (chunk.matchesLocation(0, 0, 0)) {
			ContainerControl chest = (ContainerControl)createItem("Chest");
			ClothingControl belt = (ClothingControl)createItem("Leather Belt");
			belt.addItem(0, createItem("Wood Axe"));
			belt.addItem(1, createItem("Bow"));
			chest.addItem(0, createItem("Sword"));
			chest.addItem(11, createItem("Great Sword"));
			chest.addItem(7, createItem("Coconut"));
			chest.addItem(8, belt);
			chest.addItem(10, createItem("Leather Boots"));
			chest.addItem(12, createItem("Lantern"));
			chunk.addObject(chest, 1+(chunk.getSizeX()/2), 0, chunk.getSizeZ()/2);
			//chunk.addObject(createItem("Logs"), 0, 0, chunk.getSizeZ()/2);
			
			//chunk.addObject(createControl("PalmTree"), 1, 0, chunk.getSizeZ()/2);
			/*ContainerControl logs = (ContainerControl)createControl("Logs");
			logs.addItem(0, createItem("Pine Log"));
			chunk.addObject(logs, 0, 0, chunk.getSizeZ()/2);*/
		}
		//chunk.addObject(createControl("Cabin"), chunk.getSizeX()/2, 0, chunk.getSizeZ()/2);
		System.out.println("Chunk Generated: " + chunk);
		chunk.setLoaded(true);
	}
	@Override
	protected void addMoveControl(CreatureControl creature) {
		//creature.setMoveControl(new TileMoveAction(this));
	}
	// Checks whether the player has crossed a chunk boundary and updates their location accordingly
	@Override
	protected boolean recalculateLocation(CreatureControl creature) {
		// Calculate local group location by dividing the local coordinates by the group size
		Vector3f coords = creature.getWorldTranslation();
		int nx = Math.round(coords.x/volume.getChunkSizeX());
		int ny = Math.round(coords.y);
		int nz = Math.round(coords.z/volume.getChunkSizeZ());
		return creature.setChunkLocation(nx, ny, nz);
	}
	// Activates all chunks within a given radius of the player
	@Override
	protected int activateChunks(CreatureControl creature) {
		int unloaded = 0;
		Vector3f temp = new Vector3f();
		for (int x = 0; x < dimensions.getX(); x++) {
			for (int z = 0; z < dimensions.getZ(); z++) {
				temp.set(creature.getChunkX() - radius.getX() + x, 0, creature.getChunkZ() - radius.getZ() + z);
				// Count the number of chunks that need to be loaded
				ChunkControl chunk = volume.activateChunk(temp);
				if (!chunk.isLoaded()) {
					unloaded++;
				}
			}
		}
		return unloaded;
	}
	// Searches for objects within a given radius that allow the specified action
	@Override
	public GameControl getActionableObject(Vector3f location, int radius, String action) {
		int inputX = (int)Math.floor(location.x);
		int inputZ = (int)Math.floor(location.z);
		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				GameControl result = volume.getObject(inputX + x, 0, inputZ + z);
				if (result != null && result.matchAction(action)) {
					return result;
				}
			}
		}
		return null;
	}
	@Override
	public void loadComplete() {
		updateExploration();
	}
	private void updateExploration() {
		// Submit update tasks to the executor pool to be updated on separate threads
		executor.execute(new Runnable() {
			public void run() {
				Vector3f start = new Vector3f();
				Vector3f end = new Vector3f();
				System.out.println("Updating Exploration");
				long startTime = System.currentTimeMillis();
				/*for (CreatureControl c : player.getUnits()) {
					start.set(c.getWorldTranslation());
					end.set(start);
					start.subtractLocal(50, 0, 50);
					end.addLocal(50, 0, 50);
					for (float x = start.x; x < end.x; x = x + 1f) {
						for (float z = start.z; z < end.z; z = z + 1f) {
							volume.setValue(x, TileVolume.EXPLORED, z, (byte)1);
						}
					}
				}*/
				double buildTime = (System.currentTimeMillis() - startTime)/1000.0;
				System.out.println("Exploration Finished: " + buildTime + "s");
			}
		});
	}
}