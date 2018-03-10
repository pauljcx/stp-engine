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
package com.stp.games.jme.hud;
// JME3 Dependencies
import com.jme3.font.BitmapFont;
import com.jme3.font.LineWrapMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture2D;

import java.util.ArrayList;
import com.stp.util.XMLObject;

/** @author Paul Collins
 *  @version v1.0 ~ 11/02/2017
 *  HISTORY: Version 1.0 Created the HudStyle class to hold stlye options that can be shared with multiple components ~ 11/02/2017
 */
public class HudStyle implements XMLObject {
	private static class Property {
		private String key = "";
		private Object value = null;
		public Property(String key, Object value) {
			this.key = key;
			this.value = value;
		}
		public String getKey() {
			return key;
		}
		public Object getValue() {
			return value;
		}
		public void setValue(Object value) {
			this.value = value;
		}
		@Override
		public boolean equals(Object other) {
			if (other instanceof Property) {
				return ((Property)other).getKey().equals(key);
			}
			return false;
		}
		@Override
		public String toString() {
			return key + "=" + value;
		}
	}
	protected ArrayList<Property> properties = new ArrayList<Property>();
	protected String name = "";
	protected int width;
	protected int height;
	protected float scale = 1f;
	
	public HudStyle() {
	}
	public HudStyle(String name) {
		this.name = name;
	}
	public HudStyle(String name, ColorRGBA baseColor) {
		this(name, null, baseColor, null);
	}
	public HudStyle(String name, Texture2D texture) {
		this(name, texture, ColorRGBA.White, null);
	}
	public HudStyle(String name, Texture2D texture, ColorRGBA baseColor, ColorRGBA activeColor) {
		setName(name);
		setBackground(texture, baseColor, activeColor);
	}
	public HudStyle(String name, HudFont font, float size, ColorRGBA color) {
		this(name, font, size, color, BitmapFont.Align.Left, BitmapFont.VAlign.Top, LineWrapMode.NoWrap);
	}
	public HudStyle(String name, HudFont font, float size, ColorRGBA color, BitmapFont.Align align, BitmapFont.VAlign vAlign, LineWrapMode wrap) {
		setName(name);
		setFont(font, size, color, align, vAlign, wrap);
	}
	public HudStyle(String name, int width, int height, float widthFactor, float heightFactor) {
		setName(name);
		setSize(width, height, widthFactor, heightFactor);
	}
	public HudStyle(String name, HudStyle other) {
		setName(name);
		setScale(other.getScale());
		set(other);
	}
	public void setName(String name) {
		this.name = name;
	}
	public HudStyle setScale(float scale) {
		this.scale = scale;
		return this;
	}
	public HudStyle setBackground(Texture2D texture, ColorRGBA baseColor, ColorRGBA activeColor) {
		put("texture", texture);
		put("baseColor", baseColor);
		put("activeColor", activeColor);
		return this;
	}
	public HudStyle setBorder(ColorRGBA color, int size) {
		put("borderColor", color);
		put("borderSize", size);
		return this;
	}
	public HudStyle setFont(HudFont font, float size, ColorRGBA color, BitmapFont.Align align, BitmapFont.VAlign vAlign, LineWrapMode wrap) {
		put("font", font);
		put("fontSize", size);
		put("fontColor", color);
		put("align", align);
		put("vAlign", vAlign);
		put("lineWrap", wrap);
		return this;
	}
	public HudStyle setSize(int width, int height, float widthFactor, float heightFactor) {
		this.width = width;
		this.height = height;
		put("widthFactor", widthFactor);
		put("heightFactor", heightFactor);
		return this;
	}	
	// Copies the style parameters from another style
	public HudStyle set(HudStyle other) {
		for (Property p : other.getProperties()) {
			put(p.getKey(), p.getValue());
		}
		this.width = other.getWidth();
		this.height = other.getHeight();
		this.scale = other.getScale();
		return this;
	}
	public HudStyle createCopy(String name) {
		return new HudStyle(name, this);
	}
	public String getName() {
		return name;
	}
	public Object get(String key) {
		for (Property p : properties) {
			if (p.getKey().equals(key)) {
				return p.getValue();
			}
		}
		return null;
	}
	public void put(String key, Object value) {
		for (Property p : properties) {
			if (p.getKey().equals(key)) {
				p.setValue(value);
				return;
			}
		}
		properties.add(new Property(key, value));
	}
	public int getSize() {
		return properties.size();
	}
	public ArrayList<Property> getProperties() {
		return properties;
	}
	public void remove(String key) {
		for (int p = 0; p < properties.size(); p++) {
			if (properties.get(p).getKey().equals(key)) {
				properties.remove(p);
				return;
			}
		}
	}
	public String getString(String key) {
		Object value = get(key);
		return (value != null) ? value.toString() : "";
	}
	public boolean getBool(String key, boolean defaultValue) {
		Object value = get(key);
		return (value instanceof Boolean) ? (Boolean)value : false;
	}
	public int getInt(String key) {
		Object value = get(key);
		return (value instanceof Integer) ? (Integer)value : 0;
	}
	public float getFloat(String key) {
		Object value = get(key);
		return (value instanceof Float) ? (Float)value : 0f;
	}
	public Texture2D getTexture(String key) {
		Object value = get(key);
		return (value instanceof Texture2D) ? (Texture2D)value : null;
	}
	public ColorRGBA getColor(String key) {
		Object value = get(key);
		return (value instanceof ColorRGBA) ? (ColorRGBA)value : null;
	}
	public HudFont getFont(String key) {
		Object value = get(key);
		return (value instanceof HudFont) ? (HudFont)value : null;
	}
	public BitmapFont.Align getAlignment(String key) {
		Object value = get(key);
		return (value instanceof BitmapFont.Align) ? (BitmapFont.Align)value : BitmapFont.Align.Left;
	}
	public BitmapFont.VAlign getVerticalAlignment(String key) {
		Object value = get(key);
		return (value instanceof BitmapFont.VAlign) ? (BitmapFont.VAlign)value : BitmapFont.VAlign.Center;
	}
	public LineWrapMode getLineWrapMode(String key) {
		Object value = get(key);
		return (value instanceof LineWrapMode) ? (LineWrapMode)value : LineWrapMode.NoWrap;
	}
	public HudAudio getAudio(String key) {
		Object value = get(key);
		return (value instanceof HudAudio) ? (HudAudio)value : null;
	}
	public int getTextDisplayWidth(String textValue, BitmapFont font, float fontSize) {
		if (font != null && fontSize > 0) {
			return Math.round(font.getLineWidth(textValue)*(fontSize/font.getPreferredSize()));
		} else {
			return 0;
		}
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public float getScale() {
		return scale;
	}
	public String[] getPropertyNames() {
		String[] nodeNames = new String[properties.size()+4];
		nodeNames[0] = "name";
		nodeNames[1] = "scale";
		nodeNames[2] = "width";
		nodeNames[3] = "height";
		for (int n = 0; n < nodeNames.length; n++) {
			nodeNames[n+4] = properties.get(n).getKey();
		}
		return nodeNames;
	}
	public Object getProperty(String nodeName) {
		if (nodeName.equals("name")) {
			return name;
		} else if (nodeName.equals("scale")) {
			return scale;
		} else if (nodeName.equals("width")) {
			return width;
		} else if (nodeName.equals("height")) {
			return height;
		} else {
			Object value = get(nodeName);
			if (value instanceof Texture2D) {
				return ((Texture2D)value).getKey();
			} else {
				return value;
			}
		}
	}
	public void setProperty(String nodeName, Object value, String className) {
		if (nodeName.equals("name")) {
			this.name = "" + value;
		} else if (nodeName.equals("scale")) {
			this.scale = (value instanceof Float) ? (Float)value : 0f;
		} else if (nodeName.equals("width")) {
			this.width = (value instanceof Integer) ? (Integer)value : 0;
		} else if (nodeName.equals("height")) {
			this.height = (value instanceof Integer) ? (Integer)value : 0;
		} else if (className.equals("TextureKey")) {
			put(nodeName, HudManager.getInstance().loadTexture2D("" + value));
		} else if (className.equals("ColorRGBA")) {
			put(nodeName, decodeColor("" + value));
		} else if (className.equals("HudFont")) {
			put(nodeName, HudManager.getInstance().getFont("" + value));
		} else if (className.equals("HudAudio")) {
			put(nodeName, HudManager.getInstance().getAudio("" + value));
		} else if (className.equals("Align")) {
			put(nodeName, BitmapFont.Align.valueOf("" + value));
		} else if (className.equals("VAlign")) {
			put(nodeName, BitmapFont.VAlign.valueOf("" + value));
		} else if (className.equals("LineWrapMode")) {
			put(nodeName, LineWrapMode.valueOf("" + value));
		} else {
			put(nodeName, value);
		}
	}
	private ColorRGBA decodeColor(String input) {
		 // "Color[" + r + ", " + g + ", " + b + ", " + a + "]";
		 if (input.contains("Color[")) {
			 ColorRGBA color = new ColorRGBA();
			 int start = 6;
			 int end = input.indexOf(",");
			 color.r = Float.valueOf(input.substring(start, end));
			 start = end + 1;
			 end = input.indexOf(",", start);
			 color.g = Float.valueOf(input.substring(start, end));
			 start = end + 1;
			 end = input.indexOf(",", start);
			 color.b = Float.valueOf(input.substring(start, end));
			 start = end + 1;
			 end = input.length()-1;
			 color.a = Float.valueOf(input.substring(start, end));
			 return color;
		 } else {
			 return null;
		 }
	}
}