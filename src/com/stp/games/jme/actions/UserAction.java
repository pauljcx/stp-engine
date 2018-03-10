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
package com.stp.games.jme.actions;

public class UserAction {
	protected String iconPath;
	protected String command;
	protected String description;
	
	public UserAction(String command) {
		this (command, "", "");
	}
	public UserAction(String command, String iconPath) {
		this (command, iconPath, "");
	}
	public UserAction(String command, String iconPath, String description) {
		this.command = command;
		this.iconPath = iconPath;
		this.description = description;
	}
	public boolean hasIcon() {
		return iconPath.length() > 0;
	}
	public String getIconPath() {
		return iconPath;
	}
	public String getCommand() {
		return command;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean equals(Object obj) {
		if (obj instanceof UserAction) {
			return command.equals(((UserAction)obj).getCommand());
		}
		return false;
	}
}