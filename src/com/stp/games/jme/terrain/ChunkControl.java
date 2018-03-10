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
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.export.Savable;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.binary.ByteUtils;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.material.Material;
import com.jme3.scene.Spatial;
import com.jme3.scene.Node;
import com.jme3.scene.Mesh;
import com.jme3.scene.Geometry;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.collision.shapes.CollisionShape;
// Java Dependencies
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.stp.games.jme.GameRegistry;
import com.stp.games.jme.controls.GameControl;
import com.stp.games.jme.controls.Schematic;
import com.stp.games.jme.forester.paging.ChunkPage;
import com.stp.games.jme.forester.paging.DetailLevel;

public class ChunkControl extends AbstractControl implements Savable,Comparable<ChunkControl> {
	private static final DetailLevel[] DEFAULT_LEVELS = new DetailLevel[] { new DetailLevel(0f, 150f, 30f) };

	protected final ArrayList<GameControl> objects = new ArrayList<GameControl>();	
	protected final ArrayList<ChunkPage> pages = new ArrayList<ChunkPage>();
	//protected final ConcurrentLinkedQueue<Integer> removeList = new ConcurrentLinkedQueue<Integer>();
	protected final Vector3f location = new Vector3f();
	protected final Vector3f position = new Vector3f();
	protected final Vector3f start = new Vector3f();
	protected final Node node = new Node();
	protected final Node objNode = new Node();
	protected byte[][][] data;
	
	protected Region region;
	protected Geometry shape;
	protected boolean active;
	protected boolean restricted;
	protected boolean empty;
	protected boolean visible;
	protected boolean explored;
	protected volatile boolean loaded;
	protected volatile boolean updated;
	protected volatile boolean needsMeshUpdate;
	protected volatile boolean needsObjectUpdate;
	
	protected int nextAddress;
	protected float distance;
	protected int lod;

	public ChunkControl() {
		this (0, 0, 0);
	}
	public ChunkControl(Vector3f location) {
		this ((int)location.x, (int)location.y, (int)location.z);
	}
	public ChunkControl(Vector3f location, Region region) {
		this ((int)location.x, (int)location.y, (int)location.z);
		setRegion(region);
	}
	public ChunkControl(int x, int y, int z) {
		this.location.set(x, y, z);
		this.active = false;
		this.loaded = false;
		this.visible = false;
		this.empty = true;
		this.updated = false;
		this.explored = true;
		this.needsMeshUpdate = false;
		node.setName("ChunkNode: " + getName());
		objNode.setName("ObjectNode: " + getName());
		objNode.setShadowMode(ShadowMode.Cast);
		setObjectsVisible(true);
		
		this.data = new byte[0][0][0];
	}
	// Initialize the dimensions for this chunk, must be called before any data updates
	public void setChunkDimensions(int width, int height, int length) {
		this.data = new byte[width][height][length];
		this.position.set(location.x*width, location.y*height, location.z*length);
		this.start.set(position);
		start.subtractLocal(width/2, height/2, length/2);
	}
	// Create pages to store batched geometry for this chunk and control level of detail
	protected void createPages() {
		for (int j = 0; j < getPageResolution(); j++) {
			for (int i = 0; i < getPageResolution(); i++) {
				Vector3f center = new Vector3f((i + 0.5f) * getPageSize() + getStartX(), 0, (j + 0.5f) * getPageSize() + getStartZ());
				pages.add(new ChunkPage(i, j, center, node, getPageSize()));
            }
        }
	}
	// Retrieves the size of each page create for this chunk
	public int getPageSize() {
		return 16;
	}
	// Retrieves the resolution of pages to create for this chunk, number of rows and columns
	public int getPageResolution() {
		return 4;
	}
	public DetailLevel[] getPageDetailLevels() {
		return DEFAULT_LEVELS;
	}
	public ArrayList<ChunkPage> getPages() {
		return pages;
	}
	public Region getRegion() {
		return region;
	}
	public void setRegion(Region region) {
		this.region = region;
	}
	public int getChunkType() {
		return 0; 
	}
	public long getSeed() {
		return (region != null) ? region.getSeed() : ((long)getX() << 32) | (getZ() & 0xFFFFFFFFL);
	}	
	public float getTreeDensity() {
		return 1f;
	}
	public float getFoliageDensity() {
		return 1f;
	}
	public long getVersion() {
		return 0L;
	}
	@Override
    public void setSpatial(Spatial newSpatial) {
		if (newSpatial != spatial) {
			setVisible(false);
		}
        super.setSpatial(newSpatial);
    }
	// To be overriden in subclasses method to build the chunks geometry
	public void buildMesh(Volume volume) {
	}
	// Updates the geometry of this chunk
	public void updateMesh(Mesh mesh, Material material) {
		if (shape == null) {
			shape = new Geometry("Chunk_" + getName());
			shape.setMaterial(material);
			shape.setShadowMode(ShadowMode.Receive);
		}
		if (mesh != null) {
			shape.setMesh(mesh);
			node.attachChild(shape);
		} else {
			node.detachChild(shape);
		}
		updated = true;
	}
	// Checks whether the chunk has been updated
	public void setMeshUpdateNeeded(boolean needsMeshUpdate) {
		this.needsMeshUpdate = needsMeshUpdate;
	}
	// Checks whether the chunk has been updated
	public void setObjectUpdateNeeded(boolean needsObjectUpdate) {
		this.needsObjectUpdate = needsObjectUpdate;
	}
	// Checks whether the chunk has been updated
	public boolean isMeshUpdateNeeded() {
		return needsMeshUpdate;
	}
	// Checks whether the chunk has been updated
	public boolean isObjectUpdateNeeded() {
		return needsObjectUpdate;
	}
	// Gets the mesh for this chunk
	public Mesh getMesh() {
		return (shape != null) ? shape.getMesh() : null;
	}
	// Checks for collisions with the chunks shape
	public int collideWith(Collidable other, CollisionResults results){
		if (shape != null) {
			return shape.collideWith(other, results);
		}
		return 0;
    }
	// Only attach this chunk to the scene graph it isn't already visible and it's been explored
	@Override
    protected void controlUpdate(float tpf) {
		if (updated && !visible && !restricted && explored) {
			setVisible(true);
		}
		if (needsObjectUpdate) {
			for (GameControl object : objects) {
				switch (object.getSceneState()) {
					case Add: addUpdate(object); break;
					case Remove: removeUpdate(object); break;
					default: break;
				}
			}
			needsObjectUpdate = false;
		}
	}
	// Internal method to add objects only to be called inbetween frames in the update phase
	private void addUpdate(GameControl object) {
		if (!object.isActive() && object.hasSpatial()) {
			objNode.attachChild(object.getSpatial());
			object.setActive(true);
			object.setSceneState(GameControl.SceneState.None);
			//System.out.println("Adding object to scene: " + object + " at " + object.getWorldTranslation());
		}
	}
	// Internal method to remove objects only to be called inbetween frames in the update phase
	private void removeUpdate(GameControl object) {
		if (object.isActive() && object.hasSpatial()) {
			objNode.detachChild(object.getSpatial());
			object.setActive(false);
			object.setSceneState(GameControl.SceneState.None);
		}
	}

    @Override
    protected void controlRender(RenderManager renderManager, ViewPort viewPort) {}

	// Called with the current camera position to update the level of detail for this control and it's pages
	public void updateView(Vector3f cameraPosition) {
		this.distance = cameraPosition.distance(position);
		updatePages(cameraPosition);
	}
	// Called to update the pages based on the new camera position
    protected void updatePages(Vector3f cameraPosition) {
		for (ChunkPage page : pages) {
			// If the pagingNode is hidden, don't make any visibility calculations.
			/*if (!visible) {
				return;
			}*/
			// Fast fail if the page has no nodes
			if (page.getNodes() == null) {
				continue;
			}
			DetailLevel[] dLevels = getPageDetailLevels();
			for (int level = 0; level < dLevels.length; level++) {
				// Fast fail if the page node has no children
				if (page.getNode(level).getChildren().isEmpty()){
					continue;
				}
				//Get the distance to the page center.
				float dx = page.getCenterPoint().x - cameraPosition.x;
				float dz = page.getCenterPoint().z - cameraPosition.z;
				float dist = (float) Math.sqrt(dx * dx + dz * dz);
				
				System.out.println("Page: " + page + " | " + page.getCenterPoint() + " | " + dist);
				
				boolean vis = false;
				//Standard visibility check.
				if (dist < dLevels[level].getFarDist() && dist >= dLevels[level].getNearDist()) {
					vis = true;
				}
				if (isFadeEnabled()) {
					//This is the diameter of the (smallest) circle enclosing the page and all its geometry in the xz plane.
					float halfPageDiag = page.getOverlap()*1.414214f;
					float pageMin = dist - halfPageDiag;
					float pageMax = dist + halfPageDiag;
					//Fading visibility check.
					if (pageMax >= dLevels[level].getNearDist() && pageMin < dLevels[level].getFarTransDist()) {
						if (dLevels[level].isFadeEnabled() && pageMax >= dLevels[level].getFarDist()) {
							vis = true;
							page.setFade(true, dLevels[level].getFarDist(), dLevels[level].getFarTransDist(), level);
						} else {
							page.setFade(false, 0, 0, level);
						}
					}
				}
				page.setVisible(vis, level);
			}
		}
	}

    @Override
    public Control cloneForSpatial(Spatial spatial)	{
        throw new UnsupportedOperationException("Not supported yet.");
    }
	public void setVisible(boolean visible) {
		this.visible = visible;
		/*if (spatial instanceof Node) {
			if (visible) {
				((Node)spatial).attachChild(node);
				//node.setLocalTranslation(start);
				System.out.println("Set Visible: " + this + " | " + node.getLocalTranslation());
			} else {
				((Node)spatial).detachChild(node);
			}
		}*/
	}
	// Add or remove the object node from the scene graph
	protected void setObjectsVisible(boolean objectsVisible) {
		if (objectsVisible) {
			node.attachChild(objNode);
		} else {
			node.detachChild(objNode);
		}
	}
	// Gets the node that holds the geometry data for this chunk
	public Node getNode() {
        return node;
    }
	// Returns the collision shape that will be used for this chunk, the default is null indicating the chunk has no physical presence
	public CollisionShape getCollisionShape() {
		return null;
	}
	// Initialize the PhysicsControl for this chunk and return it to be added to the PhysicsSpace
	public PhysicsControl initializePhysics() {
		return null;
	}
	// Provide camera access for chunks that will have there own level of detail controls
	public void addLodControl(Camera camera) {
	}
	// Gets the objects that are in this chunk
	public Collection<GameControl> getObjects() {
		return objects;
	}
	// Add an object to this chunk at the specified local coordinates
	public GameControl addObject(GameControl object, Vector3f coordinates) {
		return addObject(object, coordinates.x, coordinates.y, coordinates.z);
	}
	// Adds an object to this chunk
	public GameControl addObject(GameControl object, float x, float y, float z) {
		if (object != null) {
			object.setActive(false);
			object.setSceneState(GameControl.SceneState.Add);
			object.setWorldTranslation(x, y, z);
			objects.add(object);
			needsObjectUpdate = true;
		}
		return object;
	}
	// Removes and object from this chunk
	public GameControl removeObject(GameControl object) {
		int index = objects.indexOf(object);
		if (index >= 0) {
			objects.get(index).setSceneState(GameControl.SceneState.Remove);
			needsObjectUpdate = true;
			return objects.get(index);
		}
		return null;
	}
	public GameControl getObject(Vector3f coordinates) {
		return this.getObject(coordinates.x, coordinates.y, coordinates.z);
	}
	public GameControl getObject(float x, float y, float z) {
		for (GameControl object : objects) {
			if (object.matchLocation(x, y, z)) {
				return object;
			}
		}
		return null;
	}
	// Returns the local coordinates of this chunk in relation to it's neighbor chunks
	public Vector3f getLocation()	{
		return location;
	}
	// Returns the global coordinates of this chunk (location multiplied by chunk dimensions)
	public Vector3f getGlobalLocation() {
		return position;
	}
	// Returns the coordinates of the lower left corner of the chunk
	public Vector3f getStartLocation() {
		return start;
	}
	// Gets the level of detail that should be used for rendering this chunk
	public int getLod() {
		return lod;
	}
	// Sets the level of detail that should be used for rendering this chunk
	public void setLod(int lod) {
		this.lod = lod;
	}
	// Gets the distance away from the player / camera
	public float getDistance() {
		return distance;
	}
	// Sets the distance away from the given object coordinates and adjusts the lod
	public void setDistance(Vector3f coords) {
		//this.distance = coords.distance(location);
		/*if (distance <= 2) {
			this.lod = 0;
		} else if ( distance <= 4) {
			this.lod = 1;
		} else {
			this.lod = 2;
		}*/
	}
	public float getHeight(float x, float z) {
		return 0f;
	}
	// Gets the local data location given the specified global coordinates
	public Vector3f getLocalLocation(int x, int y, int z) {
		return new Vector3f(x - start.x, y - start.y, z -start.z);
	}
	// Gets the local data location given the specified global coordinates
	public Vector3f getLocalLocation(Vector3f globalLocation) {
		return globalLocation.subtract(start);
	}
	// Returns the x coordinate of the chunk location as an Integer
	public int getX() {
		return (int)location.getX();
	}
	// Returns the y coordinate of the chunk location as an Integer
	public int getY() {
		return (int)location.getY();
	}
	// Returns the z coordinate of the chunk location as an Integer
	public int getZ() {
		return (int)location.getZ();
	}
	public int getStartX() {
		return (int)start.x;
	}
	public int getStartY() {
		return (int)start.y;
	}
	public int getStartZ() {
		return (int)start.z;
	}
	// Returns the x coordinate of the starting location for this chunk
	public float getGlobalX() {
		return start.x;
	}
	// Returns the y coordinate of the starting location for this chunk
	public float getGlobalY() {
		return start.y;
	}
	// Returns the z coordinate of the starting location for this chunk
	public float getGlobalZ() {
		return start.z;
	}
	// Sets the local location of the chunk in relation to other chunks (converted to world space by multiplying by the chunk dimensions
	public Vector3f setLocation(int x, int y, int z) {
		//node.setLocalTranslation(new Vector3f(x*CHUNK_SIZE_X, y*CHUNK_SIZE_Y, z*CHUNK_SIZE_Z));
		return location.set(x, y, z);
	}
	// Gets the chunks size on the x axis
	public int getSizeX() {
		return data.length;
	}
	// Gets the chunks size on the y axis
	public int getSizeY() {
		return data[0].length;
	}
	// Gets the chunks size on the z axis
	public int getSizeZ() {
		return data[0][0].length;
	}
	// Gets the total number of data bytes this chunk holds
	public int getDataCount() {
		return getSizeX() * getSizeY() * getSizeZ();
	}
	// Checks whether the chunk is currently active
	public boolean isActive() {
		return active;
	}
	// Sets if the chunk is active or not
	public void setActive(boolean active) {
		this.active = active;
		setRestricted(!active);
	}
	// Check whether the chunk is restricted (whether mesh updates are allowed)
	public boolean isRestricted() {
		return restricted;
	}
	// Sets if the chunk is restricted or not (whether mesh updates are allowed)
	public void setRestricted(boolean restricted) {
		this.restricted = restricted;
	}
	// Checks whether the chunks mesh is up to date
	public boolean isUpdated() {
		return updated;
	}
	// Checks whether the chunk has been loaded yet
	public boolean isLoaded() {
		return loaded;
	}
	// Sets whether this chunk has been loaded already
	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}
	// Checks whether the chunk is empty
	public boolean isEmpty() {
		return empty;
	}
	public boolean isFadeEnabled() {
		return true;
	}
	// Gets a string that uniquely identifies this chunk based on its location
	public String getName() {
		return getX() + "_" + getY() + "_" + getZ();
	}
	// Gets a string with chunk information used for debuging purposes
	public String toString() {
		return "Chunk: location=[" + getX() + "," + getY() + "," + getZ()  + "]";
	}
	// Checks whether two chunks are equal which is base on their locations
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ChunkControl) {
			return location.equals(((ChunkControl)obj).getLocation());
		}
		return false;
	}
	@Override
	public int hashCode() {
		return location.hashCode();
	}
	// Compares two chunks for ordering, the order is based on their distance values
	public int compareTo(ChunkControl chunk) {
		if (location.equals(chunk.getLocation())) {
			return 0;
		}
		return (chunk.getDistance() > distance) ? 1 : -1;
	}
	// Checks to see if the specified coordinates match this chunks location
	public boolean matchesLocation(float x, float y, float z) {
		return (location.x == x) && (location.y == y) && (location.z == z);
	}
	// Internal function to check if location is in this chunk
	protected boolean isValidLocation(float x, float y, float z) {
		int px = (int)x;
		int py = (int)y;
		int pz = (int)z;
		return (px >= 0) && (px < data.length)
		&& (py >= 0) && (py < data[0].length)
		&& (pz >= 0) && (pz < data[0][0].length);
    }
	// Gets the tile value located at the specified global location
	public byte getGlobalValue(float gx, float gy, float gz) {
		return getValue(gx - start.x, gy - start.y, gz - start.z);
	}
	// Gets the voxel value at the specified location
	public byte getValue(Vector3f location) {
		return getValue(location.x, location.y, location.z);
	}
	// Gets the voxel value at the specified location
	public byte getValue(float x, float y, float z) {
		if (isValidLocation(x, y, z)) {
			return data[(int)x][(int)y][(int)z];
		}
		return 0;
	}
	// Sets the voxel value at the specified global location
	public void setGlobalValue(float gx, float gy, float gz, byte value) {
		setValue(gx - start.x, gy - start.y, gz - start.z, value);
	}
	// Sets the voxel value at the specified local location
	public void setValue(Vector3f location, byte value) {
		setValue(location.x, location.y, location.z, value);
	}
	// Sets the voxel value at the specified local location
	public void setValue(float x, float y, float z, byte value) {
		if (isValidLocation(x, y, z)) {
			data[(int)x][(int)y][(int)z] = value;
		}
		if (empty) {
			empty = (value == 0);
		}
		needsMeshUpdate = true;
		updated = false;
	}	
	// Sets all the data in the specified range to the given value
	public void setValues(Vector3f start, Vector3f length, byte value)	{
		for (int x = (int)start.getX(); x < length.getX(); x++) {
			for (int y = (int)start.getY(); y < length.getY(); y++) {
				for (int z = (int)start.getZ(); z < length.getZ(); z++) {
					if (isValidLocation(x, y, z)) {
						data[x][y][z] = value;
					}
				}
			}
		}
		if (empty) {
			empty = (value == 0);
		}
		needsMeshUpdate = true;
		updated = false;
	}
	public void setShortValue(float x, float y, float z, short value) {
		setValue(x, y, z, (byte)value);
		setValue(x, y+1, z, (byte)(value >> 8));
	}
	public short getShortValue(float x, float y, float z) {
		return (short)((getValue(x, y, z) & 0xFF) | (getValue(x, y+1, z) << 8));
	}
	public int getUnsignedShortValue(float x, float y, float z) {
		return getShortValue(x, y, z) & 0xffff;
	}
	
	/* I/O Functions */
	
	// Copies all the data information from one chunk to another
	public void copyDataFrom(ChunkControl chunk) {
		try {
			ByteBuffer data = ByteBuffer.allocate(chunk.getBufferSize());
			chunk.fillBuffer(data);
			data.flip();
			readBuffer(data);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	// Gets the amount of memory in bytes required to store this chunks data
	public int getBufferSize() {
		return 25 + ((empty) ? 0 : getDataCount());
	}
	// Writes the chunks data to the specified out stream
	public void write(OutputStream outputStream) throws IOException {
		outputStream.write(ByteUtils.convertToBytes(location.getX()));
		outputStream.write(ByteUtils.convertToBytes(location.getY()));
		outputStream.write(ByteUtils.convertToBytes(location.getZ()));
		outputStream.write(ByteUtils.convertToBytes(getSizeX()));
		outputStream.write(ByteUtils.convertToBytes(getSizeY()));
		outputStream.write(ByteUtils.convertToBytes(getSizeZ()));
		outputStream.write(ByteUtils.convertToBytes(empty));
		if (!empty) {
			for (int x = 0; x < data.length; x++) {
				for (int y = 0; y < data[0].length; y++) {
					outputStream.write(data[x][y]);
				}
			}
		}
		outputStream.write(ByteUtils.convertToBytes(objects.size()));
		for (GameControl object : objects) {
			long id = object.getId();
			if (id != 0) {
				outputStream.write(ByteUtils.convertToBytes(id));
				object.writeFields(outputStream);
			}
		}
    }
	// Reads the chunks data back in from the specified in stream
	public void read(InputStream inputStream) throws IOException {
		location.setX(ByteUtils.readFloat(inputStream));
		location.setY(ByteUtils.readFloat(inputStream));
		location.setZ(ByteUtils.readFloat(inputStream));
		setChunkDimensions(ByteUtils.readInt(inputStream), ByteUtils.readInt(inputStream), ByteUtils.readInt(inputStream));
		this.empty = ByteUtils.readBoolean(inputStream);
		if (!empty) {
			for (int x = 0; x < data.length; x++) {
				for (int y = 0; y < data[0].length; y++) {
					for (int z = 0; z < data[0][0].length; z++) {
						data[x][y][z] = (byte)inputStream.read();
					}
				}
			}
			needsMeshUpdate = true;
			updated = false;
		}
		objects.clear();
		int count = ByteUtils.readInt(inputStream);
		for (int i = 0; i < count; i++) {
			GameControl object = GameRegistry.getInstance().createObject(ByteUtils.readLong(inputStream));
			if (object != null) {
				object.readFields(inputStream);
				addObject(object, object.getStoredLocation());
			}
		}
	}
	// Writes the chunks data to the specified buffer
	public void fillBuffer(ByteBuffer buffer) throws IOException {
		buffer.putFloat(location.getX());
		buffer.putFloat(location.getY());
		buffer.putFloat(location.getZ());
		buffer.putInt(getSizeX());
		buffer.putInt(getSizeY());
		buffer.putInt(getSizeZ());
		buffer.put(empty ? (byte)1 : (byte)0);
		if (!empty) {
			for (int x = 0; x < data.length; x++) {
				for (int y = 0; y < data[0].length; y++) {
					buffer.put(data[x][y], 0, data[0][0].length);
				}
			}
		}
		buffer.putInt(objects.size());
		for (GameControl object : objects) {
			long id = object.getId();
			if (id != 0) {
				buffer.putLong(id);
				object.fillBuffer(buffer);
			}
		}
	}
	// Reads the chunks data from the specified buffer
	public void readBuffer(ByteBuffer buffer) throws IOException {
		if (buffer == null) {
			return;
		}
		location.setX(buffer.getFloat());
		location.setY(buffer.getFloat());
		location.setZ(buffer.getFloat());
		setChunkDimensions(buffer.getInt(), buffer.getInt(), buffer.getInt());
		this.empty = (buffer.get() != 0);
		if (!empty) {
			for (int x = 0; x < data.length; x++) {
				for (int y = 0; y < data[0].length; y++) {
					buffer.get(data[x][y]);
				}
			}
			needsMeshUpdate = true;
			updated = false;
		}
		objects.clear();
		int count = buffer.getInt();
		for (int i = 0; i < count; i++) {
			GameControl object = GameRegistry.getInstance().createObject(buffer.getLong());
			if (object != null) {
				object.readBuffer(buffer);
				addObject(object, object.getStoredLocation());
			}
		}
	}
	// Saves chunk data in it's current render state for quick caching using the specified JME exporter
	@Override
	public void write(JmeExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
		//ByteBuffer buffer = ByteBuffer.allocate(getBufferSize());
		//fillBuffer(buffer);
		//oc.write(buffer, "data", null);
		oc.write(shape.getMaterial(), "material", null);
		oc.write(shape.getMesh(), "mesh", null);
    }
	// Reads cached chunk data back in from the specified JME importer
	@Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
		//readBuffer(ic.readByteBuffer("data", null));
		Material material = (Material)ic.readSavable("material", null);
		Mesh mesh = (Mesh)ic.readSavable("mesh", null);
		updateMesh(mesh, material);
    }
}