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
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture2D;

/** @author Paul Collins
 *  @version v1.0 ~ 03/25/2015
 *  HISTORY: Version 1.0 Created control HudDragItem ~ 03/25/2015
 */
public class HudDragItem extends HudCursor {
	protected HudContainer source;
	protected HudItem item;
	
	public HudDragItem(String name) {
		super (name, null, 32, 24, 24);
		setVisible(false);
	}
	public void startDrag(HudComponent component) {
		if (!isDragging() && component instanceof HudItem) {
			if (component.getParent() instanceof HudContainer) {
				source = (HudContainer)component.getParent();
				source.remove(component);
			}
			item = (HudItem)component;
			//item.setScale(1.0f);
			item.setDraggable(false);
			setVisible(true);
		}
	}
	// Returns the item back to the source
	public void returnItem() {
		if (isDragging()) {
			if (source != null) {
				source.add(item);
			} else {
				item.setParent(source);
			}
			item.setDraggable(true);
			item = null;
			setVisible(false);
		}
	}
	// Transfers the dragging item to it's new destination
	public boolean transfer(HudComponent destination, int x, int y) {
		if (isDragging() && destination.dropEnabled()) {
			if (((HudContainer)destination).drop(item, x, y)) {
				if (source != null) {
					source.updateView();
				}
				item.setDraggable(true);
				item = null;
				setVisible(false);
				return true;
			}
		}
		return false;
	}
	public boolean isDragging() {
		return item != null;
	}
	@Override
	public Texture2D getTexture() {
		return (item != null) ? item.getTexture() : null;
	}
	@Override
	public ColorRGBA getBackground() {
		return (item != null) ? item.getBackground() : null;
	}
	@Override
	public int getWidth() {
		return (item != null) ? item.getWidth() : width;
	}
	@Override
	public int getHeight() {
		return (item != null) ? item.getHeight() : height;
	}
	@Override
	public int getScaledWidth() {
		return (item != null) ? item.getScaledWidth() : super.getScaledWidth();
	}
	@Override
	public int getScaledHeight() {
		return (item != null) ? item.getScaledHeight() : super.getScaledHeight();
	}
	// Disable clicking on items that are being dragged
	@Override
	public HudComponent doClick(int x, int y, boolean isPressed) {
		return null;
	}
	@Override
	public boolean contains(int x, int y) {
		return false;
	}
}