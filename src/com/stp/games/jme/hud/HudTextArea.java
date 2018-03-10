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
import com.jme3.font.BitmapFont;
import com.jme3.font.LineWrapMode;
import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.input.RawInputListener;

/* TODOs: fix contain / do click function in the scrollpane so that it accounts for clipped regions correctly */
/** @author Paul Collins
 *  @version v1.0 ~ 10/04/2015
 *  HISTORY: Version 1.0 Created control HudTextArea~ 10/04/2015
 */
public class HudTextArea extends HudContainer {
	public static final HudStyle DEFAULT_TEXT_AREA_STYLE = new HudStyle("HudTextArea");
	
	protected HudScrollPane scrollPane;
	protected HudTextField textField;
	
	public HudTextArea(String name, int width, int height) {
		this (name, DEFAULT_TEXT_AREA_STYLE, width, height);
	}
	public HudTextArea(String name, HudStyle fieldStyle, int width, int height) {
		super (name, width, height);
		textField = new HudTextField(name + "-content", fieldStyle, HudTextField.ALL_CHARS, 0, 0);
		textField.setNewLineAllowed(true);
		scrollPane = new HudScrollPane(name + "-scrollpane", width, height, textField);
		add(scrollPane);
	}
	public void setRestriction(int r) {
		textField.setRestriction(r);
	}
	public void append(String newText) {
		textField.append(newText);
	}
	public void append(char ch) {
		textField.append(ch);
	}
	@Override
	public String getTextValue() {
		return textField.getTextValue();
	}
	@Override
	public void setTextValue(String value) {
		textField.setTextValue(value);
	}
	public void setConsoleMode(boolean consoleMode) {
		textField.setConsoleMode(consoleMode);
	}
	public void setTextProcessor(HudTextProcessor processor) {
		textField.setTextProcessor(processor);
	}
	// Register to receive raw input 
	public void registerWithInput(InputManager inputManager) {
		textField.registerWithInput(inputManager);
	}
}