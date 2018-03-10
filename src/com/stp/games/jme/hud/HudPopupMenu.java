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
import com.jme3.font.BitmapFont;
// Java Dependencies
import java.util.ArrayList;

/** @author Paul Collins
 *  @version v1.0 ~ 01/23/2016
 *  HISTORY: Version 1.0 Created control HudPopupMenu ~ 01/23/2016
 */
public class HudPopupMenu extends HudContainer {
	protected int selected = -1;
	protected BitmapFont font;
	protected float textSize = 12f;
	protected Texture2D texture;
	
	public HudPopupMenu(String name, float textSize, String... items) {
		this (name, null, textSize, items);
	}
	public HudPopupMenu(String name, Texture2D texture, float textSize, String... items) {
		super (name, (int)textSize, (int)(Math.ceil(textSize)*items.length));
		this.texture = texture;
		//setFont(HudManager.DEFAULT_FONT);
		//setTextSize(textSize);
		setItems(items);
		setAbsolute();
		setVisible(false);
	}
	/*public void setFont(BitmapFont font) {
		this.font = font;
	}
	public void setTextSize(float textSize) {
		this.textSize = textSize;
	}*/
	public void setItems(String... items) {
		removeAll();
		setWidth((int)textSize);
		selected = -1;
		if (items != null) {
			for (int i = 0; i < items.length; i++) {
				addItem(items[i]);
			}
		}
		setLayoutMode(VERTICAL_LIST);
	}
	public void addItem(String itemText) {
		/*float lineWidth = (font.getLineWidth(itemText)*(textSize/font.getPreferredSize()))+10;
		if (lineWidth > getWidth()) {
			setWidth((int)Math.ceil(lineWidth));
		}
		HudButton item = new HudButton(itemText, font, texture, 0, (int)Math.ceil(textSize));
		item.setTextSize(textSize);
		item.setAlignment(BitmapFont.Align.Center);
		//item.setBackground(hasBackground() ? new ColorRGBA(getBackground()) : null);
		//item.setSelectColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.5f));
		item.setRelative();
		add(item);*/
	}
	public HudPopupMenu showPopupList(int x, int y, String... items) {
		setItems(items);
		return showAt(x, y);
	}
	public HudPopupMenu showAt(int x, int y) {
		setLocation(x, y);
		setVisible(true);
		return this;
	}
	public int getSelectedIndex() {
		return selected;
	}
	public String getSelectedValue() {
		if (selected >= 0) {
			return getChildAt(selected).getTextValue();
		} else {
			return "";
		}
	}
	public boolean hasSelection() {
		return selected >= 0;
	}
	@Override
	public boolean isPopup() {
		return true;
	}
	/*@Override
	public HudComponent doClick(int x, int y) {
		if (isVisible() && this.contains(x, y) && getChildCount() > 0) {
			int index = 0;
			String textValue = "";
			for (HudComponent c : getChildren()) {
				if (c.contains(x, y)) {
					((HudButton)c).setSelected(true);
					textValue = c.getTextValue();
					selected = index;
				} else {
					((HudButton)c).setSelected(false);
				}
				index++;
			}
			super.doEvent(SELECT_EVENT, this, textValue, selected);
			setVisible(false);
			clearFocus();
			return this;
		}
		return null;
	}*/
}