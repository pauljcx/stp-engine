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
import com.jme3.math.Vector3f;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.binary.ByteUtils;
import com.jme3.animation.SkeletonControl;
import com.jme3.animation.Skeleton;
import com.jme3.scene.control.LightControl;
// Java Dependencies
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collection;
// Internal Dependencies
import com.stp.games.jme.GameRegistry;
import com.stp.games.jme.actions.ActionTask;
import com.stp.util.JavaIO;

/** @author Paul Collins
 *  @version v0.2 ~ 05/04/2013
 *  HISTORY: Version 0.2 recoded as SentientControl object for use in JME 05/04/2013
 *		Version 0.1 created a Sentient object ~ 03/14/2012
 */
public class SentientControl extends CreatureControl implements ItemHolder {
	// Field Params
	//public static final Param RACE = new Param("Race", Race.class, 0);
	//public static final Param WALK_SPEED = new Param("WalkSpeed", Float.class, 1);
	
	public static final Param[] SENTIENT_PARAMS = { RACE, VISION_RADIUS };
	public static final ObjectType<SentientControl> SENTIENT_TYPE = new ObjectType<SentientControl>("Sentient", SentientControl.class, SENTIENT_PARAMS);
	
	private static final ArrayList<ItemControl> EMPTY_LIST = new ArrayList<ItemControl>(0);
	
	public static final String[] SENTIENT_ACTIONS = { "select", "trade", "converse" };
	
	public enum Slot {
		HELM(ClothingControl.SlotType.HELM.text(), ClothingControl.CLOTHING_TYPE, 48, 96),
		AMULET(ClothingControl.SlotType.AMULET.text(), ClothingControl.CLOTHING_TYPE, 48, 48),
		HEAD_PIECE("any", ClothingControl.CLOTHING_TYPE, 48, 48),
		CHEST(ClothingControl.SlotType.CHEST.text(), ClothingControl.CLOTHING_TYPE, 48, 96),
		HANDS(ClothingControl.SlotType.HANDS.text(), ClothingControl.CLOTHING_TYPE, 48, 96),
		RING_ONE(ClothingControl.SlotType.RING.text(), ClothingControl.CLOTHING_TYPE, 48, 48),
		RING_TWO(ClothingControl.SlotType.RING.text(), ClothingControl.CLOTHING_TYPE, 48, 48),
		BELT(ClothingControl.SlotType.BELT.text(), ClothingControl.CLOTHING_TYPE, 96, 48),
		LEGS(ClothingControl.SlotType.LEGS.text(), ClothingControl.CLOTHING_TYPE, 48, 96),
		FEET(ClothingControl.SlotType.FEET.text(), ClothingControl.CLOTHING_TYPE, 48, 96),
		PACK(ClothingControl.SlotType.PACK.text(), ClothingControl.CLOTHING_TYPE, 96, 96),
		BACK("back", ClothingControl.CLOTHING_TYPE, 48, 96),
		LHAND("left_hand", ToolControl.TOOL_TYPE, 48, 96),
		RHAND("right_hand", ToolControl.TOOL_TYPE, 48, 96),
		BELT_ITEM("belt_item", ToolControl.TOOL_TYPE, 48, 96),
		PACK_ITEM("pack_item", GENERIC_TYPE, 48, 96),
		CART_ITEM("cart_item", GENERIC_TYPE, 48, 96);
		private String text;
		private ObjectType type;
		private int width;
		private int height;
		private Slot(String text, ObjectType type, int width, int height) {
			this.text = text;
			this.type = type;
			this.width = width;
			this.height = height;
		}
		public byte index() { return (byte)ordinal(); }
		public String text() { return text; }
		public ObjectType type() { return type; }
		public int getWidth() { return width; }
		public int getHeight() { return height; }
		public boolean validate(ItemControl item) {
			if (item != null && !item.isNone())
			{
				if (type.matches(GENERIC_TYPE))
				{
					return true;
				}
				if (item.isType(type))
				{
					if (item.isClothing())
					{
						return ((ClothingControl)item).getSlotType().matches(text);
					}
					return true;
				}
				return false;
			}
			return true;
		}
	}
	/*public static final Schematic HUMAN = new Schematic("Human",
													"blank",
													Type.SENTIENT,
													Schematic.Category.OTHER,
													"Models/Sinbad/Sinbad.mesh.xml",
													"",
													new Object[] { Race.HUMAN.ordinal(), // Race
														0, // Profession
														10, // Strength
														10, // Dexterity
														10, // Wisdom
														10 }); // Charisma
	*/
	
	//private Spatial body;
	private int slots = 14;
	protected final LinkedList<ItemControl> items = new LinkedList<ItemControl>();
	private float[] actionRatings = new float[ToolControl.RatingType.values().length];
	private float[] defenceRatings = new float[ToolControl.RatingType.values().length];
	private ArrayList<Long> schematicList = new ArrayList<Long>();
	private SlotListener listener;
	private String firstName;
	private String lastName;
	private long birthDate;
	private boolean updateLights;
	
	protected ActionTask combatTask;

	// Primary constructor with no parameters
	public SentientControl() {
		this (null);
	}
	public SentientControl(Schematic schema) {
		super(schema);
		setCapacity(slots);
		needsUpdate = false;
		updateLights = true;
		this.combatTask = new ActionTask();
	}
	@Override
	public void resetFields() {
		super.resetFields();
		setFirstName("Jane");
		setLastName("Doe");
		setBirthDate(0L);	
	}
	@Override
	public void setFields(Schematic schema) {
		super.setFields(schema);
		setFirstName("Jane");
		setLastName("Doe");
		setBirthDate(0L);	
	}
	@Override
	protected void controlUpdate(float tpf) {
		if (needsUpdate) {
			for (int i = 0; i < items.size(); i++) {
				ItemControl item = items.get(i);
				if (item.isNone()) {
					Node slotNode = getSlotNode(Slot.values()[i]);
					if (slotNode != null) {
						Spatial defaultObj = getNodeDefault(Slot.values()[i].text());
						if (defaultObj != null) {
							if (slotNode.getChild(defaultObj.getName()) == null) {
								slotNode.detachAllChildren();
								slotNode.attachChild(defaultObj);
								System.out.println("Attaching Default: " + defaultObj.getName());
							}
						}
					}
				} else if (!item.isActive()) {
					Node slotNode = getSlotNode(Slot.values()[i]);
					if (slotNode != null) {
						slotNode.detachAllChildren();
						if (item != null && !item.isNone()) {
							slotNode.attachChild(item.getSpatial());
						}
					}
					item.setActive(true);
				}
			}
			needsUpdate = false;
		}
		/*if (updateLights && spatial != null) {
			for (ItemControl item : getBeltItems()) {
				if (item.hasLight()) {
					Spatial itemSpatial = item.getSpatial();
					if (itemSpatial != null) {
						LightControl lc = itemSpatial.getControl(LightControl.class);
						//itemSpatial.removeControl(lc);
						//addControl(lc);
						//addLightToWorld(lc.getLight());
					}
				}
			}
			System.out.println("Light Check");
			updateLights = false;
		}*/
	}
	// Sets the carrying capacity
	private void setCapacity(int capacity) {
		for (int i = items.size(); i < capacity; i++) {
			items.add(ItemControl.NONE);
		}
	}
	/*public void setBody(Spatial body) {	
		this.body = body;
		body.setName("body");
		SkeletonControl sc = body.getControl(SkeletonControl.class);
		if (sc != null) {
			Skeleton s = sc.getSkeleton();
			for (int b = 0; b < s.getBoneCount(); b++) {
				System.out.println("Skelton Bone: " + s.getBone(b).getName());
			}
		}
	}
	public void setEnableBody(boolean enabled) {
		if (body != null) {
			Node node = (Node)spatial;
			if (enabled) {
				node.attachChild(body);
			} else {
				node.detachChild(body);
			}
		}
	}*/
	public ActionTask getCombatTask() {
		return combatTask;
	}
	public void activateCombatTask() {
		if (action != null) {
			action.setTask(combatTask);
		}
	}
	@Override
	public void setGivenName(String firstName, String lastName) {
		setFirstName(firstName);
		setLastName(lastName);
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public void setBirthDate(long birthDate) {
		this.birthDate = birthDate;
	}
	public String getFirstName() {
		return firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public long getBirthDate() {
		return birthDate;
	}
	@Override
	public boolean acceptItem(ItemControl item) {
		if (item.isTool()) {
			ItemControl result = addBeltItem(item, -1);
			return result.isNone();
		}
		return false;
	}
	public ItemControl addItem(int address, ItemControl item) {
		return addItem(Slot.values()[address], item, 0);
	}
	public ItemControl addItem(Slot slot, ItemControl item) {
		return addItem(slot, item, 0);
	}
	public ItemControl addItem(Slot slot, ItemControl item, int spot) {
		// Fail fast if slot doesn't accept item type
		if (!slot.validate(item) || !(spatial instanceof Node)) {
			return item;
		}
		switch (slot) {
			case BELT_ITEM:
				return addBeltItem(item, spot);
			case PACK_ITEM:
				return addPackItem(item, spot);
			case CART_ITEM:
				return item;
			default:
				ItemControl oldItem = getItem(slot);
				if (item != null) {
					items.set(slot.ordinal(), item);
					item.setActive(false);
				} else {
					items.set(slot.ordinal(), ItemControl.NONE);
				}
				needsUpdate = true;
				fireSlotChanged(slot);
				return oldItem;
		}
	}
	public ItemControl addBeltItem(ItemControl item, int address)
	{
		if (hasBelt() && Slot.BELT_ITEM.validate(item))
		{
			ItemControl result = ((ClothingControl)getItem(Slot.BELT)).addItem(address, item);
			fireSlotChanged(Slot.BELT_ITEM);
			return result;
		}
		return item;
	}
	public ItemControl addPackItem(ItemControl item, int address)
	{
		if (hasPack() && !item.equals(getItem(Slot.PACK)))
		{
			fireSlotChanged(Slot.PACK_ITEM);
			return ((ClothingControl)getItem(Slot.PACK)).addItem(address, item);
		}
		return item;
	}
	public boolean validateItem(Slot slot, ItemControl item) {
		switch (slot)
		{
			case BELT_ITEM: return hasBelt() && slot.validate(item);
			case PACK_ITEM: return hasPack() && slot.validate(item);
			default: return slot.validate(item);
		}
	}
	public void swapSlots(Slot slotA, int indexA, Slot slotB, int indexB) {
		ItemControl itemA = getItem(slotA, indexA);
		ItemControl itemB = getItem(slotB, indexB);
		switch (slotA)
		{
			case BELT_ITEM: ((ClothingControl)getItem(Slot.BELT)).addItem(indexA, itemB); break;
			case PACK_ITEM: ((ClothingControl)getItem(Slot.PACK)).addItem(indexA, itemB); break;
			default: items.set(indexA, itemB);
		}
		switch (slotB)
		{
			case BELT_ITEM: ((ClothingControl)getItem(Slot.BELT)).addItem(indexB, itemA); break;
			case PACK_ITEM: ((ClothingControl)getItem(Slot.PACK)).addItem(indexB, itemA); break;
			default: items.set(indexB, itemA);
		}
	}
	private ItemControl addSlotItem(String slot, ItemControl item) {
		if (slot.contains("b"))
		{
			return addItem(Slot.BELT_ITEM, item, Character.digit(slot.charAt(6), 10));
		}
		else if (slot.contains("p"))
		{
			return addItem(Slot.PACK_ITEM, item, Character.digit(slot.charAt(6), 10));
		}
		else
		{
			return addItem(Slot.values()[Character.digit(slot.charAt(4), 10)], item);
		}
	}
	public Node getSlotNode(Slot slot) {
		SkeletonControl sc = getSkeleton();
		if (sc != null) {
			switch (slot) {
				//case RHAND: return sc.getAttachmentsNode("RightHand");
				//case LHAND: return sc.getAttachmentsNode("LeftHand");
				//case BELT: return sc.getAttachmentsNode("Torso");
				default: 
					Spatial result = rig.getChild(slot.text());
					return (result instanceof Node) ? (Node)result : null;
			}
		}
		return null;
	}
	public ItemControl getItem(Slot slot, int index) {
		switch (slot) {
			case BELT_ITEM: return getBeltItem(index);
			case PACK_ITEM: return getPackItem(index);
			default: return getItem(slot);
		}
	}
	public ItemControl getItem(Slot slot) {
		return getItem(slot.ordinal());
	}
	public ItemControl getItem(int index) {
		if (index >= 0 && index < items.size()) {
			return items.get(index);
		}
		return ItemControl.NONE;
	}
	public int getItemCount() {
		return items.size();
	}
	public Collection<ItemControl> getItems() {
		return items;
	}
	public ItemControl getBeltItem(int index) {
		if (hasBelt()) {
			return ((ClothingControl)getItem(Slot.BELT)).getItem(index);
		}
		return ItemControl.NONE;
	}
	public Collection<ItemControl> getBeltItems() {
		if (hasBelt()) {
			return ((ClothingControl)getItem(Slot.BELT)).getItems();
		} else {
			return EMPTY_LIST;
		}
	}
	public ItemControl getPackItem(int index) {
		if (hasPack()) {
			return ((ClothingControl)getItem(Slot.PACK)).getItem(index);
		}
		return ItemControl.NONE;
	}
	public Collection<ItemControl> getPackItems() {
		if (hasPack()) {
			return ((ClothingControl)getItem(Slot.PACK)).getItems();
		} else {
			return EMPTY_LIST;
		}
	}
	// Searches for an object to be removed
	public boolean removeItem(ItemControl item) {
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).equals(item)) {
				items.set(i, ItemControl.NONE);
				Node slotNode = getSlotNode(Slot.values()[i]);
				if (slotNode != null) {
					slotNode.detachAllChildren();
				}
				return true;
			}
		}
		boolean found = false;
		if (hasBelt()) {
			found = ((ClothingControl)getItem(Slot.BELT)).removeItem(item);
		}
		if (!found && hasPack()) {
			found = ((ClothingControl)getItem(Slot.PACK)).removeItem(item);
		}
		return found;
	}
	// Searches for and returns a specific object
	public ItemControl getItemById(long uid) {
		for (ItemControl item : items) {
			if (item.getUniqueId() == uid) {
				return item;
			}
		}
		if (hasBelt()) {
			ItemControl item = ((ClothingControl)getItem(Slot.BELT)).getItemById(uid);
			if (!item.isNone()) {
				return item;
			}
		}
		if (hasPack()) {
			return ((ClothingControl)getItem(Slot.PACK)).getItemById(uid);
		}
		return ItemControl.NONE;
	}
	public boolean hasPack() {
		return !getItem(Slot.PACK).isNone();
	}
	public boolean hasBelt() {
		return !getItem(Slot.BELT).isNone();
	}
	@Override
	public String getDisplayName() {
		return getFirstName();
	}
	public String getName() {
		return getFirstName() + " " + getLastName();
	}
	public String toString() {
		return super.toString() + " | " + getName();
	}
	public Object getNamedProperty(String prop) {
		return firstName;
	}
	public void addRelationship(String type, String name) {
		//TODO
	}
	public boolean isKnown(String name) {
		return false;
	}
	public String getVoiceName() {
		return "Emma";
	}
	public float[] getActionRatings(Slot hand) {
		for (int r = 0; r < actionRatings.length; r++) {
			actionRatings[r] = 0;
		}
		if (items.get(hand.ordinal()).isTool()) {
			((ToolControl)items.get(hand.ordinal())).getActionRatings(actionRatings);
		}
		return actionRatings;
	}
	public void updateRatings() {
		for (int r = 0; r < actionRatings.length; r++) {
			actionRatings[r] = 0;
		}
		if (items.get(Slot.LHAND.ordinal()).isTool()) {
			((ToolControl)items.get(Slot.LHAND.ordinal())).getActionRatings(actionRatings);
		}
		if (items.get(Slot.RHAND.ordinal()).isTool()) {
			((ToolControl)items.get(Slot.RHAND.ordinal())).getActionRatings(actionRatings);
		}
		for (ItemControl item : getBeltItems()) {
			if (item != null && item.isTool()) {
				((ToolControl)item).getActionRatings(actionRatings);
			}
		}
	}
	@Override
	public float getActionRating(int actionType) {
		return actionRatings[actionType];
	}
	public float[] getActionRatings() {
		return actionRatings;
	}
	public float[] getDefenceRatings()
	{
		for (int r = 0; r < defenceRatings.length; r++)
		{
			defenceRatings[r] = 0;
		}
		if (items.get(Slot.LHAND.ordinal()).isTool())
		{
			((ToolControl)items.get(Slot.LHAND.ordinal())).getDefenceRatings(defenceRatings);
		}
		if (items.get(Slot.RHAND.ordinal()).isTool())
		{
			((ToolControl)items.get(Slot.RHAND.ordinal())).getDefenceRatings(defenceRatings);
		}
		return defenceRatings;
	}
	public void setListener(SlotListener listener)
	{
		this.listener = listener;
	}
	public void fireSlotChanged(Slot slot) {
		if (listener != null) {
			listener.slotChanged(slot);
		}
		updateRatings();
	}
	public void addSchematic(Schematic schema)
	{
		schematicList.add(schema.getId());
	}
	public Long[] getSchematicList()
	{
		return schematicList.toArray(new Long[schematicList.size()]);
	}
	@Override
	public float getWalkSpeed() {
		return 1.8f;
	}
	@Override
	public String[] getActionList() {
		return SENTIENT_ACTIONS;
	}
	@Override
	public void writeFields(OutputStream os) throws IOException
	{
		super.writeFields(os);
		JavaIO.writeString(os, firstName);
		JavaIO.writeString(os, lastName);
		os.write(ByteUtils.convertToBytes(getBirthDate()));
		os.write(ByteUtils.convertToBytes(items.size()));
		for (ItemControl item : items) {
			os.write(item.isNone() ? (byte)1 : 0);
			if (!item.isNone()) {
				os.write(ByteUtils.convertToBytes(item.getId()));
				item.writeFields(os);
			}
		}
	}
	@Override
	public void readFields(InputStream is) throws IOException {
		super.readFields(is);
		setFirstName(JavaIO.readString(is));
		setLastName(JavaIO.readString(is));
		setBirthDate(ByteUtils.readLong(is));
		int itemCount = ByteUtils.readInt(is);
		setCapacity(itemCount);
		for (int i = 0; i < itemCount; i++) {
			boolean none = is.read() != 0;
			if (!none) {
				GameControl control = GameRegistry.getInstance().createObject(ByteUtils.readLong(is));
				if (control instanceof ItemControl) {
					ItemControl item = (ItemControl)control;
					item.readFields(is);
					items.set(i, item);
				} else {
					items.set(i, ItemControl.NONE);
				}
			} else {
				if (!items.get(i).isNone()) {
					items.set(i, ItemControl.NONE);
				}
			}
		}
		needsUpdate = true;
	}
	@Override
	public void fillBuffer(ByteBuffer buffer) throws IOException {
		super.fillBuffer(buffer);
		buffer.putShort((short)firstName.length());
		buffer.put(firstName.getBytes("UTF8"));
		buffer.putShort((short)lastName.length());
		buffer.put(lastName.getBytes("UTF8"));
		buffer.putLong(getBirthDate());
		buffer.putInt(items.size());
		for (ItemControl item : items) {
			buffer.put(item.isNone() ? (byte)1 : 0);
			if (!item.isNone()) {
				buffer.putLong(item.getId());
				item.fillBuffer(buffer);
			}
		}
	}
	@Override
	public void readBuffer(ByteBuffer buffer) throws IOException {
		super.readBuffer(buffer);
		byte[] chars = new byte[buffer.getShort()];
		buffer.get(chars);
		setFirstName(new String(chars, "UTF8"));
		chars = new byte[buffer.getShort()];
		buffer.get(chars);
		setLastName(new String(chars, "UTF8"));
		setBirthDate(buffer.getLong());
		int itemCount = buffer.getInt();
		setCapacity(itemCount);
		for (int i = 0; i < itemCount; i++) {
			boolean none = buffer.get() != 0;
			if (!none) {
				GameControl control = GameRegistry.getInstance().createObject(buffer.getLong());
				if (control instanceof ItemControl) {
					ItemControl item = (ItemControl)control;
					item.readBuffer(buffer);
					items.set(i, item);
				} else {
					items.set(i, ItemControl.NONE);
				}
			} else {
				if (!items.get(i).isNone()) {
					items.set(i, ItemControl.NONE);
				}
			}
		}
		needsUpdate = true;
	}
	@Override
	public void write(JmeExporter ex) throws IOException {
		super.write(ex);
		OutputCapsule oc = ex.getCapsule(this);
		oc.write(getFirstName(), "firstName", "");
		oc.write(getLastName(), "lastName", "");
		oc.write(items.size(), "itemCount", 0);
		for (int i = 0; i < items.size(); i++) {
			if (!items.get(i).isNone()) {
				oc.write(items.get(i), ("slot" + i), null);
			}
		}
	}
	@Override
	public void read(JmeImporter im) throws IOException {
		super.read(im);
		InputCapsule ic = im.getCapsule(this);
		setFirstName(ic.readString("firstName", "Jane"));
		setLastName(ic.readString("lastName", "Doe"));
		setCapacity(ic.readInt("itemCount", 0));
		for (int i = 0; i < items.size(); i++) {
			items.set(i, (ItemControl)ic.readSavable("slot" + i, ItemControl.NONE));
		}
		needsUpdate = true;
	}
}