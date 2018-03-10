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
import com.jme3.math.Vector3f;
import com.jme3.math.Quaternion;
import com.jme3.scene.Spatial;
import com.jme3.scene.Node;
import com.jme3.scene.control.Control;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.binary.ByteUtils;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.animation.AnimControl;
import com.jme3.animation.SkeletonControl;
import com.jme3.animation.Skeleton;
import com.jme3.util.TempVars;
// Java Dependencies
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
// Internal Dependencies
import com.stp.games.jme.actions.MoveAction;
import com.stp.games.jme.actions.GameAction;
import com.stp.games.jme.actions.ActionTask;
import com.stp.games.jme.actions.Movable;
import com.stp.games.jme.terrain.Volume;

/** @author Paul Collins
 *  @version v0.2 ~ 04/22/2013
 *  HISTORY: Version 0.2 recoded as CreatureControl object for use in JME 04/22/2013
 *		Version 0.1 created a Creature object ~ 01/03/2013
 */
public class CreatureControl extends GameControl implements Movable {
	// Field Params
	public static final Param RACE = new Param("Race", Race.class, 0);
	public static final Param VISION_RADIUS = new Param("VisionRadius", Float.class, 1);
	public static final Param WALK_SPEED = new Param("WalkSpeed", Float.class, 2);
	public static final Param SKIN_MATERIAL = new Param("SkinMaterial", String.class, 3);

	public static final Param[] CREATURE_PARAMS = { RACE, VISION_RADIUS, WALK_SPEED, SKIN_MATERIAL };
	public static final ObjectType CREATURE_TYPE = new ObjectType<CreatureControl>("Creature", CreatureControl.class, CREATURE_PARAMS);
	
	public static final String[] CREATURE_ACTIONS = { "hunt" };
	
	public static final int RACE_ID = 0;	
	public static final int PROFESSION = 1;
	public static final int STRENGTH = 2;
	public static final int DEXTERITY = 3;
	public static final int WISDOM = 4;
	public static final int CHARISMA = 5;
	
	// Game Object Types
	public enum Race {
		NONE("", 0),
		HUMAN("Human", 100),
		RABBIT("Rabbit", 20),
		DEER("Deer", 80),
		WOLF("Wolf", 120),
		BEAR("Bear", 180),
		FOX("Fox", 35),
		HORSE("Horse", 150),
		HOG("Hog", 45),
		SHEEP("Sheep", 30),
		GOAT("Goat", 30),
		COW("Cow", 90),
		CHICKEN("Chicken", 25);
		private String text;
		private int health;
		private Race(String text, int health) { this.text = text; this.health = health; }
		public byte index() { return (byte)ordinal(); }
		public String text() { return text; }
		public int getHealthBase() { return health; }
		public String toString() { return text; }
		public boolean matches(Object obj) {
			if (obj != null) {
				return text.equals("Generic") || text.equals(obj.toString());
			}
			return false;
		}
	}
	public enum NodeType {
		HAIR("hair"),
		HEAD("head"),
		CHEST("chest"),
		LEGS("legs"),
		HANDS("hands"),
		FEET("feet");
		private String text;
		private NodeType(String text) { this.text = text; }
		public String text() { return text; }
	}
	
	protected final Spatial[] nodeDefaults = new Spatial[6];
	protected MoveAction movement;
	protected GameAction action;
	protected ActionTask task;
	protected Vector3f chunkLocation;
	protected Spatial body;
	protected Node rig;
	protected long uid;
	protected boolean owned;

	protected volatile boolean needsUpdate;	
	protected float stepHeight = 0.5f;
	
	protected int health;
	protected int health_base;
	protected int profession;
	protected int strength;
	protected int dexterity;
	protected int wisdom;
	protected int charisma;
	protected int hydration;
	protected int nourishment;
	protected int sleep;
	protected int focus;
	protected int morale;

	// Primary constructor with no parameters
	public CreatureControl() {
		this (null);
	}
	public CreatureControl(Schematic schema) {
		super(schema);
		this.chunkLocation = new Vector3f(Float.NaN, Float.NaN, Float.NaN);
		this.task = new ActionTask();
		this.owned = false;
		this.sleep = 100;
		this.hydration = 100;
		this.nourishment = 100;
	}
	public void setSpatial(Spatial spatial) {
		super.setSpatial(spatial);
		// Assign a reference to the parent node for later use
		if (spatial instanceof Node) {
			this.rig = (Node)spatial;
			// Save the default values for each rig node
			for (NodeType type : NodeType.values()) {
				Spatial result = rig.getChild(type.text());
				if (result instanceof Node) {
					nodeDefaults[type.ordinal()] = ((Node)result).getChild(0);
				}
			}
		}
	}
	@Override
	public void setFields(Schematic schema) {
		super.setFields(schema);
		setHealthBase(Race.values()[schema.getInt(RACE)].getHealthBase());
		setHealth(health_base);
		setProfession((short)schema.getInt(PROFESSION));
		setStrength((byte)schema.getInt(STRENGTH));
		setDexterity((byte)schema.getInt(DEXTERITY));
		setWisdom((byte)schema.getInt(WISDOM));
		setCharisma((byte)schema.getInt(CHARISMA));
	}
	@Override
	public void resetFields()	{
		super.resetFields();
		if (spatial != null)	{
			// Setup Creature UserData
		}
	}
	public void setGivenName(String firstName, String lastName) {
	}
	public BetterCharacterControl getCharacterControl() {
		return (rig != null) ? rig.getControl(BetterCharacterControl.class) : null;
	}
	public SkeletonControl getSkeleton() {
		return (rig != null) ? rig.getControl(SkeletonControl.class) : null;
	}
	public AnimControl getAnimator() {
		return (rig != null) ? rig.getControl(AnimControl.class) : null;
	}
	protected Spatial getNodeDefault(String nodeName) {
		for (int n = 0; n < nodeDefaults.length; n++) {
			if (NodeType.values()[n].text().equals(nodeName)) {
				return nodeDefaults[n];
			}
		}
		return null;
	}
	protected boolean swapNodes(NodeType type, Spatial obj) {
		if (rig == null) {
			return false;
		}
		try {
			// Find the node matching the given type and remove all children
			Node node = (Node)rig.getChild(type.text());
			node.detachAllChildren();
			if (obj instanceof Node) {
				for (Spatial child : ((Node)obj).getChildren()) {
					node.attachChild(child);
				}
			} else {
				node.attachChild(obj);
			}
			return true;
		} catch (Exception ex) {
			System.out.println("Unable to set spatial for node: " + type.text());
			return false;
		}
	}
	protected void setupControls() {
		if (spatial != null) {
			if (spatial.getControl(BetterCharacterControl.class) == null) {
				BetterCharacterControl bcc = new BetterCharacterControl(.08f, 1f, 68f);
				bcc.setJumpForce(new Vector3f(0, 4, 0));
				//bcc.setGravity(new Vector3f(0, 9.8f, 0));
				spatial.addControl(bcc);
			}
			SkeletonControl sc = spatial.getControl(SkeletonControl.class);
			if (sc != null) {
				Skeleton s = sc.getSkeleton();
				/*for (int b = 0; b < s.getBoneCount(); b++) {
					System.out.println("Skelton Bone: " + s.getBone(b).getName());
				}*/
			}
		}
	}
	public void setEnablePhysics(boolean hasPhysics) {
		/*if (hasPhysics) {
			if (spatial.getControl(BetterCharacterControl.class) == null) {
				BetterCharacterControl bcc = new BetterCharacterControl(0.5f, 2f, 68f);
				bcc.setJumpForce(new Vector3f(0, 4, 0));
				//bcc.setGravity(new Vector3f(0, 9.8f, 0));
				spatial.addControl(bcc);
			}
			movement.setCharacterControl(getCharacterControl());
		} else {
			spatial.removeControl(BetterCharacterControl.class);
			if (movement != null) {
				movement.setCharacterControl(null);
			}
		}*/
	}
	public void setEnableCharacterControl(boolean enabled) {
		BetterCharacterControl bcc = getCharacterControl();
		if (bcc != null) {
			bcc.setEnabled(enabled);
		}
	}
	@Override
	public void setWorldTranslation(Vector3f translation) {
		BetterCharacterControl bcc = getCharacterControl();
		if (bcc != null) {
			bcc.warp(translation);
		} else {
			super.setWorldTranslation(translation);
		}
	}
	@Override
	public void setWorldTranslation(float x, float y, float z) {
		BetterCharacterControl bcc = getCharacterControl();
		if (bcc != null) {
			TempVars temp = TempVars.get();
			temp.vect1.set(x, y, z);
			bcc.warp(temp.vect1);
			temp.release();
		} else {
			super.setWorldTranslation(x, y, z);
		}
	}
	public boolean setChunkLocation(float x, float y, float z) {
		boolean changed = (chunkLocation.x != x) || (chunkLocation.y != y) || (chunkLocation.z != z);;
		chunkLocation.set(x, y, z);
		return changed;
	}
	public Vector3f getChunkLocation() {
		return chunkLocation;
	}
	public int getChunkX() {
		return (int)chunkLocation.x;
	}
	public int getChunkY() {
		return (int)chunkLocation.y;
	}
	public int getChunkZ() {
		return (int)chunkLocation.z;
	}
	public void resumeTask() {
		if (action != null) {
			action.start();
		}
	}
	public void pauseTask() {
		if (action != null) {
			action.pause();
		}
	}
	public ActionTask getTask() {
		return task;
	}
	public void activateTask() {
		if (action != null) {
			action.setTask(task);
		}
	}
	public boolean acceptItem(ItemControl item) {
		return false;
	}
	public void setActionControl(GameAction action) {
		this.action = action;
		addControl(action);
	}
	public boolean hasMoveControl() {
		return (movement != null);
	}
	public void setMoveControl(MoveAction movement) {
		this.movement = movement;
		addControl(movement);
	}
	public boolean setMotionStates(int[] motionStates) {
		if (movement != null) {
			if (movement.setCommandStates(motionStates)) {
				//action.pause();
				return true;
			}
		}
		return false;
	}
	public void processMovement(String command, boolean state) {
		if (movement != null) {
			movement.setCommandState(command, state);
		}
		//action.pause();
	}
	public boolean moveTo(Vector3f target, float threshold) {
		if (movement != null) {
			return movement.setTargetCoords(target, threshold);
		}
		return false;
	}
	public boolean isMoving() {
		return (movement != null) ? movement.isProcessing() : false;
	}
	public void lookAt(Vector3f direction) {
		BetterCharacterControl bcc = getCharacterControl();
		if (bcc != null) {
			bcc.setViewDirection(direction);
		}
		if (spatial != null) {
			spatial.lookAt(direction, Vector3f.UNIT_Y);
		}
	}
	public void setRotation(float rx, float ry, float rz) {
		if (spatial != null) {
			spatial.setLocalRotation(spatial.getLocalRotation().fromAngles(rx, ry, rz));
		}
	}
	public void updateRotation(Quaternion quaternion) {
		if (movement != null) {
			movement.updateRotation(quaternion);
		}
	}
	public void updateRotation(float x, float y, float z, float w) {
		if (movement != null) {
			movement.updateRotation(x, y, z, w);
		}
	}
	public float getActionRating(int actionType) {
		return 0;
	}
	public float getVitality() {
		return (float)health/(float)health_base;
	}
	public float getEnergy()	{
		return (getSleep() * getNourishment() * getHydration()) / 1000000.0f;
	}
	public Race getRace() {
		return (schema != null) ? Race.values()[schema.getInt(RACE)] : Race.NONE;
	}
	public float getWalkSpeed() {
		return (schema != null) ? schema.getFloat(WALK_SPEED) : 1f;
	}
	// Gets the creatures active vision radius
	public float getVisionRadiusNear() {
		return (schema != null) ? schema.getFloat(VISION_RADIUS) : 10f;
	}
	// Gets the creatures distant vision radius (used for map exploration)
	public float getVisionRadiusFar() {
		return 100f;
	}
	public void setOwned(boolean owned) {
		this.owned = owned;
	}
	public boolean isOwned() {
		return owned;
	}
	public void setHealthBase(int h) {
		this.health_base = h;
	}
	public void setHealth(int h) {
		this.health = h;
	}
	public void setProfession(int profession) {
		this.profession = profession;
	}
	public void setNourishment(int nourishment) {
		this.nourishment = nourishment;
	}
	public void setHydration(int hydration) {
		this.hydration = hydration;
	}
	public void setSleep(int sleep) {	
		this.sleep = sleep;
	}
	public void setFocus(int focus) {	
		this.focus = focus;
	}
	public void setStrength(int strength) {
		this.strength = strength;
	}
	public void setDexterity(int dexterity) {
		this.dexterity = dexterity;
	}
	public void setWisdom(int wisdom)	{
		this.wisdom = wisdom;
	}
	public void setCharisma(int charisma)	{
		this.charisma = charisma;
	}
	public int getProfession() {
		return profession;
	}
	public int getStrength() {
		return strength;
	}
	public int getDexterity() {
		return dexterity;
	}
	public int getWisdom() {
		return wisdom;
	}
	public int getCharisma() {
		return charisma;
	}
	public int getNourishment() {
		return nourishment;
	}
	public int getHydration() {
		return hydration;
	}
	public int getSleep() {
		return sleep;
	}
	public int getFocus() {
		return focus;
	}
	// 0 to 100
	public int getMorale() {
		return morale;
	}
	// 60% to 120%
	public float getPerformanceMod() {
		return 0.6f + (getMorale()*0.004f) + (getFocus()*0.002f);
	}
	public float getSkill(String skillName) {
		return 1f;
	}
	// -5 to 24
	public float getMeleeRating() {
		return (getStrength()/2) + getSkill("WeaponClass") + (getSkill("Combat")/2) - 5;
	}
	public float getAC() {
		return 8f;
	}
	public float getVitalAC() {
		return 12f;
	}
	public String getDisplayName() {
		return getRace().text();
	}
	public String toString() {
		return getRace().text();
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CreatureControl) {
			return ((CreatureControl)obj).getUniqueId() == uid;
		}
		return false;
	}
	@Override
	public int hashCode() {
		return (int) (uid ^ (uid >>> 32));
	}
	// Sets the unique id of this object
	public void setUniqueId(long uid) {
		this.uid = uid;
	}
	// Gets the unique id of this object
	public long getUniqueId() {
		return uid;
	}
	@Override
	public String[] getActionList() {
		return CREATURE_ACTIONS;
	}
	@Override
	public void writeFields(OutputStream os) throws IOException {
		super.writeFields(os);
		os.write(ByteUtils.convertToBytes((short)getProfession()));
		os.write((byte)getNourishment());
		os.write((byte)getHydration());
		os.write((byte)getSleep());
		os.write((byte)getFocus());
		os.write((byte)getStrength());
		os.write((byte)getDexterity());
		os.write((byte)getWisdom());
		os.write((byte)getCharisma());
	}
	@Override
	public void readFields(InputStream is) throws IOException {
		super.readFields(is);
		setProfession(ByteUtils.readShort(is));
		setNourishment(is.read());
		setHydration(is.read());
		setSleep(is.read());
		setFocus(is.read());
		setStrength(is.read());
		setDexterity(is.read());
		setWisdom(is.read());
		setCharisma(is.read());
	}
	@Override
	public void fillBuffer(ByteBuffer buffer) throws IOException	{
		super.fillBuffer(buffer);
		buffer.putShort((short)getProfession());
		buffer.put((byte)getNourishment());
		buffer.put((byte)getHydration());
		buffer.put((byte)getSleep());
		buffer.put((byte)getFocus());
		buffer.put((byte)getStrength());
		buffer.put((byte)getDexterity());
		buffer.put((byte)getWisdom());
		buffer.put((byte)getCharisma());
	}
	@Override
	public void readBuffer(ByteBuffer buffer) throws IOException	{
		super.readBuffer(buffer);
		setProfession(buffer.getShort());
		setNourishment(buffer.get());
		setHydration(buffer.get());
		setSleep(buffer.get());
		setFocus(buffer.get());
		setStrength(buffer.get());
		setDexterity(buffer.get());
		setWisdom(buffer.get());
		setCharisma(buffer.get());
	}
	@Override
	public void write(JmeExporter ex) throws IOException {
		super.write(ex);
		OutputCapsule oc = ex.getCapsule(this);
		oc.write(getProfession(), "profession", 0);
		oc.write(getNourishment(), "nourishment", 0);
		oc.write(getHydration(), "hydration", 0);
		oc.write(getSleep(), "sleep", 0);
		oc.write(getFocus(), "focus", 0);
		oc.write(getStrength(), "strength", 0);
		oc.write(getDexterity(), "dexterity", 0);
		oc.write(getWisdom(), "wisdom", 0);
		oc.write(getCharisma(), "charisma", 0);
	}
	@Override
	public void read(JmeImporter im) throws IOException {
		super.read(im);
		InputCapsule ic = im.getCapsule(this);
		setProfession(ic.readInt("profession", 0));
		setNourishment(ic.readInt("nourishment", 0));
		setHydration(ic.readInt("hydration", 0));
		setSleep(ic.readInt("sleep", 0));
		setFocus(ic.readInt("focus", 0));
		setStrength(ic.readInt("strength", 0));
		setDexterity(ic.readInt("dexterity", 0));
		setWisdom(ic.readInt("wisdom", 0));
		setCharisma(ic.readInt("charisma", 0));
	}
}