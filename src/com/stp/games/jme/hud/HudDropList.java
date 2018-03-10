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
import com.jme3.scene.Node;
import com.jme3.ui.Picture;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture2D;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.input.InputManager;
// Java Dependencies
import java.util.ArrayList;

/** @author Paul Collins
 *  @version v1.1 ~ 10/06/2015
 *  HISTORY: Version 1.1 Updated most of the code to work with new rendering routines ~ 10/06/2015
 * 				 Version 1.0 Created control HudDropList ~ 01/12/2015
 */
public class HudDropList extends HudContainer {
	// Texture to be set externally during load routine once the device context has been realized and the assetManager is available
	public static Texture2D DEFAULT_TEXTURE;
	
	protected HudContainer dropList;
	protected HudTextField textField;
	protected HudComponent listener;
	protected int selected = -1;
	protected int baseHeight = 0;
	protected int dropY = 0;
	
	public HudDropList(String name, int width, int height) {
		this (name, null, null, width, height);
	}
	public HudDropList(String name, String[] items, ColorRGBA backColor, int width, int height) {
		super (name, width, height);
		// Store the base height
		this.baseHeight = height;
		this.dropY = baseHeight;
		// Setup drop down arrow
		HudComponent dropArrow = new HudComponent(name + "-arrow", 20, height+1);
		dropArrow.setHorizontalAlignment(HudComponent.MAX_JUSTIFY, 0);
		add(dropArrow);
		// Setup selection element
		this.textField = new HudTextField(name + "-textfield", width-20, height);
		textField.setNewLineAllowed(false);
		textField.setRestriction(HudTextField.TEXT_ONLY);
		textField.setHorizontalAlignment(HudComponent.MIN_JUSTIFY, 0);
		add(textField);
		// Setup the drop list
		dropList = new HudContainer(name + "-list", width, 1);
		//dropList.setBorder(new Border(ColorRGBA.Black, 1));
		dropList.setLayoutMode(VERTICAL_LIST);
		dropList.setRelative();
		add(dropList);
		dropList.setZ(100);
		dropList.setPopup(true);
		dropList.setVisible(false);
		// Setup background component
		//setBackground(backColor);
		//setBorder(new Border(ColorRGBA.Black, 1));
		setItems(items);
	}
	// Register to receive raw input 
	public void registerWithInput(InputManager inputManager) {
		textField.registerWithInput(inputManager);
	}
	// Sets all items in the drop list first removing any existing entries and sets the current selection to index 0
	public void setItems(String... items) {
		dropList.removeAll();
		selected = -1;
		if (items != null) {
			for (int i = 0; i < items.length; i++) {
				addItem(items[i]);
			}
			textField.setTextValue(items.length > 0 ? items[0] : "None");
			selected = items.length > 0 ? 0 : -1;
		}
	}
	public void setItems(Object[] objects) {
		for (int i = 0; i < objects.length; i++) {
			addItem(objects[i].toString());
		}
		textField.setTextValue(objects.length > 0 ? objects[0].toString() : "None");
		selected = objects.length > 0 ? 0 : -1;
	}
	// Adds a new text item to the drop list
	public void addItem(String itemText) {
		/*HudButton item = new HudButton(itemText, "HudButton", dropList.getWidth()-1, baseHeight);
		item.setTextSize(textField.getText().size);
		item.setAlignment(BitmapFont.Align.Left);
		item.setBackground(hasBackground() ? new ColorRGBA(getBackground()) : null);
		item.setSelectColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.5f));
		item.setRelative();
		if (dropList.getChildCount() == 0) {
			item.setSelected(true);
		}
		dropList.add(item);*/
	}
	public void filterItems(String filter) {
		for (HudComponent c : dropList.getChildren()) {
			if (filter == null || c.getName().contains(filter)) {
				c.setVisible(true);
			} else {
				c.setVisible(false);
			}
		}
		dropList.setLayoutMode(VERTICAL_LIST);
	}
	// Override to set the background of the drop list as well
	/*@Override
	public void setFont(BitmapFont font) {
		super.setFont(font);
		for (HudComponent c : dropList.getChildren()) {
			c.setFont(font);
		}
	}*/
	// Override to set the background of the drop list as well
	/*@Override
	public void setBackground(ColorRGBA background) {
		super.setBackground(background);
		if (dropList != null) {
			dropList.setBackground((background != null) ? new ColorRGBA(background) : null);
		}
	}*/
	// Override to set the text size of all components in the drop list as well
	/*@Override
	public void setTextSize(float size) {
		super.setTextSize(size);
		textField.setTextSize(size);
		for (HudComponent c : dropList.getChildren()) {
			c.setTextSize(size);
		}
	}*/
	// Returns the index of the currently selected item or -1 if there are no items
	public int getSelectedIndex() {
		return selected;
	}
	public void setSelectedIndex(int selection) {
		if (selection >= 0 && selection < dropList.getChildCount()) {
			textField.setTextValue(dropList.getChildAt(selection).getName());
			((HudButton)dropList.getChildAt(selection)).setSelected(true);
			if (selected >= 0) {
				((HudButton)dropList.getChildAt(selected)).setSelected(false);
			}
			selected = selection;
			filterItems(null);
		}
	}
	public void setSelectedValue(String value) {
		int selection = 0;
		for (HudComponent c : dropList.getChildren()) {
			if (c.getName().equals(value)) {
				if (selected >= 0) {
					((HudButton)dropList.getChildAt(selected)).setSelected(false);
				}
				this.selected = selection;
				textField.setTextValue(value);
				((HudButton)c).setSelected(true);
				filterItems(null);
				break;
			}
			selection++;
		}
	}
	public String getSelectedValue() {
		if (selected >= 0 && selected < dropList.getChildCount()) {
			return dropList.getChildAt(selected).getName();
		}
		return "";
	}
	// Removes all items from the drop list
	public void clear() {
		dropList.removeAll();
		selected = -1;
	}
	// Returns whether the drop list is currently visible
	public boolean isDropVisible() {
		return dropList.isVisible();
	}
	// Manually toggles the visibility of the drop list
	public boolean toggleDropDown() {
		return dropList.toggleVisible();
	}
	public void setListener(HudComponent listener) {
		this.listener = listener;
	}
	@Override
	public boolean hasFocus(HudComponent comp) {
		return super.hasFocus(this);
	}
	@Override
	public boolean receiveInput(String command, float value) {
		if (command.equals("increment")) {
			setSelectedIndex(selected-1);
			return true;
		}
		if (command.equals("decrement")) {
			setSelectedIndex(selected+1);
			return true;
		}
		if (command.equals("enter")) {
			dropList.setVisible(false);
			setSize(getWidth(), baseHeight);
			return true;
		}
		if (command.equals("menu")) {
			dropList.setVisible(false);
			setSize(getWidth(), baseHeight);
			clearFocus();
			return true;
		}
		if (command.equals("scrollup")) {
			if (isDropVisible()) {
				//dropY++;
				//dropList.setY(dropY);
			} else {
				if (super.hasFocus(this)) {
					setSelectedIndex(selected-1);
				}
			}
			return true;
		}
		if (command.equals("scrolldown")) {
			if (isDropVisible()) {
				//dropY--;
				//dropList.setY(dropY);
			} else {
				if (super.hasFocus(this)) {
					setSelectedIndex(selected+1);
				}
			}
			return true;
		}
		if (super.hasFocus(this)) {
			return true;
		}
		return false;
	}
	@Override
	public void doEvent(int eventCode, HudComponent comp, String message, int value) {
		if (comp == textField && eventCode == CHANGE_EVENT) {
			filterItems(message);
		} else {
			super.doEvent(eventCode, comp, message, value);
		}
	}
	// Update the Hud components position given it parents dimensions
	@Override
	public void doLayout(int parentWidth, int parentHeight) {
		dropList.setY(dropY);
		super.doLayout(parentWidth, parentHeight);
	}
	@Override
	public HudComponent doClick(int x, int y, boolean isPressed) {
		if (isViewable() && dropList.getChildCount() > 0 && this.contains(x, y)) {
			if (!isPressed) {
				if (dropList.isVisible()) {
					if (dropList.contains(x, y)) {
						int index = 0;
						for (HudComponent c : dropList.getChildren()) {
							if (c.isVisible() && c.contains(x, y)) {
								textField.setTextValue(c.getTextValue());
								((HudButton)c).setSelected(true);
								selected = index;
							} else {
								((HudButton)c).setSelected(false);
							}
							index++;
						}
						if (listener != null) {
							listener.notify(this);
						}
						super.doEvent(SELECT_EVENT, this, textField.getTextValue(), selected);
					}
					dropList.setVisible(false);
					setSize(getWidth(), baseHeight);
					requestFocus(this);
				} else {
					this.dropY = baseHeight;
					dropList.setY(dropY);
					dropList.setVisible(true);
					setSize(getWidth(), baseHeight + dropList.getHeight());
					requestFocus(this);
				}
			}
			return this;
		}
		return null;
	}
}