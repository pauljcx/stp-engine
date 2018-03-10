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
import com.jme3.audio.AudioContext;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.AudioSource;
import com.jme3.font.BitmapFont;
import com.jme3.font.LineWrapMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture2D;
// Java Dependencies

/** @author Paul Collins
 *  @version v1.0 ~ 03/21/2015
 *  HISTORY: Version 1.0 Created class for Hud Components ~ 03/21/2015
 */
public class HudComponent implements Comparable<HudComponent> {
	// Orientation Options
	public static final int ABSOLUTE = 0;
	public static final int RELATIVE = 1;
	public static final int CENTERED = 2;
	public static final int MIN_JUSTIFY = 3;
	public static final int MAX_JUSTIFY = 4;
	public static final int LOCAL_CENTER = 5;
	
	// Inset Options
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int TOP = 2;
	public static final int BOTTOM = 3;
	
	// Fade Options
	public static final int FADE_IN = 1;
	public static final int FADE_NONE = 0;
	public static final int FADE_OUT = -1;
	
	// Zoom Options
	public static final int ZOOM_IN = 1;
	public static final int ZOOM_NONE = 0;
	public static final int ZOOM_OUT = -1;
	
	// Scroll Axis
	public static final int SCROLL_X = 0;
	public static final int SCROLL_Y = 1;
	public static final int SCROLL_Z = 2;
	public static final int SCROLL_UP = 3;
	public static final int SCROLL_DOWN = 4;
	
	// Event Codes
	public static final int CLICK_EVENT = 0;
	public static final int SELECT_EVENT = 1;
	public static final int CHANGE_EVENT = 2;
	public static final int CLOSE_EVENT = 3;
	public static final int SIZE_EVENT = 4;
	public static final int FOCUS_EVENT = 5;
	public static final int SCROLL_EVENT = 6;
	
	//public static final float[] DEFAULT_TX_COORDS = new float[]{ 0, 0, 1, 0, 1, 1, 0, 1 };
	public static final float[] DEFAULT_TX_COORDS = new float[]{ 0, 1, 1, 1, 1, 0, 0, 0 };
	public static final HudStyle DEFAULT_STYLE = new HudStyle("HudComponent");

	protected String name = "";
	protected String command = "";
	protected HudStyle style;
	protected ClipArea clip;
	protected HudComponent parent;
	protected String textureName = "texture";
	protected String colorName = "baseColor";
	
	protected float[] txCoords;
	protected int[] insets = new int[4];
	protected int xOrient;
	protected int yOrient;
	protected int x;
	protected int y;
	protected int z;
	protected int width;
	protected int height;
	protected float fadeAlpha;
	protected float zoomLevel;
	protected int fading;
	protected int zooming;
	protected float speed;
	protected Vector3f angles;

	protected boolean visible;
	protected boolean active;
	protected boolean popup;
	protected boolean draggable;
	protected boolean highlight;
	protected boolean inheritWidth;
	protected boolean inheritHeight;
	protected boolean inheritScale;
	protected boolean renderBackground;
	protected boolean renderText;

	public static class ClipArea {
		int x, y, w, h;
		public ClipArea(int x, int y, int w, int h) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}
	}
	
	public HudComponent() {
		this ("HudComponent", 0, 0);
	}
	public HudComponent(String name, int width, int height) {
		this (name, DEFAULT_STYLE, width, height);
	}
	public HudComponent(String name, HudStyle style, int width, int height) {
		setName(name);
		setStyle(style);
		setSize(width, height);
		setRelative();
		setVisible(true);
		setInheritWidth(width <= 0);
		setInheritHeight(height <= 0);
		setInheritScale(false);
		setRenderBackground(true);
		setRenderText(true);
		setPopup(false);
		setHighlight(false);
		setDraggable(false);
		setTxCoords(DEFAULT_TX_COORDS);
		this.active = false;
		this.angles = new Vector3f();
		this.fading = FADE_NONE;
		this.fadeAlpha = 1f;
		this.zooming = ZOOM_NONE;
		this.speed = 1f;
		this.zoomLevel = 1f;
	}
	// Gets the name of this component
	public String getName() {
		return name;
	}
	// Sets the name of this component
	public void setName(String name) {
		this.name = name;
	}
	// Gets an action command to use in events
	public String getActionCommand() {
		return command;
	}
	public void setActionCommand(String command) {
		this.command = command;
	}
	// Returns true if this component is currently visible and should be rendered
	public boolean isVisible() {
		return visible;
	}
	// Sets the component visible so that it will be rendered
	public void setVisible(boolean visible) {
		this.visible = visible;
		this.fadeAlpha = 1f;
	}
	// Toggles the current visibilty state
	public boolean toggleVisible() {
		setVisible(!visible);
		return visible;
	}
	// Determine if this component is actually rendered by check the visibilty of it's parents
	public boolean isViewable() {
		if (!visible) {
			return false;
		}
		HudComponent p = getParent();
		while (p != null) {
			if (!p.isVisible()) {
				return false;
			}
			p = p.getParent();
		}
		return true;
	}
	// Returns true if this component is currently active
	public boolean isActive() {
		return active;
	}
	// Sets the active flag for this component
	public void setActive(boolean active) {
		this.active = active;
	}
	// Returns true if this component is considered a popup and should be rendered on top
	public boolean isPopup() {
		return popup;
	}
	// Sets whether this component is a popup and should be rendered on top
	public void setPopup(boolean popup) {
		this.popup = popup;
	}
	// Sets this components dragable flag
	public void setDraggable(boolean draggable) {
		this.draggable = draggable;
	}
	// Returns true if dragging is allowed for this component
	public boolean dragEnabled() {
		return draggable;
	}
	// Returns true if other components can be dropped on this component
	public boolean dropEnabled() {
		return false;
	}
	// Returns true if this component has a parent
	public boolean hasParent() {
		return parent != null;
	}
	// Returns the parent of this component
	public HudComponent getParent() {
		return parent;
	}
	// Sets the parent of this component
	public void setParent(HudComponent parent) {
		this.parent = parent;
	}
	// Returns true if the contents of this component need to be clipped
	public boolean hasClip() {
		return (clip != null);
	}
	// Returns the clip area to use when rendering this component
	public ClipArea getClip() {
		return clip;
	}
	// Sets the clip area to use when rendering this component
	public void setClip(ClipArea clip) {
		this.clip = clip;
	}
	// Gets the rendering style for this hud component
	public HudStyle getStyle() {
		return style;
	}
	// Sets the rendering style for this component
	public void setStyle(HudStyle style) {
		if (style == null) {
			this.style = DEFAULT_STYLE;
		} else {
			this.style = style;
		}
	}
	// Returns true if this component has a background to render
	public boolean hasBackground() {
		return renderBackground && style.getColor(colorName) != null;
	}
	// Returns the color to use for rendering the background
	public ColorRGBA getBackground() {
		if (isActive() && style.getColor("activeColor") != null) {
			return style.getColor("activeColor");
		} else {
			return style.getColor(colorName);
		}
	}
	public void setColorName(String colorName) {
		this.colorName = colorName;
	}
	public void setRenderBackground(boolean renderBackground) {
		this.renderBackground = renderBackground;
	}
	public void setRenderText(boolean renderText) {
		this.renderText = renderText;
	}
	// Returns true if this component is currently fading in or out
	public boolean isFading() {
		return fading != FADE_NONE;
	}
	// Gets the current fading amount for this component
	public float getFadeAlpha() {
		return fadeAlpha;
	}
	// Sets the current fading amount for this component
	public void setFadeAlpha(float fadeAlpha) {
		this.fadeAlpha = fadeAlpha;
	}
	// Returns the texture to use for rendering the background
	public Texture2D getTexture() {
		return style.getTexture(textureName);
	}
	public void setTextureName(String textureName) {
		this.textureName = textureName;
	}
	// Returns the 3D object to use for rendering this component
	public Spatial getSpatial() {
		return null;
	}
	// Returns true if this component has text to render
	public boolean hasText() {
		return renderText && getFontSize() > 0 && getFont() != null;
	}
	// Returns the font to use for rendering this components text
	public BitmapFont getFont() {
		HudFont font = style.getFont("font");
		if (font != null) {
			return font.getFont();
		} else {
			return null;
		}
	}
	// Returns the size to use for rendering this components text
	public float getFontSize() {
		return style.getFloat("fontSize");
	}
	public float getScaledFontSize() {
		return getFontSize()*style.getScale();
	}
	// Returns the color to use for rendering this components text
	public ColorRGBA getFontColor() {
		return style.getColor("fontColor");
	}
	// Returns the horizontal alignment of this components text
	public BitmapFont.Align getTextAlignment() {
		return style.getAlignment("align");
	}
	// Returns the vertical alignment of this components text
	public BitmapFont.VAlign getTextVerticalAlignment() {
		return style.getVerticalAlignment("vAlign");
	}
	// Returns the line wrap mode to use for rendering this components text
	public LineWrapMode getLineWrapMode() {
		return style.getLineWrapMode("lineWrap");
	}
	public boolean isTextObscured() {
		return false;
	}
	// Returns the text to render for this component
	public String getTextValue() {
		return "";
	}
	// To be overidden in sub classes for components that have text to set the text value
	public void setTextValue(String value) {
	}
	// Returns true if this component has a border to render
	public boolean hasBorder() {
		return getBorderSize() > 0;
	}
	// Returns the size of the border for this component
	public int getBorderSize() {
		return style.getInt("borderSize");
	}
	// Returns the border color for this component
	public ColorRGBA getBorderColor() {
		return style.getColor("borderColor");
	}

	public boolean hasHighlight() {
		return highlight;
	}	
	public void setHighlight(boolean highlight) {
		this.highlight = highlight;
	}
	public void setRenderedTextHeight(int renderedHeight) {
	}

	// Returns true if the x and y parameters are within the boundaries of this control
	public boolean contains(int inputX, int inputY) {
		int nx = inputX;
		int ny = inputY;
		if (parent != null) {
			nx = inputX - parent.getAbsoluteX();
			ny = inputY - parent.getAbsoluteY();
			//System.out.println(this + ": px=" +  parent.getAbsoluteX() + " py=" + parent.getAbsoluteY());
		}
		if (clip != null) {
			nx = inputX - clip.x;
			ny = inputY - (clip.y-clip.h);
		}
		int w = (clip != null) ? clip.w : this.getScaledWidth();
		int h = (clip != null) ? clip.h : this.getScaledHeight();
		if ((w | h) < 0) {
			// At least one of the dimensions is negative...
			return false;
		}
		// Note: if either dimension is zero, tests below must return false...
		int x = this.getScaledX();
		int y = this.getScaledY();
		if (nx < x || ny < y) {
			return false;
		}
		w += x;
		h += y;
		//    overflow || intersect
		return ((w < x || w > nx) && (h < y || h > ny));
	}
	public void setHorizontalAlignment(int align, int dx) {
		this.xOrient = align;
		this.insets[LEFT] = dx;
		this.insets[RIGHT] = dx;
	}
	public void setVerticalAlignment(int align, int dy) {
		this.yOrient = align;
		this.insets[BOTTOM] = dy;
		this.insets[TOP] = dy;
	}
	public void setCentered() {
		this.xOrient = CENTERED;
		this.yOrient = CENTERED;
	}
	public void setAbsolute() {
		this.xOrient = ABSOLUTE;
		this.yOrient = ABSOLUTE;
	}
	public void setRelative() {
		this.xOrient = RELATIVE;
		this.yOrient = RELATIVE;
	}
	public void setCenterLocally() {
		this.xOrient = LOCAL_CENTER;
		this.yOrient = LOCAL_CENTER;
	}
	
	// Update the Hud components position given it parents dimensions
	public void doLayout(int parentWidth, int parentHeight) {
	}
	public void zoomFadeIn(float speed) {
		this.speed = speed;
		this.fadeAlpha = 0;
		setZoomLevel(0);
		zooming = ZOOM_IN;
		fading = FADE_IN;
		setVisible(true);
	}
	public void zoomFadeOut(float speed) {
		if (visible) {
			this.speed = speed;
			this.fadeAlpha = 1f;
			this.zoomLevel = 1f;
			zooming = ZOOM_OUT;
			fading = FADE_OUT;
		}
	}
	public void zoomIn(float speed) {
		this.speed = speed;
		setZoomLevel(0);
		zooming = ZOOM_IN;
		setVisible(true);
	}
	public void zoomOut(float speed) {
		this.speed = speed;
		this.zoomLevel = 1f;
		if (visible) {
			zooming = ZOOM_OUT;
		}
	}
	public void fadeIn(float speed) {
		setVisible(true);
		this.speed = speed;
		this.fadeAlpha = 0;
		fading = FADE_IN;
	}
	public void fadeOut(float speed) {
		if (visible) {
			this.speed = speed;
			this.fadeAlpha = 1f;
			fading = FADE_OUT;
		}
	}
	// Toggles the visibilty of the component by fading in or out at a designated speed and returns whether the component is now visible or not.
	public boolean toggleFade(float speed) {
		if (visible) {
			fadeOut(speed);
			return false;
		} else {
			fadeIn(speed);
			return true;
		}
	}
	// Updates component before next render
	public void update(float tpf) {
		if (fading != FADE_NONE) {
			fadeAlpha = fadeAlpha + (float)(tpf*speed*fading);
			if (fadeAlpha < 0) {
				fading = FADE_NONE;
				setVisible(false);
				fadeAlpha = 1f;
			}
			if (fadeAlpha > 1f) {
				fadeAlpha = 1f;
				fading = FADE_NONE;
			}
			setFadeAlpha(fadeAlpha);
		}
		if (zooming != ZOOM_NONE) {
			zoomLevel = zoomLevel + (float)(tpf*speed*zooming);
			if (zoomLevel < 0) {
				zooming = ZOOM_NONE;
				setVisible(false);
				zoomLevel = 1f;
			}
			if (zoomLevel > 1f) {
				zoomLevel = 1f;
				zooming = ZOOM_NONE;
			}
			setZoomLevel(zoomLevel);
		}
	}
	public boolean receiveInput(String command, float value) {
		return false;
	}
	public boolean needsKeyInput() {
		return false;
	}
	public HudComponent doClick(int x, int y, boolean isPressed) {
		if (isVisible() && this.contains(x, y)) {
			return this;
		}
		return null;
	}
	public HudComponent doScroll(int x, int y, int axis, float value) {
		return null;
	}
	public void doEvent(int eventCode, HudComponent comp, String message, int value) {
		if (hasParent()) {
			parent.doEvent(eventCode, comp, message, value);
		}
	}
	public void requestFocus(HudComponent comp) {
		if (hasParent()) {
			parent.requestFocus(comp);
		}
	}
	public boolean hasFocus(HudComponent comp) {
		if (hasParent()) {
			return parent.hasFocus(comp);
		}
		return false;
	}
	public boolean hasMouseFocus(int x, int y) {
		if (isVisible() && this.contains(x, y)) {
			return true;
		}
		return false;
	}
	public void dragStart() {
	}
	// Calculates texture coordinates assuming a grid pattern where txCount is the number of textures in each row and column
	public float[] generateTxCoords(int column, int row, int txCount) {
        float txUnit = 1f / txCount;
		float x0 = column*txUnit;
		float x1 = (column+1)*txUnit;
		float y0 = ((row+1)*-txUnit) + 1;
		float y1 = (row*-txUnit) + 1;
		
		//return new float[] { x0, y0, x1, y0, x1, y1, x0, y1 };
		return new float[] { x0, y1, x1, y1, x1, y0, x0, y0 };
	}
	// Compares two components for ordering, the order is based on their z index
	public int compareTo(HudComponent other) {
		if (z < other.getZ()) {
			return -1;
		} else if (z > other.getZ()) {
			return 1;
		}
		return 0;
	}
	@Override
	public boolean equals(Object other) {
		if (other instanceof HudComponent) {
			if (name == null || name.length() == 0) {
				return false;
			} else {
				return name.equals(((HudComponent)other).getName());
			}
		}
		return false;
	}
	@Override
	public String toString() {
		return name;
	}
	
	/* CONVENIENCE METHODS */
	// Sets the location of the underlying hud component in local coordinate space
	public void setX(int x) {
		this.x = x;
	}
	public void setY(int y) {
		this.y = y;
	}
	public void setZ(int z) {
		this.z = z;
	}
	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}
	// Sets the dimensions of the hud component
	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
	}
	// Sets the dimensions of the hud component
	public void setWidth(int width) {
		this.width = width;
	}
	// Sets the dimensions of the hud component
	public void setHeight(int height) {
		this.height = height;
	}
	public boolean isZooming() {
		return zooming != ZOOM_NONE;
	}
	public void setZoomLevel(float zoomLevel) {
		this.zoomLevel = zoomLevel;
	}
	public float getZoomLevel() {
		return zoomLevel;
	}
	public boolean isScaled() {
		return style.getScale() != 1f;
	}
	public float getDeltaScale() {
		return 1f - (zoomLevel*style.getScale());
	}
	public void setTxCoords(float[] txCoords) {
		this.txCoords = txCoords;
	}
	public float[] getTxCoords() {
		return txCoords;
	}
	public Vector3f getRotationAngles() {
		return angles;
	}
	public void setRotationAngles(float xAngle, float yAngle, float zAngle) {
		angles.set(xAngle, yAngle, zAngle);
	}
	
	public boolean isInheritWidth() {
		return inheritWidth;
	}
	public boolean isInheritHeight() {
		return inheritHeight;
	}
	public void setInheritWidth(boolean value) {
		this.inheritWidth = value;
	}
	public void setInheritHeight(boolean value) {
		this.inheritHeight = value;
	}
	public void setInheritScale(boolean value) {
		this.inheritScale = value;
	}
	public float getScale() {
		if (inheritScale && hasParent()) {
			return parent.getScale();
		} else {
			return style.getScale();
		}
	}
	// Returns the width of this component
	public int getWidth() {
		if (inheritWidth) {
			if (style.getWidth() > 0) {
				return style.getWidth();
			} else if (hasParent()) {
				return (int)Math.ceil(parent.getWidth()*style.getFloat("widthFactor"));
			} else {
				return width;
			}
		} else {
			return width;
		}
	}
	// Returns the height of this component
	public int getHeight() {
		if (inheritHeight) {
			if (style.getHeight() > 0) {
				return style.getHeight();
			} else if (hasParent()) {
				return (int)Math.ceil(parent.getHeight()*style.getFloat("heightFactor"));
			} else {
				return height;
			}
		} else {
			return height;
		}
	}
	// Returns the scaled width of this component
	public int getScaledWidth() {
		if (inheritWidth) {
			if (style.getWidth() > 0) {
				return (int)Math.ceil(style.getWidth()*getZoomLevel()*getScale());
			} else if (hasParent()) {
				return (int)Math.ceil(parent.getScaledWidth()*style.getFloat("widthFactor"));
			} else {
				return (int)Math.ceil(width*getZoomLevel()*getScale());
			}
		} else {
			return (int)Math.ceil(width*getZoomLevel()*getScale());
		}
	}
	// Returns the scaled height of this component
	public int getScaledHeight() {
		if (inheritHeight) {
			if (style.getHeight() > 0) {
				return (int)Math.ceil(style.getHeight()*getZoomLevel()*getScale());
			} else if (hasParent()) {
				return (int)Math.ceil(parent.getScaledHeight()*style.getFloat("heightFactor"));
			} else {
				return (int)Math.ceil(height*getZoomLevel()*getScale());
			}
		} else {
			return (int)Math.ceil(height*getZoomLevel()*getScale());
		}
	}
	// Returns the scaled depth of this component
	public int getScaledDepth() {
		return getZ();
	}
	public int getCenterX()	{
		return getWidth()/2;
	}
	public int getCenterY()	{
		return getHeight()/2;
	}
	public int getMaxX() {
		return getX()+getWidth();
	}
	public int getMaxY() {
		return getY()+getHeight();
	}
	// Returns the x coordinate base on the components horizontal orientation
	public int getX() {
		// Centers the component within it's parents boundaries
		if (xOrient == CENTERED && hasParent()) {
			return Math.round((parent.getWidth() - getWidth())/2f);
		}
		// Places the component relative to the right edge of it's parent
		if (xOrient == MAX_JUSTIFY && hasParent()) {
			return parent.getWidth() - getWidth() - insets[RIGHT];
		}
		// Places the component relative to the left edge of it's parent
		if (xOrient == MIN_JUSTIFY) {
			return insets[LEFT];
		}
		if (xOrient == LOCAL_CENTER) {
			 return x - getCenterX();
		}
		return x;
	}
	// Returns the y coordinate base on the components vertical orientation
	public int getY() {
		if (yOrient == CENTERED && hasParent()) {
			return (int)Math.floor((parent.getHeight() - getHeight())/2f);
		}
		if (yOrient == MAX_JUSTIFY && hasParent()) {
			return parent.getHeight() - getHeight() - insets[TOP];
		}
		if (yOrient == MIN_JUSTIFY) {
			return insets[BOTTOM];
		}
		if (yOrient == LOCAL_CENTER) {
			 return y - getCenterY();
		}
		return y;
	}
	// Returns the z coordinate of the component used for layering
	public int getZ() {
		return z;
	}
	// Returns the x coordinate with scaling applied
	public int getScaledX() {
		// Centers the component within it's parents boundaries
		if (xOrient == CENTERED && hasParent()) {
			return (int)Math.floor((parent.getScaledWidth() - getScaledWidth())/2f);
		}
		// Places the component relative to the right edge of it's parent
		if (xOrient == MAX_JUSTIFY && hasParent()) {
			return parent.getScaledWidth() - getScaledWidth() - (int)Math.floor(insets[RIGHT]*getZoomLevel()*getScale());
		}
		// Places the component relative to the left edge of it's parent
		if (xOrient == MIN_JUSTIFY) {
			return (int)Math.floor(insets[LEFT]*getZoomLevel()*getScale());
		}
		return (int)Math.floor(x*getZoomLevel()*getScale());
	}
	// Returns the y coordinate with scaling applied
	public int getScaledY() {
		if (yOrient == CENTERED && hasParent()) {
			return (int)Math.floor((parent.getScaledHeight() - getScaledHeight())/2f);
		}
		if (yOrient == MAX_JUSTIFY && hasParent()) {
			return parent.getScaledHeight() - getScaledHeight() - (int)Math.floor(insets[TOP]*getZoomLevel()*getScale());
		}
		if (yOrient == MIN_JUSTIFY) {
			return (int)Math.floor(insets[BOTTOM]*getZoomLevel()*getScale());
		}
		return (int)Math.floor(y*getZoomLevel()*getScale());
	}
	// Returns the z coordinate with scaling applied
	public int getScaledZ() {
		return getZ();
	}
	// Returns the final x coordinate in screen space by factoring both scale and it's parents location
	public int getAbsoluteX() {
		if (xOrient == ABSOLUTE) {
			return getX();
		} else {
			return (parent != null) ? getScaledX() + parent.getAbsoluteX() : getScaledX();
		}
	}
	// Returns the final y coordinate in screen space by factoring both scale and it's parents location
	public int getAbsoluteY() {
		if (yOrient == ABSOLUTE) {
			return getY();
		} else {
			return (parent != null) ? getScaledY() + parent.getAbsoluteY() : getScaledY(); 
		}
	}
	public void notify(HudComponent component) {
	}
	public void playSound(AudioSource sound) {
		if (sound != null) {
			AudioRenderer renderer = AudioContext.getAudioRenderer();
			if (renderer != null) {
				renderer.playSourceInstance(sound);
			} else {
				System.out.println("No audio renderer");
			}
		}
	}
}