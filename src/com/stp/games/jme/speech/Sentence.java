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
import java.util.LinkedList;

/** @author Paul Collins
 *  @version v0.01 ~ 04/02/201
 *  HISTORY: Version 0.01 created the Sentence class ~ 04/02/2012
 */
public class Sentence implements SpeechConstants {
	// Sentence Categories
	public static final int QUESTION = 0;
	public static final int GREETING = 1;
	public static final int STATEMENT = 2;
	public static final int DIRECTIVE = 3;
	public static final int INFORMATIVE = 4;
	public static final int CLOSING = 5;
	
	private final LinkedList<Word> words = new LinkedList<Word>();
	
	private String sentence;
	private int classValues;	
	private int category;
	private int wordCount;
	
	private Word expected;
	private Word verb;
	private Word target;
	private Word subject;

	public Sentence(String sentence) {
		String types = "";
		String classes = "";
		String descriptions = "";
		
		this.sentence = sentence;
		String input = sentence.toLowerCase();

		int type = OTHER;
		int lastType = OTHER;
		String partial = "";
		boolean end = false;
		
		// Parse the input into seperate words
		for (int c = 0; c <= input.length(); c++) {
			char ch = 0;
			if (c < input.length()) {
				ch = input.charAt(c);
				if (Character.isLetterOrDigit(ch)) {
					if (Character.isDigit(ch)) {
						type = DIGIT;
					} else {
						type = LETTER;
					}
				} else {
					type = OTHER;
				}
			} else {
				type = -1;
			}
			if (type != lastType && partial.length() > 0) {
				Word word = Linguistics.getInstance().lookup(partial);
				words.add(word);
				types += word.getProperty("type") + " ";
				classes += word.getProperty("classification") + " ";
				descriptions += word.getProperty("description") + " ";
				classValues |= word.getClassIndex();
				partial = "";
				if (lastType == LETTER) {
					wordCount++;
				}
			}
			lastType = type;
			if (!Character.isWhitespace(ch)) {
				partial += ch;
			}
		}
		
		expected = Word.UNKNOWN;
		verb = Word.UNKNOWN;
		target = Word.UNKNOWN;
		subject = Word.UNKNOWN;
		category = STATEMENT;
		
		evaluate();
		
		//System.out.println(wordCount + ": " + sentence);
		//System.out.println(types);
		//System.out.println(classValues + ": " + classes);
		//System.out.println(descriptions);
		
		
		/*System.out.println(words.size() + ": Expected: " + expected.getDescription() +
							" Target: " + target.getDescription() +
							" Subject: " + subject.getText());*/
	}
	private void evaluate() {
		for (WordPattern pattern :  Linguistics.getInstance().getPatterns()) {
			if ((pattern.getMask() & classValues) == pattern.getMask()) {
				if (pattern.hasExpected()) {
					this.expected = getWord(pattern.getExpectedClass(), pattern.getExpectedIndex());
				}
				if (pattern.hasVerb()) {
					this.verb = getWord(pattern.getVerbClass(), pattern.getVerbIndex());
				}
				if (pattern.hasTarget()) {
					this.target = getWord(pattern.getTargetClass(), pattern.getTargetIndex());
				}
				if (pattern.hasSubject()) {
					this.subject = getWord(pattern.getSubjectClass(), pattern.getSubjectIndex());
				}
				this.category = pattern.getCategory();
				return;
			}
		}
	}
	public Word getWord(int classValue, int count) {
		int found = 0;
		for (int w = 0; w < words.size(); w++) {
			if (words.get(w).getClassIndex() == classValue) {
				found++;
				if (found == count) {
					return words.get(w);
				}
			}
		}
		return Word.UNKNOWN;
	}
	public int getWordCount() {
		return wordCount;
	}
	public Word getExpected() {
		return expected;
	}
	public Word getVerb() {
		return verb;
	}
	public Word getTarget() {
		return target;
	}
	public Word getSubject() {
		return subject;
	}
	// Return the first action word
	public Word getAction() {
		return getWord(ACTION, 1);
	}
	public int getCategory() {
		return category;
	}
	public String toString() {
		return sentence;
	}
	public void printDetails() {
		System.out.println(words.size() + ": Category: " + category + " Expected: " + expected + " Verb: " + verb + " Target: " + target + " Subject: " + subject);
	}
	public boolean isQuestion() {
		return category == QUESTION;
	}
	public boolean isDirective() {
		return category == DIRECTIVE;
	}
	public boolean isInformative() {
		return category == INFORMATIVE;
	}
	public boolean isGreeting() {
		return category == GREETING;
	}
	public boolean hasExpected() {
		return !expected.equals(Word.UNKNOWN);
	}
	public boolean hasVerb() {
		return !verb.equals(Word.UNKNOWN);
	}
	public boolean hasTarget() {
		return !target.equals(Word.UNKNOWN);
	}
	public boolean hasSubject() {
		return !subject.equals(Word.UNKNOWN);
	}
}