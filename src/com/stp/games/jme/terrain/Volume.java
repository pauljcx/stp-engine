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
import com.jme3.cinematic.MotionPath;
import com.jme3.export.Savable;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.binary.ByteUtils;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector2f;
import com.jme3.scene.Node;
import com.jme3.scene.Mesh;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsSpace.BroadphaseType;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.BulletAppState.ThreadingType;
// Java Dependencies
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
// Internal Dependencies
import com.stp.games.jme.GameRegistry;
import com.stp.games.jme.controls.*;
import com.stp.games.jme.actions.MoveAction;
import com.stp.games.jme.terrain.layer.TreeLayer;

// Base class for managing and referencing regions, chunks, and objects loaded in the world
public class Volume implements Savable {
	public static enum Face {
		Top, Bottom, Left, Right, Front, Back
	};
	public static enum VolumeType {
		Height, Voxel, Space, Tile
	};
	
	protected final ArrayList<CreatureControl> creatures = new ArrayList<CreatureControl>();
	protected final ArrayList<VehicleControl> vehicles = new ArrayList<VehicleControl>();
	protected final ArrayList<StructureControl> structures = new ArrayList<StructureControl>();
	
	protected final ArrayList<ChunkControl> active = new ArrayList<ChunkControl>();
	protected final ArrayList<Region> regions = new ArrayList<Region>();
	protected final ArrayList<TreeLayer> treeLayers = new ArrayList<TreeLayer>(2);
	protected final ConcurrentLinkedQueue<GameControl> addList = new ConcurrentLinkedQueue<GameControl>();
	protected final ConcurrentLinkedQueue<GameControl> removeList = new ConcurrentLinkedQueue<GameControl>();
	protected final ScheduledThreadPoolExecutor m_thread_pool = new ScheduledThreadPoolExecutor(4);
	protected final Node m_node = new Node("Volume");
	protected final Node m_creatures_node = new Node("Creatures");
	protected final Node objNode = new Node("Objects");

	protected int wRegions  = 1;
	protected int hRegions = 1;
	protected long uid = 0;
	protected long parentId = -1;
	protected VolumeType m_volume_type;
	protected BulletAppState bulletState;
	protected ArrayList<Long> children = new ArrayList<Long>();
	
	protected int chunkSizeX = 32;
	protected int chunkSizeY = 32;
	protected int chunkSizeZ = 32;

	public Volume() {
		this(VolumeType.Height, 129, 6, 129);
	}
	public Volume(VolumeType type, int width, int height, int length) {
		setVolumeType(type);
		setChunkDimensions(width, height, length);
		regions.add(new Region(null, 0, 0, getChunkSizeX(), getChunkSizeZ()));
		m_node.attachChild(m_creatures_node);
		
		bulletState = new BulletAppState(new Vector3f(-10000f, -10000f, -10000f), new Vector3f(10000f, 10000f, 10000f), BroadphaseType.AXIS_SWEEP_3);
		bulletState.setThreadingType(ThreadingType.PARALLEL);
		bulletState.startPhysics();
	}
	public BulletAppState getBulletAppState() {
		return bulletState;
	}
	public void addPhysicsObject(PhysicsControl obj) {
		PhysicsSpace pSpace = bulletState.getPhysicsSpace();
		if (pSpace != null && obj != null) {
			pSpace.add(obj);
		}
	}
	public void removePhysicsObject(PhysicsControl obj) {
		PhysicsSpace pSpace = bulletState.getPhysicsSpace();
		if (pSpace != null && obj != null) {
			pSpace.remove(obj);
		}
	}
	// Sets the volume unique id
	public void setUniqueId(long uid) {
		this.uid = uid;
	}
	// Gets the volume unique id
	public long getUniqueId() {
		return uid;
	}
	// Sets the volume parent id
	public void setParentId(long parentId) {
		this.parentId = parentId;
	}
	// Gets the volume parent id
	public long getParentId() {
		return parentId;
	}
	public void setVolumeType(VolumeType type) {
		this.m_volume_type = type;
	}
	// Retrieves the main node for the volume
	public Node getNode() {
		return m_node;
	}
	public boolean isActive() {
		return m_node.getParent() != null;
	}
	// Sets the dimensions of each chunk for this volume
	public void setChunkDimensions(int x, int y, int z) {
		this.chunkSizeX = x;
		this.chunkSizeY = y;
		this.chunkSizeZ = z;
	}
	// retrieves the size of each chunk along the x plane
	public int getChunkSizeX() {
		return chunkSizeX;
	}
	// retrieves the size of each chunk along the y plane
	public int getChunkSizeY() {
		return chunkSizeY;
	}
	// retrieves the size of each chunk along the z plane
	public int getChunkSizeZ() {
		return chunkSizeZ;
	}
	public int getRegionSizeX() {
		return getChunkSizeX()*Region.DIMENSION_X;
	}
	public int getRegionSizeZ() {
		return getChunkSizeZ()*Region.DIMENSION_Z;
	}
	public int getVisibleX() {
		return 3;
	}
	public int getVisibleY() {
		return (m_volume_type == VolumeType.Height) ? 1 : 3;
	}
	public int getVisibleZ() {
		return 3;
	}
	public CreatureControl addCreature(CreatureControl creature) {
		creatures.add(creature);
		// Ensure creatures are able to move in the volume
		if (!creature.hasMoveControl()) {
			creature.setMoveControl(new MoveAction());
		}
		creature.setVolume(this);
		// Add the creature to the SceneGraph
		if (creature.hasSpatial()) {
			if (isActive()) {
				// Queue creature to be added during next update
				System.out.println("Queued Creature: " + creature);
			} else {
				System.out.println("Add creature to SceneGraph: " + creature);
				m_creatures_node.attachChild(creature.getSpatial());
			}
		}
		return creature;
	}
	public CreatureControl getCreature(long uid) {
		for (CreatureControl c : creatures) {
			if (c.getUniqueId() == uid) {
				return c;
			}
		}
		return null;
	}
	public ArrayList<CreatureControl> getCreatures() {
		return creatures;
	}
	public VehicleControl addVehicle(VehicleControl vehicle) {
		vehicles.add(vehicle);
		vehicle.setVolume(this);
		return vehicle;
	}
	public VehicleControl getVehicle(long uid) {
		for (VehicleControl v : vehicles) {
			if (v.getUniqueId() == uid) {
				return v;
			}
		}
		return null;
	}
	public StructureControl addStructure(StructureControl structure) {
		structures.add(structure);
		structure.setVolume(this);
		return structure;
	}
	public StructureControl getStructure(long uid) {
		for (StructureControl s : structures) {
			if (s.getUniqueId() == uid) {
				return s;
			}
		}
		return null;
	}
	// Called once per frame do any scene graph updates here
	public void update(float tpf) {
		// Iterate through the add list and add all queued objects to the scene graph
		if (!addList.isEmpty()) {
			for (Iterator<GameControl> i = addList.iterator(); i.hasNext();) {
				GameControl obj = i.next();
				if (obj.hasSpatial()) {
					objNode.attachChild(obj.getSpatial());
					obj.setActive(true);
				}
				i.remove();
			}
		}
		// Iterate through the remove list and remove all queued objects from the scene graph
		if (!removeList.isEmpty()) {
			for (Iterator<GameControl> i = removeList.iterator(); i.hasNext();) {
				GameControl obj = i.next();
				if (obj.hasSpatial()) {
					objNode.detachChild(obj.getSpatial());
					obj.setActive(false);
				}
				i.remove();
			}
		}
	}
	// Mark all chunks as inactive, any that are still inactive after the next update will be removed
	public void markInactive() {
		for (ChunkControl chunk : active) {
				chunk.setActive(false);
		}
	}
	// Move all chunks still marked as inactive to the inactive list
	public void moveInactive() {
		for (int a = active.size() - 1; a >= 0; a--) {
			ChunkControl chunk = active.get(a);
			if (!chunk.isActive()) {
				// Remove the chunk from the world so it is no longer drawn
				m_node.removeControl(chunk);
				m_node.detachChild(chunk.getNode());
				active.remove(a);
				// Send any listeners a message that the chunk has been deactivated
				//world.fireChunkDeactivated(chunk);
			}
		}
	}
	// Activates the chunk at the specified location indicating it should update its data and be added to the world
	public ChunkControl activateChunk(Vector3f chunkLocation) {
		// Check to see if the requested chunk is already in the active list, if found reset it's flag to active
		for (ChunkControl chunk : active) {
			if (chunk.getLocation().equals(chunkLocation)) {
				chunk.setActive(true);
				return chunk;
			}
		}
		// Locate the chunks region using the chunks location
		Region region = getRegionForChunkAt(chunkLocation);
		if (region != null) {
			// Attempt retrieve the chunk from the region
			ChunkControl chunk = region.getChunk(chunkLocation);
			if (chunk != null) {
				// If a loaded chunk was found do any necessary processing needed before the chunk is activated
				beforeActivate(chunk);
			} else {
				// If the chunk was not found in the region create a new one
				chunk = createNewChunk(chunkLocation.clone(), region);
			}
			// Mark the chunk as active and add the chunk to the world so it can be drawn
			chunk.setActive(true);
			active.add(chunk);		
			m_node.addControl(chunk);
			m_node.attachChild(chunk.getNode());
			// Send any listeners a message indicating the chunk has just been activated
			//world.fireChunkActivated(chunk);
			return chunk;
		}
		return null;
	}
	/** METHOD: createNewChunk - creates a new chunk at the specified locaton to be overriden in subclasses 
	 *  @param location a Vector3f indicating the location of the chunk in the world
	 *  @param region a reference to the Region that the chunk is part of
	 *  @return the newly created ChunkControl object
	 */
	public ChunkControl createNewChunk(Vector3f location, Region region) {
		switch (m_volume_type) {
			case Height:
				HeightChunkControl heightChunk = new HeightChunkControl(location, region);
				heightChunk.setChunkDimensions(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
				return heightChunk;
			default:
				ChunkControl chunk = new ChunkControl(location, region);
				chunk.setChunkDimensions(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
				return chunk;
		}
	}
	public boolean loadChunk(ChunkControl chunk) {
		Region region = chunk.getRegion();
		if (region != null) {
			return region.loadChunk(chunk);
		}
		return false;
	}
	public ChunkControl addLoadedChunk(ChunkControl chunk) {
		Region region = chunk.getRegion();
		if (region != null) {
			region.addLoadedChunk(chunk);
		}
		addPhysicsObject(chunk.initializePhysics());
		return chunk;
	}
	/** METHOD: beforeActivate - called just before a chunk is activated and added to the world, do any pre-processing here
	 *  @param chunk a reference to the ChunkControl that was just activated
	 *  @return the reference to the activate chunk for convenience
	 */
	protected ChunkControl beforeActivate(ChunkControl chunk) {
		/*Region region = chunk.getRegion();
		if (region != null) {
			if ((Math.random()*100) < 25) {
				int count = (int)(Math.random()*5);
				for (int i = 0; i < count; i++) {
					world.addCreature(world.createCreature("Rabbit", (float)(Math.random()*32)+chunk.getGlobalX(), 0f, (float)(Math.random()*32)+chunk.getGlobalZ()));
				}
			}
			if ((Math.random()*100) < 50) {
				world.addCreature(world.createCreature("Buck", (float)(Math.random()*32)+chunk.getGlobalX(), 0f, (float)(Math.random()*32)+chunk.getGlobalZ()));
			}
		}*/
		return chunk;
	}
	public Region getRegionForChunkAt(Vector3f chunkLocation) {
		return this.getRegionForChunkAt(chunkLocation.x, chunkLocation.y, chunkLocation.z);
	}
	public Region getRegionForChunkAt(float x, float y, float z) {
		return getRegionByLocation(Math.round(x/Region.DIMENSION_X),
													Math.round(z/Region.DIMENSION_Z));
	}
	// Search through the list of loaded chunks to find the chunk that contains the specified location
	public Region getRegion(Vector3f globalLocation) {
		return getRegion(globalLocation.x, globalLocation.y, globalLocation.z);
	}
	// Search through the list of loaded chunks to find the chunk that contains the specified location
	public Region getRegion(float gx, float gy, float gz) {
		return getRegionByLocation(Math.round(gx/getRegionSizeX()),
													Math.round(gz/getRegionSizeZ()));
	}
	// Search through the list of loaded chunk and return the one the specified location
	public Region getRegionByLocation(Vector2f regionLocation)	{
		return getRegionByLocation(regionLocation.x, regionLocation.y);
	}
	// Search through the list of loaded chunk and return the one the specified location
	public Region getRegionByLocation(float x, float z) {
		for (Region r : regions) {
			if (r.matchesLocation(x, z)) {
				return r;
			}
		}
		return null;
	}
	/** METHOD: getChunkLocation
	 *  @param globalLocation the incoming global coordinates
	 *  @return a 3D location vector of integers representing the chunk located at the specified global location
	 */
	public Vector3f getChunkLocation(Vector3f globalLocation) {
		return getChunkLocation(globalLocation.x, globalLocation.y, globalLocation.z);
	}
    public Vector3f getChunkLocation(float gx, float gy, float gz) {
		return new Vector3f(getChunkLocationX(gx),
										getChunkLocationY(gy),
										getChunkLocationZ(gz));
	}
	// Search through the list of loaded chunks to find the chunk that contains the specified location
	public ChunkControl getChunk(Vector3f globalLocation) {
		return getChunk(globalLocation.x, globalLocation.y, globalLocation.z);
	}
	// Search through the list of loaded chunks to find the chunk that contains the specified location
	public ChunkControl getChunk(float gx, float gy, float gz) {
		return getChunkByLocation(getChunkLocationX(gx),
													getChunkLocationY(gy),
													getChunkLocationZ(gz));
	}
	public int getChunkLocationX(float gx) {
		return Math.round(gx/getChunkSizeX());
	}
	public int getChunkLocationY(float gy) {
		return (m_volume_type == VolumeType.Height) ? 0 : Math.round(gy/getChunkSizeY());
	}
	public int getChunkLocationZ(float gz) {
		return Math.round(gz/getChunkSizeZ());
	}
	// Search through the list of loaded chunk and return the one the specified location
	public ChunkControl getChunkByLocation(Vector3f chunkLocation)	{
		return getChunkByLocation(chunkLocation.x, chunkLocation.y, chunkLocation.z);
	}
	// Search through the list of loaded chunk and return the one the specified location
	public ChunkControl getChunkByLocation(float x, float y, float z) {
		Region region = getRegionForChunkAt(x, y, z);
		if (region != null) {
			return region.getChunk(x, y, z);
		}
		return null;
	}
	// Search through the list of loaded chunk and return the one with the specified name
	public ChunkControl getChunkByName(String name) {
		String[] parts = name.split("_");
		if (parts.length == 3) {
			return getChunkByLocation(Integer.valueOf(parts[0]), Integer.valueOf(parts[1]), Integer.valueOf(parts[2]));
		}
		return null;
	}
	public float getHeight(Vector3f globalLocation) {
		return getHeight(globalLocation.x, globalLocation.y, globalLocation.z);
	}
	public float getHeight(float gx, float gy, float gz) {
		ChunkControl chunk = getChunk(gx, gy, gz);
		if (chunk != null) {
			return chunk.getHeight(gx, gz);
		}
		return 0f;
	}
	public boolean isPassable(Vector3f globalLocation) {
		return isPassable(globalLocation.x, globalLocation.y, globalLocation.z);
	}
	public boolean isPassable(float x, float y, float z) {
		return true;
	}
	// Gets the data value at the specified global location
	public byte getValue(Vector3f globalLocation) {
		return getValue(globalLocation.x, globalLocation.y, globalLocation.z);
	}
	// Gets the data value at the specified global location
	public byte getValue(float gx, float gy, float gz) {
		ChunkControl chunk = getChunk(gx, gy, gz);
		if (chunk != null) {
			return chunk.getGlobalValue(gx, gy, gz);
		}
		return 0;
	}
	// Sets the data value at the specified global location
	public void setValue(Vector3f globalLocation, byte value) {
		setValue(globalLocation.x, globalLocation.y, globalLocation.z, value);
	}
	// Sets the data at the specified global location
    public void setValue(float gx, float gy, float gz, byte value)	{
		ChunkControl chunk = getChunk(gx, gy, gz);
		if (chunk != null) {
			chunk.setGlobalValue(gx, gy, gz, value);
		}
    }
	// Sets the data for the specified area
	public void setBlockArea(Vector3f location, Vector3f size, byte value)	{
		Vector3f tmpLocation = new Vector3f();
		for (int x = 0; x < size.getX(); x++) {
			for (int y = 0; y < size.getY(); y++) {
				for (int z = 0; z < size.getZ(); z++) {
					tmpLocation.set(location.getX() + x, location.getY() + y, location.getZ() + z);
					setValue(tmpLocation, value);
				}
			}
		}
	}
	public GameControl getObject(Vector3f globalLocation) {
		return this.getObject(globalLocation.x, globalLocation.y, globalLocation.z);
	}
	public GameControl getObject(float gx, float gy, float gz) {
		ChunkControl chunk = getChunk(gx, gy, gz);
		if (chunk != null) {
			return chunk.getObject(gx, gy, gz);
		}
		return null;
	}
	public GameControl addObject(GameControl object, Vector3f globalLocation) {
		ChunkControl chunk = getChunk(globalLocation);
		if (chunk != null) {
			return chunk.addObject(object, globalLocation);
		}
		return object;
	}
	public GameControl addObject(GameControl object, float gx, float gy, float gz) {
		ChunkControl chunk = getChunk(gx, gy, gz);
		if (chunk != null) {
			return chunk.addObject(object, gx, gy, gz);
		}
		return object;
	}
	public void removeObject(GameControl object) {
		ChunkControl chunk = getChunk(object.getWorldTranslation());
		if (chunk != null) {
			chunk.removeObject(object);
		}
	}
	public TreeLayer addTreeLayer(TreeLayer layer) {
		treeLayers.add(layer);
		return layer;
	}
	// Saves all loaded chunks
	public void saveVolume(File directory) {
		for (Region r : regions) {
			r.save(directory);
		}
	}
	// Clears all the data arrays for this volume
	public void clearVolume() {
		active.clear();
	}
	// Gets the number of active chunks
	public int getActiveCount() {
		return active.size();
	}
	// Returns the currently active chunks 
	public ArrayList<ChunkControl> getActiveChunks(boolean sorted) {
		// Resort the list of chunks if requested
		if (sorted) {
			Collections.sort(active);
		}
		return active;
	}
	public Mesh buildMesh(ChunkControl chunk) {
		Mesh mesh = chunk.getMesh();
		return (mesh != null) ? mesh : new Mesh();
	}
	public void initializePhysics(ChunkControl chunk) {
		/*RigidBodyControl physicsControl = chunk.getNode().getControl(RigidBodyControl.class);
		// PhysicsControl doesn't exist so we can create a new one
		if (physicsControl == null) {	
			// obtain the collision shape from the chunk
			CollisionShape cs = chunk.getCollisionShape();
			// if the chunk does not supply a collision shape it means it has no physical presence and so physics doesn't need to be initialized
			if (cs != null) {
				// initialize a new PhysicsControl with the supplied shape and set mass to zero to set it as a static object
				physicsControl = new RigidBodyControl(cs, 0);
				// attach the new PhysicsControl to the chunk
				chunk.getNode().addControl(physicsControl);
				world.addPhysicsObject(physicsControl);
			}
		// PhysicsControl already exists so we only need to update it if necessary
		} else {
		}*/
	}
	// Write all unique parameter values to the given output stream
	public void writeFields(OutputStream os) throws IOException {
		os.write(ByteUtils.convertToBytes(getUniqueId()));
		os.write(ByteUtils.convertToBytes(getParentId()));
		os.write(ByteUtils.convertToBytes(children.size()));
		for (int c = 0; c < children.size(); c++) {
			os.write(ByteUtils.convertToBytes(children.get(c)));
		}
		os.write(ByteUtils.convertToBytes(creatures.size()));
		for (CreatureControl c : creatures) {
			os.write(ByteUtils.convertToBytes(c.getId()));
			c.writeFields(os);
		}
		os.write(ByteUtils.convertToBytes(vehicles.size()));
		for (VehicleControl v : vehicles) {
			os.write(ByteUtils.convertToBytes(v.getId()));
			v.writeFields(os);
		}
		os.write(ByteUtils.convertToBytes(structures.size()));
		for (StructureControl s : structures) {
			os.write(ByteUtils.convertToBytes(s.getId()));
			s.writeFields(os);
		}
	}
	// Read all unique parameter values from the given input stream
	public void readFields(InputStream is) throws IOException {
		setUniqueId(ByteUtils.readLong(is));
		setParentId(ByteUtils.readLong(is));
		int childCount = ByteUtils.readInt(is);
		children.clear();
		for (int c = 0; c < childCount; c++) {
			children.add(ByteUtils.readLong(is));
		}
		int creatureCount = ByteUtils.readInt(is);
		creatures.clear();
		for (int c = 0; c < creatureCount; c++) {
			CreatureControl creature = (CreatureControl)GameRegistry.getInstance().createObject(ByteUtils.readLong(is));
			creature.readFields(is);
			addCreature(creature);
		}
		int vehicleCount = ByteUtils.readInt(is);
		vehicles.clear();
		for (int v = 0; v < vehicleCount; v++) {
			VehicleControl vehicle = (VehicleControl)GameRegistry.getInstance().createObject(ByteUtils.readLong(is));
			vehicle.readFields(is);
			addVehicle(vehicle);
		}
		int structureCount = ByteUtils.readInt(is);
		structures.clear();
		for (int s = 0; s < structureCount; s++) {
			StructureControl structure = (StructureControl)GameRegistry.getInstance().createObject(ByteUtils.readLong(is));
			structure.readFields(is);
			addStructure(structure);
		}
	}
	// Write all unique parameter values to the given buffer
	public void fillBuffer(ByteBuffer buffer) throws IOException	{
		buffer.putLong(getUniqueId());
		buffer.putLong(getParentId());
		buffer.putInt(children.size());
		for (int c = 0; c < children.size(); c++) {
			buffer.putLong(children.get(c));
		}
		buffer.putInt(creatures.size());
		for (CreatureControl c : creatures) {
			buffer.putLong(c.getId());
			c.fillBuffer(buffer);
		}
		buffer.putInt(vehicles.size());
		for (VehicleControl v : vehicles) {
			buffer.putLong(v.getId());
			v.fillBuffer(buffer);
		}
		buffer.putInt(structures.size());
		for (StructureControl s : structures) {
			buffer.putLong(s.getId());
			s.fillBuffer(buffer);
		}
	}
	// Read all unique parameter values from the given buffer
	public void readBuffer(ByteBuffer buffer) throws IOException {
		setUniqueId(buffer.getLong());
		setParentId(buffer.getLong());
		int childCount = buffer.getInt();
		children.clear();
		for (int c = 0; c < childCount; c++) {
			children.add(buffer.getLong());
		}
		int creatureCount = buffer.getInt();
		creatures.clear();
		for (int c = 0; c < creatureCount; c++) {
			CreatureControl creature = (CreatureControl)GameRegistry.getInstance().createObject(buffer.getLong());
			creature.readBuffer(buffer);
			addCreature(creature);
		}
		int vehicleCount = buffer.getInt();
		vehicles.clear();
		for (int v = 0; v < vehicleCount; v++) {
			VehicleControl vehicle = (VehicleControl)GameRegistry.getInstance().createObject(buffer.getLong());
			vehicle.readBuffer(buffer);
			addVehicle(vehicle);
		}
		int structureCount = buffer.getInt();
		structures.clear();
		for (int s = 0; s < structureCount; s++) {
			StructureControl structure = (StructureControl)GameRegistry.getInstance().createObject(buffer.getLong());
			structure.readBuffer(buffer);
			addStructure(structure);
		}
	}
	public void write(JmeExporter ex) throws IOException {
		OutputCapsule oc = ex.getCapsule(this);
		oc.write(uid, "uid", 0L);
		oc.write(parentId, "pid", -1L);
		oc.writeSavableArrayList(creatures, "creatures", null);
		oc.writeSavableArrayList(vehicles, "vehicles", null);
		oc.writeSavableArrayList(structures, "structures", null);
	}
	public void read(JmeImporter im) throws IOException {
		InputCapsule ic = im.getCapsule(this);
		setUniqueId(ic.readLong("uid", 0L));
		setParentId(ic.readLong("pid", -1L));
		for (Savable s : ic.readSavableArray("creatures", new CreatureControl[0])) {
			addCreature((CreatureControl)s);
		}
		for (Savable s : ic.readSavableArray("vehicles", new VehicleControl[0])) {
			addVehicle((VehicleControl)s);
		}
		for (Savable s : ic.readSavableArray("structures", new StructureControl[0])) {
			addStructure((StructureControl)s);
		}
	}
	public Future<MotionPath> getPath(Vector3f source, Vector3f target) {
		return m_thread_pool.submit(new PathProducer(this, source, target));
	}
	/* This is an implementation of the A* algorithm used to determine the shortest path from point A to B within
	 * a given volume avoiding any obsticals. The algorithm is run on a seperate thread and the result can be obtained
	 * through the returned Future object.
	 */
	public static class PathProducer implements Callable<MotionPath> {
		private final Volume volume;
		private final Vector3f source;
		private final Vector3f target;
		public PathProducer(final Volume volume, final Vector3f source, final Vector3f target) {
			this.volume = volume;
			this.source = source;
			this.target = target;
		}
		public MotionPath call() throws Exception {
			long startTime = System.currentTimeMillis();
			MotionPath path = new MotionPath();
			ArrayList<PathNode> openList= new ArrayList<PathNode>();
			ArrayList<PathNode> closedList = new ArrayList<PathNode>();
			PathNode startNode = new PathNode(source);
			PathNode endNode = new PathNode(target);
			if (!volume.isPassable(endNode.getLocation())) {
				for (int x = -1; x <= 1; x++) {
					for (int y = -1; y <= 1; y++) {
						if (x == 0 && y == 0) {
							continue;
						}
						PathNode neighbor = new PathNode((int)endNode.getX() + x, (int)endNode.getY() + y);
						if (volume.isPassable(neighbor.getLocation())) {
							neighbor.set(PathNode.getDistance(startNode, neighbor), PathNode.getDistance(neighbor, endNode));
							neighbor.setParent(endNode);
							closedList.add(neighbor);
						}
					}
				}
				endNode = closedList.get(0);
				for (PathNode node : closedList) {
					if (node.getG() < endNode.getG()) {
						endNode = node;
					}
				}
				closedList.clear();
			}
			startNode.set(0, PathNode.getDistance(startNode, endNode));
			endNode.set(PathNode.getDistance(startNode, endNode), 0);
			openList.add(startNode);
			//System.out.println("Start Node: " + startNode);
			//System.out.println("End Node: " + endNode);
			
			while (openList.size() > 0) {
				PathNode current = openList.get(0);
				for (PathNode node : openList) {
					if (node.getF() < current.getF() || (node.getF() == current.getF() && node.getH() < current.getH())) {
						current = node;
					}
				}
				//System.out.println("Current Node: " + current + " | " + openList.size());
				openList.remove(current);
				closedList.add(current);
				if (current.equals(endNode)) {
					while (!current.equals(startNode)) {
						//System.out.println("Path Node: " + current);
						path.addWayPoint(current.getLocation().setY(volume.getHeight(current.getX(), 0, current.getY())));
						current = current.getParent();
					}
					double calcTime = (System.currentTimeMillis() - startTime)/1000.0;
					//System.out.println("Path Length: " + path.getNbWayPoints() + " | " + calcTime + "s");
					return path;
				}
				// Add Neighbors
				for (int x = -1; x <= 1; x++) {
					for (int y = -1; y <= 1; y++) {
						if (x == 0 && y == 0) {
							continue;
						}
						PathNode neighbor = new PathNode((int)current.getX() + x, (int)current.getY() + y);
						// Check if the neighbor has already been evaluated and if it is passable
						if (!closedList.contains(neighbor) && volume.isPassable(neighbor.getLocation())) {
							neighbor.set(current.getG() + PathNode.getDistance(current, neighbor), PathNode.getDistance(neighbor, endNode));
							neighbor.setParent(current);
							int index = openList.indexOf(neighbor);
							if (index < 0) {
								openList.add(neighbor);
								//System.out.println("Neighbor Node: " + neighbor);
							} else if (neighbor.getG() < openList.get(index).getG()) {
								openList.get(index).setG(neighbor.getG());
							}
						}
					}
				}
			}
			return path;
		}
	}
	/* Used to hold path node values for the PathProducer algorithm */
	public static class PathNode {
		private Vector3f location;
		private PathNode parent;
		private boolean walkable;
		private int g; // distance from start
		private int h; // distance from end
		
		public PathNode(Vector3f worldLocation) {
			this ((int)worldLocation.x, (int)worldLocation.z);
		}
		public PathNode(int x, int y) {
			this.location = new Vector3f(x, 0, y);
		}
		public Vector3f getLocation() {
			return location;
		}
		public PathNode getParent() {
			return parent;
		}
		public int getG() {
			return g;
		}
		public int getH() {
			return h;
		}
		public int getF() {
			return g + h;
		}
		public float getX() {
			return location.x;
		}
		public float getY() {
			return location.z;
		}
		public void setLocation(Vector3f in) {
			setLocation(in.x, in.z);
		}
		public void setLocation(float x, float y) {
			location.set(x, 0, y);
		}
		public void setParent(PathNode parent) {
			this.parent = parent;
		}
		public void setG(int g) {
			this.g = g;
		}
		public void setH(int h) {
			this.h = h;
		}
		public void set(int g, int h) {
			this.g = g;
			this.h = h;
		}
		@Override
		public String toString() {
			return location.toString() + " gCost=" + g + " hCost=" + h + " fCost=" +getF();
		}
		@Override
		public boolean equals(Object other) {
			if (other instanceof PathNode) {
				return ((PathNode)other).getLocation().equals(location);
			}
			return false;
		}
		@Override
		public int hashCode() {
			return location.hashCode() + (13 * getF());
		}
		public static int getDistance(PathNode nodeA, PathNode nodeB) {
			int dx = (int)Math.abs(nodeA.getX() - nodeB.getX());
			int dy = (int)Math.abs(nodeA.getY() - nodeB.getY());
			if (dx > dy) {
				return 14*dy + 10*(dx-dy);
			} else {
				return 14*dx + 10*(dy-dx);
			}
		}
	}
}