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

/** @author Paul Collins
 *  @version v1.0 ~ 04/01/2017
 *  HISTORY: Version 1.0 created the WordPattern class ~ 04/01/2017
 */
public class WordPattern implements SpeechConstants {
	private int mask;
	private int category;
	private int expectedClass = -1;
	private int expectedIndex = -1;
	private int verbClass = -1;
	private int verbIndex = -1;
	private int targetClass = -1;
	private int targetIndex = -1;
	private int subjectClass = -1;
	private int subjectIndex = -1;
	
	public WordPattern(int mask, int category) {
		setMask(mask);
		setCategory(category);
	}
	public void setMask(int mask) {
		this.mask = mask;
	}
	public int getMask() {
		return mask;
	}
	public void setCategory(int category) {
		this.category = category;
	}
	public int getCategory() {
		return category;
	}
	public void setExpectedIndex(int cls, int idx) {
		this.expectedClass = cls;
		this.expectedIndex = idx;
	}
	public void setVerbIndex(int cls, int idx) {
		this.verbClass = cls;
		this.verbIndex = idx;
	}
	public void setTargetIndex(int cls, int idx) {
		this.targetClass = cls;
		this.targetIndex = idx;
	}
	public void setSubjectIndex(int cls, int idx) {
		this.subjectClass = cls;
		this.subjectIndex = idx;
	}
	public boolean hasExpected() {
		return expectedClass >= 0;
	}
	public boolean hasVerb() {
		return verbClass >= 0;
	}
	public boolean hasTarget() {
		return targetClass >= 0;
	}
	public boolean hasSubject() {
		return subjectClass >= 0;
	}
	public int getExpectedClass() {
		return expectedClass;
	}
	public int getExpectedIndex() {
		return expectedIndex;
	}
	public int getVerbClass() {
		return verbClass;
	}
	public int getVerbIndex() {
		return verbIndex;
	}
	public int getTargetClass() {
		return targetClass;
	}
	public int getTargetIndex() {
		return targetIndex;
	}
	public int getSubjectClass() {
		return subjectClass;
	}
	public int getSubjectIndex() {
		return subjectIndex;
	}
	public boolean equals(Object obj) {
		 if (obj instanceof WordPattern) {
			 return ((WordPattern)obj).getMask() == mask;
		 }
		 return false;
	}
}