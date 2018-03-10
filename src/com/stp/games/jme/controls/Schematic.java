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
import com.jme3.scene.Spatial;
import com.jme3.scene.Node;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.control.LightControl;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.light.PointLight;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.scene.control.BillboardControl;
// Java Dependencies
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.nio.ByteBuffer;

import com.stp.util.JavaIO;
import com.stp.util.XMLObject;
import com.stp.games.jme.controls.GameControl.ObjectType;
import com.stp.games.jme.controls.ResourceControl.ResourceType;
import com.stp.games.jme.GameRegistry;

/** @author Paul Collins
 *  @version v0.1 ~ 09/21/2012
 *  HISTORY: Version 0.1 created Schematic object ~ 09-21-2012
 */
public class Schematic implements XMLObject {
	public enum Category {
		TOOL("Tool"),
		WEAPON("Weapon"),
		CLOTHING("Clothing"),
		ARMOR("Armor"),
		PLANT("Plant"),
		FURNISHING("Furnishing"),
		STRUCTURE("Structure"),
		RESOURCE("Resource"),
		CREATURE("Creature"),
		CHARACTERS("Characters"),
		VEHICLE("Vehicle"),
		OTHER("Other");
		private String text;
		private Category(String text) { this.text = text; }
		public byte index() { return (byte)ordinal(); }
		public String text() { return text; }
		public String toString() { return text; }
	}
	public enum ToolEffect {
		REQUIRED("Required"),
		SPEED("Speed"),
		QUALITY("Quality");
		private String text;
		private ToolEffect(String text) { this.text = text; }
		public byte index() { return (byte)ordinal(); }
		public String text() { return text; }
		public String toString() { return text; }
	}
	public static class ResourceParam {
		public ResourceType resourceType = ResourceControl.DEFAULT_TYPE;
		public String subType;
		public short quantity = 0;
		public String description = "Any Resource";
		public ResourceParam(String typeName, String subType, int quantity, String description) {
			this (ResourceControl.findResourceType(typeName), subType, quantity, description);
		}
		public ResourceParam(ResourceType resourceType, String subType, int quantity, String description) {
			this.resourceType = resourceType;
			this.subType = subType;
			this.quantity = (short)quantity;
			this.description = description;
		}
		public ResourceType getResourceType() {
			return resourceType;
		}
		public String getSubType() {
			return subType;
		}
		public short getQuantity()	{
			return quantity;
		}
		public String getDescription()	{
			return description;
		}
		public String toString()	{
			return description + " (" + quantity + ")";
		}
	}
	public static class ItemParam	{
		public ObjectType type = GameControl.GENERIC_TYPE;
		public ItemControl.Size size = ItemControl.Size.ANY;
		public Object[] values = new Object[0];
		public short quantity = 0;
		public String description = "Generic Item";
		public ItemParam(String typeName, int sizeIndex, Object[] values, int quantity, String description) {
			this (GameRegistry.findObjectType(typeName), ItemControl.Size.values()[sizeIndex], values, quantity, description);
		}
		public ItemParam(ObjectType type, ItemControl.Size size, Object[] values, int quantity, String description) {
			this.type = type;
			this.size = size;
			this.values = values;
			this.quantity = (short)quantity;
			this.description = description;
		}
		public ObjectType getType() {
			return type;
		}
		public ItemControl.Size getSize() {
			return size;
		}
		public Object[] getValues() {
			return values;
		}
		public short getQuantity() {
			return quantity;
		}
		public String getDescription() {
			return description;
		}
		public String toString() {
			return description + " (" + quantity + ")";
		}
	}
	public static class ToolParam
	{
		public ToolControl.RatingType actionRating = ToolControl.RatingType.BASH;
		public ItemControl.Size size = ItemControl.Size.ANY;
		public ToolEffect effect = ToolEffect.REQUIRED;
		public String description = "Cutting Tool";
		public float value = 0;		
		public ToolParam(int ratingIndex, int sizeIndex, int effectIndex, String description, float value)
		{
			this (ToolControl.RatingType.values()[ratingIndex], ItemControl.Size.values()[sizeIndex], ToolEffect.values()[effectIndex], description, value);
		}
		public ToolParam(ToolControl.RatingType actionRating, ItemControl.Size size, ToolEffect effect, String description, float value)
		{
			this.actionRating = actionRating;
			this.size = size;
			this.effect = effect;
			this.description = description;
			this.value = value;
		}
		public ToolControl.RatingType getActionRating()
		{
			return actionRating;
		}
		public ItemControl.Size getSize()
		{
			return size;
		}
		public ToolEffect getEffect()
		{
			return effect;
		}
		public String getDescription()
		{
			return description;
		}
		public float getValue()
		{
			return value;
		}
		public String toString()
		{
			return description;
		}
	}
	public static class Step
	{
		public String name = "Step";
		public String description = "";
		public float duration = 0;
		public ToolParam tool = null;
		public Step(String name, String description, float duration, ToolParam tool)
		{
			this.name = name;
			this.description = description;
			this.duration = duration;
			this.tool = tool;
		}
		public String getName()
		{
			return name;
		}
		public String getDescription()
		{
			return description;
		}
		public float getDuration()
		{
			return duration;
		}
		public ToolParam getTool()
		{
			return tool;
		}
		public String toString()
		{
			return name;
		}
	}
	
	private long id;
	private String name;
	private String icon;
	private String model;
	private String materialPath = "";
	private String description;
	private Category category;
	private ObjectType objectType;
	private Object[] params;
	
	private ArrayList<Step> steps = new ArrayList<Step>();
	private ArrayList<ResourceParam> resourceParams = new ArrayList<ResourceParam>();
	private ArrayList<ItemParam> itemParams = new ArrayList<ItemParam>();
	
	public Schematic() {
		this ("Cube", "empty", GameControl.GENERIC_TYPE, Category.OTHER, "Box:x=0.5,y=0.5,z=0.5,r=1,b=1,g=1,a=1", "", new Object[0]);
	}
	public Schematic(String name, String icon, ObjectType type, Category category, String model, String description, Object[] params) {
		this (name, icon, type, category, model, description, params, new Step[0], new ResourceParam[0], new ItemParam[0]);
	}
	public Schematic(String name, String icon, ObjectType type, Category category, String model, String description, Object[] params, Step[] steps, ResourceParam[] resources) {
		this (name, icon, type, category, model, description, params, steps, resources, new ItemParam[0]);
	}
	public Schematic(String name, String icon, ObjectType type, Category category, String model, String description, Object[] params, Step[] steps, ItemParam[] items) {
		this (name, icon, type, category, model, description, params, steps, new ResourceParam[0], items);
	}
	public Schematic(String name, String icon, ObjectType type, Category category, String model, String description, Object[] params, Step[] steps, ResourceParam[] resources, ItemParam[] items) {
		this.name = name;
		this.icon = icon;		
		this.category = category;
		this.model = model;
		this.materialPath = "";
		this.params = params;
		this.setObjectType(type);
		this.description = description;
		for (Step s : steps) {
			this.steps.add(s);
		}
		for (ResourceParam r : resources) {
			this.resourceParams.add(r);
		}
		for (ItemParam i : items) {
			this.itemParams.add(i);
		}
	}
	public Object[] getParameters() {
		return params;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public String getModel() {
		return model;
	}
	public String getIcon() {
		return icon;
	}
	public String getIconPath() {
		return icon;//"Interface/Icons/" + icon + ".png";
	}
	public String getMaterialPath() {
		return materialPath;
	}
	public String getDescription() {
		return description;
	}
	public ObjectType getObjectType() {
		return objectType;
	}
	public Category getCategory() {
		return category;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public void setMaterialPath(String path) {
		this.materialPath = path;
	}
	public void setObjectType(ObjectType type)	{
		this.objectType = type;
		if (this.objectType != type || getParamCount() != objectType.getParams().size()) {
			params = new Object[objectType.getParams().size()];
		}
	}
	public void setCategory(Category category) {
		this.category = category;
	}
	public Object get(GameControl.Param p) {
		return get(p.getIndex());
	}
	public Object get(int p)	{
		if (p < params.length) {
			return params[p];
		}
		return null;
	}
	public int getInt(GameControl.Param p) {
		return getInt(p.getIndex());
	}
	public int getInt(int p) {
		if (p < params.length) {
			return (params[p] instanceof Integer) ? (Integer)params[p] : 0;
		}
		return 0;
	}
	public float getFloat(GameControl.Param p) {
		return getFloat(p.getIndex());
	}
	public float getFloat(int p) {
		if (p < params.length) {
			return (params[p] instanceof Float) ? (Float)params[p] : 0.0f;
		}
		return 0.0f;
	}
	public String getString(GameControl.Param p) {
		return getString(p.getIndex());
	}
	public String getString(int p) {
		if (p < params.length) {
			return (params[p] != null) ? params[p].toString() : "";
		}
		return "";
	}
	public boolean getBoolean(GameControl.Param p) {
		return getBoolean(p.getIndex());
	}
	public boolean getBoolean(int p) {
		if (p < params.length) {
			return (params[p] instanceof Boolean) ? (Boolean)params[p] : false;
		}
		return false;
	}
	public void set(int p, Object value) {
		if (p >= 0 && p < params.length) {
			params[p] = value;
		}
	}
	public void set(GameControl.Param p, Object value) {
		if (p.getParamClass().isEnum()) {
			set(p.getIndex(), value.toString());
			return;
		}
		if (p.getParamClass() == Boolean.class) {
			set(p.getIndex(), Boolean.valueOf(value.toString()));
			return;
		}
		if (p.getParamClass() == Integer.class) {
			set(p.getIndex(), Integer.valueOf("0" + value.toString()));
			return;
		}
		if (p.getParamClass() == Float.class) {
			set(p.getIndex(), Float.valueOf("0" + value.toString()));
			return;
		}
		if (p.getParamClass() == Long.class) {
			set(p.getIndex(), Long.valueOf("0" + value.toString()));
			return;
		}
		if (p.getParamClass() == String.class) {
			set(p.getIndex(), value.toString());
			return;
		}
	}
	public int getParamCount() {
		return (params != null) ? params.length : 0;
	}
	public void syncParamFields() {
		if (getParamCount() < objectType.getParams().size()) {
			Object[] newParams = new Object[objectType.getParams().size()];
			for (int p = 0; p < getParamCount(); p++) {
				newParams[p] = params[p];
			}
			params = newParams;
		}
	}
	public void addStep(Step step) {
		steps.add(step);
	}
	public void removeStep(Step step) {
		steps.remove(step);
	}
	public ArrayList<Step> getSteps() {
		return steps;
	}
	public void addResource(ResourceParam resource) {
		resourceParams.add(resource);
	}
	public void removeResource(ResourceParam resource) {
		resourceParams.remove(resource);
	}
	public ArrayList<ResourceParam> getRequiredResources() {
		return resourceParams;
	}
	public ArrayList<ItemParam> getRequiredItems() {
		return itemParams;
	}
	public GameControl createNew(AssetManager assetManager) {
		Spatial spatial = createSpatial(assetManager);
		GameControl control = spatial.getControl(GameControl.class);
		if (control == null) {
			control = objectType.createControl();
			control.setSchematic(this);
			spatial.addControl(control);
			/*if (control.isType(GameControl.Type.STRUCTURE)) {
				Node structNode = new Node("Structure");
				structNode.attachChild(spatial);
				String emitter = getString(StructureControl.EMITTER);
				if (emitter.equals("fire")) {
					ParticleEmitter fire = new ParticleEmitter("Emitter", Type.Triangle, 15);
					Material mat_red = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
					mat_red.setTexture("Texture", assetManager.loadTexture("Textures/fire.png"));
					fire.setMaterial(mat_red);
					fire.setImagesX(2);
					fire.setImagesY(2); // 2x2 texture animation
					fire.setEndColor(  new ColorRGBA(1f, 0f, 0f, 1f));   // red
					fire.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
					fire.getParticleInfluencer().setInitialVelocity(new Vector3f(0,0.9f,0));
					fire.setStartSize(0.35f);
					fire.setEndSize(0.01f);
					fire.setGravity(0,0,0);
					fire.setLowLife(1f);
					fire.setHighLife(2f);
					fire.getParticleInfluencer().setVelocityVariation(0.3f);
					structNode.attachChild(fire);
				}
				structNode.addControl(control);
			} else {
				spatial.addControl(control);
			}*/
		}
		control.setFields(this);
		control.initializeAssets(assetManager);
		//System.out.println("Created game control: " + control);
		return control;
	}
	public Spatial initControl(AssetManager assetManager, GameControl control) {
		Spatial spatial = createSpatial(assetManager);
		if (spatial.getControl(GameControl.class) == null) {
			control.setSchematic(this);
			spatial.addControl(control);
		}
		control.setFields(this);
		control.initializeAssets(assetManager);
		return spatial;
	}
	private Spatial createSpatial(AssetManager assetManager) {
		Spatial spatial = null;
		if (model.contains("Box:")) {
			Vector3f extents = new Vector3f(0.5f, 0.5f, 0.5f);
			ColorRGBA boxColor = new ColorRGBA(1f, 1f, 1f, 1f);
			for (String pair : model.substring(4).split(",")) {
				String[] parts = pair.split("=");
				if (parts.length == 2) {
					if (parts[0].equals("x")) {
						extents.setX(Float.valueOf(parts[1]));
					} else if (parts[0].equals("y")) {
						extents.setY(Float.valueOf(parts[1]));
					} else if (parts[0].equals("z")) {
						extents.setZ(Float.valueOf(parts[1]));
					} else if (parts[0].equals("r")) {
						boxColor.r = Float.valueOf(parts[1]);
					} else if (parts[0].equals("g")) {
						boxColor.g = Float.valueOf(parts[1]);
					} else if (parts[0].equals("b")) {
						boxColor.b = Float.valueOf(parts[1]);
					} else if (parts[0].equals("a")) {
						boxColor.a = Float.valueOf(parts[1]);
					}
				}
			}
			spatial = new Geometry(name, new Box(extents.getX(), extents.getY(), extents.getZ()));
			Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			m.setColor("Color", boxColor);
			spatial.setMaterial(m);
		} else if (model.contains("Sphere:")) {
			float radius = 0.5f;
			int zSamples = 8;
			int rSamples = 8;
			ColorRGBA sphereColor = new ColorRGBA(1f, 1f, 1f, 1f);
			for (String pair : model.substring(7).split(",")) {
				String[] parts = pair.split("=");
				if (parts.length == 2) {
					if (parts[0].equals("r")) {
						radius = Float.valueOf(parts[1]);
					} else if (parts[0].equals("rS")) {
						rSamples = Integer.valueOf(parts[1]);
					} else if (parts[0].equals("zS")) {
						zSamples = Integer.valueOf(parts[1]);
					} else if (parts[0].equals("r")) {
						sphereColor.r = Float.valueOf(parts[1]);
					} else if (parts[0].equals("g")) {
						sphereColor.g = Float.valueOf(parts[1]);
					} else if (parts[0].equals("b")) {
						sphereColor.b = Float.valueOf(parts[1]);
					} else if (parts[0].equals("a")) {
						sphereColor.a = Float.valueOf(parts[1]);
					}
				}
			}
			spatial = new Geometry(name, new Sphere(zSamples, rSamples, radius));
			Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			m.setColor("Color", sphereColor);
			spatial.setMaterial(m);
		} else if (model.contains("Cylinder:")) {
			float radius = 0.5f;
			float height = 1f;
			int aSamples = 1;
			int rSamples = 8;
			ColorRGBA cylinderColor = new ColorRGBA(1f, 1f, 1f, 1f);
			for (String pair : model.substring(9).split(",")) {
				String[] parts = pair.split("=");
				if (parts.length == 2) {
					if (parts[0].equals("r")) {
						radius = Float.valueOf(parts[1]);
					} else if (parts[0].equals("h")) {
						height = Float.valueOf(parts[1]);
					} else if (parts[0].equals("aS")) {
						aSamples = Integer.valueOf(parts[1]);
					} else if (parts[0].equals("rS")) {
						rSamples = Integer.valueOf(parts[1]);
					} else if (parts[0].equals("r")) {
						cylinderColor.r = Float.valueOf(parts[1]);
					} else if (parts[0].equals("g")) {
						cylinderColor.g = Float.valueOf(parts[1]);
					} else if (parts[0].equals("b")) {
						cylinderColor.b = Float.valueOf(parts[1]);
					} else if (parts[0].equals("a")) {
						cylinderColor.a = Float.valueOf(parts[1]);
					}
				}
			}
			spatial = new Geometry(name, new Cylinder(aSamples, rSamples, radius, height, true));
			Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			m.setColor("Color", cylinderColor);
			spatial.setMaterial(m);
		} else {
			spatial = assetManager.loadModel(model);
			if (spatial instanceof Geometry && materialPath.length() > 0) {
				Material m = assetManager.loadMaterial(materialPath);
				((Geometry)spatial).setMaterial(m);
			}
		}
		spatial.setName(name);
		return spatial;
	}
	/*public Spatial initControl(AssetManager assetManager, GameControl control) {
		control.setSchematic(this);
		switch (type) {
			case GENERIC:
				Node emptyNode = new Node("Empty");
				emptyNode.addControl(control);
				return emptyNode;
			case SENTIENT:
				SentientControl sentient = (SentientControl)control;
				Spatial rig = createSpatial(assetManager);
				sentient.setFields(this);
				rig.addControl(sentient);
				return rig;
			case PLANT:
				Spatial plant = createSpatial(assetManager);
				plant.setQueueBucket(Bucket.Transparent);
				plant.addControl(control);
				control.setFields(this);
				return plant;
			case CONTAINER:
				Spatial container = createSpatial(assetManager);
				if (getInt(ContainerControl.LOCK_TYPE) == 5) {
					Node storageNode = new Node("Container");
					storageNode.attachChild(container);
					
					if (getBoolean(ContainerControl.SEEDED)) {
						if (getInt(ContainerControl.RESOURCE_TYPE) == 2) {
							Schematic schema = SchemaRegistry.getInstance().get("Stone");
							if (schema != null) {
								ResourceControl resource = new ResourceControl(schema);
								schema.initControl(assetManager, resource);
								((ContainerControl)control).addItem(0, resource);
							}
						}
					}
					
					BillboardControl bControl = new BillboardControl();
					bControl.setAlignment(BillboardControl.Alignment.Screen);*/
					/*Geometry geom = new Geometry("BillBoard", new Quad(3f, 3f));
					Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
					geom.addControl(bControl);
					storageNode.attachChild(geom);*/
					
					/*BitmapFont myFont = assetManager.loadFont("Font/calibri-24-outline.fnt");
					BitmapText text = text = new BitmapText(myFont);
					text.setQueueBucket(Bucket.Transparent);
					text.setText("0");
					text.setColor(new ColorRGBA(1f, 1f, 1f, 1f));
					text.setSize(0.3f);
					text.updateLogicalState(0);
					text.addControl(bControl);
					storageNode.attachChild(text);
					text.setLocalTranslation(0, 1f, 0);
					((ContainerControl)control).setTextNode(text);
					
					storageNode.addControl(control);
					control.setFields(this);
					return storageNode;
				} else {
					container.addControl(control);
					control.setFields(this);
					return container;
				}
			case STRUCTURE:
				Spatial structure = createSpatial(assetManager);
				Node structNode = new Node("Structure");
				structNode.attachChild(structure);
				String emitter = getString(StructureControl.EMITTER);
				if (emitter.equals("fire")) {
					ParticleEmitter fire = new ParticleEmitter("Emitter", Type.Triangle, 15);
					Material mat_red = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
					mat_red.setTexture("Texture", assetManager.loadTexture("Textures/fire.png"));
					fire.setMaterial(mat_red);
					fire.setImagesX(2);
					fire.setImagesY(2); // 2x2 texture animation
					fire.setEndColor(  new ColorRGBA(1f, 0f, 0f, 1f));   // red
					fire.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
					fire.getParticleInfluencer().setInitialVelocity(new Vector3f(0,0.9f,0));
					fire.setStartSize(0.35f);
					fire.setEndSize(0.01f);
					fire.setGravity(0,0,0);
					fire.setLowLife(1f);
					fire.setHighLife(2f);
					fire.getParticleInfluencer().setVelocityVariation(0.3f);
					structNode.attachChild(fire);
				}
				structNode.addControl(control);
				control.setFields(this);
				return structNode;
			case TOOL:
				Spatial tool = createSpatial(assetManager);
				tool.addControl(control);
				control.setFields(this);
				if (getInt(ToolControl.TOOL_CLASS) == ToolControl.ToolClass.LIGHT.ordinal()) {
					PointLight toolLight = new PointLight();
					LightControl lightControl = new LightControl(toolLight);
					tool.addControl(lightControl); // this spatial controls the position of this light.
				}
				return tool;
			default:
				Spatial spatial = createSpatial(assetManager);
				spatial.addControl(control);
				control.setFields(this);
				return spatial;
		}
	}*/
	/*public GameControl createControl() {
		switch (type) {
			case TOOL: return new ToolControl(this);
			case CLOTHING: return new ClothingControl(this);
			case RESOURCE: return new ResourceControl(this);
			case CONTAINER: return new ContainerControl(this);
			case STRUCTURE: return new StructureControl(this);
			case SENTIENT:  return new SentientControl(this);
			case CREATURE:  return new CreatureControl(this);
			case PLANT: return PlantControl.createControl(this);
			case STONE:  return new StoneControl(this);
			default: return new GameControl(this);
		}
	}*/
	/*public PlantControl createPlant(AssetManager assetManager)
	{
		PlantControl control = null;
		switch (PlantControl.Category.values()[getInt(PlantControl.CATEGORY)])
		{
			case TREE: control = new TreeControl(); break;
			default: control = new PlantControl(); break;
		}
		createSpatial(assetManager).addControl(control);
		control.setFields(this);
		return control;
	}*/
	public void write(OutputStream os) throws IOException {
		// Write Schematic Id 8 bytes
		JavaIO.writeLong(os, id);
		// Write Schematic Name variable bytes
		JavaIO.writeString(os, name);
		// Write Icon Name variable bytes
		JavaIO.writeString(os, icon);
		// Write Type 1 byte
		JavaIO.writeString(os, objectType.getName());
		// Write Category 1 byte
		os.write(category.index());
		// Write Model Name variable bytes
		JavaIO.writeString(os, model);
		// Write Description variable bytes
		JavaIO.writeString(os, description);
		// Write Param Values variable bytes
		JavaIO.writeObjects(os, params);
		// Write Steps
		os.write(JavaIO.convertToBytes((short)steps.size()));
		for (Step s : steps) {
			JavaIO.writeString(os, s.getName());
			JavaIO.writeString(os, s.getDescription());
			JavaIO.writeFloat(os, s.getDuration());
			ToolParam tool = s.getTool();
			JavaIO.writeBoolean(os, (tool != null));
			if (tool != null) {
				os.write(tool.getActionRating().index());
				os.write(tool.getSize().index());
				os.write(tool.getEffect().index());
				JavaIO.writeString(os, tool.getDescription());
				JavaIO.writeFloat(os, tool.getValue());
			}
		}
		// Write Resources
		os.write(JavaIO.convertToBytes((short)resourceParams.size()));
		for (ResourceParam r : resourceParams) {
			JavaIO.writeString(os, r.getResourceType().text());
			JavaIO.writeString(os, r.getSubType());
			JavaIO.writeShort(os, r.getQuantity());
			JavaIO.writeString(os, r.getDescription());
		}
		// Write Items
		os.write(JavaIO.convertToBytes((short)itemParams.size()));
		for (ItemParam i : itemParams) {
			JavaIO.writeString(os, i.getType().getName());
			os.write(i.getSize().index());
			JavaIO.writeObjects(os, i.getValues());
			JavaIO.writeShort(os, i.getQuantity());
			JavaIO.writeString(os, i.getDescription());
		}
	}
	public void read(InputStream is) throws IOException {
		// Read Schematic Id
		this.id = JavaIO.readLong(is);
		// Read Schematic Name
		this.name = JavaIO.readString(is);
		// Read Icon Name
		this.icon = JavaIO.readString(is);
		// Read Type
		this.objectType = GameRegistry.findObjectType(JavaIO.readString(is));
		// Read Category
		this.category = Category.values()[(byte)is.read()];
		// Read Model Name
		this.model = JavaIO.readString(is);
		// Read Description
		this.description = JavaIO.readString(is);
		// Read Param Values
		this.params = JavaIO.readObjects(is);
		syncParamFields();
		// Read Steps
		int count = JavaIO.readShort(is);
		for (int s = 0; s < count; s++) {
			Step step = new Step(JavaIO.readString(is), JavaIO.readString(is), JavaIO.readFloat(is), null);
			if (JavaIO.readBoolean(is)) {
				step.tool = new ToolParam((byte)is.read(), (byte)is.read(), (byte)is.read(), JavaIO.readString(is), JavaIO.readFloat(is));
			}
			steps.add(step);
		}
		// Read Resources
		resourceParams.clear();
		count = JavaIO.readShort(is);
		for (int r = 0; r < count; r++) {
			resourceParams.add(new ResourceParam(JavaIO.readString(is), JavaIO.readString(is), JavaIO.readShort(is), JavaIO.readString(is)));
		}
		// Read Items
		count = JavaIO.readShort(is);
		for (int i = 0; i < count; i++) {
			itemParams.add(new ItemParam(JavaIO.readString(is), (byte)is.read(), JavaIO.readObjects(is), JavaIO.readShort(is), JavaIO.readString(is)));
		}
	}
	public String[] getPropertyNames() {
		String[] nodeNames = new String[9 + params.length];
		for (int n = 0; n < nodeNames.length; n++) {
			nodeNames[n] = getNodeName(n);
		}
		return nodeNames;
	}
	public String getNodeName(int index) {
		switch(index) {
			case 0: return "id";
			case 1: return "base36";
			case 2: return "name";
			case 3: return "icon";
			case 4: return "type";
			case 5: return "category";
			case 6: return "model";
			case 7: return "material";
			case 8: return "description";
			default: return "param:" + objectType.getParamName(index - 9);
		}
	}
	public Object getProperty(String nodeName) {
		if (nodeName.equals("id")) {
			return id;
		} else if (nodeName.equals("base36")) {
			try { return GameRegistry.stringValue(id); } catch (Exception ex) { return "00000000"; }
		} else if (nodeName.equals("name")) {
			return name;
		} else if (nodeName.equals("icon")) {
			return icon;
		} else if (nodeName.equals("type")) {
			return objectType.getName();
		} else if (nodeName.equals("category")) {
			return category.toString();
		} else if (nodeName.equals("model")) {
			return model;
		} else if (nodeName.equals("material")) {
			return materialPath;
		} else if (nodeName.equals("description")) {
			return description;
		} else if (nodeName.contains("param:")) {
			return (objectType != null) ? get(objectType.getParamIndex(nodeName.substring(6))) : null;
		}
		return null;
	}
	public void setProperty(String nodeName, Object value) {
		this.setProperty(nodeName, value, "");
	}
	public void setProperty(String nodeName, Object value, String className) {
		if (nodeName.equals("id")) {
			this.id = (value instanceof Long) ? (Long)value : 0L;
		} else if (nodeName.equals("name")) {
			this.name = "" + value;
		} else if (nodeName.equals("icon")) {
			this.icon = "" + value;
		} else if (nodeName.equals("type")) {
			this.setObjectType(GameRegistry.findObjectType("" + value));
		} else if (nodeName.equals("category")) {
			for (Category c : Category.values()) {
				if (c.text().equals("" + value)) {
					this.category = c;
					break;
				}
			}
		} else if (nodeName.equals("model")) {
			this.model = "" + value;
		} else if (nodeName.equals("material")) {
			setMaterialPath("" + value);
		} else if (nodeName.equals("description")) {
			this.description = "" + value;
		} else if (nodeName.contains("data")) {
			set(Integer.valueOf(nodeName.substring(4)), value);
		} else if (nodeName.contains("param:")) {
			if (objectType != null) {
				set(objectType.getParamIndex(nodeName.substring(6)), value);
			} 
		}
	}
}