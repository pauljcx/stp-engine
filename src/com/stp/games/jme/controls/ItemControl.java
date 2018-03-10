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
import com.jme3.scene.control.Control;
import com.jme3.export.binary.ByteUtils;
// Java Dependencies
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/** @author Paul Collins
 *  @version v1.0 ~ 04/04/2014
 *  HISTORY: Version 1.0 reformatted methods and added comments updating version to 1.0. 04/04/2014
 *			Version 0.1 created ItemControl for setting up and accessing user data for items,
 *		all inventory and usable items must derive from from this class. 05/04/2013
 */
public class ItemControl extends GameControl {
	// An empty item used as a place holder in the inventory and containers
	public static final ItemControl NONE = new ItemControl(new Node("none"));

	// Field Parameters
	public static final Param SIZE = new Param("Size", Size.class, 0);
	public static final Param WEIGHT = new Param("Weight", Float.class, 1);
	public static final Param COMPOSITION = new Param("Composition", String.class, 2);
	
	public static final Param[] ITEM_PARAMS = { SIZE ,WEIGHT, COMPOSITION };
	public static final ObjectType ITEM_TYPE = new ObjectType<ItemControl>("Item", ItemControl.class, ITEM_PARAMS);
	
	public static final String[] ITEM_ACTIONS = { "grab" };

	// Item Sizes
	public enum Size {
		ANY("any", 1, 1),
		MICRO("micro", 1, 1),//1x1 Stackable
		SMALL("small", 1, 1),//1x1
		MEDIUM("medium", 1, 2),//1x2
		WIDE("medium-wide", 2, 1),//2x1
		LARGE("large", 2, 2),//2x2
		HUGE("huge", 3, 2),//3x2
		MACRO("macro", 4, 4);//4x4
		private String text;
		private int width = 1;
		private int height = 1;
		private Size(String text, int w, int h) {
			this.text = text;
			this.width = w;
			this.height = h;
		}
		public int getWidth() {
			return width;
		}
		public int getHeight() {
			return height;
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
	protected int durability;
	
	// Primary constructor with no parameters
	public ItemControl() {
		super();
	}
	public ItemControl(Schematic schema) {
		super(schema);
	}
	// Alternate constructor that links the control with it's spatial
	public ItemControl(Spatial obj)	{
		super();
		obj.addControl(this);
		resetFields();
	}
	
	/* INHERITED METHODS */
	
	// Creates a copy of this control to add to a new spatial
	@Override
	public Control cloneForSpatial(Spatial spatial)	{
		final ItemControl control = new ItemControl(schema);
		control.setSpatial(spatial);
		return control;
	}
	// Resets all parameters values to their defaults
	@Override
	public void resetFields() {
		if (spatial != null) {
			setDurability(100);
		}
	}
	// Copies all parameters from this control to another
	public void copyFields(ItemControl control)	{
		super.copyFields(control);
		if (spatial != null) {
			setDurability(control.getDurability());
		}
	}
	// Initializes parameters based on given schematic
	@Override
	public void setFields(Schematic schema)	{
		super.setFields(schema);
		setDurability(100);
	}
	// Returns a list of user actions that indicate how a player can interact with this item
	@Override
	public String[] getActionList()	{
		return ITEM_ACTIONS;
	}
		
	/* SAVING METHODS */
	
	public int getMemSize() {
		return 10;
	}
	// Write all unique parameter values to the given output stream
	@Override
	public void writeFields(OutputStream os) throws IOException	{
		super.writeFields(os);
		os.write(ByteUtils.convertToBytes((short)getDurability()));
	}
	// Read all unique parameter values from the given input stream
	@Override
	public void readFields(InputStream is) throws IOException {
		super.readFields(is);
		setDurability(ByteUtils.readShort(is));
	}
	// Write all unique parameter values to the given buffer
	@Override
	public void fillBuffer(ByteBuffer buffer) throws IOException	{
		super.fillBuffer(buffer);
		buffer.putShort((short)getDurability());
	}
	// Read all unique parameter values from the given buffer
	@Override
	public void readBuffer(ByteBuffer buffer) throws IOException {
		super.readBuffer(buffer);
		setDurability(buffer.getShort());
	}
	
	/* INSTANCE METHODS */
	
	// Sets the value of the durability parameter
	public void setDurability(int durability) {
		this.durability = durability;
	}
	// Gets the items durability value
	public int getDurability() {
		return durability;
	}
	
	/* CONVENIENCE METHODS */
	
	// A boolean check to determine if the item is a blank placeholder item
	public boolean isNone()	{
		if (spatial != null) {
			return spatial.getName().equals("none");
		}
		return false;
	}
	// A boolean check to determine if the item is a tool
	public boolean isTool()	{
		return getType().matches("Tool") && !isNone();
	}
	// A boolean check to determine if the item is an article of clothing
	public boolean isClothing() {
		return getType().matches("Clothing") && !isNone();
	}
	// Gets the value of the size parameter which is inherited from the schematic
	public Size getSize() {
		return (schema != null) ? Size.values()[schema.getInt(SIZE)] : Size.SMALL;
	}
	// Gets the value of the composition parameter which is inherited from the schematic
	public String getComposition() {
		return (schema != null) ? schema.getString(COMPOSITION) : "";
	}
	// Gets the items weight value which is inherited from the schematic
	public float getWeight() {
		return (schema != null) ? schema.getFloat(WEIGHT) : 0;
	}
	// A place holder method to be overriden by items that are stackable
	// returns zero by default indicating the item is not stackable.
	public int getQuantity() {
		return 0;
	}
	// A place holder method to be overriden by items that are stackable
	public void increment(int count) {
	}
	// A place holder method to be overriden by items that are stackable
	public void decrement(int count) {
	}
}