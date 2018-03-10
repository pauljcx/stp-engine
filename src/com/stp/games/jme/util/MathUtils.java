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
package com.stp.games.jme.util;
import com.jme3.math.Vector3f;

public final class MathUtils
{
	/** A value to multiply a degree value by, to convert it to radians. */
	public static final double DEG_TO_RAD = Math.PI / 180.0f;
	public static final double TWO_PI = 2*Math.PI;
	
	public static Vector3f rotateX(Vector3f vector, double angle)
	{
		if (angle != 0.0)
		{
			double value = DEG_TO_RAD * angle;
			double sinAngle = Math.sin(value);
			double cosAngle = Math.cos(value);
			vector.set(vector.x,
					(float)(vector.y*cosAngle - vector.z*sinAngle),
					(float)(vector.y*sinAngle + vector.z*cosAngle));
		}
		return vector;
	}
	public static Vector3f rotateY(Vector3f vector, double angle)
	{
		if (angle != 0.0)
		{
			double value = DEG_TO_RAD * angle;
			double sinAngle = Math.sin(value);
			double cosAngle = Math.cos(value);
			vector.set((float)(vector.x*cosAngle + vector.z*sinAngle),
					vector.y,
					(float)(-vector.x*sinAngle + vector.z*cosAngle));
		}
		return vector;
	}
	public static Vector3f rotateZ(Vector3f vector, double angle)
	{
		if (angle != 0.0)
		{
			double value = DEG_TO_RAD * angle;
			double sinAngle = Math.sin(value);
			double cosAngle = Math.cos(value);
			vector.set((float)(vector.x*cosAngle - vector.y*sinAngle),
					(float)(vector.x*sinAngle + vector.y*cosAngle),
					vector.z);
		}
		return vector;
	}
	public static Vector3f constrainAll(Vector3f vector, float min, float max)
	{
		constrainX(vector, min, max);
		constrainY(vector, min, max);
		constrainZ(vector, min, max);
		return vector;
	}
	public static Vector3f constrainX(Vector3f vector, float min, float max)
	{
		vector.setX(Math.max(min, vector.x));
		vector.setX(Math.min(max, vector.x));
		return vector;
	}
	public static Vector3f constrainY(Vector3f vector, float min, float max)
	{
		vector.setY(Math.max(min, vector.y));
		vector.setY(Math.min(max, vector.y));
		return vector;
	}
	public static Vector3f constrainZ(Vector3f vector, float min, float max)
	{
		vector.setZ(Math.max(min, vector.z));
		vector.setZ(Math.min(max, vector.z));
		return vector;
	}
	public static Vector3f getConstraint(Vector3f vector, Vector3f angles, Vector3f min, Vector3f max)
	{
		Vector3f result = new Vector3f(angles);
		if (vector.x + angles.getX() < min.getX())
		{ result.setX(min.getX() - vector.x); }
		if (vector.x + angles.getX() > max.getX())
		{ result.setX(max.getX() - vector.x); }
		if (vector.y + angles.getY() < min.getY())
		{ result.setY(min.getY() - vector.y); }
		if (vector.y + angles.getY() > max.getY())
		{ result.setY(max.getY() - vector.y); }
		if (vector.z + angles.getZ() < min.getZ())
		{ result.setZ(min.getZ() - vector.z); }
		if (vector.z + angles.getZ() > max.getZ())
		{ result.setZ(max.getZ() - vector.z); }
		return result;
	}
	public static int diceRoll(int sides) {
		return diceRoll(sides, 1);
	}
	public static int diceRoll(int sides, int count) {
		int roll = 0;
		for (int d = 0; d < count; d++) {
			roll = roll + (int)Math.floor(Math.random()*sides) + 1;
		}
		return roll;
	}
	public static boolean withinTolerence(Vector3f a, Vector3f b, float tolerence) {
		return Math.abs(a.x - b.x) < tolerence && Math.abs(a.y - b.y) < tolerence && Math.abs(a.z - b.z) < tolerence;
	}
}