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
package com.stp.games.jme.controls;
// JME3 Dependencies
// Java Dependencies
import java.util.ArrayList;
// Internal Dependencies

/** @author Paul Collins
 *  @version v1.0 ~ 05/04/2015
 *  HISTORY: Version 1.0 created Player object ~ 05/04/2015
 */
public abstract class Player {
	protected String username;
	protected String token;
	protected int clientId;

	public Player() {
	}
	public void setUserName(String username) {
		this.username = username;
	}
	public String getUserName() {
		return username;
	}
	public void setTokenId(String token) {
		this.token = token;
	}
	public String getTokenId() {
		return token;
	}
	public int getClientId() {
		return clientId;
	}
	public void setClientId(int clientId) {
		this.clientId = clientId;
	}
	public abstract int getUnitCount();
	public abstract void setPlayerData(ArrayList<String> playerData);
	public String toString() {
		return "Player: " + username;
	}
}