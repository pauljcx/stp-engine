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
import com.jme3.scene.Node;
import com.jme3.scene.Mesh;
import com.jme3.scene.SimpleBatchNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Format;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.mesh.IndexBuffer;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Quaternion;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Plane;
import com.jme3.material.Material;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.Image;
import com.jme3.texture.image.ColorSpace;
import com.jme3.util.BufferUtils;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.bounding.BoundingBox;
// Java Dependencies
import java.util.ArrayList;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
// Internal Dependencies
import com.stp.games.jme.controls.GameControl;
import com.stp.games.jme.terrain.layer.TreeLayer;
import com.stp.games.jme.forester.grass.GrassLayer;
import com.stp.games.jme.forester.grass.GrassGeometryGenerator;
import com.stp.games.jme.forester.image.ColorMap;
import com.stp.games.jme.forester.image.DensityMap;
import com.stp.games.jme.forester.paging.ChunkPage;
import com.stp.games.jme.forester.RectBounds;
import com.stp.games.jme.forester.util.FastRandom;
import com.stp.games.jme.forester.paging.DetailLevel;
import com.stp.games.jme.GameRegistry;

public class HeightVolume extends Volume
{
	// Data Layers
	public static int SURFACE = 0;
	public static int OVERLAY = 1;
	public static int TYPE = 2;
	public static int EXPLORED = 3;
	public static int LOW_ADDRESS = 4;
	public static int HIGH_ADDRESS = 5;
	
	private final ArrayList<GrassLayer> layers = new ArrayList<GrassLayer>(2);
	
	private boolean physicsInitialized = false;
	private int terrainSize = 129;
	private int patchSize = 65;
	
	public HeightVolume() {
		super(VolumeType.Height, 129, 1, 129);
		//ChunkControl.PAGE_SIZE = 64;
		//ChunkControl.PAGE_RESOLUTION = 4;
		//ChunkControl.DETAIL_LEVEL.set(0f, 300f, 20f);
		//ChunkControl.DETAIL_LEVELS[0] = new DetailLevel(0f, 150f, 30f);
		//ChunkControl.DETAIL_LEVELS[1] = new DetailLevel(150f, 500f, 30f);
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
		return new HeightChunkControl(location, region);
	}
	// Search through the list of loaded chunks to find the chunk that contains the specified location
	public ChunkControl getChunk(float gx, float gy, float gz) {
		int size = (getChunkSizeX()-1)*2;
		int cx = (gx < 0) ? (int)((gx/size)-0.5f) : (int)((gx/size)+0.5f);
		int cz = (gz < 0) ? (int)((gz/size)-0.5f) : (int)((gz/size)+0.5f);
		return getChunkByLocation(cx, 0, cz);
	}
	@Override
	public void initializePhysics(ChunkControl chunk) {
		RigidBodyControl physicsControl = chunk.getNode().getControl(RigidBodyControl.class);
		// PhysicsControl doesn't exist so we can create a new one
		if (physicsControl == null) {	
			// obtain the collision shape from the chunk
			CollisionShape cs = chunk.getCollisionShape();
			// if the chunk does not supply a collision shape it means it has no physical presence and so physics doesn't need to be initialized
			if (cs != null) {
				// initialize a new PhysicsControl with the supplied shape and set mass to zero to set it as a static object
				physicsControl = new RigidBodyControl(cs, 0);
				// attach the new PhysicsControl to the chunk
				((HeightChunkControl)chunk).getTerrain().addControl(physicsControl);
				//world.addPhysicsObject(physicsControl);
			}
		// PhysicsControl already exists so we only need to update it if necessary
		} else {
		}
	}
	@Override
	public Mesh buildMesh(ChunkControl chunk) {
		HeightChunkControl control = (HeightChunkControl)chunk;
		if (control.getTerrain() == null) {
			float[] heightMap = new float[chunk.getSizeX()*chunk.getSizeZ()];
			ByteBuffer heightValues = BufferUtils.createByteBuffer(heightMap.length*4);
			int index = 0;
			for (int z = 0; z < chunk.getSizeZ(); z++) {
				for (int x = 0; x < chunk.getSizeX(); x++) {
					heightMap[index] = ((chunk.getValue(x, SURFACE, z) & 0xFF)*0.5f)-72.5f;
					heightValues.putFloat(heightMap[index]);
					index++;
				}
			}
			Image txImage = new Image(Image.Format.Depth32F, terrainSize, terrainSize, heightValues, ColorSpace.sRGB);
			Texture2D texture = new Texture2D(txImage);
			texture.setMagFilter(Texture.MagFilter.Bilinear);           
			texture.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
			//texture.setWrap(Texture.WrapMode.Clamp);
			TerrainQuad terrain = new TerrainQuad(control.getName() + "_Terrain", patchSize, terrainSize, heightMap);
			//terrain.recalculateAllNormals();
			//TerrainLodControl lodControl = new TerrainLodControl(terrain, world.getViewControl().getCamera());
			//terrain.addControl(lodControl);
			//control.setTerrain(terrain,  texture);
			
			//System.out.println("Building Mesh: " + chunk + " Start(" + chunk.getStartX() + ", " + chunk.getStartZ() + ") Position(" + chunk.getGlobalX() + ", " + chunk.getGlobalZ() + ")");
			
			// Loads grass geometry to each page for each level of detail
			for (ChunkPage page : chunk.getPages()) {
				Node[] nodes = new Node[2];
				for (int level = 0; level < 2; level++) {
					nodes[level] = new Node("LOD" + level);
					// Only add grass for the first level of detail
					if (level == 0) {
						SimpleBatchNode gNode = new SimpleBatchNode("Grass" + level);
						// Add grass layers
						for (GrassLayer layer : layers) {
							createGrassGeometryAlt(layer, chunk, page, gNode);
						}
						// Batch the grass geometry and add the node to the page nodes
						if (gNode.getQuantity() > 0) {
							System.out.println("Batch Begin: " + chunk + " for " + gNode.getQuantity());
							gNode.batch();
							nodes[level].attachChild(gNode);
						}
					}
					/*SimpleBatchNode tNode = new SimpleBatchNode("Trees" + level);
					// Only enable shadows for the first level of detail
					if (level == 0) {
						tNode.setShadowMode(ShadowMode.Cast);
					} else {
						tNode.setShadowMode(ShadowMode.Off);
					}
					tNode.setQueueBucket(Bucket.Transparent);
					// Add tree layers
					for (TreeLayer layer : treeLayers) {
						if (layer.getLod() == level) {
							TreeLayer.addTreeGeometry(layer, chunk, page, tNode);
						}
					}
					if (tNode.getQuantity() > 0) {
						if (level > 0) {
							tNode.batch();
						}
						nodes[level].attachChild(tNode);
					}*/
				}
				page.setNodes(nodes);
			}
			System.out.println("Mesh Completed: " + chunk);
		}
		return null;
	}
	public GrassLayer addLayer(GrassLayer layer) {
		//GrassLayer layer = new GrassLayer(material, type, assetManager);
		//layer.setBaseMesh(assetManager.loadModel("Models/Fern/fern.j3o"));
		layers.add(layer);
		return layer;
	}
	public ArrayList<GrassLayer> getLayers() {
		return layers;
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
    private static Node createGrassGeometryAlt(GrassLayer layer, ChunkControl chunk, ChunkPage page, Node node) {
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
        grassCount = generateGrassData(chunk, page, layer, grassData, grassCount);
		
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
        if(layer.hasBaseMesh()) {
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
				float y = chunk.getHeight(x, z);
				
				geom.setLocalTranslation(x - cX, y, z - cZ);
				geom.setLocalScale(scaleW, scaleH, scaleW);
				geom.setLocalRotation(new Quaternion().fromAngleNormalAxis(angle, Vector3f.UNIT_Y));	
				node.attachChild(geom);
			}
		}
		return node;
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
    private Geometry createGrassGeometry(GrassLayer layer, ChunkControl chunk, ChunkPage page, ColorMap colorMap) {
        RectBounds bounds = page.getBounds();
        //Calculate the area of the page
        float area = bounds.getWidth()*bounds.getWidth();
            
        //This is the grasscount variable. The initial value is the maximum
        //possible count. It may be reduced by densitymaps, height restrictions
        //and other stuff.
        int grassCount = (int) (area * layer.getDensityMultiplier());
        
        //Each "grass data point" consists of coords (x,z), scale and rotation-angle.
        //That makes 4 data points per patch of grass.
        float[] grassData = new float[grassCount*4];
        
        //The planting algorithm returns the final amount of grass.
        grassCount = generateGrassData(chunk, page, layer, grassData, grassCount);
		
        //No need running this if there's no grass data.
        if(grassCount != 0) {
			Mesh grassMesh = generateGrass(layer, chunk, page, grassData, grassCount, colorMap);
			grassMesh.setStatic();
			grassMesh.updateCounts();
			Geometry geom = new Geometry();
			geom.setMesh(grassMesh);
			geom.setMaterial(layer.getMaterial().clone());
			geom.setQueueBucket(Bucket.Transparent);
			//System.out.println(grassCount + " Grass for Bounds: " + bounds);
			return geom;
        } else {
			//System.out.println("0 Grass for Bounds: " + bounds);
			return null;
		}
    }
	private static int generateGrassData(ChunkControl chunk, ChunkPage page, GrassLayer layer, float[] grassData, int grassCount) {
		RectBounds bounds = page.getBounds();
        //Populating the array of locations (and also getting the total amount of quads).
        FastRandom rand = new FastRandom();
        float width = bounds.getWidth();		
        //Iterator
        int iIt = 0;

        for (int i = 0; i < grassCount; i++) {
            float x = rand.unitRandom() * (bounds.getWidth() - 0.01f);
            float z = rand.unitRandom() * (bounds.getWidth() - 0.01f);
			if (chunk.getHeight(x + bounds.getxMin(), z + bounds.getzMin()) < 2) {
				continue;
			}			

            if (rand.unitRandom() < layer.getFrequency()) {
                grassData[iIt++] = x + bounds.getxMin(); // the x world coord
                grassData[iIt++] = z + bounds.getzMin(); // the z world coord
                grassData[iIt++] = rand.unitRandom(); // the grass scale
                //-pi/2 -> pi/2
                grassData[iIt++] = (-0.5f + rand.unitRandom())*3.141593f; // the grass rotation
            }
        }
        //The iterator divided by four is the grass-count.
        return iIt/4;
	}
	private static int generateTreeData(ChunkControl chunk, ChunkPage page, float[] treeData, int treeCount) {
		RectBounds bounds = page.getBounds();
        //Populating the array of locations (and also getting the total amount of quads).
        FastRandom rand = new FastRandom(chunk.getSeed());	
        //Iterator
        int iIt = 0;

        for (int i = 0; i < treeCount; i++) {
            float x = rand.unitRandom() * (bounds.getWidth() - 0.01f);
            float z = rand.unitRandom() * (bounds.getWidth() - 0.01f);
			if (chunk.getHeight(x + bounds.getxMin(), z + bounds.getzMin()) < 2) {
				continue;
			}			

            if (rand.unitRandom()*5 < 1) {
				treeData[iIt++] = x + bounds.getxMin(); // the x world coord
				treeData[iIt++] = z + bounds.getzMin(); // the z world coord
				treeData[iIt++] = rand.unitRandom(); // the tree scale
				treeData[iIt++] = (-0.5f + rand.unitRandom())*3.141593f; // the tree rotation -pi/2 -> pi/2
            }
        }
        // The iterator divided by four is the tree count
        return iIt/4;
	}
	/**
     * Method for creating a static cross-quad mesh.
     * 
     * @param layer The grass-layer.
     * @param page The page.
     * @param grassData The grassdata array. See the createGrassGeometry method.
     * @param grassCount The initial grass-count. See the createGrassGeometry method.
     * @param colorMap The colormap to use (or null).
     * @return A static cross-quad mesh.
     */
    protected static Mesh generateGrass(GrassLayer layer, ChunkControl chunk, ChunkPage page, float[] grassData, int grassCount, ColorMap colorMap) {
        //The grass mesh
        Mesh mesh = new Mesh();
		mesh.setMode(Mesh.Mode.Triangles);
		/*if (layer.hasBaseMesh()) {
			Mesh baseMesh = layer.getBaseMesh();
			FloatBuffer basePositions = baseMesh.getFloatBuffer(VertexBuffer.Type.Position);
			FloatBuffer baseNormals = baseMesh.getFloatBuffer(VertexBuffer.Type.Normal);
			FloatBuffer baseTxCoords = baseMesh.getFloatBuffer(VertexBuffer.Type.TexCoord);
			IndexBuffer baseIndices = baseMesh.getIndexBuffer();
			FloatBuffer vertices = BufferUtils.createFloatBuffer(baseMesh.getVertexCount()*grassCount*3);
			FloatBuffer normals = BufferUtils.createFloatBuffer(baseMesh.getVertexCount()*grassCount*3);
			FloatBuffer txCoords = BufferUtils.createFloatBuffer(baseMesh.getVertexCount()*grassCount*2);
			IntBuffer indices = BufferUtils.createIntBuffer(baseIndices.size()*grassCount);
			int gIt = 0;
			for (int i = 0; i < grassCount; i++) {
				//Position values
				float x = grassData[gIt++];
				float z = grassData[gIt++];
				float y = chunk.getHeight(x, z);
				float size = grassData[gIt++];
				float angle = grassData[gIt++];
				basePositions.rewind();
				for (int v = 0; v < baseMesh.getVertexCount(); v++) {
					vertices.put(basePositions.get()+x);
					vertices.put(basePositions.get()+y);
					vertices.put(basePositions.get()+z);
				}
				baseNormals.rewind();
				normals.put(baseNormals);
				baseTxCoords.rewind();
				txCoords.put(baseTxCoords);
				for (int j = 0; j < baseIndices.size(); j++) {
					int offset = i*baseIndices.size();
					indices.put(baseIndices.get(j)+offset);
				}
			}
			vertices.rewind();
			normals.rewind();
			txCoords.rewind();
			indices.rewind();
			mesh.setBuffer(Type.Position, 3, vertices);
			mesh.setBuffer(Type.Normal, 3, normals);
			mesh.setBuffer(Type.TexCoord, 2, txCoords);
			mesh.setBuffer(Type.Index, 3, indices);
        
			BoundingBox box = new BoundingBox();
			mesh.setBound(box);
			mesh.updateBound();
			return mesh;
		}       */ 
        
        // ***************** Setting up the mesh buffers. *****************
        
        //Each grass has eight positions, each position is 3 floats.
        float[] positions = new float[grassCount*24];
        //Each grass has got eight texture coordinates, each coord is 2 floats.
        float[] texCoords = new float[grassCount*16];
        //This is the angle of the quad.
        float[] normals = new float[grassCount*16];
        
        //Colormap stuff
        float[] colors = null;
        ColorRGBA cols[] = null;
        ColorRGBA color = null;
        
        boolean useColorMap = false;
        if(colorMap != null){
            useColorMap = true;
            //Each grass has got four vertices, each vertice has one color, each
            //color is 4 floats.
            colors = new float[grassCount*32];
            cols = colorMap.getColorsUnfiltered(page);
        }
        
        //Slim the mesh down a little.
        Format form = Format.UnsignedShort;
        if (grassCount*4 > 65535) {
            form = Format.UnsignedInt;
        } else if (grassCount*4 > 255){ 
            form = Format.UnsignedShort;
        } else {
            form = Format.UnsignedByte;
        }
        
        Buffer data = VertexBuffer.createBuffer(form, 1, grassCount*12);           
        VertexBuffer iBuf = new VertexBuffer(VertexBuffer.Type.Index);
        iBuf.setupData(VertexBuffer.Usage.Dynamic, 1, form, data);
        mesh.setBuffer(iBuf);
        
        //Getting the dimensions
        float minHeight = layer.getMinHeight();
        float maxHeight = layer.getMaxHeight();
        
        float minWidth = layer.getMinWidth();
        float maxWidth = layer.getMaxWidth();
        
        //A bunch of array iterators.
        //Grass data iterator
        int gIt = 0;
        //position, texcoord, angle and color iterators
        int pIt = 0;
        int tIt = 0;
        int nIt = 0;
        int cIt = 0;
        
        RectBounds bounds = page.getBounds();
        float cX = bounds.getCenter().x;
        float cZ = bounds.getCenter().z;
        
        int pw = (int) bounds.getWidth();
        
        float xOffset = -page.getCenterPoint().x + pw*(page.getX() + 0.5f);
        float zOffset = -page.getCenterPoint().z + pw*(page.getZ() + 0.5f);
        
        //Bounding box stuff.
//        float yMin = 0, yMax = 0;
        
        //Generating quads
        for(int i = 0; i < grassCount; i++)
        {
            //Position values
            float x = grassData[gIt++];
            float z = grassData[gIt++];
            float size = grassData[gIt++];
            float angle = grassData[gIt++];
            
            float halfScaleX = (minWidth + size*(maxWidth - minWidth))*0.5f;
            float scaleY = minHeight + size*(maxHeight - minHeight);
            
            float xAng = (float)(Math.cos(angle));
            float zAng = (float)(Math.sin(angle));
            
            float xTrans = xAng * halfScaleX;
            float zTrans = zAng * halfScaleX;
            
            float x1 = x - xTrans, z1 = z - zTrans;
            float x2 = x + xTrans, z2 = z + zTrans;
            float x3 = x + zTrans, z3 = z - xTrans;
            float x4 = x - zTrans, z4 = z + xTrans;
            
            float y1 = chunk.getHeight(x1, z1); 
            float y2 = chunk.getHeight(x2, z2);
            float y3 = chunk.getHeight(x3, z3);
            float y4 = chunk.getHeight(x4, z4);
            
            float y1h = y1 + scaleY;
            float y2h = y2 + scaleY;
            float y3h = y3 + scaleY;
            float y4h = y4 + scaleY;
            
            //Bounding box stuff.
//            float ym1 = (y1 <= y2) ? y1 : y2;
//            float ym2 = (y3 <= y4) ? y3 : y4;
//            float ym = (ym1 <= ym2) ? ym1 : ym2;
//            if(ym < yMin ){
//                yMin = ym;
//            }
//            
//            float yM1 = (y1h >= y2h) ? y1h : y2h;
//            float yM2 = (y3h >= y4h) ? y3h : y4h;
//            float yM = (yM1 >= yM2) ? yM1 : yM2;
//            if(yM > yMax){
//                yMax = yM;
//            }
            
            //************Generate the first quad**************
            
            positions[pIt++] = x1 - cX;                         //pos
            positions[pIt++] = y1h;
            positions[pIt++] = z1 - cZ;
            
            normals[nIt++] = zAng;    normals[nIt++] = -xAng;   //xz normal
            texCoords[tIt++] = 0.f;    texCoords[tIt++]=1.f;    //uv
            
            positions[pIt++] = x2 - cX;                         //pos
            positions[pIt++] = y2h;
            positions[pIt++] = z2 - cZ;
            
            normals[nIt++] = zAng;    normals[nIt++] = -xAng;   //xz normal
            texCoords[tIt++] = 1.f;   texCoords[tIt++]=1.f;     //uv
            
            positions[pIt++] = x1 - cX;                         //pos
            positions[pIt++] = y1; 
            positions[pIt++] = z1 - cZ; 
            
            normals[nIt++] = zAng;    normals[nIt++] = -xAng;   //xz normal
            texCoords[tIt++] = 0.f;  texCoords[tIt++]=0.f;      //uv
            
            positions[pIt++] = x2 - cX;                         //pos
            positions[pIt++] = y2;
            positions[pIt++] = z2 - cZ;
            
            normals[nIt++] = zAng;    normals[nIt++] = -xAng;   //xz normal
            texCoords[tIt++] = 1.f;  texCoords[tIt++]=0.f;      //uv
            
            //************Generate the second quad**************
            
            positions[pIt++] = x3 - cX;                         //pos
            positions[pIt++] = y3h;
            positions[pIt++] = z3 - cZ;
            
            normals[nIt++] = xAng;    normals[nIt++] = zAng;    //xz normal
            texCoords[tIt++] = 0.f;    texCoords[tIt++]=1.f;    //uv
            
            positions[pIt++] = x4 - cX;                         //pos
            positions[pIt++] = y4h;
            positions[pIt++] = z4 - cZ;
            
            normals[nIt++] = xAng;    normals[nIt++] = zAng;    //xz normal
            texCoords[tIt++] = 1.f;   texCoords[tIt++]=1.f;     //uv
            
            positions[pIt++] = x3 - cX;                         //pos
            positions[pIt++] = y3; 
            positions[pIt++] = z3 - cZ;
            
            normals[nIt++] = xAng;    normals[nIt++] = zAng;    //xz normal
            texCoords[tIt++] = 0.f;  texCoords[tIt++]=0.f;      //uv
            
            positions[pIt++] = x4 - cX;                         //pos
            positions[pIt++] = y4;
            positions[pIt++] = z4 - cZ;
            
            normals[nIt++] = xAng;    normals[nIt++] = zAng;    //xz normal
            texCoords[tIt++] = 1.f;  texCoords[tIt++]=0.f;      //uv

            if(useColorMap){
                //Get the map coordinates for x and z.
                int xIdx = (int) (x + xOffset);
                int zIdx = (int) (z + zOffset);
                
                color = cols[xIdx + pw*zIdx];
                
                colors[cIt++] = color.r;
                colors[cIt++] = color.g;
                colors[cIt++] = color.b;
                colors[cIt++] = 1.f;
                
                colors[cIt++] = color.r;
                colors[cIt++] = color.g;
                colors[cIt++] = color.b;
                colors[cIt++] = 1.f;
                
                colors[cIt++] = color.r;
                colors[cIt++] = color.g;
                colors[cIt++] = color.b;
                colors[cIt++] = 1.f;
                
                colors[cIt++] = color.r;
                colors[cIt++] = color.g;
                colors[cIt++] = color.b;
                colors[cIt++] = 1.f;
                
                colors[cIt++] = color.r;
                colors[cIt++] = color.g;
                colors[cIt++] = color.b;
                colors[cIt++] = 1.f;
                
                colors[cIt++] = color.r;
                colors[cIt++] = color.g;
                colors[cIt++] = color.b;
                colors[cIt++] = 1.f;
                
                colors[cIt++] = color.r;
                colors[cIt++] = color.g;
                colors[cIt++] = color.b;
                colors[cIt++] = 1.f;
                
                colors[cIt++] = color.r;
                colors[cIt++] = color.g;
                colors[cIt++] = color.b;
                colors[cIt++] = 1.f;
            }
        }
        
        //Indices
        int iIt = 0;
        
        int offset = 0;
        IndexBuffer iB = mesh.getIndexBuffer();
        for(int i = 0; i < grassCount; i++){
            offset = i*8;
            iB.put(iIt++, 0 + offset);
            iB.put(iIt++, 2 + offset);
            iB.put(iIt++, 1 + offset);
                
            iB.put(iIt++, 1 + offset);
            iB.put(iIt++, 2 + offset);
            iB.put(iIt++, 3 + offset);
            
            iB.put(iIt++, 4 + offset);
            iB.put(iIt++, 6 + offset);
            iB.put(iIt++, 5 + offset);
                
            iB.put(iIt++, 5 + offset);
            iB.put(iIt++, 6 + offset);
            iB.put(iIt++, 7 + offset);
        }
        
        //********************* Finalizing the mesh ***********************
        
        // Setting buffers
        mesh.setBuffer(Type.Position, 3, positions);
        mesh.setBuffer(Type.TexCoord, 2, texCoords);
        mesh.setBuffer(Type.TexCoord2,2, normals);
        
        if(useColorMap){
            mesh.setBuffer(Type.Color,4,colors);
        }
        
        BoundingBox box = new BoundingBox();
        
//        Vector3f boxCenter = Vector3f.ZERO;
//        boxCenter.y = (yMax + yMin)*0.5f;
//        box.setCenter(boxCenter);
//        float extent = (bounds.getWidth() + maxWidth)*0.5f;
//        box.setXExtent(extent);
//        box.setYExtent((yMax - yMin)*0.5f);
//        box.setZExtent(extent);
        
        mesh.setBound(box);
        mesh.updateBound();
        return mesh;
    }
	// Creates the voxel shape geometry using marching cubes algorithm
	public static Mesh generateVoxelMesh(VoxelShape source) {
		return generateVoxelMesh(source, new Vector3f(-source.getWidth()/2, -source.getHeight()/2, -source.getLength()/2), new Vector3f(1f, 1f, 1f));
	}
	// Creates the voxel shape geometry using marching cubes algorithm
	public static Mesh generateVoxelMesh(VoxelShape source, Vector3f loc, Vector3f lodSize) {
		MeshBuilder meshBuilder = new MeshBuilder();
        for (float i = -1f; i < source.getWidth(); i += lodSize.x) {
            for (float j = -1f; j < source.getHeight(); j += lodSize.y) {
                for (float k = -1f; k < source.getLength(); k += lodSize.z) {
				
                    float[] values = {
						source.getVoxel(i, j, k),
                        source.getVoxel(i + lodSize.x, j, k),
                        source.getVoxel(i + lodSize.x, j, k + lodSize.z),
                        source.getVoxel(i, j, k + lodSize.z),
                        source.getVoxel(i, j + lodSize.y, k),
                        source.getVoxel(i + lodSize.x, j + lodSize.y, k),
                        source.getVoxel(i + lodSize.x, j + lodSize.y, k + lodSize.z),
                        source.getVoxel(i, j + lodSize.y, k + lodSize.z)
					};

                    Vector3f[] locations = {
                        new Vector3f(loc.x + i + 0, loc.y + j + 0, loc.z + k + 0),
                        new Vector3f(loc.x + i + lodSize.x, loc.y  + j + 0, loc.z + k + 0),
                        new Vector3f(loc.x + i + lodSize.x, loc.y  + j + 0, loc.z + k + lodSize.z),
                        new Vector3f(loc.x + i + 0, loc.y  + j + 0, loc.z + k + lodSize.z),
                        new Vector3f(loc.x + i + 0, loc.y  + j + lodSize.y, loc.z + k + 0),
                        new Vector3f(loc.x + i + lodSize.x, loc.y  + j + lodSize.y, loc.z + k + 0),
                        new Vector3f(loc.x + i + lodSize.x, loc.y  + j + lodSize.y, loc.z + k + lodSize.z),
                        new Vector3f(loc.x + i + 0, loc.y  + j + lodSize.y, loc.z + k + lodSize.z)
                    };
					IsoSurface.addMarchingCubesTriangles(source, locations, values, null, meshBuilder);
                }
            }
        }
		if (meshBuilder.countVertices() > 0) {
			return meshBuilder.generateMesh();
		}
		return null;
	}
}