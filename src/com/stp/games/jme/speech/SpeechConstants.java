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
 *  @version v0.01 ~ 04/04/2012
 *  HISTORY: Version 0.01 created the SpeechConstancts interface ~ 04/04/2012
 */
public interface SpeechConstants {
	public static final int OTHER = 0;
	public static final int DIGIT = 1;
	public static final int LETTER = 2;

	public static final int PERSON = 1;
	public static final int PLACE = 2;
	public static final int THING = 4;
	public static final int DESCRIPTIVE = 8;
	public static final int NOMINATIVE = 16;
	public static final int POSSESSIVE = 32;
	public static final int OBJECTIVE = 64;
	public static final int INTERJECTION = 128;
	public static final int AUXILIARY = 256;
	public static final int ACTION = 512;
	public static final int STATIVE = 1024;
	public static final int INTERROGATIVE = 2048;
	public static final int NUMBER = 4096;
	public static final int OPERAND = 8192;
	public static final int RELATIONAL = 16384;
	public static final int PUNCTUATION = 32768;
	public static final int SEPERATOR = 65536;

	// Types
	public static final String[] TYPES = { "noun",
											"pronoun",
											"adjective",
											"verb",
											"adverb",
											"conjunction",
											"symbol" };
	// Classes							
	public static final String[] CLASSES = { "person",
											"place",
											"thing",
											"descriptive",
											"nominative",
											"possessive",
											"objective",
											"interjection",
											"auxiliary",
											"action",
											"interrogative",
											"number",
											"operand",
											"relational",
											"punctuation",
											"seperator" };
	// Class Values							
	public static final int[] CLASS_VALUES = { PERSON,
												PLACE,
												THING,
												DESCRIPTIVE,
												NOMINATIVE,
												POSSESSIVE,
												OBJECTIVE,
												INTERJECTION,
												AUXILIARY,
												ACTION,
												INTERROGATIVE,
												NUMBER,
												OPERAND,
												RELATIONAL,
												PUNCTUATION,
												SEPERATOR };
}