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
// Java Dependencies
import java.util.ArrayList;
// JME3 Dependencies
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;

/** @author Paul Collins
 *  @version v0.1 ~ 02/10/2014
 *  HISTORY: Version 0.1 created ResourceControl ~ 02/10/2014
 */
public class ResourceControl extends ItemControl {
	// Inherited Params from ItemControl
	// public static final Param SIZE = new Param("Size", Size.class, 0);
	// public static final Param WEIGHT = new Param("Weight", Float.class, 1);
	// public static final Param COMPOSITION = new Param("Composition", String.class, 2);

	// Field Params
	public static final Param RESOURCE_TYPE = new Param("ResourceType", String.class, 3);
	public static final Param SUB_TYPE = new Param("SubType", String.class, 4);
	
	public static final Param[] RESOURCE_PARAMS = { SIZE ,WEIGHT, COMPOSITION, RESOURCE_TYPE, SUB_TYPE };
	public static final ObjectType RESOURCE_OBJ_TYPE = new ObjectType<ResourceControl>("Resource", ResourceControl.class, RESOURCE_PARAMS);
	
	public static final ResourceType DEFAULT_TYPE = new ResourceType("Any");
	public static final SubType UNSPECIFIED_SUBTYPE = new SubType("Unspecified", 0, 0);
	
	private static final ArrayList<ResourceType> RESOURCE_TYPES = new ArrayList<ResourceType>();
	
	public static ResourceType registerResourceType(ResourceType type) {
		RESOURCE_TYPES.add(type);
		return type;
	}
	
	public static ArrayList<ResourceType> getResourceTypes() {
		return RESOURCE_TYPES;
	}
	
	public static ResourceType findResourceType(String text) {
		for (ResourceType t : RESOURCE_TYPES) {
			if (t.matches(text)) {
				return t;
			}
		}
		return DEFAULT_TYPE;
	}
	
	// Resource Styles
	/*public static final Style NO_STYLE = new Style("None", 0, 0);
	public static final Style ANY_STYLE = new Style("Any", 0, 0);
	public static final Style TWIGS = new Style("Twigs", 2, 300);
	public static final Style BRANCHES = new Style("Branches", 1, 50);
	public static final Style LOGS = new Style("Logs", 0, 30);
	public static final Style COPPER = new Style("Copper", 1, 15);
	public static final Style IRON = new Style("Iron", 1, 20);
	public static final Style SILVER = new Style("Silver", 1, 10);
	public static final Style GOLD = new Style("Gold", 1, 5);*/

	/*public static class Profile {
		private Category category = Category.ANY;
		private short style = 0;
		private short quantity = 0;
		private String description = "Any Resource";
		public Profile(int categoryIndex, int style, int quantity, String description) {
			this (Category.values()[categoryIndex], style, quantity, description);
		}
		public Profile(Category category, int style, int quantity, String description) {
			this.category = category;
			this.style = (short)style;
			this.quantity = (short)quantity;
			this.description = description;
		}
		public ResourceControl.Category getCategory() {
			return category;
		}
		public short getStyle() {
			return style;
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
	}*/
	
	/*public static class Style {
		private String name;
		private int min;
		private int max;
		
		public Style(String name, int min, int max) {
			this.name = name;
			this.min = min;
			this.max = max;
		}
		public int getMin() {
			return min;
		}
		public int getMax() {
			return max;
		}
		public String getName() {
			return name;
		}
		public String toString() {
			return name;
		}
	}*/
	
	/* Resource Categories */
	public static class ResourceType {	
		private String text = "Any";
		private ArrayList<SubType> types = new ArrayList<SubType>();
		
		public ResourceType(String text) {
			this.text = text;
		}
		public ArrayList<SubType> getSubTypes() {
			return types;
		}
		public SubType addSubType(SubType type) {
			types.add(type);
			return type;
		}
		public SubType findSubType(String text) {
			for (SubType t : types) {
				if (t.getName().equals(text)) {
					return t;
				}
			}
			return UNSPECIFIED_SUBTYPE;
		}
		public String text() {
			return text;
		}
		public String toString() {
			return text;
		}
		public boolean equals(Object obj) {
			return matches(obj);
		}
		public boolean matches(Object obj) {
			if (obj != null) {
				return text.equals(obj.toString());
			}
			return false;
		}
	}
	
	public static class SubType {
		private String name;
		private int min;
		private int max;
		
		public SubType(String name, int min, int max) {
			this.name = name;
			this.min = min;
			this.max = max;
		}
		public int getMin() {
			return min;
		}
		public int getMax() {
			return max;
		}
		public String getName() {
			return name;
		}
		public String toString() {
			return name;
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

	protected int quantity;
	
	public ResourceControl() {
		super();
	}
	public ResourceControl(Schematic schema) {
		super(schema);
	}
	// Alternate constructor that links the control with it's spatial
	public ResourceControl(Spatial obj)	{
		super(obj);
	}
	@Override
	public Control cloneForSpatial(Spatial spatial)	{
		final ResourceControl control = new ResourceControl(schema);
		control.setSpatial(spatial);
		return control;
	}
	@Override
	public void resetFields() {
		super.resetFields();
		if (spatial != null) {
			// Setup Resource UserData
			setQuantity(0);
		}
	}
	public void copyFields(ResourceControl control)	{
		super.copyFields(control);
		if (spatial != null) {
			// Copy UserData
			setQuantity(control.getQuantity());
		}
	}
	public ResourceType getResourceType() {
		return (schema != null) ? findResourceType(schema.getString(RESOURCE_TYPE)): DEFAULT_TYPE;
	}
	public SubType getSubType() {
		return (schema != null) ? getResourceType().findSubType(schema.getString(SUB_TYPE)) : UNSPECIFIED_SUBTYPE;
	}
	public int getMax() {
		return getSubType().getMax();
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	@Override
	public int getQuantity() {
		return quantity;
	}
	@Override
	public void increment(int count)	{
		quantity = quantity + count;
	}
	@Override
	public void decrement(int count) {
		quantity = quantity - count;
	}
	public boolean validateResources(Schematic.ResourceParam param) {
		return (schema != null) && (param.getResourceType().matches(getResourceType())) && (param.getQuantity() <= quantity);
	}
	public boolean isResourceType(ResourceControl resource) {
		return resource.getResourceType().matches(getResourceType()) && resource.getSubType().matches(getSubType());
	}
}