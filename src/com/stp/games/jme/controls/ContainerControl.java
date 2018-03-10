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
import com.jme3.font.BitmapText;
// Java Dependencies
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Collection;
import java.util.Set;
// Internal Dependencies
import com.stp.games.jme.controls.Schematic;
import com.stp.games.jme.GameRegistry;

/** @author Paul Collins
 *  @version v1.0 ~ 04/12/2013
 *  HISTORY: Version 1.0 reformatted methods and added comments updating version to 1.0. 04/12/2014
 *		Version 0.1 created ContainerControl an object to store other game items  in ~ 05/04/2013
 */
public class ContainerControl extends ItemControl implements ItemHolder
{
	// Inherited Params from ItemControl
	//public static final Param SIZE = new Param("Size", Integer.class, 0);
	//public static final Param WEIGHT = new Param("Weight", Float.class, 1);
	//public static final Param COMPOSITION = new Param("Composition", String.class, 2);
	
	// Field Params
	public static final Param LOCK_TYPE = new Param("Lock Type", LockType.class, 3);
	public static final Param CAPACITY = new Param("Capacity", Integer.class, 4);
	public static final Param ROWS = new Param("Rows", Integer.class, 5);
	public static final Param RESOURCE_PARAM = new Param("Resource Type", String.class, 6);
	public static final Param SEEDED = new Param("Seeded", Boolean.class, 7);
	
	public static final Param[] CONTAINER_PARAMS = { SIZE ,WEIGHT, COMPOSITION, LOCK_TYPE, CAPACITY, ROWS, RESOURCE_PARAM, SEEDED };
	public static final ObjectType CONTAINER_TYPE = new ObjectType<ContainerControl>("Container", ContainerControl.class, CONTAINER_PARAMS);
	
	public static final String[] CONTAINER_ACTIONS = { "lock", "activate" };
	public static final String[] RESOURCE_ACTIONS = { "gather" };
	
	/* Lock Types */
	public enum LockType {
		UNLOCKED("unlocked"),
		EASY("easy"),
		AVERAGE("average"),
		HARD("hard"),
		KEYED("keyed"),
		RESOURCE("resource");
		private String text;
		private LockType(String text) {
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
	
	/*public static final Schematic CHEST = new Schematic("Chest", "chest", Type.CONTAINER, Schematic.Category.OTHER, "Models/ChestA/chest_a.j3o", "", 
				new Object[] { Size.MEDIUM.text(), 10f, "Wood & Iron", LockType.UNLOCKED.ordinal(), 24, 4 });*/
														
	/*public static final Schematic BARREL = new Schematic("Barrel", "barrel", Type.CONTAINER, Schematic.Category.OTHER, "Models/Teapot/Teapot.mesh.xml", "",
				new Object[] { Size.MEDIUM.text(), 30f, "Wood & Iron", LockType.UNLOCKED.ordinal(), 24, 6 });*/
	
	/* INSTANCE VARIABLES */
	protected HashMap<Integer, ItemControl> items = new HashMap<Integer, ItemControl>();
	protected int[] addressList = new int[0];
	protected BitmapText displayText;
	
	// Primary constructor with no parameters
	public ContainerControl() {
		super();
	}
	public ContainerControl(Schematic schema) {
		super(schema);
		addressList = new int[getCapacity()];
		for (int a = 0; a < addressList.length; a++) {
			addressList[a] = -1;
		}
	}
	// Alternate constructor that links the control with it's spatial
	public ContainerControl(Spatial obj)	{
		super(obj);
	}
	
	/* INHERITED METHODS */
	
	// Creates a copy of this control to add to a new spatial
	@Override
	public Control cloneForSpatial(Spatial spatial)	{
		final ContainerControl control = new ContainerControl();
		control.setSpatial(spatial);
		control.copyFields(this);
		return control;
	}
	
	// Resets all parameters values to their defaults
	@Override
	public void resetFields()	{
		super.resetFields();
		if (spatial != null) {
			setLockType(LockType.UNLOCKED);
		}
	}
	// Copies all parameters from this control to another
	public void copyFields(ContainerControl control) {
		super.copyFields(control);
		if (spatial != null)	{
			setLockType(control.getLockType());
		}
	}
	// Initializes parameters based on given schematic
	@Override
	public void setFields(Schematic schema) {
		super.setFields(schema);
		setLockType(schema.getInt(LOCK_TYPE));
		if (addressList.length < getCapacity()) {
			addressList = new int[getCapacity()];
			for (int a = 0; a < addressList.length; a++) {
				addressList[a] = -1;
			}
		}
		if (isSeeded()) {
			if (isResourceContainer()) {
				increment((int)Math.round(Math.random()*getCapacity()));
			}
		}
	}
	
	/* INSTANCE METHODS */
	
	public void setTextNode(BitmapText displayText) {
		this.displayText = displayText;
	}
	public void setDisplayText(String text) {
		if (displayText != null) {
			displayText.setText(text);
		}
	}
	// Sets the lock type parameter for this container
	public void setLockType(int lockIndex)	{
		setLockType(LockType.values()[lockIndex]);
	}
	// Sets the lock type parameter for this container
	public void setLockType(LockType lock)	{
		put("lock", lock.name());
	}
	// Gets the lock type parameter for this container
	public LockType getLockType() {
		return LockType.valueOf(getString("lock"));
	}
	// Gets the items weight value which is inherited from the schematic
	public int getCapacity() {
		return (schema != null) ? schema.getInt(CAPACITY) : 0;
	}
	public boolean isSeeded() {
		return (schema != null) ? schema.getBoolean(SEEDED) : false;
	}
	public int getRows() {
		return (schema != null) ? schema.getInt(ROWS) : 0;
	}
	public int getColumns() {
		return (getRows() != 0) ? getCapacity() / getRows() : 0;
	}
	// Gets the number of items in this container
	public int getCount()	{
		return items.size();
	}
	public int getAddress(ItemControl item) {
		if (item == null || item.isNone()) {
			return -1;
		}
		for (Integer address : items.keySet()) {
			if (items.get(address).equals(item)) {
				return address;
			}
		}
		return -1;
	}
	public ItemControl getItemAt(int column, int row) {
		int index = getItemIndexAt(column, row);
		if (index >= 0 && index < items.size()) {
			return items.get(index);
		}
		return NONE;
	}
	public int getItemIndexAt(int column, int row) {
		int address = (row * getColumns()) + column;
		return (address < addressList.length) ? addressList[address] : -1;
	}
	public boolean isAddressAvailable(int address, int value) {
		if (address >= 0 && address < addressList.length) {
			return addressList[address] < 0 || addressList[address] == value;
		}
		return false;
	}
	public boolean isSpaceAvailable(int address, ItemControl item) {
		int value = getAddress(item);
		switch (item.getSize()) {
			case SMALL: return isAddressAvailable(address, value);
			case MEDIUM: return isAddressAvailable(address, value) &&
											isAddressAvailable(address + getColumns(), value);
			case WIDE: return isAddressAvailable(address, value) &&
											isAddressAvailable(address + 1, value);
			case LARGE: return isAddressAvailable(address, value) &&
											isAddressAvailable(address+1, value) &&
											isAddressAvailable(address + getColumns(), value) &&
											isAddressAvailable(address + getColumns()+1, value);
			default: return false;
		}
	}
	private void setItemAddress(int address, ItemControl item, int value) {
		switch (item.getSize()) {
			case MEDIUM:
				addressList[address] = value;
				addressList[address + getColumns()] = value;
				break;
			case LARGE:
				addressList[address] = value;
				addressList[address+1] = value;
				addressList[address + getColumns()] = value;
				addressList[address + getColumns() + 1] = value;
				break;
			default: addressList[address] = value;
		}
		System.out.println(" Address: " + address + " Value: " + value + " Address List: ");
		int index = 0;
		for (int r = 0; r < getRows(); r++) {
			for (int c= 0; c < getColumns(); c++) {
				System.out.print(addressList[index] + ",");
				index++;
			}
			System.out.print( "\n");
		}
	}
	public ItemControl addItem(int address, ItemControl item) {
		if (isSpaceAvailable(address, item)) {
			items.put(address, item);
			setItemAddress(address, item, address);
			return NONE;
		}
		return item;
	}
	public boolean removeItem(int address) {
		ItemControl result = items.remove(address);
		if (result != null) {
			setItemAddress(address, result, -1);
			return true;
		}
		return false;
	}
	public boolean removeItem(ItemControl item) {
		if (item == null || item.isNone()) {
			return false;
		}
		for (Integer address : items.keySet()) {
			if (items.get(address).equals(item)) {
				return removeItem(address);
			}
		}
		return false;
	}
	public Set<Integer> getAddresses() {
		return items.keySet();
	}
	public Collection<ItemControl> getItems() {
		return items.values();
	}
	public ItemControl getItem(int address) {
		if (address >= 0 && address < addressList.length) {
			ItemControl result = items.get(addressList[address]);
			if (result != null) {
				return result;
			}
		}
		return NONE;
	}
	@Override
	public ItemControl getItemById(long uid) {
		for (ItemControl item : items.values()) {
			if (item.getUniqueId() == uid) {
				return item;
			}
		}
		return NONE;
	}
	// Returns a list of user actions that indicate how a player can interact with this item
	@Override
	public String[] getActionList()	{
		return isResourceContainer() ? RESOURCE_ACTIONS : CONTAINER_ACTIONS;
	}
	
	/* SAVING METHODS */
	
	// Write all unique parameter values to the given output stream
	@Override
	public void writeFields(OutputStream os) throws IOException {
		super.writeFields(os);
		os.write(getLockType().index());
		os.write(ByteUtils.convertToBytes((short)getCount()));
		for (Integer address : items.keySet()) {
			ItemControl item = items.get(address);
			long id = item.getId();
			if (id != 0) {
				os.write(ByteUtils.convertToBytes(id));
				os.write(ByteUtils.convertToBytes(address.shortValue()));
				item.writeFields(os);
			}
		}
	}
	// Read all unique parameter values from the given input stream
	@Override
	public void readFields(InputStream is) throws IOException {
		super.readFields(is);
		setLockType((byte)is.read());
		int count = (int)ByteUtils.readShort(is);
		for (int i = 0; i < count; i++) {
			GameControl control = GameRegistry.getInstance().createObject(ByteUtils.readLong(is));
			int address = (int)ByteUtils.readShort(is);
			if (control instanceof ItemControl) {
				ItemControl item = (ItemControl)control;
				item.readFields(is);
				items.put(address, item);
				setItemAddress(address, item, address);
			}
		}
	}
	// Write all unique parameter values to the given buffer
	@Override
	public void fillBuffer(ByteBuffer buffer) throws IOException	{
		super.fillBuffer(buffer);
		buffer.put(getLockType().index());
		buffer.putShort((short)getCount());
		for (Integer address : items.keySet()) {
			ItemControl item = items.get(address);
			long id = item.getId();
			if (id != 0) {
				buffer.putLong(id);
				buffer.putShort(address.shortValue());
				item.fillBuffer(buffer);
			}
		}
	}
	// Read all unique parameter values from the given buffer
	@Override
	public void readBuffer(ByteBuffer buffer) throws IOException {
		super.readBuffer(buffer);
		setLockType(buffer.get());
		int count = (int)buffer.getShort();
		for (int i = 0; i < count; i++) {
			GameControl control = GameRegistry.getInstance().createObject(buffer.getLong());
			int address = (int)buffer.getShort();
			if (control instanceof ItemControl) {
				ItemControl item = (ItemControl)control;
				item.readBuffer(buffer);
				items.put(address, item);
				setItemAddress(address, item, address);
			}
		}
	}

	/* CONVENIENCE METHODS */
	
	// Check whether the container has reached it's capacity
	public boolean isFull() {
		return items.size() >= getCapacity();
	}
	// Check whether the container is empty
	public boolean isEmpty() {
		return items.size() == 0;
	}
	// Check whether the container is locked
	public boolean isLocked() {
		return !isUnlocked() && !isKeyed() && ! isResourceContainer();
	}
	// Check whether the container is unlocked
	public boolean isUnlocked() {
		return getLockType().matches(LockType.UNLOCKED);
	}
	// Check whether the container requires a specific key to open
	public boolean isKeyed() {
		return getLockType().matches(LockType.KEYED);
	}
	// Check if the container is a resource container
	public boolean isResourceContainer() {
		return getLockType().matches(LockType.RESOURCE);
	}
	// Check if the container is a resource container and is empty
	public boolean isResourcesEmpty() {
		return isResourceContainer() && isEmpty();
	}
	public boolean hasResources(Schematic.ResourceParam param) {
		if (isResourceContainer() && !isEmpty()) {
			for (ItemControl item : getItems()) {
				if (((ResourceControl)item).validateResources(param)) {
					return true;
				}
			}
		}
		return false;
	}
	@Override
	public void increment(int count)	{
		if (isResourceContainer()) {
			if (items.size() > 0) {
				items.get(0).increment(count);
				setDisplayText("" + items.get(0).getQuantity());
			}
		}
	}
	@Override
	public void decrement(int count) {
		if (isResourceContainer()) {
			if (items.size() > 0) {
				items.get(0).decrement(count);
				setDisplayText("" + items.get(0).getQuantity());
			}
		}
	}
}