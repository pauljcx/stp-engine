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
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture2D;

/** @author Paul Collins
 *  @version v1.0 ~ 03/22/2015
 *  HISTORY: Version 1.0 Created control HudWindow ~ 03/22/2015
 */
public class HudWindow extends HudContainer {
	public static final HudStyle DEFAULT_WINDOW_STYLE = new HudStyle("HudWindow");
	protected HudButton exitButton;
	protected HudLabel title;
	protected HudContainer content;
	protected HudComponent topLeft;
	protected HudComponent topCenter;
	protected HudComponent topRight;
	protected HudComponent midLeft;
	protected HudComponent midCenter;
	protected HudComponent midRight;
	protected HudComponent bottomLeft;
	protected HudComponent bottomCenter;
	protected HudComponent bottomRight;
	protected int cornerSize;

	public HudWindow(String name, String titleText, int width, int height) {
		this (name, titleText, DEFAULT_WINDOW_STYLE, width, height);
	}
	public HudWindow(String name, String titleText, HudStyle style, int width, int height) {
		super (name, style, width, height);
		cornerSize = style.getInt("windowEdgeSize");
		if (cornerSize > 0) {
			createNinePartTexture(cornerSize);
			title = new HudLabel(titleText, name + "-title", style, 1, cornerSize);
			title.setRenderBackground(false);
			title.setRelative();
			add(title);
			Texture2D exitTexture = style.getTexture("textureExit");
			if (exitTexture != null) {
				exitButton = new HudButton(getName() + "-exit-button", exitTexture, cornerSize-8, cornerSize-8);
				exitButton.setActionCommand("close-window");
				exitButton.setHorizontalAlignment(HudComponent.MAX_JUSTIFY, 4);
				exitButton.setVerticalAlignment(HudComponent.MIN_JUSTIFY, 4);
				add(exitButton);
			}
		}
		content = new HudContainer(name + "-content", style, 1, 1);
		content.setRenderBackground(false);
		content.setRenderText(false);
		content.setRelative();
		add(content);
	}
	public void setTitle(String titleText) {
		if (title != null) {
			title.setTextValue(titleText);
		}
	}
	public void disableExitButton() {
		if (exitButton != null) {
			remove(exitButton);
		}
	}
	@Override
	public boolean hasBackground() {
		if (cornerSize > 0) {
			return false;
		} else {
			return super.hasBackground();
		}
	}
	@Override
	public boolean hasBorder() {
		return false;
	}
	@Override
	public void doLayout(int parentWidth, int parentHeight) {
		title.setWidth(getWidth());
		content.setSize(getWidth()-(cornerSize*2), getHeight()-(cornerSize*3));
		content.setLocation(cornerSize, cornerSize*2);
		super.doLayout(parentWidth, parentHeight);
	}
	public HudContainer getContentPane() {
		return content;
	}
	public void createNinePartTexture(int cornerSize) {
		this.cornerSize = cornerSize;
		topLeft = createPart(0, 0, 0, 0, 0, cornerSize, cornerSize);
		topCenter = createPart(1, 0, 1, cornerSize, 0, getWidth()-(cornerSize*2), cornerSize);
		topRight = createPart(2, 0, 2, getWidth()-cornerSize, 0, cornerSize, cornerSize);
		midLeft = createPart(3, 1, 0, 0, cornerSize, cornerSize, getHeight()-(cornerSize*2));
		midCenter = createPart(4, 1, 1, cornerSize, cornerSize, getWidth()-(cornerSize*2), getHeight()-(cornerSize*2));
		midRight = createPart(5, 1, 2, getWidth()-cornerSize, cornerSize, cornerSize, getHeight()-(cornerSize*2));
		bottomLeft = createPart(6, 2, 0, 0, getHeight()-cornerSize, cornerSize, cornerSize);
		bottomCenter = createPart(7, 2, 1, cornerSize, getHeight()-cornerSize, getWidth()-(cornerSize*2), cornerSize);
		bottomRight = createPart(8, 2, 2, getWidth()-cornerSize, getHeight()-cornerSize, cornerSize, cornerSize);
	}
	private HudComponent createPart(int index, int r, int c, int x, int y, int w, int h) {
		HudComponent part = new HudComponent(getName() + "-part" + index, getStyle(), w, h);
		part.setTxCoords(generateTxCoords(c, r, 3));
		part.setRelative();
		part.setLocation(x, y);
		part.setParent(this);
		part.setZ(-1);
		components.add(part);
		return part;
	}
	public void updateSize() {
		if (cornerSize > 0) {
			title.setWidth(getWidth());
			content.setSize(getWidth()-(cornerSize*2), getHeight()-(cornerSize*3));
			content.setLocation(cornerSize, cornerSize*2);
			topCenter.setWidth(getWidth()-(cornerSize*2));
			topRight.setX(getWidth()-cornerSize);
			midLeft.setHeight(getHeight()-(cornerSize*2));
			midCenter.setWidth(getWidth()-(cornerSize*2));
			midCenter.setHeight(getHeight()-(cornerSize*2));
			midRight.setX(getWidth()-cornerSize);
			midRight.setHeight(getHeight()-(cornerSize*2));
			bottomLeft.setY(getHeight()-cornerSize);
			bottomCenter.setY(getHeight()-cornerSize);
			bottomCenter.setWidth(getWidth()-(cornerSize*2));
			bottomRight.setX(getWidth()-cornerSize);
			bottomRight.setY(getHeight()-cornerSize);
		}
	}
	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		updateSize();
	}
	@Override
	public void setHeight( int height) {
		super.setHeight(height);
		updateSize();
	}
	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		updateSize();
	}
	public void pack() {
		if (content.getChildCount() == 1) {
			HudComponent child = content.getChildAt(0);
			int preferredWidth = (!child.isInheritWidth()) ? child.getWidth() + (cornerSize*2) : 0;
			int preferredHeight = (!child.isInheritHeight()) ? child.getHeight() + (cornerSize*3): 0;
			if (preferredWidth > 0 && preferredHeight > 0) {
				setSize(preferredWidth, preferredHeight);
			} else if (preferredWidth > 0) {
				setWidth(preferredWidth);
			} else if (preferredHeight > 0) {
				setHeight(preferredHeight);
			}
		}
	}
	@Override
	public void doEvent(int eventCode, HudComponent comp, String message, int value) {
		if (eventCode == CLICK_EVENT) {
			if (comp.equals(exitButton)) {
				doEvent(CLOSE_EVENT, this, exitButton.getActionCommand(), 0);
				return;
			}
		}
		super.doEvent(eventCode, comp, message, value);
	}
	@Override
	public HudComponent doClick(int x, int y, boolean isPressed) {
		if (isViewable()) {
			// Check if the windows child compoents consume the click
			HudComponent result = super.doClick(x, y, isPressed);
			if (result == null) {
				// If no children consume the click consume it if it is within the window bounds to prevent clicking through windows
				if (this.contains(x, y)) {
					return this;
				}
			}
			return result;
		}
		return null;
	}
}