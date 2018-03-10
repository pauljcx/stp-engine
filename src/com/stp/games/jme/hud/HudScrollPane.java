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
 *  @version v1.0 ~ 03/11/2015
 *  HISTORY: Version 1.0 Created control HudScrollPane ~ 03/11/2015
 */
public class HudScrollPane extends HudContainer {
	protected HudNinePartTx ninePartTx;
	protected HudComponent content;
	protected HudScrollBar hScroll;
	protected HudScrollBar vScroll;
	protected int scrollSpeed;
	protected int scrollBarSize;
	protected int edgeSize;
	protected int clipInset;
	
	public HudScrollPane(String name, int width, int height, HudComponent content) {
		this (name, HudScrollBar.DEFAULT_SCROLL_BAR_STYLE, width, height, content);
	}
	public HudScrollPane(String name, HudStyle style, int width, int height, HudComponent content) {
		super (name, style, width, height);
		this.scrollBarSize = style.getInt("scrollBarSize");
		this.edgeSize = style.getInt("scrollEdgeSize");
		if (edgeSize > 0) {
			ninePartTx = new HudNinePartTx(name + "-background", style, edgeSize);
			setRenderBackground(false);
			add(-1, ninePartTx);
			clipInset = edgeSize/2;
		}

		this.content = content;
		content.setName(name + "-content");
		content.setRelative();
		content.setLocation(0, 0);
		add(content);
		
		this.hScroll = new HudScrollBar(name + "-horizontal-scroll", style, scrollBarSize, HudScrollBar.HORIZONTAL);
		hScroll.setContentSize(content.getWidth());
		add(hScroll);
		
		this.vScroll = new HudScrollBar(name + "-vertical-scroll", style, scrollBarSize, HudScrollBar.VERTICAL);
		vScroll.setContentSize(content.getHeight());
		add(vScroll);
	}
	@Override
	public void setTextureName(String textureName) {
		super.setTextureName(textureName);
		if (ninePartTx != null) {
			ninePartTx.setTextureName(textureName);
		}
	}
	@Override
	public void setColorName(String colorName) {
		super.setColorName(colorName);
		if (ninePartTx != null) {
			ninePartTx.setColorName(colorName);
		}
	}
	public int getScrollBarSize() {
		return scrollBarSize;
	}
	public void packHorizontal() {
		setWidth(content.getWidth());
	}
	public void packVertical() {
		setHeight(content.getHeight());
	}
	@Override
	public void doEvent(int eventCode, HudComponent comp, String message, int value) {
		if (eventCode == CHANGE_EVENT) {
			if (content.getHeight() > getHeight()) {
				content.setY(getHeight()-content.getHeight());
			} else {
				content.setY(0);
			}
		}
		super.doEvent(eventCode, comp, message, value);
	}
	@Override
	public void doLayout(int parentWidth, int parentHeight) {
		int w = getWidth();
		int h = getHeight();
		content.doLayout(w, h);
		hScroll.setContentSize(content.getWidth());
		vScroll.setContentSize(content.getHeight());
		hScroll.doLayout(w, h);
		vScroll.doLayout(w, h);
		if (ninePartTx != null) {
			ninePartTx.doLayout(w, h);
		}
		content.setClip(new ClipArea(getAbsoluteX()+clipInset,
												getAbsoluteY()+getScaledHeight()-clipInset,
												getScaledWidth()-(clipInset*2),
												getScaledHeight()-(clipInset*2)));
	}
	@Override
	public HudComponent doClick(int x, int y, boolean isPressed) {
		if (isViewable() && this.contains(x, y)) {
			HudComponent result = super.doClick(x, y, isPressed);
			content.setX(-hScroll.getPosition());
			content.setY(-vScroll.getPosition());
			return result;
		}
		return null;
	}
	@Override
	public HudComponent doScroll(int x, int y, int axis, float value) {
		if (isViewable() && this.contains(x, y)) {
			if (vScroll.isVisible() && axis != SCROLL_X) {
				vScroll.adjustPosition((int)value);
				content.setY(-vScroll.getPosition());
				return this;
			}
			if (hScroll.isVisible() && axis != SCROLL_Y) {
				hScroll.adjustPosition((int)-value);
				content.setX(-hScroll.getPosition());
				return this;
			}
		}
		return null;
	}
	@Override
	public boolean receiveInput(String command, float value) {
		if (isViewable()) {
			//System.out.println("InputTest: " + command);
			if (command.equals("increment")) {
				vScroll.increment();
				content.setY(-vScroll.getPosition());
				return true;
			}
			if (command.equals("decrement")) {
				vScroll.decrement();
				content.setY(-vScroll.getPosition());
				return true;
			}
			if (command.equals("next")) {
				hScroll.increment();
				content.setX(-hScroll.getPosition());
				return true;
			}
			if (command.equals("previous")) {
				hScroll.decrement();
				content.setX(-hScroll.getPosition());
				return true;
			}
		}
		return false;
	}
}