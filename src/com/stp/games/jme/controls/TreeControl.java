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
/** @author Paul Collins
 *  @version v0.1 ~ 02/11/2014
 *  HISTORY: Version 0.1 created TreeControl for setting up and accessing user data for trees 02/11/2014
 */
public class TreeControl extends PlantControl {
	public static final String[] TREE_ACTIONS = { "chop", "gather" };
	
	// Inherited Field Params
	//public static final Param AGE = new Param("Age", Integer.class, 0);
	//public static final Param STATE = new Param("State", State.class, 1);
	//public static final Param DENSITY = new Param("Density", Integer.class, 2);	
	//public static final Param CATEGORY = new Param("Category", Category.class, 3);
	//public static final Param RESOURCES = new Param("Resources", Integer.class, 4);
	//public static final Param QUANTITIES = new Param("Quantities", String.class, 5);
	
	// Field Indices
	public static final Param STYLE = new Param("Style", Style.class, 6);
		
	// Tree Styles
	public enum Style
	{
		NONE("none"),
		APPLE("apple"),
		BIRCH("birch"),
		CHERRY("cherry"),
		LEMON("lemon"),
		MAPLE("maple"),
		OAK("oak"),
		PEAR("pear"),
		PINE("pine"),
		SPRUCE("spruce"),
		WILLOW("willow");
		private String text;
		private Style(String text) { this.text = text; }
		public byte index() { return (byte)ordinal(); }
		public String text() { return text; }
		public String toString() { return text; }
		public boolean matches(Object obj)
		{
			if (obj != null)
			{
				return text.equals(obj.toString());
			}
			return false;
		}
	}
	// Stab,Slash,Chop,Pick,Bash,Whip,Lasso,Splash,Illuminate,Ignite
	
	/*public static final Schematic OAK_TREE = new Schematic("Oak Tree",
													"none",
													Type.PLANT,
													Schematic.Category.PLANT,
													"Models/Tree/Tree.mesh.j3o",
													"", // Description
													new Object[] { 12, // Age
														State.ACTIVE, // State
														10, // Density
														Category.TREE.ordinal(), // Category
														"Leaves,Twigs,Branches,Logs", // Resources
														"0,0,0,0", // Quantities
														Style.OAK.ordinal() }); // Style
	*/													
	/*public static final Schematic SPRUCE_TREE = new Schematic("Spruce Tree",
													"none",
													Type.PLANT,
													Schematic.Category.PLANT,
													"Models/Spruce/SpruceMediumPoly.j3o",
													"", // Description
													new Object[] { 20, // Age
														State.ACTIVE, // State
														8, // Density
														Category.TREE.ordinal(), // Category
														"Pine Cones,Twigs,Branches,Logs", // Resources
														"0,0,0,0", // Quantities
														Style.SPRUCE.ordinal() }); // Style
	*/
	public TreeControl() {
		super();
	}
	public TreeControl(Schematic schema) {
		super(schema);
	}
	@Override
	public Control cloneForSpatial(Spatial spatial)
	{
		final TreeControl control = new TreeControl(schema);
		control.setSpatial(spatial);
		return control;
	}
	@Override
	public void setFields(Schematic schema)
	{
		super.setFields(schema);
	}
	@Override
	public void resetFields()
	{
		super.resetFields();
		if (spatial != null)
		{
			// Setup Item UserData
		}
	}
	public void copyFields(TreeControl control)
	{
		super.copyFields(control);
		if (spatial != null)
		{
			// Copy UserData
		}
	}
	@Override
	public void writeFields(OutputStream os) throws IOException
	{
		super.writeFields(os);
	}
	@Override
	public void readFields(InputStream is) throws IOException
	{
		super.readFields(is);
	}
	public Style getStyle()
	{
		return (schema != null) ? Style.values()[schema.getInt(STYLE)] : Style.NONE;
	}
	public Style getStyle(String style)
	{
		for (Style s : Style.values())
		{
			if (s.matches(style))
			{
				return s;
			}
		}
		return Style.NONE;
	}
	// Returns a list of user actions that indicate how a player can interact with this object
	@Override
	public String[] getActionList() {
		return TREE_ACTIONS;
	}
}