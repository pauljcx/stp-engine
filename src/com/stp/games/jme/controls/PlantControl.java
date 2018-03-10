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
import com.jme3.bounding.BoundingBox;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.export.binary.ByteUtils;
// Java Dependencies
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/** @author Paul Collins
 *  @version v0.1 ~ 05/04/2013
 *  HISTORY: Version 0.1 created PlantControl for setting up and accessing user data for plants 02/10/2014
 */
public class PlantControl extends GameControl {
	// Field Params
	public static final Param AGE = new Param("Age", Integer.class, 0);
	public static final Param MIN_HEIGHT = new Param("MinHeight", Float.class, 1);
	public static final Param MAX_HEIGHT = new Param("MaxHeight", Float.class, 2);
	public static final Param STATE = new Param("State", State.class, 3);
	public static final Param DENSITY = new Param("Density", Integer.class, 4);	
	public static final Param CATEGORY = new Param("Category", Category.class, 5);
	public static final Param RESOURCES = new Param("Resources",String.class, 6);
	public static final Param QUANTITIES = new Param("Quantities", String.class, 7);
	public static final Param SCHEMA_ALT = new Param("Alternate Schematic", String.class, 8);
	public static final Param BOUNDS = new Param("Bounds", String.class, 9);
	
	public static final Param[] PLANT_PARAMS = { AGE, MIN_HEIGHT, MAX_HEIGHT, STATE, DENSITY, CATEGORY, RESOURCES, QUANTITIES, SCHEMA_ALT, BOUNDS };
	public static final ObjectType PLANT_TYPE = new ObjectType<PlantControl>("Plant", PlantControl.class, PLANT_PARAMS);
	
	public static final String[] PLANT_ACTIONS = { "harvest" };
	public static final String[] TREE_ACTIONS = { "chop", "gather" };
	
	// Plant States
	public enum State {
		ACTIVE("active"),
		DORMANT("dormant"),
		DEAD("dead"),
		UNROOTED("unrooted");
		private String text;
		private State(String text) {
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
		public boolean matches(Object obj) {
			if (obj != null) {
				return text.equals(obj.toString());
			}
			return false;
		}
	}
	
	// Plant Categories
	public enum Category {
		UNKNOWN("unknown"),
		CACTUS("cactus"),
		FLOWER("flower"),
		POACEAE("poaceae"),
		SHRUB("shrub"),
		TREE("tree");
		private String text;
		private Category(String text) { this.text = text; }
		public byte index() { return (byte)ordinal(); }
		public String text() { return text; }
		public String toString() { return text; }
		public boolean matches(Object obj) {
			if (obj != null) {
				return text.equals(obj.toString());
			}
			return false;
		}
	}
	/*public static final Schematic DOGBANE = new Schematic("Dog Bane",
													"none",
													Type.PLANT,
													Schematic.Category.PLANT,
													"Models/dogbane.j3o",
													"", // Description
													new Object[] { 1, // Age
														State.ACTIVE, // State
														1, // Density
														Category.POACEAE.ordinal(), // Category
														"Stalks", // Resources
														"0" }); // Quantities
	*/
	
	protected int[] quantities = new int[0];
	protected int age = -1;
	protected State state = State.ACTIVE;
	protected Spatial oldSpatial = null;
	
	
	public PlantControl() {
		this(null);
	}
	
	public PlantControl(Schematic schema) {
		super(schema);
	}
	// Inherited from super class, all spatial updates between frames should be implemented here
	@Override
	protected void controlUpdate(float tpf)	{
		if (oldSpatial != null) {
			if (oldSpatial.getParent() instanceof Node) {
				((Node)oldSpatial.getParent()).detachChild(oldSpatial);
			}
			oldSpatial = null;
		}
	}
	@Override
	public Control cloneForSpatial(Spatial spatial) {
		final PlantControl control = new PlantControl(schema);
		control.setSpatial(spatial);
		return control;
	}
	@Override
	public void setFields(Schematic schema) {
		super.setFields(schema);
		if (age < 0) {
			setAge((int)(Math.random()*schema.getInt(AGE)*0.33)+33);
		} else {
			setAge(age);
		}
		setState((byte)schema.getInt(STATE));
		
	}
	@Override
	public void resetFields() {
		super.resetFields();
		if (spatial != null) {
			// Setup Item UserData
			setAge(10);
			setState(State.ACTIVE);
			setQuantities("");
		}
	}
	public void copyFields(PlantControl control) {
		super.copyFields(control);
		if (spatial != null) {
			// Copy UserData
			setAge(control.getAge());
			setState(control.getState());
		}
	}
	@Override
	public void writeFields(OutputStream os) throws IOException {
		super.writeFields(os);
		os.write(ByteUtils.convertToBytes((short)getAge()));
		os.write(ByteUtils.convertToBytes((short)getState().ordinal()));
		/*String qty = getQuantities();
		os.write(ByteUtils.convertToBytes((short)qty.length()));
		if (qty.length() > 0)	{
			os.write(qty.getBytes("UTF8"));
		}*/
	}
	@Override
	public void readFields(InputStream is) throws IOException	{
		super.readFields(is);
		setAge(ByteUtils.readShort(is));
		setState(ByteUtils.readShort(is));
		/*byte[] chars = new byte[ByteUtils.readShort(is)];
		if (chars.length > 0) {
			setQuantities(new String(chars, "UTF8"));
		} else {
			setQuantities("");
		}*/
	}
	public void setAge(int age) {
		this.age = age;
		if (spatial != null) {
			spatial.setLocalScale((getMinHeight()/getMaxHeight())*(float)Math.random()*10);
			//spatial.setLocalScale(age * 0.03f);
		}
	}
	public int getAge() {
		return age;
	}
	public int getDensity() {
		return (schema != null) ? schema.getInt(DENSITY) : 1;
	}
	public float getMinHeight() {
		return (schema != null) ? schema.getFloat(MIN_HEIGHT) : 1f;
	}
	public float getMaxHeight() {
		return (schema != null) ? schema.getFloat(MAX_HEIGHT) : 1f;
	}
	public String[] getResourceList() {
		// Resources are saved in a string delimited by a comma
		return (schema != null) ? schema.getString(RESOURCES).split(",") : new String[0];
	}
	public void setQuantities(String values) {
		if (values.length() > 0) {
			String[] list = values.split(",");
			quantities = new int[list.length];
			for (int q = 0; q < quantities.length; q++)	{
				quantities[q] = Integer.valueOf(list[q]);
			}
		}
	}
	public String getQuantities() {
		String values = "";
		for (int q = 0; q < quantities.length; q++) {
			values = (q > 0) ? values + "," + quantities[q] : "" + quantities[q];
		}
		return values;
	}
	public int[] getQuantityList() {
		if (quantities.length == 0)	{
			generateResources();
		}
		return quantities;
	}
	public void setState(int state) {
		if (state >= 0 && state < State.values().length) {
			setState(State.values()[state]);
		} else {
			setState(State.ACTIVE);
		}
	}
	public void setState(String state) {
		setState(getState(state));
	}
	public void setState(State state) {
		this.state = state;
	}
	public State getState() {
		return getState(getStateString());
	}
	public String getStateString()	{
		return state.text();
	}
	public byte getStateIndex() {
		return getState().index();
	}
	public State getState(String state)	{
		for (State s : State.values()) {
			if (s.matches(state)) {
				return s;
			}
		}
		return State.ACTIVE;
	}
	public boolean setDead() {
		if (!isDead()) {
			this.state = State.DEAD;
			swapMesh(spatial.getName(), "cut");
			if (quantities.length == 0)	{
				generateResources();
			}
			/*String schemaName = getAlternateSchema();
			if (schemaName.length() > 0) {
				setSchematic(SchemaRegistry.getInstance().get(schemaName));
				this.oldSpatial = spatial;
				if (spatial != null) {
					spatial.removeControl(this);
				}
				setActive(false);
				return true;
			}*/
			return true;
		}
		return false;
	}
	public Category getCategory() {
		return (schema != null) ? Category.values()[schema.getInt(CATEGORY)] : Category.UNKNOWN;
	}
	public Category getCategory(String category) {
		for (Category c: Category.values()) {
			if (c.matches(category)) {
				return c;
			}
		}
		return Category.UNKNOWN;
	}
	public String getAlternateSchema() {
		return (schema != null) ? schema.getString(SCHEMA_ALT) : "";
	}
	public boolean isPlantActive()	{
		return State.ACTIVE.matches(getStateString());
	}
	public boolean isDormant() {
		return State.DORMANT.matches(getStateString());
	}
	public boolean isDead()	{
		return State.DEAD.matches(getStateString());
	}
	public boolean isUnrooted() {
		return State.UNROOTED.matches(getStateString());
	}
	public boolean isTree() {
		return getCategory().matches(Category.TREE);
	}
	public String[] getActionList() {
		if (isTree()) {
			return TREE_ACTIONS;
		} else {
			return PLANT_ACTIONS;
		}
	}
	private void generateResources() {
		String[] resources = getResourceList();
		quantities = new int[resources.length];
		for (int r = 0; r < resources.length; r++) {
			int age = getAge();
			double ratio = 1.25;
			double scale = (Math.random() *0.6)+0.4;
			quantities[r] = (int)(age * ratio * scale);
		}
	}
	public int getResourceQuantity(String resource) {
		String[] resources = getResourceList();
		for (int r = 0; r < resources.length; r++) {
			if (resources[r].equals(resource)) {
				return quantities[r];
			}
		}
		return 0;
	}
	public int depleteResource(String resource, int amount) {
		String[] resources = getResourceList();
		for (int r = 0; r < resources.length; r++) {
			if (resources[r].equals(resource)) {
				quantities[r] = quantities[r] - amount;
				if (quantities[r] < 0) {
					return quantities[r] + amount;
				} else {
					return quantities[r];
				}
			}
		}
		return 0;
	}
	public static PlantControl createControl(Schematic schema) {
		switch (Category.values()[schema.getInt(CATEGORY)]) {
			case TREE: return new TreeControl(schema);
			default: return new PlantControl(schema);
		}
	}
}