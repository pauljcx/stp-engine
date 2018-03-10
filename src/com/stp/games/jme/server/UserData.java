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
package com.stp.util;
import java.sql.Timestamp;

/** @author Paul Collins
 *  @version v0.1 ~ 2/2/2011
 *  HISTORY: Version 0.1 UserData class to store details about users connection profile ~ 2/2/2012
 */
public class UserData {
	public static final String BLANK_IP = "0.0.0.0";
	
	private int id;
	private String ip;
	private String local;
	private int port;
	private String cpname;
	private String name;
	private String email;
	private String friends;
	private Timestamp time;
	private String type;
	private String game;
	
	// Constructor for lan users
	public UserData(String local, int port, String cpname) {
		this (-1, BLANK_IP, local, port, cpname, cpname, "", "", new Timestamp(System.currentTimeMillis()), "LAN");
	}
	// Constructor for friends
	public UserData(int id, String ip, String local, int port, String name, Timestamp time) {
		this(id, ip, local, port, "", name, "", "", time, "NET");
	}
	// Constructor for user login
	public UserData(int id, String ip, String local, int port, String name, String email, String friends) {
		this(id, ip, local, port, "", name, email, friends, new Timestamp(System.currentTimeMillis()), "USER");
	}
	// Constructor for user login
	public UserData(int id, String clientKey, String name, String email, String friends) {
		this(id, BLANK_IP, BLANK_IP, 0, clientKey, name, email, friends, new Timestamp(System.currentTimeMillis()), "USER");
	}
	// Constructor for servers
	public UserData(String name, String ip, int port) {
		this (-1, ip, BLANK_IP, port, name, name, "", "", new Timestamp(System.currentTimeMillis()), "REMOTE");
		setGame("");
	}
	// Constructor for all params
	public UserData(int id, String ip, String local, int port, String cpname, String name, String email, String friends, Timestamp time, String type)	{
		this.id = id;
		this.ip = ip;
		this.local = local;
		this.port = port;
		this.name = name;
		this.cpname = cpname;
		this.email = email;
		this.friends = friends;
		this.time = time;
		this.type = type;
	}
	public void setIP(String ip)	{
		this.ip = ip;
	}
	public void setLocalIP(String local) {
		this.local = local;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public void setCPName(String cpname)	{
		this.cpname = cpname;
	}
	public void setType(String type)	{
		this.type = type;
	}
	public void setGame(String game) {
		this.game = game;
	}
	public int getID()
	{
		return id;
	}
	public String getIP()
	{
		return ip;
	}
	public String getLocalIP()
	{
		return local;
	}
	public int getPort()
	{
		return port;
	}
	public String getName()
	{
		return name;
	}
	public String getCPName()
	{
		return cpname;
	}
	public String getEmail()
	{
		return email;
	}
	public String getFriends()
	{
		return friends;
	}
	public Timestamp getTime()
	{
		return time;
	}
	public String getType()
	{
		return type;
	}
	public String getGame()
	{
		return game;
	}
	public boolean isOnLan(UserData other)
	{
		return ip.equals(other.getIP()) && !local.equals(BLANK_IP);
	}
	public boolean hasConnection()
	{
		return !ip.equals(BLANK_IP) || !local.equals(BLANK_IP);
	}
	public boolean isSignedIn()
	{
		return id >= 0 && email.contains("@");
	}
	public boolean hasFriends()
	{
		return friends.length() > 0;
	}
	public boolean addFriend(String userid)
	{
		if (!checkFriends(userid))
		{
			this.friends = friends.length() > 0 ? friends + ";" + userid : userid;
			return true;
		}
		return false;
	}
	public boolean checkFriends(String userid)
	{
		if (friends.length() == 0)
		{
			return false;
		}
		else
		{
			String[] list = friends.split(";");
			for (int i = 0; i < list.length; i++)
			{
				if (list[i].equals(userid))
				{
					return true;
				}
			}
		}
		return false;
	}
	public String toString()
	{
		return name;
	}
	public void printValues()
	{
		System.out.println("Id: " + id);
		System.out.println("Public IP: " + ip);
		System.out.println("Local IP: " + local);
		System.out.println("Port: " + port);
		System.out.println("Computer Name: " + cpname);
		System.out.println("Name: " + name);
		System.out.println("Email: " + email);
		System.out.println("Friends: " + friends);
		System.out.println("Time: " + time);
		System.out.println("Type: " + type);
		System.out.println("Game: " + game);
	}
}