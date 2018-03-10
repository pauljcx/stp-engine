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
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.export.binary.ByteUtils;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector2f;
import com.jme3.math.Quaternion;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Mesh;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.Node;
import com.jme3.scene.SimpleBatchNode;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ColorSpace;
import com.jme3.util.BufferUtils;
import com.jme3.util.TempVars;
// Java Dependencies
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
// Internal Dependencies
import com.stp.games.jme.controls.GameControl;
import com.stp.games.jme.forester.paging.ChunkPage;
import com.stp.games.jme.forester.paging.DetailLevel;
import com.stp.games.jme.forester.grass.GrassLayer;
import com.stp.games.jme.forester.util.FastRandom;
import com.stp.games.jme.forester.RectBounds;
import com.stp.games.jme.GameRegistry;

public class HeightChunkControl extends ChunkControl {
	// Data Layers
	public static int SURFACE = 0;
	public static int OVERLAY = 1;
	public static int TYPE = 2;
	public static int EXPLORED = 3;
	public static int LOW_ADDRESS = 4;
	public static int HIGH_ADDRESS = 5;
	
	private static final DetailLevel[] PAGE_DETAIL_LEVELS = { new DetailLevel(0f, 150f, 30f), new DetailLevel(150f, 500f, 30f) };
		
	private final ArrayList<GrassLayer> grassLayers = new ArrayList<GrassLayer>();
	private TerrainQuad terrain;
	private Texture2D texture;
	private float heightScale = 1f;
	private float heightOffset = -72.5f;
	private float terrainScale = 3f;
	private int terrainSize = 129;
	private int patchSize = 65;
	
	public HeightChunkControl() {
		this (0, 0, 0);
	}
	public HeightChunkControl(int x, int y, int z) {
		this (x, y, z, null);
	}
	public HeightChunkControl(Vector3f location) {
		this (location, null);
	}
	public HeightChunkControl(Vector3f location, Region region) {
		this ((int)location.x, (int)location.y, (int)location.z, region);
	}
	public HeightChunkControl(int x, int y, int z, Region region) {
		super (x, y, z);
		setRegion(region);
		createPages();
	}
	// Initialize the dimensions for this chunk, must be called before any data updates
	@Override
	public void setChunkDimensions(int width, int height, int length) {
		super.setChunkDimensions(width, height, length);
		this.position.set(location.getX()*(getSizeX()-1)*terrainScale, 0, location.getZ()*(getSizeZ()-1)*terrainScale);
		this.start.set(position);
		start.subtractLocal((getSizeX()-1)*terrainScale/2, 0, (getSizeZ()-1)*terrainScale/2);
	}
	// Retrieves the size of each page create for this chunk
	@Override
	public int getPageSize() {
		return 64;
	}
	// Retrieves the resolution of pages to create for this chunk, number of rows and columns
	@Override
	public int getPageResolution() {
		return 4;
	}
	@Override
	public DetailLevel[] getPageDetailLevels() {
		return PAGE_DETAIL_LEVELS;
	}
	public TerrainQuad getTerrain() {
		return terrain;
	}
	public void setTerrain(TerrainQuad terrain, Texture2D texture) {
	}
	// Updates the geometry of this chunk
	@Override
	public void updateMesh(Mesh mesh, Material material) {
		if (terrain != null && terrain.getParent() == null) {
			Material terrainMat = material.clone();
			terrainMat.setTexture("HeightMap", texture);
			//terrainMat.setVector2("CenterPoint", new Vector2f(272f, -72f));
			terrain.setMaterial(terrainMat);
			terrain.setShadowMode(ShadowMode.Receive);
			terrain.setLocalTranslation(position.x, 0, position.z);
			terrain.setLocalScale(terrainScale, 1, terrainScale);
			node.attachChild(terrain);
			System.out.println("Attached terrain object at: x=" + position.x + " z=" + position.z);
		}
		updated = true;
		System.out.println("World Coords: " + this + " | " + terrain.getWorldTranslation() + " | " + terrain.getWorldLightList().size());
	}
	@Override
	public float getHeight(float gx, float gz) {
		return getInterpolatedHeight((gx - getStartX())/terrainScale, (gz -  getStartZ())/terrainScale)+heightOffset;
	}
	public float getTrueHeightAtPoint(int x, int z) {
		if (x >= 0 && x < getSizeX() && z >= 0 && z < getSizeZ()) {
			return (getValue(x, SURFACE, z) & 0xFF);
		}
		return 0f;
	}
	 public float getScaledHeightAtPoint(int x, int z) {
        return (getTrueHeightAtPoint(x, z) * heightScale);
    }
	 /**
     * <code>getInterpolatedHeight</code> returns the height of a point that
     * does not fall directly on the height posts.
     *
     * @param x the x coordinate of the point.
     * @param z the y coordinate of the point.
     * @return the interpolated height at this point.
     */
    public float getInterpolatedHeight(float x, float z) {
        float low, highX, highZ;
        float intX, intZ;
        float interpolation;

        low = getScaledHeightAtPoint((int) x, (int) z);
		
        if (x + 1 >= getSizeX()) {
            return low;
        }

        highX = getScaledHeightAtPoint((int) x + 1, (int) z);

        interpolation = x - (int) x;
        intX = ((highX - low) * interpolation) + low;

        if (z + 1 >= getSizeZ()) {
            return low;
        }

        highZ = getScaledHeightAtPoint((int) x, (int) z + 1);

        interpolation = z - (int) z;
        intZ = ((highZ - low) * interpolation) + low;

        return ((intX + intZ) / 2);
    }
	@Override
	public CollisionShape getCollisionShape() {
		if (terrain != null) {
			return new HeightfieldCollisionShape(terrain.getHeightMap(), terrain.getLocalScale());
		}
		return null;
	}
	// Checks for collisions with the chunks shape
	@Override
	public int collideWith(Collidable other, CollisionResults results){
		if (terrain != null) {
			return terrain.collideWith(other, results);
		}
		return 0;
    }
	// Builds the mesh for this shape based on it's underlying data
	@Override
	public void buildMesh(Volume volume) {
		if (terrain == null) {
			float[] heightMap = new float[getSizeX()*getSizeZ()];
			ByteBuffer heightValues = BufferUtils.createByteBuffer(heightMap.length*4);
			int index = 0;
			for (int z = 0; z < getSizeZ(); z++) {
				for (int x = 0; x < getSizeX(); x++) {
					heightMap[index] = getScaledHeightAtPoint(x, z) + heightOffset;
					heightValues.putFloat(heightMap[index]);
					index++;
				}
			}
			Image txImage = new Image(Image.Format.Depth32F, terrainSize, terrainSize, heightValues, ColorSpace.sRGB);
			this.texture = new Texture2D(txImage);
			texture.setMagFilter(Texture.MagFilter.Bilinear);           
			texture.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
			//texture.setWrap(Texture.WrapMode.Clamp);
			this.terrain = new TerrainQuad(getName() + "_Terrain", patchSize, terrainSize, heightMap);
			System.out.println("Chunk mesh complete: " + heightMap.length);
			//terrain.recalculateAllNormals();
			//TerrainLodControl lodControl = new TerrainLodControl(terrain, world.getViewControl().getCamera());
			//terrain.addControl(lodControl);
			
			// Don't generate pages if no grass layers exist
			if (grassLayers.size() < 1 || true) {
				return;
			}
			
			// Create a new random number generator with a seed based on this chunks x and z location values
			long seed = ((long)getX() << 32) | (getZ() & 0xFFFFFFFFL);
			FastRandom rand = new FastRandom(seed);
			// Loads grass geometry to each page for each level of detail
			for (ChunkPage page : getPages()) {
				Node[] nodes = new Node[2];
				for (int level = 0; level < 2; level++) {
					nodes[level] = new Node("LOD" + level);
					// Only add grass for the first level of detail
					if (level == 0) {
						SimpleBatchNode gNode = new SimpleBatchNode("Grass" + level);
						// Add grass layers
						for (GrassLayer layer : grassLayers) {
							createGrassGeometry(rand, layer, page, gNode);
						}
						// Batch the grass geometry and add the node to the page nodes
						if (gNode.getQuantity() > 0) {
							System.out.println("Batch Begin: " + this + " for " + gNode.getQuantity());
							gNode.batch();
							nodes[level].attachChild(gNode);
						}
					}
				}
				page.setNodes(nodes);
			}
		}
	}
	public void addLodControl(Camera camera) {
		if (terrain != null) {
			TerrainLodControl lodControl = terrain.getControl(TerrainLodControl.class);
			if (lodControl == null) {
				lodControl = new TerrainLodControl(terrain, camera);
				terrain.addControl(lodControl);
			}
		}
	}
	// Initializes the PhysicsControl for this chunk and returns it so it can be added to the PhysicsSpace
	@Override
	public PhysicsControl initializePhysics() {
		// Fast fail if terrain mesh has not been built
		if (terrain == null) {
			return null;
		}
		RigidBodyControl physicsControl = terrain.getControl(RigidBodyControl.class);
		// PhysicsControl doesn't exist so we can create a new one
		if (physicsControl == null) {	
			// obtain the collision shape from the chunk
			CollisionShape cs = getCollisionShape();
			// if the chunk does not supply a collision shape it means it has no physical presence and so physics doesn't need to be initialized
			if (cs != null) {
				// initialize a new PhysicsControl with the supplied shape and set mass to zero to set it as a static object
				physicsControl = new RigidBodyControl(cs, 0);
				// attach the new PhysicsControl to the chunk
				terrain.addControl(physicsControl);
			}
		// PhysicsControl already exists so we only need to update it if necessary
		} else {
		}
		return physicsControl;
	}
	public void addGrassLayer(GrassLayer layer) {
		if (layer != null) {
			grassLayers.add(layer);
		}
	}
	// Writes the chunks data to the specified out stream
	@Override
	public void write(OutputStream outputStream) throws IOException {
		super.write(outputStream);
		outputStream.write(ByteUtils.convertToBytes(grassLayers.size()));
		for (GrassLayer layer : grassLayers) {
			outputStream.write(ByteUtils.convertToBytes(layer.getLayerId()));
		}
	}
	// Reads the chunks data back in from the specified in stream
	@Override
	public void read(InputStream inputStream) throws IOException {
		super.read(inputStream);
		grassLayers.clear();
		int layerCount = ByteUtils.readInt(inputStream);
		for (int i = 0; i < layerCount; i++) {
			GrassLayer layer = GameRegistry.getInstance().getGrassLayer(ByteUtils.readInt(inputStream));
			if (layer != null) {
				grassLayers.add(layer);
			}
		}
	}
	// Writes the chunks data to the specified buffer
	@Override
	public void fillBuffer(ByteBuffer buffer) throws IOException {
		super.fillBuffer(buffer);
		buffer.putInt(grassLayers.size());
		for (GrassLayer layer : grassLayers) {
			buffer.putInt(layer.getLayerId());
		}
	}
	// Reads the chunks data from the specified buffer
	@Override
	public void readBuffer(ByteBuffer buffer) throws IOException {
		super.readBuffer(buffer);
		grassLayers.clear();
		int layerCount = buffer.getInt();
		for (int i = 0; i < layerCount; i++) {
			GrassLayer layer = GameRegistry.getInstance().getGrassLayer(buffer.getInt());
			if (layer != null) {
				grassLayers.add(layer);
			}
		}
	}
	private boolean getObjectCollisions(Collidable other, CollisionResults results) {
		for (GameControl object : objects) {
			if (object.collideWith(other, results) > 0) {
				return true;
			}
		}
		return false;
	}
	
	// Generates random data for grass planting including 4 components x and z coordinates the scale and the rotation
	private int generateGrassData(FastRandom rand, ChunkPage page, GrassLayer layer, float[] grassData, int grassCount) {
		RectBounds bounds = page.getBounds();
        //Populating the array of locations (and also getting the total amount of quads).
        float width = bounds.getWidth();
        //Iterator
        int iIt = 0;
		
		/*TempVars temp = TempVars.get();
		temp.bbox.setXExtent(0.25f);
		temp.bbox.setYExtent(1f);
		temp.bbox.setZExtent(0.25f);*/

        for (int i = 0; i < grassCount; i++) {
            float x = rand.unitRandom() * (bounds.getWidth() - 0.01f);
            float z = rand.unitRandom() * (bounds.getWidth() - 0.01f);
			
			// Don't add any grass with a height below sea level
			float y = getHeight(x + bounds.getxMin(), z + bounds.getzMin());
			if (y < 2) {
				continue;
			}
			// Don't add any grass that would overlap with other objects
			/*temp.bbox.setCenter(x + bounds.getxMin(), y, z + bounds.getzMin());
			if (getObjectCollisions(temp.bbox, temp.collisionResults)) {
				continue;
			}*/

            if (rand.unitRandom() < layer.getFrequency()) {
                grassData[iIt++] = x + bounds.getxMin(); // the x world coord
                grassData[iIt++] = z + bounds.getzMin(); // the z world coord
                grassData[iIt++] = rand.unitRandom(); // the grass scale
                //-pi/2 -> pi/2
                grassData[iIt++] = (-0.5f + rand.unitRandom())*3.141593f; // the grass rotation
            }
        }
		//temp.release();
        //The iterator divided by four is the grass-count.
        return iIt/4;
	}
	/**
     * This method creates a grass geometry.
     * 
     * @param layer The grasslayer.
     * @param page The grass page.
     * @param densityMap The densitymap (or null).
     * @param colorMap The colormap (or null).
     * @return A batched grass geometry.
     */
    private Node createGrassGeometry(FastRandom rand, GrassLayer layer, ChunkPage page, Node node) {
        RectBounds bounds = page.getBounds();
        // Calculate the area of the page
        float area = bounds.getWidth()*bounds.getWidth();
            
        // This is the grasscount variable. The initial value is the maximum
        // possible count. It may be reduced by densitymaps, height restrictions
        // and other stuff.
        int grassCount = (int) (area * layer.getDensityMultiplier());
        
        //Each "grass data point" consists of coords (x,z), scale and rotation-angle.
        //That makes 4 data points per patch of grass.
        float[] grassData = new float[grassCount*4];
        
        //The planting algorithm returns the final amount of grass.
        grassCount = generateGrassData(rand, page, layer, grassData, grassCount);
		
		//Grass data iterator
        int gIt = 0;
		
		 //Getting the dimensions
        float minHeight = layer.getMinHeight();
        float maxHeight = layer.getMaxHeight();        
        float minWidth = layer.getMinWidth();
        float maxWidth = layer.getMaxWidth();
        float cX = bounds.getCenter().x;
        float cZ = bounds.getCenter().z;
        
        //No need running this if there's no grass data.
        if (layer.hasBaseMesh()) {
			for(int i = 0; i < grassCount; i++) {
				Geometry geom = new Geometry("Grass" + i, layer.getBaseMesh());
				geom.setMaterial(layer.getMaterial());
				geom.setQueueBucket(Bucket.Transparent);
				geom.setShadowMode(layer.getShadowMode());
				
				 //Position values
				float x = grassData[gIt++];
				float z = grassData[gIt++];
				float size = grassData[gIt++];
				float angle = grassData[gIt++];
				
				float scaleW = minWidth + size*(maxWidth - minWidth);
				float scaleH = minHeight + size*(maxHeight - minHeight);
				float y = getHeight(x, z);
				
				geom.setLocalTranslation(x - cX, y, z - cZ);
				geom.setLocalScale(scaleW, scaleH, scaleW);
				geom.setLocalRotation(new Quaternion().fromAngleNormalAxis(angle, Vector3f.UNIT_Y));	
				node.attachChild(geom);
			}
		}
		return node;
    }
}