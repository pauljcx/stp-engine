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
import com.jme3.texture.Texture2D;
import com.jme3.math.ColorRGBA;
// Java Dependencies
import java.util.ArrayList;

/** @author Paul Collins
 *  @version v1.0 ~ 03/22/2015
 *  HISTORY: Version 1.0 Created HudScrollBar ~ 03/22/2015
 */
public class HudScrollBar extends HudContainer {
	public static final HudStyle DEFAULT_SCROLL_BAR_STYLE = new HudStyle("HudScrollBar");
	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;
	
	protected int alignment = HORIZONTAL;
	protected int size;
	protected int contentSize;
	protected int position;
	protected int maxPosition;
	protected float changeFactor;
	protected float scaleFactor;
	protected HudComponent mark;
	protected HudStyle markStyle;

	public HudScrollBar(String name, int size, int alignment) {
		this (name, DEFAULT_SCROLL_BAR_STYLE, size, alignment);
	}
	public HudScrollBar(String name, HudStyle style, int size, int alignment) {
		super (name, style, size, size);
		this.alignment = alignment;
		this.size = size;
		this.changeFactor = 0.05f; // 5% per change
		
		this.markStyle = new HudStyle("ScrollMark", style.getColor("scrollBarColor"));
		this.mark = new HudComponent(name + "-mark", markStyle, size, size);
		mark.setInheritScale(true);
		if (alignment == HORIZONTAL) {
			setInheritWidth(true);
			mark.setInheritWidth(true);
			setVerticalAlignment(MAX_JUSTIFY, -size);
		} else {
			setInheritHeight(true);
			mark.setInheritHeight(true);
			setHorizontalAlignment(MAX_JUSTIFY, -size);
		}
		add(mark);
	}
	@Override
	public boolean hasBackground() {
		return style.getColor("scrollBaseColor") != null;
	}
	@Override
	public ColorRGBA getBackground() {
		return style.getColor("scrollBaseColor");
	}
	@Override
	public Texture2D getTexture() {
		return null;
	}
	public int getPosition() {
		return position;
	}
	public void setContentSize(int contentSize) {
		this.contentSize = contentSize;
	}
	public void setChangeFactor(float changeFactor) {
		this.changeFactor = changeFactor;
	}
	public void adjustPosition(int amount) {
		position += amount;
		if (position < 0) {
			position = 0;
		}
		if (position > maxPosition) {
			position = maxPosition;
		}
		if (alignment == HORIZONTAL) {
			mark.setX(Math.round(position*scaleFactor));
		} else{
			mark.setY(Math.round(position*scaleFactor));
		}
	}
	public void increment() {
		position = position + (int)(maxPosition*changeFactor);
		if (position > maxPosition) {
			position = maxPosition;
		}
		if (alignment == HORIZONTAL) {
			mark.setX(Math.round(position*scaleFactor));
		} else{
			mark.setY(Math.round(position*scaleFactor));
		}
	}
	public void decrement() {
		position = position - (int)(maxPosition*changeFactor);
		if (position < 0) {
			position = 0;
		}
		if (alignment == HORIZONTAL) {
			mark.setX(Math.round(position*scaleFactor));
		} else{
			mark.setY(Math.round(position*scaleFactor));
		}
	}
	@Override
	public void doLayout(int parentWidth, int parentHeight) {
		float scaledSize = contentSize*getScale();
		if (alignment == HORIZONTAL) {
			int w = getScaledWidth();
			this.maxPosition = Math.round((scaledSize - w)/getScale());
			this.scaleFactor = (float)w/scaledSize;
			markStyle.put("widthFactor", scaleFactor);
			//System.out.println("ScrollBarWidth: " + w + " ScaleFactor: " + scaleFactor + " ContentSize: " + contentSize + "MaxPosition: " + maxPosition);
			if (maxPosition > 0) {
				setVisible(true);
			} else {
				setVisible(false);
			}
		} else {
			int h = getScaledHeight();
			this.maxPosition = Math.round((scaledSize - h)/getScale());
			this.scaleFactor = (float)h/scaledSize;
			markStyle.put("heightFactor", scaleFactor);
			if (Math.round(scaleFactor*h) < h) {
				setVisible(true);
			} else {
				setVisible(false);
			}
		}
		//System.out.println("Visible: " + isVisible() + " Scaled X: " + getScaledX() + " Scaled Y: " + getScaledY());
	}
	@Override
	public HudComponent doClick(int x, int y, boolean isPressed) {
		return null;
	}
}