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
import com.jme3.scene.control.Control;
import com.jme3.export.binary.ByteUtils;
// Java Dependencies
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Collection;
// Internal Dependencies
import com.stp.util.JavaIO;
import com.stp.games.jme.GameRegistry;

/** @author Paul Collins
 *  @version v1.0 ~ 04/12/2014
 *  HISTORY: Version 1.0 reformatted methods and added comments updating version to 1.0. 04/12/2014
 *			Version 0.1 created ClothingControl which manages the parameters associated with clothing items ~ 05/04/2013
 */
public class ClothingControl extends ItemControl implements ItemHolder
{
	// Inherited Params from ItemControl
	//public static final Param SIZE = new Param("Size", Integer.class, 0);
	//public static final Param WEIGHT = new Param("Weight", Float.class, 1);
	//public static final Param COMPOSITION = new Param("Composition", String.class, 2);
	
	// Field Params
	public static final Param SLOT_TYPE = new Param("SlotType", SlotType.class, 3);
	public static final Param PROTECTION = new Param("Protection", Integer.class, 4);
	public static final Param EFFECTS = new Param("Effects", String.class, 5);
	public static final Param CAPACITY = new Param("Capacity", Integer.class, 6);
	
	public static final Param[] CLOTHING_PARAMS = { SIZE ,WEIGHT, COMPOSITION, SLOT_TYPE, PROTECTION, EFFECTS, CAPACITY };	
	public static final ObjectType CLOTHING_TYPE = new ObjectType<ClothingControl>("Clothing", ClothingControl.class, CLOTHING_PARAMS);
	
	// Clothing Styles
	public enum SlotType {
		NONE("none"),
		HELM("helm"),
		AMULET("amulet"),
		CHEST("chest"),
		HANDS("hands"),
		RING("ring"),
		BELT("belt"),
		LEGS("legs"),
		FEET("feet"),
		PACK("pack"),
		POUCH("pouch");
		private String text;
		private SlotType(String text) {
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
	/* TEMPLATES */
	/*public static final Schematic BELT = new Schematic("Belt", "belt", Type.CLOTHING, Schematic.Category.CLOTHING, "Teapot/Teapot.mesh.xml", "",
				new Object[] { Size.SMALL.ordinal(), 1f, "Leather", Style.BELT.ordinal(), 0, "None", 3 });*/
														
	/*public static final Schematic PACK = new Schematic("Pack", "pack", Type.CLOTHING, Schematic.Category.CLOTHING, "Teapot/Teapot.mesh.xml", "",
				new Object[] { Size.MEDIUM.ordinal(), 2f, "Leather", Style.PACK.ordinal(), 0, "None", 9 });*/
	
	/* INSTANCE VARIABLES */
	protected LinkedList<ItemControl> items = new LinkedList<ItemControl>();
	protected String[] effects = new String[0];
	
	// Primary constructor with no parameters
	public ClothingControl()	{
		super();
	}
	public ClothingControl(Schematic schema) {
		super(schema);
	}
	// Alternate constructor that links the control with it's spatial
	public ClothingControl(Spatial obj)	{
		super(obj);
	}
	
	/* INHERITED METHODS */
	
	// Creates a copy of this control to add to a new spatial
	@Override
	public Control cloneForSpatial(Spatial spatial) {
		final ClothingControl control = new ClothingControl();
		control.setSpatial(spatial);
		control.copyFields(this);
		return control;
	}

	@Override
	// Reset all parameters to their defaults
	public void resetFields() {
		super.resetFields();
		setEffects("");
		items.clear();
		if (schema != null) {
			setCapacity(schema.getInt(CAPACITY));
		}
	}
	// Copy all fields from another controls
	public void copyFields(ClothingControl control) {
		super.copyFields(control);
		setEffects(control.getEffects());
		setCapacity(control.getCapacity());
	}
	// Initialized all field to their initial values specified by the schematic
	@Override
	public void setFields(Schematic schema) {
		super.setFields(schema);
		setEffects(schema.getString(EFFECTS));
		setCapacity(schema.getInt(CAPACITY));
	}
	
	/* SAVING METHODS */
	
	// Gets the memory size required to store this item
	public int getMemSize() {
		return 0;
	}	
	// Write all unique parameter values to the given output stream
	@Override
	public void writeFields(OutputStream os) throws IOException {
		super.writeFields(os);
		JavaIO.writeString(os, getEffectString());
		os.write(JavaIO.convertToBytes((short)items.size()));
		for (ItemControl item : items) {
			os.write(JavaIO.convertToBytes(item.isNone()));
			long id = item.getId();
			if (id != 0) {
				os.write(JavaIO.convertToBytes(id));
				item.writeFields(os);
			}
		}
	}
	// Read all unique parameter values from the given input stream
	@Override
	public void readFields(InputStream is) throws IOException {
		super.readFields(is);
		setEffects(JavaIO.readString(is));
		int count = (int)JavaIO.readShort(is);
		setCapacity(count);
		for (int i = 0; i < count; i++) {
			boolean none = JavaIO.readBoolean(is);
			if (!none) {
				GameControl control = GameRegistry.getInstance().createObject(JavaIO.readLong(is));
				if (control instanceof ItemControl) {
					ItemControl item = (ItemControl)control;
					item.readFields(is);
					items.set(i, item);
				} else {
					items.set(i, NONE);
				}
			} else {
				items.set(i, NONE);
			}
		}
	}
	// Write all unique parameter values to the given buffer
	@Override
	public void fillBuffer(ByteBuffer buffer) throws IOException	{
		byte[] chars = getEffectString().getBytes("UTF8");
		buffer.putShort((short)chars.length);
		buffer.put(chars);
		buffer.putShort((short)items.size());
		for (ItemControl item : items) {
			buffer.put(item.isNone() ? (byte)1 : 0);
			long id = item.getId();
			if (id != 0) {
				buffer.putLong(id);
				item.fillBuffer(buffer);
			}
		}
	}
	// Read all unique parameter values from the given buffer
	@Override
	public void readBuffer(ByteBuffer buffer) throws IOException {
		byte[] chars = new byte[buffer.getShort()];
		buffer.get(chars);
		setEffects(new String(chars, "UTF8"));
		int count = (int)buffer.getShort();
		setCapacity(count);
		for (int i = 0; i < count; i++) {
			boolean none = buffer.get() != 0;
			if (!none) {
				GameControl control = GameRegistry.getInstance().createObject(buffer.getLong());
				if (control instanceof ItemControl) {
					ItemControl item = (ItemControl)control;
					item.readBuffer(buffer);
					items.set(i, item);
				} else {
					items.set(i, NONE);
				}
			} else {
				items.set(i, NONE);
			}
		}
	}
	
	/* INSTANCE METHODS */
	
	// Sets the effects applied to the clothing
	public void setEffects(String effectsList) {
		effects = effectsList.split(",");
	}
	// Sets the effects applied to the clothing
	public void setEffects(String[] effects) {
		this.effects = effects;
	}
	// Gets the effects applied to the clothing as and array
	public String[] getEffects()	{
		return effects;
	}
	// Gets the effects applied to the clothing as a delimited string
	public String getEffectString() {
		return getDelimitedString(effects, ",");
	}
	// Sets the carrying capacity
	public void setCapacity(int capacity) {
		for (int i = items.size(); i < capacity; i++) {
			items.add(NONE);
		}
	}
	// Gets the current carrying capacity
	public int getCapacity() {
		return items.size();
	}
	// Gets the number of actual items being carried
	public int getItemCount() {
		int count = 0;
		for (ItemControl item : items) {
			if (!item.isNone()) {
				count++;
			}
		}
		return count;
	}
	// Adds the item at the specified location, -1 indicates add it to the first unoccupied location
	public ItemControl addItem(int index, ItemControl item) {
		if (index < 0) {
			for (int i = 0; i < items.size(); i++) {
				if (items.get(i).isNone()) {
					items.set(i, item);
					return NONE;
				}
			}
			return item;
		} else if (index < items.size()) {
			return items.set(index, item);
		}
		return item;
	}
	// Removes the item at the specified location returns the item remove or null if no item was found
	public ItemControl removeItem(int index) {
		if (index >= 0 && index < items.size()) {
			return items.set(index, NONE);
		}
		return NONE;
	}
	// Removes the specified item, returns true if the item was found and removed false otherwise
	public boolean removeItem(ItemControl item) {
		int index = items.indexOf(item);
		if (index >= 0) {
			items.set(index, NONE);
			return true;
		}
		return false;
	}
	// Gets the contained items as an array
	@Override
	public Collection<ItemControl> getItems() {
		return items;
	}
	// Gets the item at the specified location
	@Override
	public ItemControl getItem(int index)	{
		if (index >= 0 && index < items.size()) {
			return items.get(index);
		}
		return NONE;
	}
	// Locates an item by the specified Id or return the None item if not found
	@Override
	public ItemControl getItemById(long uid) {
		for (ItemControl item : items) {
			if (item.getUniqueId() == uid)	{
				return item;
			}
		}
		return NONE;
	}
	
	/* CONVENIENCE METHODS */
	
	// Gets the style of the clothing which is inherited from the schematic
	public SlotType getSlotType() {
		return (schema != null) ? SlotType.values()[schema.getInt(SLOT_TYPE)] : SlotType.NONE;
	}
	// Gets the amount of protection the clothing provides which is inherited from the schematic
	public int getProtection() {
		return (schema != null) ? schema.getInt(PROTECTION) : 0;
	}
	// Check to see if the carrying capacity has been reached
	public boolean isFull() {
		return getItemCount() >= getCapacity();
	}
}