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
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture2D;
// Java Dependencies
import java.util.ArrayList;

/** @author Paul Collins
 *  @version v1.0 ~ 10/27/2015
 *  HISTORY: Version 1.0 Created control HudItemList ~ 10/27/2015
 */
public class HudItemList extends HudContainer {

	protected final ArrayList<Object> items = new ArrayList<Object>();
	protected HudComponent listener;
	protected int selected = -1;
	protected int itemSize;
	protected BitmapFont font;
	
	public HudItemList(String name, ColorRGBA color, int width, int height, int itemSize) {
		super (name, width, height);
		//this.font = HudManager.DEFAULT_FONT;
		this.itemSize = itemSize;
		//setBackground(color);
		setLayoutMode(VERTICAL_LIST);
	}
	public void addItem(Object obj) {
		/*HudButton item = new HudButton(obj.toString(), "HudButton", getWidth()-1, itemSize);
		item.setTextSize(itemSize);
		item.setAlignment(BitmapFont.Align.Left);
		item.setBackground(hasBackground() ? new ColorRGBA(getBackground()) : null);
		item.setSelectColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.5f));
		item.setRelative();
		item.setListener(this);
		if (getChildCount() == 0) {
			item.setSelected(true);
			selected = 0;
		}
		add(item);
		items.add(obj);*/
	}
	public Object removeItem(int index) {
		if (index >= 0 && index < items.size()) {
			if (index == selected) {
				selected = -1;
			}
			remove(index);
			return items.remove(index);
		}
		return null;
	}
	public Object getSelectedItem() {
		return getItem(selected);
	}
	public Object getItem(int index) {
		if (index >= 0 && index < items.size()) {
			return items.get(index);
		}
		return null;
	}
	// Returns the index of the currently selected item or -1 if there are no items
	public int getSelectedIndex() {
		return selected;
	}
	public void setSelectedIndex(int selection) {
		for (int i = 0; i < getChildCount(); i++) {
			if (i == selection) {
				((HudButton)getChildAt(i)).setSelected(true);
			} else {
				((HudButton)getChildAt(i)).setSelected(false);
			}
		}
	}
	@Override
	public void removeAll() {
		super.removeAll();
		items.clear();
	}
	public void setListener(HudComponent listener) {
		this.listener = listener;
	}
	@Override
	public void notify(HudComponent comp) {
		if (selected >= 0 && selected < getChildCount()) {
			HudButton button = (HudButton)getChildAt(selected);
			if (!button.isSelected()) {
				button.setSelected(true);
				if (listener != null) {
					listener.notify(this);
				}
				return;
			} else {
				button.setSelected(false);
			}
		}
		selected = -1;
		for (int i = 0; i < getChildCount(); i++) {
			if (((HudButton)getChildAt(i)).isSelected()) {
				selected = i;
				break;
			}
		}
	}
	public void updateList() {
		/*for (int i = 0; i < getChildCount(); i++) {
			String value = items.get(i).toString();
			getChildAt(i).getText().text = value;
			getChildAt(i).setName(value);
		}*/
	}
}