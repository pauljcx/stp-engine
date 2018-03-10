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
package com.stp.games.jme.controls;
// JME3 Dependencies
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResults;
import com.jme3.collision.Collidable;
import com.jme3.export.Savable;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.binary.ByteUtils;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Geometry;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.math.Quaternion;
import com.jme3.light.Light;
import com.jme3.scene.control.Control;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.LightControl;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bounding.BoundingVolume;
import com.jme3.util.TempVars;
// Java Dependencies
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.lang.reflect.Constructor;
import com.stp.games.jme.GameRegistry;
import com.stp.games.jme.terrain.Volume;

/** @author Paul Collins
 *  @version v1.0 ~ 04/04/2014
 *  HISTORY: Version 1.0 reformatted methods and added comments updating version to 1.0. 04/04/2014 
 *		Version 0.1 created GameControl, all objects in the game are required to derive
 *			from this class ~ 05/04/2013
 */
public class GameControl extends AbstractControl implements Savable, Collidable {
	
	// State flags used to indicated changes that need to be made to the scene graph for this object
	public enum SceneState {
		None,
		Add,
		Update,
		Remove;
	}
	
	public static final String[] DEFAULT_ACTIONS = new String[0];
	public static final ObjectType<GameControl> GENERIC_TYPE = new ObjectType<GameControl>("Generic", GameControl.class);
	
	/*private static final ArrayList<ObjectType> OBJECT_TYPES = new ArrayList<ObjectType>();
	
	public static ObjectType registerObjectType(ObjectType type) {
		int index = OBJECT_TYPES.indexOf(type);
		if (index < 0) {
			OBJECT_TYPES.add(type);
			return type;
		} else {
			return OBJECT_TYPES.get(index);
		}
	}
	
	public static ArrayList<ObjectType> getObjectTypes() {
		return OBJECT_TYPES;
	}
	
	public static ObjectType findObjectType(String text) {
		for (ObjectType t : OBJECT_TYPES) {
			if (t.matches(text)) {
				return t;
			}
		}
		return GENERIC_TYPE;
	}*/

	public static class ObjectType<T extends GameControl> {
		private String name = "Generic";
		private ArrayList<Param> params = new ArrayList<Param>();
		private java.lang.Class<T> controlClass;
		public ObjectType(String name, java.lang.Class<T> controlClass) {
			this.name = name;
			this.controlClass = controlClass;
		}
		public ObjectType(String name, java.lang.Class<T> controlClass, Param[] paramIn) {
			this.name = name;
			this.controlClass = controlClass;
			for (int p = 0; p < paramIn.length; p++) {
				this.params.add(paramIn[p]);
			}
		}
		public java.lang.Class<T> getControlClass() {
			return controlClass;
		}
		public T createControl() {
			try {
				System.out.println("Creating control for class: " + name + " | " + controlClass);
				Constructor<T> constructor = controlClass.getConstructor();
				return constructor.newInstance();
			} catch (Exception ex) { 
				System.out.println("Unable to create control for class: " + controlClass);
				return null;
			}
		}
		public String getName() {
			return name;
		}
		public String toString() {
			return name;
		}
		public ArrayList<Param> getParams() {
			return params;
		}
		public Param getParam(int index) {
			return params.get(index);
		}
		public String getParamName(int index) {
			if (index >= 0 && index < params.size()) {
				return params.get(index).getName();
			} else {
				return "";
			}
		}
		public int getParamIndex(String name) {
			for (Param p : params) {
				if (p.getName().equals(name)) {
					return p.getIndex();
				}
			}
			return -1;
		}
		public boolean equals(Object obj) {
			return matches(obj);
		}
		public boolean matches(Object obj) {
			if (obj != null) {
				return name.equals(obj.toString());
			}
			return false;
		}
	}
	private static class Property {
		private String key = "";
		private Object value = null;
		public Property(String key, Object value) {
			this.key = key;
			this.value = value;
		}
		public String getKey() {
			return key;
		}
		public Object getValue() {
			return value;
		}
		public void setValue(Object value) {
			this.value = value;
		}
		@Override
		public boolean equals(Object other) {
			if (other instanceof Property) {
				return ((Property)other).getKey().equals(key);
			}
			return false;
		}
		@Override
		public String toString() {
			return key + "=" + value;
		}
	}
	
	// Game Object Types
	/*public enum Type
	{
		GENERIC("Generic", new Param[0]),
		CLOTHING("Clothing", ClothingControl.CLOTHING_PARAMS),
		TOOL("Tool", ToolControl.TOOL_PARAMS),
		CONSUMABLE("Consumable", new Param[0]),
		RESOURCE("Resource", ResourceControl.RESOURCE_PARAMS),
		TRANSPORT("Transport", new Param[0]),
		CONTAINER("Container", ContainerControl.CONTAINER_PARAMS),
		MAP("Map", new Param[0]),
		CREATURE("Creature", CreatureControl.CREATURE_PARAMS),
		SENTIENT("Sentient", SentientControl.SENTIENT_PARAMS),
		PLAYER("Player", new Param[0]),
		PLANT("Plant", PlantControl.PLANT_PARAMS),
		STRUCTURE("Structure", StructureControl.STRUCT_PARAMS),
		WAYPOINT("Waypoint", new Param[0]),
		STONE("Stone", StoneControl.STONE_PARAMS),
		SURFACE("Surface", new Param[0]),
		AREA("Area", new Param[0]),
		LIGHT("Light", new Param[0]),
		TERRAIN("Terrain", new Param[0]),
		VEHICLE("Vehicle", new Param[0]),
		COMPONENT("Component", new Param[0]);
		private String text;
		private Param[] params;
		private Type(String text, Param[] params) { this.text = text; this.params = params; }
		public byte index() { return (byte)ordinal(); }
		public String text() { return text; }
		public String toString() { return text; }
		public Param[] getParams() { return params; }
		public boolean matches(Object obj) {
			if (obj != null) {
				return text.equals(obj.toString());
			}
			return false;
		}
	}*/
	// Shapes used to create physics object
	public enum BoundingShape{
		AUTO("auto"),
		AABB("aabb" ),
		BOX("box" ),
		SPHERE("sphere"),
		CYLINDER("cylinder"),
		CAPSULE("capsule");
		private String text;
		private BoundingShape(String text) {
			this.text = text;
		}
		public byte index() {
			return (byte)ordinal();
		}
		public String text() {
			return text;
		}
		public String toString() {
			return text;
		}
	}
	// Structure class for storing information about an objects parameters
	public static class Param {
		public Class cls;
		public String name;
		public int index;
		private int min;
		private int max;
		
		public Param(String name, Class cls, int index) {
			this (name, cls, index, 0, 0);
		}
		public Param(String name, Class cls, int index, int min, int max) {
			this.name = name;
			this.cls = cls;
			this.index = index;
			this.min = min;
			this.max = max;
		}
		public String getName()	{
			return name;
		}
		public Class getParamClass() {
			return cls;
		}
		public int getIndex() {
			return index;
		}
		public int getMin() {
			return min;
		}
		public int getMax() {
			return max;
		}
		public int getRange() {
			return max - min;
		}
	}
	
	/* INSTANCE VARIABLES */
	protected final ArrayList<Property> properties = new ArrayList<Property>();
	protected final Vector3f location = new Vector3f();
	protected Schematic schema;
	protected volatile boolean active;
	protected volatile SceneState m_scene_state;
	protected long uid;
	protected long volumeId;
	
	// Primary constructor with no parameters
	public GameControl() {
		this (null);
	}
	public GameControl(Schematic schema) {
		super();
		setSchematic(schema);
		this.active = false;
		this.m_scene_state = SceneState.None;
	}
	
	/* INHERITED METHODS */
	
	// Inherited from super class
	@Override
	protected void controlRender(RenderManager rm, ViewPort vp)	{
	}
	// Inherited from super class, all spatial updates between frames should be implemented here
	@Override
	protected void controlUpdate(float tpf)	{
	}
	// Inherited from super class, assigns a spatial to this control
	@Override
	public void setSpatial(Spatial spatial)	{
		super.setSpatial(spatial);
		if (spatial != null) {
			spatial.setLocalTranslation(location);
		}
	}
	// Inherited from super class, creates a copy of this control to add to a new spatial
	@Override
	public Control cloneForSpatial(Spatial spatial)	{
		final GameControl control = new GameControl(schema);
		control.setSpatial(spatial);
		return control;
	}
	
	/* INSTANCE METHODS */
	
	// Sets the objects unique id
	public void setUniqueId(long uid) {
		this.uid = uid;
	}
	// Gets the objects unique id
	public long getUniqueId() {
		return uid;
	}
	public Object get(String key) {
		for (Property p : properties) {
			if (p.getKey().equals(key)) {
				return p.getValue();
			}
		}
		return null;
	}
	public void put(String key, Object value) {
		for (Property p : properties) {
			if (p.getKey().equals(key)) {
				p.setValue(value);
				return;
			}
		}
		properties.add(new Property(key, value));
	}
	public int getPropertyCount() {
		return properties.size();
	}
	public ArrayList<Property> getProperties() {
		return properties;
	}
	public void remove(String key) {
		for (int p = 0; p < properties.size(); p++) {
			if (properties.get(p).getKey().equals(key)) {
				properties.remove(p);
				return;
			}
		}
	}
	public String getString(String key) {
		Object value = get(key);
		return (value != null) ? value.toString() : "";
	}
	public boolean getBool(String key) {
		return getBool(key, false);
	}
	public boolean getBool(String key, boolean defaultValue) {
		Object value = get(key);
		return (value instanceof Boolean) ? (Boolean)value : defaultValue;
	}
	public int getInt(String key) {
		return getInt(key, 0);
	}
	public int getInt(String key, int defaultValue) {
		Object value = get(key);
		return (value instanceof Integer) ? (Integer)value : defaultValue;
	}
	public float getFloat(String key) {
		return getFloat(key, 0f);
	}
	public float getFloat(String key, float defaultValue) {
		Object value = get(key);
		return (value instanceof Float) ? (Float)value : defaultValue;
	}
	public long getLong(String key) {
		return getLong(key, 0L);
	}
	public long getLong(String key, long defaultValue) {
		Object value = get(key);
		return (value instanceof Long) ? (Long)value : defaultValue;
	}
	// Sets the objects unique id
	public void setVolume(Volume volume) {
		setVolumeId(volume.getUniqueId());
	}
	// Sets the objects unique id
	public void setVolumeId(long volumeId) {
		this.volumeId = volumeId;
	}
	// Gets the objects unique id
	public long getVolumeId() {
		return volumeId;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GameControl) {
			return ((GameControl)obj).getUniqueId() == uid;
		}
		return false;
	}
	@Override
	public int hashCode() {
		return (int) (uid ^ (uid >>> 32));
	}
	// To be overriden in sub-classes that need access to the asset manager during initialization
	public void initializeAssets(AssetManager assetManager) {
	}
	// To be implemented in subclass, resets all parameters to their defaults
	public void resetFields() {
	}
	// Copies all parameters from this control to another
	public void copyFields(GameControl control)	{
		setFields(control.getSchematic());
	}
	// Initializes parameters based on given schematic
	public void setFields(Schematic schema)	{
		this.schema = schema;
	}
	// Assigns the schematic for this object
	public void setSchematic(Schematic schema) {
		this.schema = schema;
	}
	// Gets the schematic that defines this object
	public Schematic getSchematic() {
		return schema;
	}
	// Gets the schematic id
	public long getId() {
		return (schema != null) ? schema.getId() : 0L;
	}
	// Sets whether the spatial for this control has been added to the scene graph for rendering
	public void setActive(boolean active) {
		this.active = active;
	}
	// Gets whether the spatial for this control has been added to the scene graph for rendering
	public boolean isActive() {
		return active;
	}
	// Sets whether the spatial for this control has an update to be done
	public void setSceneState(SceneState state) {
		this.m_scene_state = state;
	}
	// Gets whether the spatial for this control has an update to be done
	public SceneState getSceneState() {
		return m_scene_state;
	}
	// Gets whether the control has been attached to a spatial 
	public boolean hasSpatial() {
		return spatial != null;
	}
	// Quick access method to get user data from the items spatial
	public String getUserData(String key) {
		if (hasSpatial()) {
			return spatial.getUserData(key);
		}
		return "";
	}
	// Returns a list of user actions that indicate how a player can interact with this object
	public String[] getActionList() {
		return DEFAULT_ACTIONS;
	}
	public String getDefaultAction() {
		if (getActionList().length > 0) {
			return getActionList()[0];
		}
		return "";
	}
	public boolean swapMesh(String childName, String meshName) {
		if (spatial != null) {
			MeshSwapControl control = spatial.getControl(MeshSwapControl.class);
			if (control != null) {
				return control.swapMesh(childName, meshName);
			}
		}
		return false;
	}
	// Gets a string that describes this object for error logging etc.
	public String toString() {
		return getType() + " | " + getName();
	}
	// Write all unique parameter values to the given output stream
	public void writeFields(OutputStream os) throws IOException {
		os.write(ByteUtils.convertToBytes(getUniqueId()));
		os.write(ByteUtils.convertToBytes(getVolumeId()));
		getWorldTranslation(location);
		os.write(ByteUtils.convertToBytes(location.getX()));
		os.write(ByteUtils.convertToBytes(location.getY()));
		os.write(ByteUtils.convertToBytes(location.getZ()));
		os.write(ByteUtils.convertToBytes(getId()));
	}
	// Read all unique parameter values from the given input stream
	public void readFields(InputStream is) throws IOException {
		setUniqueId(ByteUtils.readLong(is));
		setVolumeId(ByteUtils.readLong(is));
		location.setX(ByteUtils.readFloat(is));
		location.setY(ByteUtils.readFloat(is));
		location.setZ(ByteUtils.readFloat(is));
		GameRegistry.getInstance().initializeControl(this, ByteUtils.readLong(is));
	}
	// Write all unique parameter values to the given buffer
	public void fillBuffer(ByteBuffer buffer) throws IOException	{
		buffer.putLong(getUniqueId());
		buffer.putLong(getVolumeId());
		getWorldTranslation(location);
		buffer.putFloat(location.getX());
		buffer.putFloat(location.getY());
		buffer.putFloat(location.getZ());
		buffer.putLong(getId());
	}
	// Read all unique parameter values from the given buffer
	public void readBuffer(ByteBuffer buffer) throws IOException {
		setUniqueId(buffer.getLong());
		setVolumeId(buffer.getLong());
		location.setX(buffer.getFloat());
		location.setY(buffer.getFloat());
		location.setZ(buffer.getFloat());
		GameRegistry.getInstance().initializeControl(this, buffer.getLong());
	}
	// Write all unique parameter values using the JmeExporter utility
	public void write(JmeExporter ex) throws IOException {
		OutputCapsule oc = ex.getCapsule(this);
		oc.write(getUniqueId(), "uid", 0L);
		oc.write(getVolumeId(), "vid", 0L);
		oc.write(location, "location", Vector3f.ZERO);
		oc.write(getId(), "schema", 0L);
	}
	// Read all unique parameter values using the JmeImporter utility
	public void read(JmeImporter im) throws IOException {
		InputCapsule ic = im.getCapsule(this);
		setUniqueId(ic.readLong("uid", 0L));
		setVolumeId(ic.readLong("vid", 0L));
		location.set((Vector3f)ic.readSavable("location", Vector3f.ZERO));
		GameRegistry.getInstance().initializeControl(this, ic.readLong("schema", 0L));
	}
	
	/* CONVENIENCE METHODS */
	
	// Gets the objects name which is inherited from the schematic 
	public String getName()	{
		return (schema != null) ? schema.getName() : "";
	}
	// Gets the objects icon which is inherited from the schematic
	public String getIcon()	{
		return (schema != null) ? schema.getIcon() : "none";
	}
	// Gets the path to the objects icon based on a pre-determined location
	public String getIconPath()	{
		return "Interface/icons/" + getIcon() + ".png";
	}
	// Gets the objects type which is inherited from the schematic
	public ObjectType getType() {
		return (schema != null) ? schema.getObjectType() : GENERIC_TYPE;
	}
	// A boolean check to see if an object is of a certain type
	public boolean isType(ObjectType type) {
		return getType().matches(type);
	}
	public boolean inSceneGraph() {
		return (spatial != null) && (spatial.getParent() != null);
	}
	// A boolean check to see if an object emits any light.
	public boolean hasLight() {
		return false;
	}
	// Gets the light associated with this object if there is any.
	public Light getLight() {
		if (spatial != null) {
			return spatial.getControl(LightControl.class).getLight();
		}
		return null;
	}
	public void addLightToWorld(Light light) {
		if (spatial != null) {
			Spatial next = spatial;
			while (next.getParent() != null) {
				next = next.getParent();
				if (next.getName() != null && next.getName().equals("world")) {
					next.addLight(light);
					System.out.println("Adding Light: " + light);
					break;
				}
			}
		}
	}
	// Adds a new control to the underlying spatial
	public void addControl(Control control) {
		if (spatial != null) {
			spatial.addControl(control);
		}
	}
	// Gets a control from the underlying spatial
	public <T extends Control> T getControl(java.lang.Class<T> controlType) {
		if (spatial != null) {
			return spatial.getControl(controlType);
		}
		return null;
	}
	// Removes a control from the underlying spatial
	public void removeControl(Control control) {
		if (spatial != null) {
			spatial.removeControl(control);
		}
	}
	// Attempts to locate and return the underlying PhysicsControl return null if none exists
	public PhysicsControl getPhysicsControl() {
		if (spatial != null) {
			return spatial.getControl(PhysicsControl.class);
		}
		return null;
	}
	public void setEnabledPhysics(boolean state) {
		PhysicsControl physicsControl = getPhysicsControl();
		if (physicsControl != null) {
			physicsControl.setEnabled(state);
		}
	}
	// A boolean check to see if this object responds to the specified action
	public boolean matchAction(String action) {
		for (String a : getActionList()) {
			if (a.equals(action)) {
				return true;
			}
		}
		return false;
	}
	// A boolean check to see if this object is at the specified location
	public boolean matchLocation(Vector3f loc) {
		return this.matchLocation(loc.x, loc.y, loc.z);
	}
	// A boolean check to see if this object is at the specified location
	public boolean matchLocation(float x, float y, float z) {
		Vector3f source = getWorldTranslation();
		return (source.x == x) && Math.abs(source.y - y) < 0.2f && (source.z == z);
	}

	// Convenience method to set the local translation of the underlying spatial
	public void setWorldTranslation(float x, float y, float z) {
		if (hasSpatial()) {
			PhysicsControl physicsControl = getPhysicsControl();
			if (physicsControl instanceof PhysicsRigidBody) {
				TempVars temp = TempVars.get();
				temp.vect1.set(x, y, z);
				((PhysicsRigidBody)physicsControl).setPhysicsLocation(temp.vect1);
				temp.release();
			} else {
				spatial.setLocalTranslation(x, y, z);
			}
		}
	}
	// Convenience method to set the local translation of the underlying spatial
	public void setWorldTranslation(Vector3f translation) {
		if (hasSpatial()) {
			PhysicsControl physicsControl = getPhysicsControl();
			if (physicsControl instanceof PhysicsRigidBody) {
				((PhysicsRigidBody)physicsControl).setPhysicsLocation(translation);
			} else {
				spatial.setLocalTranslation(translation);
			}
		}
	}
	// Convenience method to get the local translation of the underlying spatial
	public Vector3f getWorldTranslation() {
		if (hasSpatial()) {
			PhysicsControl physicsControl = getPhysicsControl();
			if (physicsControl instanceof PhysicsRigidBody) {
				return ((PhysicsRigidBody)physicsControl).getPhysicsLocation();
			} else {
				return spatial.getWorldTranslation();
			}
		} else {
			return location;
		}
	}
	// Convenience method to get the local translation of the underlying spatial and store it in an existing Vector3f
	public Vector3f getWorldTranslation(Vector3f store) {
		if (store == null) {
			store = new Vector3f();
		}
		if (hasSpatial()) {
			PhysicsControl physicsControl = getPhysicsControl();
			if (physicsControl instanceof PhysicsRigidBody) {
				((PhysicsRigidBody)physicsControl).getPhysicsLocation(store);
			} else {
				store.set(spatial.getWorldTranslation());
			}
		}
		return store;
	}
	public Vector3f setStoredLocation(float x, float y, float z) {
		return location.set(x, y, z);
	}
	// Get the stored location that should be used to initialize the location of the underlying spatial
	public Vector3f getStoredLocation() {
		return location;
	}

	// Convenience method to set the rotation of the underlying spatial
	public void setRotation(Quaternion rotation) {
		PhysicsControl physicsControl = getPhysicsControl();
		if (physicsControl instanceof PhysicsRigidBody) {
			((PhysicsRigidBody)physicsControl).setPhysicsRotation(rotation);
		} else if (spatial != null) {
			spatial.setLocalRotation(rotation);
		}
	}
	// Convenience method to set the rotation of the underlying spatial
	public Quaternion getWorldRotation(Quaternion store) {
		PhysicsControl physicsControl = getPhysicsControl();
		if (physicsControl instanceof PhysicsRigidBody) {
			((PhysicsRigidBody)physicsControl).getPhysicsRotation(store);
		} else if (spatial != null) {
			store.set(spatial.getWorldRotation());
		}
		return store;
	}
	// A boolean check to see if a set of coordinates intersects with this object and therefore should not be allowed to pass
	public boolean isPassable(Vector3f position) {
		return true;
	}
	// Convenience method to forward collision checks to the attached spatial if there is one
	public int collideWith(Collidable other, CollisionResults results) {
		if (hasSpatial()) {
			return spatial.collideWith(other, results);
		} else {
			return 0;
		}
	}
	public String getChunkName() {
		if (spatial != null) {
			Spatial next = spatial;
			while (next.getParent() != null) {
				next = next.getParent();
				if (next.getName() != null && next.getName().contains("ObjectNode:")) {
					return next.getName().substring(12);
				}
			}
		}
		return "";
	}
	// Convenience method to get the material of the underlying geometry
	public Material getMaterial() {
		Geometry geom = getGeometry();
		if (geom != null) {
			return geom.getMaterial();
		}
		return null;
	}
	public Geometry getGeometry() {
		if (spatial instanceof Geometry) {
			return (Geometry)spatial;
		}
		if (spatial instanceof Node) {
			for (Spatial s : ((Node)spatial).getChildren()) {
				if (s instanceof Geometry) {
					return (Geometry)s;
				}
			}
		}
		return null;
	}
	
	/* STATIC METHODS */
	
	// Conversion method getting the represented object as a String
	protected static String getDelimitedString(String[] list, String delimiter) {
		String result = "";
		for (int s = 0; s < list.length; s++) {
			result = (s == 0) ? result + list[s] : result + delimiter + list[s];
		}
		return result;
	}
}