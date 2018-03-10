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
// Java Dependencies
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/** @author Paul Collins
 *  @version v1.1 ~ 10/06/2015
 *  HISTORY: Version 1.1 Added support for automatic layout of components ~ 10/06/2015
 *					 Version 1.0 Created control for managing Hud Containers ~ 01/05/2015
 */
public class HudContainer extends HudComponent {
	public static final HudStyle DEFAULT_CONTAINER_STYLE = new HudStyle("HudContainer", 0, 0, 1f, 1f);
	// Layout Types
	public static final int ARBITRARY = 0;
	public static final int VERTICAL_LIST = 1;
	public static final int HORIZONTAL_LIST = 2;
	public static final int GRID_LIST = 3;
	
	protected ArrayList<HudComponent> components;
	protected HudComponent lastFocus;
	protected int layout = ARBITRARY;
	protected int lastZ;
	
	public HudContainer(String name) {
		this (name, 0, 0);
	}
	public HudContainer(String name, int width, int height) {
		this (name, DEFAULT_CONTAINER_STYLE, width, height);
	}
	public HudContainer(String name, HudStyle style, int width, int height) {
		super (name, style, width, height);
		components = new ArrayList<HudComponent>();
	}
	// Returns a list of this containers children
	public ArrayList<HudComponent> getChildren() {
		return components;
	}
	public List<HudComponent> getRenderList() {
		List<HudComponent> renderList = components.subList(0, components.size());
		Collections.sort(renderList);
		return renderList;
	}
	// Returns the child component at the specified index
	public HudComponent getChildAt(int index) {
		if (index >= 0 && index < components.size()) {
			return components.get(index);
		}
		return null;
	}
	// Returns the child component with the specifed name
	public HudComponent getChild(String name) {
		for (HudComponent child : components) {
			if (child.getName().equals(name)) {
				return child;
			}
		}
		return null;
	}
	// Returns true if the child exist in this container
	public boolean contains(HudComponent component) {
		for (HudComponent child : components) {
			if (child.equals(component)) {
				return true;
			}
		}
		return false;
	}
	// Add a component to the container
	public void add(HudComponent component) {
		add(lastZ, component);
		lastZ++;
	}
	// Add a component to the container
	public void add(int zIndex, HudComponent component) {
		components.add(component);
		component.setParent(this);
		component.setZ(zIndex);
		updateLayout();
	}
	// Returns the number of child components in the container
	public int getChildCount() {
		return components.size();
	}
	// Remove the component at the specified index from the container
	public void remove(int index) {
		HudComponent component = components.remove(index);
		if (component != null) {
			component.setParent(null);
			updateLayout();
		}
	}
	// Remove the specified component from the container
	public void remove(HudComponent component) {
		components.remove(component);
		component.setParent(null);
		updateLayout();
	}
	// Remove any child components that do not have their parent value set
	public void removeOrphaned() {
		for (int c = components.size() - 1; c >= 0; c--) {
			if (!components.get(c).hasParent()) {
				components.remove(c);
			}
		}
		updateLayout();
	}
	// Remove all components from this container first setting their parent value to null
	public void removeAll() {
		for (HudComponent c : components) {
			c.setParent(null);
		}
		components.clear();
	}
	// Abstract method for updating any elements that may have change due to external influences
	public void updateView() {
	}
	public void setLayoutMode(int layout) {
		this.layout = layout;
		updateLayout();
	}
	public void updateLayout() {
		if (layout == VERTICAL_LIST) {
			int y = 0;
			for (HudComponent c : components) {
				if (c.isVisible()) {
					c.setY(y);
					y+=c.getHeight();
				}
			}
			setHeight(y);
		} else if (layout == HORIZONTAL_LIST) {
			int x = 0;
			for (HudComponent c : components) {
				c.setX(x);
				if (c.isInheritWidth()) {
					c.setWidth(getWidth()/getChildCount());
				}
				x+=c.getWidth();
			}
			setWidth(x);
		// Layout components in a grid like pattern putting them into horizontal rows that fill up the width
		} else if (layout == GRID_LIST) {
			int x = 0;
			int y = 0;
			int h = 0;
			for (HudComponent c : components) {
				if (x+c.getWidth() > getWidth()) {
					y = y + Math.max(h, c.getHeight());
					c.setX(0);
					c.setY(y);
					x = c.getWidth();
					h = 0;
				} else {
					c.setX(x);
					c.setY(y);
					x+=c.getWidth();
					h = Math.max(h, c.getHeight());
				}
			}
		}
	}
	@Override
	public void setFadeAlpha(float alpha) {
		super.setFadeAlpha(alpha);
		for (HudComponent c : components) {
			c.setFadeAlpha(alpha);
		}
	}
	// Updates component before next render, this method is called once per frame
	@Override
	public void update(float tpf) {
		super.update(tpf);
		for (HudComponent c : components) {
			c.update(tpf);
		}
	}
	// Update the Hud components position given it parents dimensions
	@Override
	public void doLayout(int parentWidth, int parentHeight) {
		super.doLayout(parentWidth, parentHeight);
		//Collections.sort(components);
		for (HudComponent c : components) {
			c.doLayout(getWidth(), getHeight());
		}
		updateLayout();
	}
	@Override
	public HudComponent doClick(int x, int y, boolean isPressed) {
		if (isViewable()) {
			List<HudComponent> clickList = getRenderList();
			for (int c = clickList.size()-1; c >= 0; c--) {
				HudComponent result = clickList.get(c).doClick(x, y, isPressed);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}
	@Override
	public HudComponent doScroll(int x, int y, int axis, float value) {
		if (isViewable()) {
			List<HudComponent> scrollList = getRenderList();
			for (int c = scrollList.size()-1; c >= 0; c--) {
				HudComponent result = scrollList.get(c).doScroll(x, y, axis, value);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}
	@Override
	public boolean hasMouseFocus(int x, int y) {
		if (isVisible()) {
			if (hasBackground() && this.contains(x, y)) {
				//System.out.println("Has Mouse Focus: " + this);
				return true;
			}
			for (HudComponent c : components) {
				if (c.hasMouseFocus(x, y)) {
					//System.out.println("Has Mouse Focus: " + c);
					return true;
				}
			}
		}
		return false;
	}
	@Override
	public void requestFocus(HudComponent comp) {
		if (hasParent()) {
			parent.requestFocus(comp);
		} else {
			if (lastFocus != null && !lastFocus.equals(comp)) {
				lastFocus.doEvent(FOCUS_EVENT, comp, "", 0);
			}
			comp.doEvent(FOCUS_EVENT, lastFocus, "", 1);
			this.lastFocus = comp;
		}
	}
	@Override
	public boolean hasFocus(HudComponent comp) {
		if (hasParent()) {
			return parent.hasFocus(comp);
		}
		if (lastFocus != null) {
			return lastFocus.equals(comp);
		}
		return false;
	}
	@Override
	public boolean receiveInput(String command, float value) {
		if (!isViewable()) {
			return false;
		}
		if (lastFocus != null && lastFocus.receiveInput(command, value)) {
			return true;
		}
		for (HudComponent c : components) {
			if (c.receiveInput(command, value)) {
				return true;
			}
		}
		return false;
	}
	public HudComponent getChildAt(int x, int y) {
		for (HudComponent c : components) {
			if (c.isVisible() && c.contains(x, y)) {
				if (c instanceof HudContainer) {
					return ((HudContainer)c).getChildAt(x, y);
				}
				return c;
			}
		}
		if (isVisible() && contains(x, y)) {
			return this;
		}
		return null;
	}
	public HudComponent getDropTarget(int x, int y) {
		for (HudComponent c : components) {
			if (c.contains(x, y)) {
				if (c instanceof HudContainer) {
					HudComponent result = ((HudContainer)c).getDropTarget(x, y);
					if (result != null) {
						return result;
					}
				}
				if (c.dropEnabled()) {
					return c;
				}
			}
		}
		if (contains(x, y) && dropEnabled()) {
			return this;
		}
		return null;
	}
	public boolean drop(HudItem dragItem, int x, int y) {
		return false;
	}
	// Returns the component that last currently has focus
	public HudComponent getFocusComponent() {
		return lastFocus;
	}
	// Release focus
	public void clearFocus() {
		this.lastFocus = null;
	}
	@Override
	public void setZoomLevel(float zoomLevel) {
		super.setZoomLevel(zoomLevel);
		for (HudComponent c : components) {
			c.setZoomLevel(zoomLevel);
		}
	}
}