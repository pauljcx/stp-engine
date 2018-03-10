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
import com.jme3.font.LineWrapMode;
import com.jme3.input.KeyInput;
import com.jme3.input.InputManager;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.*;

/** @author Paul Collins
 *  @version v1.0 ~ 10/04/2015
 *  HISTORY: Version 1.0 HudTextField~ 10/04/2015
 */
public class HudTextField extends HudContainer implements RawInputListener {
	public static final HudStyle DEFAULT_TEXT_FIELD_STYLE = new HudStyle("HudTextField");
	
	// Character Input Restrictions
	public static final int ALL_CHARS = 0;
	public static final int DIGITS_ONLY = 1;
	public static final int ALPHA_ONLY = 2;
	public static final int TEXT_ONLY = 3;

	protected HudLabel textLabel;
	protected HudComponent leftEdge;
	protected HudComponent rightEdge;
	protected StringBuilder bufferText;
	protected HudTextProcessor processor;
	protected int restriction;
	protected boolean newLineAllowed;
	protected boolean consoleMode;
	protected int charIndex;
	protected int edgeSize;
	
	public HudTextField(String name, int width, int height) {
		this (name, DEFAULT_TEXT_FIELD_STYLE, ALL_CHARS, width, height);
	}
	public HudTextField(String name, HudStyle style, int restriction, int width, int height) {
		super(name, style, width, height);
		this.bufferText = new StringBuilder();
		this.restriction = restriction;
		this.newLineAllowed = false;
		this.consoleMode = false;
		this.charIndex = 0;
		this.edgeSize = width;
		
		this.textLabel = new HudLabel("", getName() + "-text-label", style, 1, getHeight());
		textLabel.setRelative();

		if (getTexture() != null && getHeight() > 0) {
			createThreePartTexture(Math.round(getHeight()/3f));
		} else {
			add(textLabel);
		}
	}
	public void setRestriction(int r) {
		this.restriction = r;
	}
	public void append(String newText) {
		if (newText == null || newText.isEmpty()) {
			return;
		}
		bufferText.append(newText);
		doEvent(CHANGE_EVENT, this, bufferText.toString(), bufferText.length());
	}
	public void append(char ch) {
		if (restriction == DIGITS_ONLY && !Character.isDigit(ch) && (ch != '.' || bufferText.indexOf(".") >= 0)) {
			return;
		}
		if (restriction == ALPHA_ONLY && !Character.isLetter(ch)) {
			return;
		}
		if (restriction == TEXT_ONLY && ((int)ch < 32 || (int)ch > 126)) {
			return;
		}
		if (newLineAllowed && ((int)ch == 10 || (int)ch == 13)) {
			bufferText.append("\n");
		} else {
			bufferText.append(ch);
		}
		doEvent(CHANGE_EVENT, this, bufferText.toString(), bufferText.length());
	}
	@Override
	public void setRenderedTextHeight(int renderedHeight) {
		if (newLineAllowed) {
			setHeight(renderedHeight);
			doEvent(SIZE_EVENT, this, "", renderedHeight);
		}
	}
	@Override
	public void setActive(boolean active) {
		super.setActive(active);
		for (HudComponent child : getChildren()) {
			child.setActive(active);
		}
	}
	@Override
	public boolean hasBackground() {
		if (getChildCount() > 1) {
			return false;
		} else {
			return super.hasBackground();
		}
	}
	@Override
	public boolean hasText() {
		return false;
	}
	@Override
	public String getTextValue() {
		return bufferText.toString();
	}
	@Override
	public void setTextValue(String value) {
		this.bufferText = new StringBuilder(value);
		textLabel.setTextValue(value);
	}
	public void setTextObscurity(boolean obscured) {
		textLabel.setTextObscurity(obscured);
	}
	public void setConsoleMode(boolean consoleMode) {
		this.consoleMode = consoleMode;
		if (consoleMode) {
			this.bufferText = new StringBuilder();
			append(">");
			charIndex = 1;
		}
	}
	public void setNewLineAllowed(boolean newLineAllowed) {
		this.newLineAllowed = newLineAllowed;
	}
	@Override
	public void updateLayout() {
		textLabel.setX(edgeSize);
		textLabel.setWidth(getWidth()-(edgeSize*2));
		if (rightEdge != null) {
			rightEdge.setX(getWidth()-edgeSize);
		}
	}
	@Override
	public HudComponent doClick(int x, int y, boolean isPressed) {
		if (isViewable() && this.contains(x, y)) {
			if (!isPressed) {
				requestFocus(this);
			}
			return this;
		}
		return null;
	}
	@Override
	public void doEvent(int eventCode, HudComponent comp, String message, int value) {
		super.doEvent(eventCode, comp, message, value);
		if (eventCode == FOCUS_EVENT) {
			if (value == 0) {
				setActive(false);
			} else {
				setActive(true);
			}
			return;
		}
		if (eventCode == CHANGE_EVENT) {
			textLabel.setTextValue(message);
		}
	}
	@Override
	public boolean needsKeyInput() {
		return true;
	}
	public void setTextProcessor(HudTextProcessor processor) {
		this.processor = processor;
	}
	// Register to receive raw input 
	public void registerWithInput(InputManager inputManager) {
		if (inputManager != null) {
			inputManager.addRawInputListener(this);
		}
	}
	//  Invoked on keyboard key press or release events.
	public void onKeyEvent(KeyInputEvent evt) {
		if (hasFocus(this) && evt.isPressed()) {
			if ((int)evt.getKeyChar() == 8) {
				if (bufferText.length() > charIndex) {
					bufferText.deleteCharAt(bufferText.length()-1);
					doEvent(CHANGE_EVENT, this, bufferText.toString(), bufferText.length());
				}
			} else if ((int)evt.getKeyChar() == 13) {
				if (consoleMode) {
					if (processor != null) {
						String result = processor.processCommand(bufferText.substring(charIndex));
						if (result.length() == 0) {
							append("\n>");
						} else {
							append("\n" + result + "\n>");
						}
					} else {
						append("\nerror: no processor found\n>");
					}
					charIndex = bufferText.length();
				} else {
					append(evt.getKeyChar());
				}
			} else if ((int)evt.getKeyChar() > 31 && (int)evt.getKeyChar() < 127) {
				append(evt.getKeyChar());
			}
		}
	}
	private void handleKeyPress(int keyCode) {
		if (keyCode == 8) {
			if (bufferText.length() > charIndex) {
				bufferText.deleteCharAt(bufferText.length()-1);
				doEvent(CHANGE_EVENT, this, bufferText.toString(), bufferText.length());
			}
		} else if (keyCode == 13) {
			if (consoleMode) {
				if (processor != null) {
					String result = processor.processCommand(bufferText.substring(charIndex));
					if (result.length() == 0) {
						append("\n>");
					} else {
						append("\n" + result + "\n>");
					}
				} else {
					append("\nerror: no processor found\n>");
				}
				charIndex = bufferText.length();
			} else {
				append((char)keyCode);
			}
		} else if (keyCode > 31 && keyCode < 127) {
			append((char)keyCode);
		}
	}
	// Called before a batch of input will be sent to this RawInputListener.
	public void beginInput() {}
	// Called after a batch of input was sent to this RawInputListener.
	public void endInput(){}
	// Invoked on joystick axis events.   
	public void onJoyAxisEvent(JoyAxisEvent evt) {}
	// Invoked on joystick button presses.
	public void onJoyButtonEvent(JoyButtonEvent evt) {}
	// Invoked on mouse button events.
	public void onMouseButtonEvent(MouseButtonEvent evt) {}
	// Invoked on mouse movement/motion events.
	public void onMouseMotionEvent(MouseMotionEvent evt) {}
	// Invoked on touchscreen touch events.   
	public void onTouchEvent(TouchEvent evt) {
		if (evt.getType() == TouchEvent.Type.KEY_DOWN) {
			handleKeyPress(evt.getKeyCode());
		}
	}
	public void createThreePartTexture(int edgeSize) {
		this.edgeSize = edgeSize;
		removeAll();
		float x1 = 1f/3f;
		float x2 = 2f/3f;
		leftEdge = new HudComponent(getName() + "-left-edge", getStyle(), edgeSize, getHeight());
		leftEdge.setTxCoords(new float[] { 0, 1, x1, 1, x1, 0, 0, 0 });
		leftEdge.setRelative();
		leftEdge.setLocation(0, 0);
		add(-1, leftEdge);
		textLabel.setWidth(edgeSize);
		textLabel.setTxCoords(new float[] { x1, 1, x2, 1, x2, 0, x1, 0 });
		textLabel.setRenderBackground(true);
		textLabel.setRelative();
		textLabel.setLocation(edgeSize, 0);
		add(0, textLabel);
		rightEdge = new HudComponent(getName() + "-right-edge", getStyle(), edgeSize, getHeight());
		rightEdge.setTxCoords(new float[] { x2, 1, 1, 1, 1, 0, x2, 0 });
		rightEdge.setRelative();
		rightEdge.setLocation(getWidth()-edgeSize, 0);
		add(-1, rightEdge);
	}
}