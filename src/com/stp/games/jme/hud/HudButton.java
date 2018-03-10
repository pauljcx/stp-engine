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
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture2D;

/** @author Paul Collins
 *  @version v1.0 ~ 01/05/2015
 *  HISTORY: Version 1.0 Hud Button ~ 01/05/2015
 */
public class HudButton extends HudContainer {
	public static HudStyle TEXT_BUTTON_STYLE = new HudStyle("HudButtonText");
	public static HudStyle IMAGE_BUTTON_STYLE = new HudStyle("HudButtonImage");
	
	public static float CLICK_THRESHOLD = 0.35f;
	public static float TOGGLE_TIME = 1f;
	
	protected HudComponent leftEdge;
	protected HudComponent centerPart;
	protected HudComponent rightEdge;
	protected String textValue = "";
	protected Texture2D texture;
	protected boolean selected;
	protected boolean autoWidth;
	protected int clickCount;
	protected int totalClicks;
	protected float elapse;
	
	public HudButton(String textValue, String name) {
		this (textValue, name, TEXT_BUTTON_STYLE, 0, 0);
	}
	public HudButton(String textValue, String name, HudStyle style, int width, int height) {
		this(textValue, name, "texture", style, width, height);
	}
	public HudButton(String textValue, String name, String txName, HudStyle style, int width, int height) {
		super(name, style, width, height);
		this.autoWidth = (width == 0 && style.getWidth() == 0 && style.getFloat("widthFactor") == 0);
		setTextValue(textValue);
		setTextureName(txName);
		setSelected(false);
		setClickCount(1);
		
		if (getTexture() != null && getHeight() > 0) {
			createThreePartTexture((int)(getHeight()/3f));
		}
	}
	public HudButton(String name, Texture2D texture, int width, int height) {
		this(name, texture, IMAGE_BUTTON_STYLE, width, height);
	}
	public HudButton(String name, Texture2D texture, HudStyle style, int width, int height) {
		super(name, style, width, height);
		setTexture(texture);
		setSelected(false);
		setClickCount(1);
	}
	public HudButton(String name, HudStyle style, int width, int height) {
		super(name, style, width, height);
		setSelected(false);
		setClickCount(1);
	}
	public void setAutoWidth(boolean value) {
		this.autoWidth = value;
		if (autoWidth) {
			this.inheritWidth = false;
			int edgeSize = (int)(getHeight()/3f);
			setWidth((int)getFontSize() + style.getTextDisplayWidth(textValue, getFont(), getFontSize()) + edgeSize);
			updateSize();
		}
	}
	@Override
	public String getTextValue() {
		return textValue;
	}
	@Override
	public void setTextValue(String input) {
		this.textValue = input;
		if (centerPart != null) {
			centerPart.setTextValue(input);
		}
		if (autoWidth) {
			this.inheritWidth = false;
			int edgeSize = (int)(getHeight()/3f);
			setWidth((int)getFontSize() + style.getTextDisplayWidth(textValue, getFont(), getFontSize()) + edgeSize);
			updateSize();
		}
	}
	@Override
	public boolean hasBackground() {
		if (getChildCount() > 0) {
			return false;
		} else {
			return super.hasBackground();
		}
	}
	@Override
	public Texture2D getTexture() {
		if (texture != null) {
			return texture;
		} else {
			return super.getTexture();
		}
	}
	public void setTexture(Texture2D texture) {
		this.texture = texture;
	}
	@Override
	public void setColorName(String value) {
		super.setColorName(value);
		if (getChildCount() > 0) {
			leftEdge.setColorName(value);
			centerPart.setColorName(value);
			rightEdge.setColorName(value);
		}
	}
	@Override
	public void setTextureName(String value) {
		super.setTextureName(value);
		if (getChildCount() > 0) {
			leftEdge.setTextureName(value);
			centerPart.setTextureName(value);
			rightEdge.setTextureName(value);
		}
	}
	public void createThreePartTexture(int edgeSize) {
		removeAll();
		float x1 = 1f/3f;
		float x2 = 2f/3f;
		leftEdge = new HudComponent(getName() + "-left-edge", getStyle(), edgeSize, getHeight());
		leftEdge.setTxCoords(new float[] { 0, 1, x1, 1, x1, 0, 0, 0 });
		leftEdge.setTextureName(textureName);
		leftEdge.setRelative();
		leftEdge.setLocation(0, 0);
		add(-1, leftEdge);
		centerPart = new HudLabel(textValue, getName() + "-center-part", getStyle(), getWidth()-(edgeSize*2), getHeight());
		centerPart.setTxCoords(new float[] { x1, 1, x2, 1, x2, 0, x1, 0 });
		centerPart.setRenderBackground(true);
		centerPart.setTextureName(textureName);
		centerPart.setRelative();
		centerPart.setLocation(edgeSize, 0);
		add(-1, centerPart);
		rightEdge = new HudComponent(getName() + "-right-edge", getStyle(), edgeSize, getHeight());
		rightEdge.setTxCoords(new float[] { x2, 1, 1, 1, 1, 0, x2, 0 });
		rightEdge.setTextureName(textureName);
		rightEdge.setRelative();
		rightEdge.setLocation(getWidth()-edgeSize, 0);
		add(-1, rightEdge);
	}
	public void updateSize() {
		if (getChildCount() > 0) {
			int edgeSize = (int)(getHeight()/3f);
			leftEdge.setSize(edgeSize, getHeight());
			centerPart.setSize(getWidth()-(edgeSize*2), getHeight());
			centerPart.setX(edgeSize);
			rightEdge.setSize(edgeSize, getHeight());
			rightEdge.setX(getWidth()-edgeSize);
		}
	}
	@Override
	public void setActive(boolean active) {
		super.setActive(active);
		for (HudComponent child : getChildren()) {
			child.setActive(active);
		}
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	public boolean toggleSelected() {
		setSelected(!selected);
		return selected;
	}
	public void setClickCount(int clickCount) {
		this.clickCount = clickCount;
	}
	@Override
	public HudComponent doClick(int x, int y, boolean isPressed) {
		if (isViewable() && this.contains(x, y)) {
			if (isPressed) {
				setActive(true);
			} else {
				totalClicks++;
				if (totalClicks >= clickCount) {
					setSelected(true);
					doEvent(CLICK_EVENT, this, getActionCommand(), clickCount);
					playSound(style.getAudio("clickAudio"));
					totalClicks = 0;
					elapse = 0;
				}
				setActive(false);
			}
			return this;
		}
		setActive(false);
		return null;
	}
	@Override
	public void update(float tpf) {
		super.update(tpf);
		if (totalClicks > 0) {
			elapse = elapse + tpf;
			if (elapse > CLICK_THRESHOLD) {
				totalClicks = 0;
				elapse = 0;
			}
		}
	}
}