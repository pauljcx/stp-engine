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
package com.stp.games.jme.terrain;
// JME3 Dependencies
// Java Dependencies

public class TileGen
{
	public static final String[] MASK_KEYS = { "[]", "SE", "WS", "NE", "WN", "WSE", "WNE",
																	"wSe", "wNe", "wSE", "WSe", "wNE", "WNe", "NES",
																	"NWS", "nEs", "nWs", "NWs", "NEs", "nES", "nWS",
																	"nE", "En", "Ws", "NWe", "wEN", "wES", "nWSe", "wSEn",
																	"Se", "Ne", "wN", "nWSE", "WNEs", "NWSE", "WSEN",
																	"se",  "ws", "ne", "wn" };

	public static final KeyPair[] PAIRS = { new KeyPair("WS", "SE"),  new KeyPair("WN", "NE"),  new KeyPair("NW", "WS"),  new KeyPair("NE", "ES"),
															new KeyPair("wS", "Se"),  new KeyPair("wN", "Ne"),  new KeyPair("nW", "Ws"),  new KeyPair("nE", "Es") };
	public static class KeyPair {
		public String term1;
		public String term2;
		public KeyPair(String term1, String term2) {
			this.term1 = term1;
			this.term2 = term2;
		}
		public boolean matches(String value) {
			return term1.equals(value) || term2.equals(value);
		}
		public String getPairTerm(String value) {
			if (term1.equals(value)) {
				return term2;
			}
			return term1;
		}
	}

	public static int getRandomMatch(int key, boolean leftSide) {
		int result = 0;
		int start = (int)(Math.random()*MASK_KEYS.length);
		String mask = MASK_KEYS[key];
		if (leftSide) {
			
		} else {
			String term = "[]";
			if (mask.length() == 3) {
				String end = mask.substring(1, 3);
				for (KeyPair k : PAIRS) {
					if (k.matches(end)) {
						term = k.getPairTerm(end);
						break;
					}
				}
			}
			System.out.println("Term: " + term + " | Mask: " + mask + " Start: " + start);
			int count = 0;
			while (count < MASK_KEYS.length) {
				if (MASK_KEYS[start].length() == 3 && MASK_KEYS[start].startsWith(term)) {
					System.out.println("Final: " + count + " Start: " + start);
					return start;
				}
				start++;
				if (start >= MASK_KEYS.length) {
					start = 0;
				}
				count++;
			}
		}
		return result;
	}
}

// World Size = 64k x 32k
// Hemisphere Continents = 2 to 5
// Total Continents = 5 to 9
// Height: Min = 4k | Max = 12k
// Width: Min = 4k | Max = 64 / Count
// Equator Biomes: Desert Tropics Mountains Jungle
// Polar Biomes: Tundra Mountains Forests
// Median Biomes: Grasslands Forests Mountains