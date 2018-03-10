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
public class HudTabs extends HudContainer {
	public HudButton lastTab;

	public HudTabs(String name, HudStyle style, int width, int height) {
		super (name, style, width, height);
		setRenderBackground(false);
		setLayoutMode(HORIZONTAL_LIST);
	}
	public void addTab(String text) {
		HudButton tab = new HudButton(text, name + "-tab-" + text.toLowerCase(), getStyle(), 0, 0);
		add(tab);
		if (lastTab == null) {
			this.lastTab = tab;
			lastTab.setSelected(true);
			lastTab.setColorName("activeColor");
		}
	}
	@Override
	public void doEvent(int eventCode, HudComponent comp, String message, int value) {
		if (eventCode == CLICK_EVENT) {
			if (contains(comp)) {
				if (lastTab != null) {
					lastTab.setSelected(false);
					lastTab.setColorName("baseColor");
				}
				this.lastTab = (HudButton)comp;
				lastTab.setSelected(true);
				lastTab.setColorName("activeColor");
			}
		}
		super.doEvent(eventCode, comp, message, value);
	}
	public void setSelected(String tabText) {
		if (lastTab != null) {
			lastTab.setSelected(false);
			lastTab.setColorName("baseColor");
		}
		for (HudComponent c : components) {
			if (c.getTextValue().equals(tabText)) {
				this.lastTab = (HudButton)c;
				lastTab.setSelected(true);
				lastTab.setColorName("activeColor");
				return;
			}
		}
	}
	public String getSelectedValue() {
		if (lastTab != null) {
			return lastTab.getTextValue();
		} else {
			return "";
		}
	}
}