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
package com.stp.games.jme.speech;
import com.stp.util.XMLObject;
import java.util.HashMap;

/** @author Paul Collins
 *  @version v0.01 ~ 12/05/2011
 *  HISTORY: Version 0.01 created the Word class ~ 12/05/2011
 */
public class Word implements XMLObject, SpeechConstants, Comparable<Word> {
	public static final Word UNKNOWN = new Word("tbd", "unknown");
	public static final Word GREETING = new Word("hello", "noun", "interjection", "greeting");
	
	private int classIndex = 0;
	private HashMap<String, String> properties;
	
	public Word() {
		this("tbd", "unknown");
	}
	public Word(String text) {
		this (text, "unknown");
	}
	public Word(String text, String type) {
		this (text, type, "none", "none");
	}
	public Word(String text, String type, String classification, String description) {
		this.properties = new HashMap<String, String>();
		setText(text);
		setType(type);
		setClassification(classification);
		setDescription(description);
	}
	public String getText() {
		return getProperty("text");
	}
	public int getClassIndex() {
		return classIndex;
	}
	public String getProperty(String key) {	
		String result = properties.get(key);
		return (result != null) ? result : "unknown";
	}
	public boolean isNoun() {
		return getProperty("type").contains("noun");
	}
	public boolean isProNoun() {
		return getProperty("type").equals("pronoun");
	}
	public boolean isVerb() {
		return getProperty("type").contains("verb");
	}
	public boolean isRelational() {
		return getProperty("classification").equals("relational");
	}
	public boolean isInterrogative() {
		return getProperty("classification").equals("interrogative");
	}
	public void setText(String text) {
		properties.put("text", text);
	}
	public void setType(String type) {
		properties.put("type", type);
	}
	public void setClassification(String classification) {
		properties.put("classification", classification);
		for (int c = 0; c < CLASSES.length; c++) {
			if (classification.equals(CLASSES[c])) {
				classIndex = CLASS_VALUES[c];
				return;
			}
		}
	}
	public void setDescription(String description) {
		properties.put("description", description);
	}
	
	public String[] getPropertyNames() {
		return new String[] { "Text", "Type", "WordClass", "Description", "Properties" };
	}
	public void setProperty(String key, String value) {
		this.setProperty(key, value, "");
	}
	public void setProperty(String nodeName, Object value, String className) {
		if (value != null) {
			properties.put(nodeName, value.toString());
		}
	}
	public String toString() {
		return getText();
	}
	public boolean equals(Object obj) {
		return getText().equals(obj.toString());
	}
	public boolean isLike(String input) {
		if (getText().equals(input)) {
			return true;
		}
		// Check nouns for plurality
		if (isNoun()) {
			return input.equals(getText() + "s") || input.equals(getText() + "es");
		}
		return false;
	}
	public int compareTo(Word other) {
		return getText().compareTo(other.getText());
	}
}