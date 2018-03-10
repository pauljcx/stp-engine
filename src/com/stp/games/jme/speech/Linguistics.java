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
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import com.stp.util.XMLFileUtility;

/** @author Paul Collins
 *  @version v0.1 ~ 12/05/2011
 *  HISTORY: Version 0.1 created the Linguistics class ~ 12/05/2011
 */
public class Linguistics implements SpeechConstants {
	private static Linguistics instance;	
	private final ArrayList<Word> WORDS = new ArrayList<Word>();
	private final ArrayList<WordPattern> PATTERNS = new ArrayList<WordPattern>();

	private Linguistics() {
		// Initialize some default word patterns
		// what is your name
		WordPattern pattern = new WordPattern(INTERROGATIVE + AUXILIARY + POSSESSIVE + THING, Sentence.QUESTION);
		pattern.setExpectedIndex(INTERROGATIVE, 1);
		pattern.setVerbIndex(AUXILIARY, 1);
		pattern.setTargetIndex(POSSESSIVE, 1);
		pattern.setSubjectIndex(THING, 1);
		registerPattern(pattern);
		
		// who is he
		// who are you
		// where am I
		pattern = new WordPattern(INTERROGATIVE + AUXILIARY + NOMINATIVE, Sentence.QUESTION);
		pattern.setExpectedIndex(INTERROGATIVE, 1);
		pattern.setVerbIndex(AUXILIARY, 1);
		pattern.setTargetIndex(NOMINATIVE, 1);
		pattern.setSubjectIndex(INTERROGATIVE, 1);
		registerPattern(pattern);
		
		// what locations are nearby
		pattern = new WordPattern(INTERROGATIVE + PLACE + AUXILIARY + RELATIONAL, Sentence.QUESTION);
		pattern.setExpectedIndex(INTERROGATIVE, 1);
		pattern.setVerbIndex(AUXILIARY, 1);
		pattern.setTargetIndex(RELATIONAL, 1);
		pattern.setSubjectIndex(PLACE, 1);
		registerPattern(pattern);
		
		// what is 1 + 1
		pattern = new WordPattern(INTERROGATIVE + NUMBER + OPERAND, Sentence.QUESTION);
		pattern.setExpectedIndex(INTERROGATIVE, 1);
		pattern.setVerbIndex(OPERAND, 1);
		pattern.setTargetIndex(NUMBER, 1);
		pattern.setSubjectIndex(NUMBER, 2);
		registerPattern(pattern);

		// walk across the river
		// go to the village
		pattern = new WordPattern(ACTION + RELATIONAL + PERSON, Sentence.DIRECTIVE);
		pattern.setVerbIndex(ACTION, 1);
		pattern.setTargetIndex(RELATIONAL, 1);
		pattern.setSubjectIndex(PERSON, 1);
		registerPattern(pattern);
		
		pattern = new WordPattern(ACTION + RELATIONAL + PLACE, Sentence.DIRECTIVE);
		pattern.setVerbIndex(ACTION, 1);
		pattern.setTargetIndex(RELATIONAL, 1);
		pattern.setSubjectIndex(PLACE, 1);
		registerPattern(pattern);

		// get some food
		pattern = new WordPattern(ACTION + RELATIONAL + THING, Sentence.DIRECTIVE);
		pattern.setVerbIndex(ACTION, 1);
		pattern.setTargetIndex(RELATIONAL, 1);
		pattern.setSubjectIndex(THING, 1);
		registerPattern(pattern);
		
		// wake up
		// jump down
		// run away
		pattern = new WordPattern(ACTION + RELATIONAL, Sentence.DIRECTIVE);
		pattern.setVerbIndex(ACTION, 1);
		pattern.setTargetIndex(RELATIONAL, 1);
		registerPattern(pattern);
		
		// play ball
		// throw sword
		// mix potion
		// shoot arrow
		pattern = new WordPattern(ACTION + THING, Sentence.DIRECTIVE);
		pattern.setVerbIndex(ACTION, 1);
		pattern.setSubjectIndex(THING, 1);
		registerPattern(pattern);
		
		// stab goblin
		pattern = new WordPattern(ACTION + PERSON, Sentence.DIRECTIVE);
		pattern.setVerbIndex(ACTION, 1);
		pattern.setSubjectIndex(PERSON, 1);
		registerPattern(pattern);
		
		// list locations
		pattern = new WordPattern(ACTION + PLACE, Sentence.DIRECTIVE);
		pattern.setVerbIndex(ACTION, 1);
		pattern.setSubjectIndex(PLACE, 1);
		registerPattern(pattern);
		
		// wander
		pattern = new WordPattern(ACTION, Sentence.DIRECTIVE);
		pattern.setVerbIndex(ACTION, 1);
		registerPattern(pattern);
	}
	public static Linguistics getInstance() {
		if (instance == null) {
			instance = new Linguistics();
		}
		return instance;
	}
	public boolean registerWord(Word word) {
		int found = WORDS.indexOf(word);
		if (found < 0) {
			WORDS.add(word);
			return true;
		}
		return false;
	}
	public Collection<Word> getWords() {
		return WORDS;
	}
	public Word[] getWordArray() {
		return WORDS.toArray(new Word[WORDS.size()]);
	}
	public void registerWordArray(Word[] words) {
		for (Word w : words) {
			registerWord(w);
		}
	}
	public int getWordCount() {
		return WORDS.size();
	}
	public boolean registerPattern(WordPattern pattern) {
		int found = PATTERNS.indexOf(pattern);
		if (found < 0) {
			PATTERNS.add(pattern);
			return true;
		}
		return false;
	}
	public Collection<WordPattern> getPatterns() {
		return PATTERNS;
	}
	public WordPattern[] getPatternArray() {
		return PATTERNS.toArray(new WordPattern[PATTERNS.size()]);
	}
	public void registerPatternArray(WordPattern[] patterns) {
		for (WordPattern p : patterns) {
			registerPattern(p);
		}
	}
	public int getPatternCount() {
		return PATTERNS.size();
	}
	public Word lookup(String input) {
		if (input.length() > 0) {
			if (Character.isDigit(input.charAt(0))) {
				return new Word(input, "symbol", "number", "integer");
			}
		} for (int w = 0; w < WORDS.size(); w++) {
			if (WORDS.get(w).isLike(input)) {
				return WORDS.get(w);
			}
		}
		return new Word(input, "unknown", "none", "none");
	}
	public void loadFromFile(File file) {
		/*try {
			registerWordArray((Word[])XMLFileUtility.readXMLObjects(new FileInputStream(file), "words", Word.class));
		} catch (Exception ex) {
			System.out.println("Unable to load words from file:" + file);
		}*/
	}
}