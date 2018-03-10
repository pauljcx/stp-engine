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
import com.jme3.scene.control.LightControl;

/** @author Paul Collins
 *  @version v0.1 ~ 05/04/2013
 *  HISTORY: Version 0.1 created ToolControl ~ 05/04/2013
 */
public class ToolControl extends ItemControl {
	// Inherited Params
	//public static final Param SIZE = new Param("Size", Integer.class, 0);
	//public static final Param WEIGHT = new Param("Weight", Float.class, 1);
	//public static final Param COMPOSITION = new Param("Composition", String.class, 2);
	
	// Field Params
	public static final Param TOOL_CLASS = new Param("Tool Class", ToolClass.class, 3);
	public static final Param POWER = new Param("Power", Integer.class, 4);
	public static final Param QUALITY = new Param("Quality", Integer.class, 5);
	public static final Param SHAPE = new Param("Shape", BoundingShape.class, 6);
	
	public static final Param[] TOOL_PARAMS = { SIZE ,WEIGHT, COMPOSITION, TOOL_CLASS, POWER, QUALITY, SHAPE };
	public static final ObjectType TOOL_TYPE = new ObjectType<ToolControl>("Tool", ToolControl.class, TOOL_PARAMS);

	//"1,8,8,8,4,8,0,0,0,0"
	public enum RatingType {
		STAB("stab"),
		SLASH("slash"),
		CHOP("chop"),
		PICK("pick"),
		BASH("bash"),
		WHIP("whip"),
		LASSO("lasso"),
		SPLASH("splash"),
		ILLUMINATE("illuminate"),
		IGNITE("ignite");
		private String text;
		private RatingType(String text) { this.text = text; }
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
	
	public enum ToolClass {
		NONE("None", new int[] { 0,0,0,0,0,0,0,0,0,0 }, new int[] { 0,0,0,0,0,0,0,0,0,0 }),
		BLUDGEONING("Bludgeoning", new int[] { 0,0,1,2,10,0,0,0,0,0 }, new int[] { 0,4,6,7,2,1,0,0,0,0 }),
		BLADE("Blade", new int[] { 8,10,7,3,2,0,0,0,0,0 }, new int[] { 1,8,8,8,4,8,0,0,0,0 }),
		SPLITTING("Splitting", new int[] { 1,6,10,2,5,0,0,0,0,0 }, new int[] { 1,4,4,4,3,8,1,0,0,0 }),
		SPEAR("Spear", new int[] { 10,6,2,4,5,0,0,0,0,0 }, new int[] { 1,6,6,5,4,8,0,0,0,0 }),
		BOW("Bow", new int[] {1,1,0,0,1,1,0,0,0,0 }, new int[] { 0,6,4,4,2,5,0,0,0,0 }),
		CORDAGE("Cordage", new int[] { 0,0,0,0,0,10,10,0,0,0 }, new int[] { 0,0,0,0,0,10,10,0,0,0 }),
		LIGHT("Light",  new int[] { 0,0,0,0,0,0,0,0,10,0 }, new int[] { 0,3,3,3,2,2,6,0,0,0 });
		private String text;
		private int[] actions;
		private int[] defences;
		private ToolClass(String text, int[] actions, int[] defences) { this.text = text; this.actions =actions; this.defences = defences; }
		public byte index() { return (byte)ordinal(); }
		public String text() { return text; }
		public String toString() { return text; }
		public int[] getActionRatings() { return actions; }
		public int [] getDefenceRatings() { return defences; }
		public boolean matches(Object obj) {
			if (obj != null) {
				return text.equals(obj.toString());
			}
			return false;
		}
	}
	
	// Stab,Slash,Chop,Pick,Bash,Whip,Lasso,Splash,Illuminate,Ignite
	
	/*public static final Schematic AXE = new Schematic("Steel Axe",
													"axe",
													Type.CLOTHING,
													Schematic.Category.WEAPON,
													"Models/Teapot/Teapot.mesh.xml",
													"", // Description
													new Object[] { Size.MEDIUM.ordinal(),
														12f,
														"",
														ToolClass.SPLITTING.ordinal(),
														75,
														50 });*/
														
	/*public static final Schematic PICK = new Schematic("Pick",
													"pick",
													Type.TOOL,
													Schematic.Category.WEAPON,
													"Models/Teapot/Teapot.mesh.xml",
													"", // Description
													new Object[] { Size.MEDIUM.ordinal(),
														16f,
														"",
														ToolClass.SPLITTING.ordinal(),
														75,
														50 });*/
														
	/*public static final Schematic HAMMER = new Schematic("Stone Hammer",
													"hammer_tall",
													Type.TOOL,
													Schematic.Category.WEAPON,
													"Models/Teapot/Teapot.mesh.xml",
													"", // Description
													new Object[] { Size.MEDIUM.ordinal(),
														8f, // Weight
														"Wood & Stone",
														ToolClass.BLUDGEONING.ordinal(),
														80,
														35 },
													new Schematic.Step[] {
														new Schematic.Step("Form Handle", // Name
															"Split branch on one end to form a Y. Smooth out any rough areas.", // Description
															12f, // Duration
															new Schematic.ToolParam(RatingType.SLASH, Size.ANY, Schematic.ToolEffect.SPEED, "Blade", 0.35f)), // Tool
														new Schematic.Step("Set Stone", // Name
															"Set the stone in the Y opening wrapping the ends of the branch over it.", // Description
															2f, // Duration
															null), // Tool
														new Schematic.Step("Secure With Cordage", // Name
															"Tie cordage around the overlapped branch ends and Y base to secure stone.", // Description
															6f, // Duration
															null)}, // Tool
													new Schematic.ResourceParam[] {
														new Schematic.ResourceParam(ResourceControl.Category.WOOD, 0, 1, "Branch"),
														new Schematic.ResourceParam(ResourceControl.Category.STONE, 0, 1, "Stone")},
													new Schematic.ItemParam[] {
														new Schematic.ItemParam(Type.TOOL, Size.ANY, new Object[] { ToolClass.CORDAGE, 3 }, 1, "Cordage")
													});*/
														
	/*public static final Schematic PRIMATIVE_ROPE = new Schematic("Primative Rope",
													"twine", // Icon
													Type.TOOL, // Type
													Schematic.Category.TOOL, // Category
													"Models/Teapot/Teapot.mesh.xml", // Model
													"", // Description
													new Object[] { Size.SMALL.ordinal(), // Size
														0.1f, // Weight
														"Plant Fibers", // Composition
														ToolClass.CORDAGE.ordinal(), // Tool Class
														1, // Power (length)
														35 // Quality
													}, 
													new Schematic.Step[] {
														new Schematic.Step("Break Stalk", // Name
															"Break stalk into 3-4 pieces lengthwise. You may use a blade to cut it half then quarters.", // Description
															15f, // Duration
															new Schematic.ToolParam(RatingType.SLASH, Size.ANY, Schematic.ToolEffect.SPEED, "Blade", 0.5f)), // Tool
														new Schematic.Step("Seperate Fibers", // Name
															"Break woody insides away and clean bark away from plant fibers.", // Description
															10f, // Duration
															null), // Tool
														new Schematic.Step("Twist Fibers", // Name
															"Twist fibers until kink forms, tie off ends to make cordage.", // Description
															5f, // Duration
															null)}, // Tool
													new Schematic.ResourceParam[] {
														new Schematic.ResourceParam(ResourceControl.Category.STALK, 0, 1, "Dead Plant Stalk")
													});*/

	// Stab,Slash,Chop,Pick,Bash,Whip,Lasso,Splash,Illuminate,Ignite

	protected int selected = 0;
	
	public ToolControl() {
		super();
	}
	public ToolControl(Schematic schema) {
		super(schema);
	}
	// Alternate constructor that links the control with it's spatial
	public ToolControl(Spatial obj)	{
		super(obj);
	}
	@Override
	public Control cloneForSpatial(Spatial spatial) {
		final ToolControl control = new ToolControl(schema);
		control.setSpatial(spatial);
		return control;
	}
	@Override
	public void setFields(Schematic schema) {
		super.setFields(schema);
		setQuality(schema.getInt(QUALITY));
	}
	@Override
	public void resetFields() {
		super.resetFields();
		if (spatial != null) {
			// Setup Item UserData
			setQuality(0);
		}
	}
	public void copyFields(ToolControl control) {
		super.copyFields(control);
		if (spatial != null) {
			// Copy UserData
			setQuality(control.getQuality());
		}
	}
	@Override
	public boolean hasLight() {
		if (spatial != null) {
			return spatial.getControl(LightControl.class) != null;
		}
		return false;
	}
	public void setQuality(int quality) {
		put("quality", quality);
	}
	public float getRating() {
		return (getDurability()/100f) * (getQuality() /100f) * (getPower()/100f);
	}
	public ToolClass getToolClass() {
		return (schema != null) ? ToolClass.values()[schema.getInt(TOOL_CLASS)] : ToolClass.NONE;
	}
	public int getPower() {
		return (schema != null) ? schema.getInt(POWER) : 0;
	}
	public int getQuality() {
		return getInt("quality");
	}
	public float[] getActionRatings(float[] ratings) {
		int[] classRatings = getToolClass().getActionRatings();
		for (int r = 0; r < classRatings.length; r++) {
			ratings[r] = Math.max(ratings[r], (getRating()*classRatings[r])/10f);
		}
		return ratings;
	}
	public float[] getDefenceRatings(float[] ratings) {
		int[] classRatings = getToolClass().getDefenceRatings();
		for (int r = 0; r < classRatings.length; r++) {
			ratings[r] = Math.max(ratings[r], (getRating()*classRatings[r])/10f);
		}
		return ratings;
	}
	public static int getRatingIndex(String ratingType) {
		for (RatingType r : RatingType.values()) {
			if (r.text().equals(ratingType)) {
				return r.ordinal();
			}
		}
		return -1;
	}
}