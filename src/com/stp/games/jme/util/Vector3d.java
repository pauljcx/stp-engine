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
import com.jme3.export.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Logger;

public final class Vector3d implements Savable, Cloneable, Serializable {
	static final long serialVersionUID = 1;
	private static final Logger logger = Logger.getLogger(Vector3d.class.getName());

    public final static Vector3d ZERO = new Vector3d(0, 0, 0);
    public final static Vector3d NAN = new Vector3d(Double.NaN, Double.NaN, Double.NaN);
    public final static Vector3d UNIT_X = new Vector3d(1, 0, 0);
    public final static Vector3d UNIT_Y = new Vector3d(0, 1, 0);
    public final static Vector3d UNIT_Z = new Vector3d(0, 0, 1);
    public final static Vector3d UNIT_XYZ = new Vector3d(1, 1, 1);
    public final static Vector3d POSITIVE_INFINITY = new Vector3d(
            Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY);
    public final static Vector3d NEGATIVE_INFINITY = new Vector3d(
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY);

    public double x;
    public double y;
    public double z;

	public Vector3d() {
		x = y = z = 0;
	}

    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3d(Vector3d copy) {
        this.set(copy);
    }

    public Vector3d set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector3d set(Vector3d vect) {
        this.x = vect.x;
        this.y = vect.y;
        this.z = vect.z;
        return this;
    }

    public Vector3d add(Vector3d vec) {
        if (null == vec) {
            logger.warning("Provided vector is null, null returned.");
            return null;
        }
        return new Vector3d(x + vec.x, y + vec.y, z + vec.z);
    }

    public Vector3d add(Vector3d vec, Vector3d result) {
        result.x = x + vec.x;
        result.y = y + vec.y;
        result.z = z + vec.z;
        return result;
    }

    public Vector3d addLocal(Vector3d vec) {
        if (null == vec) {
            logger.warning("Provided vector is null, null returned.");
            return null;
        }
        x += vec.x;
        y += vec.y;
        z += vec.z;
        return this;
    }

    public Vector3d add(double addX, double addY, double addZ) {
        return new Vector3d(x + addX, y + addY, z + addZ);
    }

    public Vector3d addLocal(double addX, double addY, double addZ) {
        x += addX;
        y += addY;
        z += addZ;
        return this;
    }
	
	public void addAbsolute(Vector3d vector) {
		this.x = Math.abs(x) + Math.abs(vector.x);
		this.y = Math.abs(y) + Math.abs(vector.y);
		this.z = Math.abs(z) + Math.abs(vector.z);
	}

    public Vector3d scaleAdd(double scalar, Vector3d add) {
        x = x * scalar + add.x;
        y = y * scalar + add.y;
        z = z * scalar + add.z;
        return this;
    }

    public Vector3d scaleAdd(double scalar, Vector3d mult, Vector3d add) {
        this.x = mult.x * scalar + add.x;
        this.y = mult.y * scalar + add.y;
        this.z = mult.z * scalar + add.z;
        return this;
    }

    public double dot(Vector3d vec) {
        if (null == vec) {
            logger.warning("Provided vector is null, 0 returned.");
            return 0;
        }
        return x * vec.x + y * vec.y + z * vec.z;
    }

    public Vector3d cross(Vector3d v) {
        return cross(v, null);
    }

    public Vector3d cross(Vector3d v,Vector3d result) {
        return cross(v.x, v.y, v.z, result);
    }

    public Vector3d cross(double otherX, double otherY, double otherZ, Vector3d result) {
        if (result == null) result = new Vector3d();
        double resX = ((y * otherZ) - (z * otherY)); 
        double resY = ((z * otherX) - (x * otherZ));
        double resZ = ((x * otherY) - (y * otherX));
        result.set(resX, resY, resZ);
        return result;
    }

    public Vector3d crossLocal(Vector3d v) {
        return crossLocal(v.x, v.y, v.z);
    }

    public Vector3d crossLocal(double otherX, double otherY, double otherZ) {
        double tempx = ( y * otherZ ) - ( z * otherY );
        double tempy = ( z * otherX ) - ( x * otherZ );
        z = (x * otherY) - (y * otherX);
        x = tempx;
        y = tempy;
        return this;
    }

    public Vector3d project(Vector3d other){
        double n = this.dot(other); // A . B
        double d = other.lengthSquared(); // |B|^2
        return new Vector3d(other).normalizeLocal().multLocal(n/d);
    }

    public Vector3d projectLocal(Vector3d other){
        double n = this.dot(other); // A . B
        double d = other.lengthSquared(); // |B|^2
        return set(other).normalizeLocal().multLocal(n/d);
    }
    
    public boolean isUnitVector(){
        double len = length();
        return 0.999 < len && len < 1.001;
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public double lengthSquared() {
        return x * x + y * y + z * z;
    }

    public double distanceSquared(Vector3d v) {
        double dx = x - v.x;
        double dy = y - v.y;
        double dz = z - v.z;
        return (dx * dx + dy * dy + dz * dz);
    }

    public double distance(Vector3d v) {
        return Math.sqrt(distanceSquared(v));
    }
	public double total() {
		return x + y + z;
	}

    public Vector3d mult(double scalar) {
        return new Vector3d(x * scalar, y * scalar, z * scalar);
    }

    public Vector3d mult(double scalar, Vector3d product) {
        if (null == product) {
            product = new Vector3d();
        }

        product.x = x * scalar;
        product.y = y * scalar;
        product.z = z * scalar;
        return product;
    }

    public Vector3d multLocal(double scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
        return this;
    }

    public Vector3d multLocal(Vector3d vec) {
        if (null == vec) {
            logger.warning("Provided vector is null, null returned.");
            return null;
        }
        x *= vec.x;
        y *= vec.y;
        z *= vec.z;
        return this;
    }

    public Vector3d multLocal(double x, double y, double z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        return this;
    }

    /**
     * <code>multLocal</code> multiplies a provided vector to this vector
     * internally, and returns a handle to this vector for easy chaining of
     * calls. If the provided vector is null, null is returned.
     *
     * @param vec
     *            the vector to mult to this vector.
     * @return this
     */
    public Vector3d mult(Vector3d vec) {
        if (null == vec) {
            logger.warning("Provided vector is null, null returned.");
            return null;
        }
        return mult(vec, null);
    }

    /**
     * <code>multLocal</code> multiplies a provided vector to this vector
     * internally, and returns a handle to this vector for easy chaining of
     * calls. If the provided vector is null, null is returned.
     *
     * @param vec
     *            the vector to mult to this vector.
     * @param store result vector (null to create a new vector)
     * @return this
     */
    public Vector3d mult(Vector3d vec, Vector3d store) {
        if (null == vec) {
            logger.warning("Provided vector is null, null returned.");
            return null;
        }
        if (store == null) store = new Vector3d();
        return store.set(x * vec.x, y * vec.y, z * vec.z);
    }


    /**
     * <code>divide</code> divides the values of this vector by a scalar and
     * returns the result. The values of this vector remain untouched.
     *
     * @param scalar
     *            the value to divide this vectors attributes by.
     * @return the result <code>Vector</code>.
     */
    public Vector3d divide(double scalar) {
        scalar = 1f/scalar;
        return new Vector3d(x * scalar, y * scalar, z * scalar);
    }

    /**
     * <code>divideLocal</code> divides this vector by a scalar internally,
     * and returns a handle to this vector for easy chaining of calls. Dividing
     * by zero will result in an exception.
     *
     * @param scalar
     *            the value to divides this vector by.
     * @return this
     */
    public Vector3d divideLocal(double scalar) {
        scalar = 1f/scalar;
        x *= scalar;
        y *= scalar;
        z *= scalar;
        return this;
    }


    /**
     * <code>divide</code> divides the values of this vector by a scalar and
     * returns the result. The values of this vector remain untouched.
     *
     * @param scalar
     *            the value to divide this vectors attributes by.
     * @return the result <code>Vector</code>.
     */
    public Vector3d divide(Vector3d scalar) {
        return new Vector3d(x / scalar.x, y / scalar.y, z / scalar.z);
    }

    /**
     * <code>divideLocal</code> divides this vector by a scalar internally,
     * and returns a handle to this vector for easy chaining of calls. Dividing
     * by zero will result in an exception.
     *
     * @param scalar
     *            the value to divides this vector by.
     * @return this
     */
    public Vector3d divideLocal(Vector3d scalar) {
        x /= scalar.x;
        y /= scalar.y;
        z /= scalar.z;
        return this;
    }

    /**
     *
     * <code>negate</code> returns the negative of this vector. All values are
     * negated and set to a new vector.
     *
     * @return the negated vector.
     */
    public Vector3d negate() {
        return new Vector3d(-x, -y, -z);
    }

    /**
     *
     * <code>negateLocal</code> negates the internal values of this vector.
     *
     * @return this.
     */
    public Vector3d negateLocal() {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    /**
     *
     * <code>subtract</code> subtracts the values of a given vector from those
     * of this vector creating a new vector object. If the provided vector is
     * null, null is returned.
     *
     * @param vec
     *            the vector to subtract from this vector.
     * @return the result vector.
     */
    public Vector3d subtract(Vector3d vec) {
        return new Vector3d(x - vec.x, y - vec.y, z - vec.z);
    }

    /**
     * <code>subtractLocal</code> subtracts a provided vector to this vector
     * internally, and returns a handle to this vector for easy chaining of
     * calls. If the provided vector is null, null is returned.
     *
     * @param vec
     *            the vector to subtract
     * @return this
     */
    public Vector3d subtractLocal(Vector3d vec) {
        if (null == vec) {
            logger.warning("Provided vector is null, null returned.");
            return null;
        }
        x -= vec.x;
        y -= vec.y;
        z -= vec.z;
        return this;
    }

    /**
     *
     * <code>subtract</code>
     *
     * @param vec
     *            the vector to subtract from this
     * @param result
     *            the vector to store the result in
     * @return result
     */
    public Vector3d subtract(Vector3d vec, Vector3d result) {
        if(result == null) {
            result = new Vector3d();
        }
        result.x = x - vec.x;
        result.y = y - vec.y;
        result.z = z - vec.z;
        return result;
    }

    /**
     *
     * <code>subtract</code> subtracts the provided values from this vector,
     * creating a new vector that is then returned.
     *
     * @param subtractX
     *            the x value to subtract.
     * @param subtractY
     *            the y value to subtract.
     * @param subtractZ
     *            the z value to subtract.
     * @return the result vector.
     */
    public Vector3d subtract(double subtractX, double subtractY, double subtractZ) {
        return new Vector3d(x - subtractX, y - subtractY, z - subtractZ);
    }

    /**
     * <code>subtractLocal</code> subtracts the provided values from this vector
     * internally, and returns a handle to this vector for easy chaining of
     * calls.
     *
     * @param subtractX
     *            the x value to subtract.
     * @param subtractY
     *            the y value to subtract.
     * @param subtractZ
     *            the z value to subtract.
     * @return this
     */
    public Vector3d subtractLocal(double subtractX, double subtractY, double subtractZ) {
        x -= subtractX;
        y -= subtractY;
        z -= subtractZ;
        return this;
    }

    /**
     * <code>normalize</code> returns the unit vector of this vector.
     *
     * @return unit vector of this vector.
     */
    public Vector3d normalize() {
//        double length = length();
//        if (length != 0) {
//            return divide(length);
//        }
//
//        return divide(1);
        double length = x * x + y * y + z * z;
        if (length != 1f && length != 0f){
            length = 1.0f / Math.sqrt(length);
            return new Vector3d(x * length, y * length, z * length);
        }
        return clone();
    }

    /**
     * <code>normalizeLocal</code> makes this vector into a unit vector of
     * itself.
     *
     * @return this.
     */
    public Vector3d normalizeLocal() {
        // NOTE: this implementation is more optimized
        // than the old jme normalize as this method
        // is commonly used.
        double length = x * x + y * y + z * z;
        if (length != 1f && length != 0f){
            length = 1.0f / Math.sqrt(length);
            x *= length;
            y *= length;
            z *= length;
        }
        return this;
    }

    /**
     * <code>maxLocal</code> computes the maximum value for each 
     * component in this and <code>other</code> vector. The result is stored
     * in this vector.
     * @param other 
     */
    public Vector3d maxLocal(Vector3d other){
        x = other.x > x ? other.x : x;
        y = other.y > y ? other.y : y;
        z = other.z > z ? other.z : z;
        return this;
    }

    /**
     * <code>minLocal</code> computes the minimum value for each
     * component in this and <code>other</code> vector. The result is stored
     * in this vector.
     * @param other
     */
    public Vector3d minLocal(Vector3d other){
        x = other.x < x ? other.x : x;
        y = other.y < y ? other.y : y;
        z = other.z < z ? other.z : z;
        return this;
    }

    /**
     * <code>zero</code> resets this vector's data to zero internally.
     */
    public Vector3d zero() {
        x = y = z = 0;
        return this;
    }
	public Vector3d absolutes() {
		this.x = Math.abs(x);
		this.y = Math.abs(y);
		this.z = Math.abs(z);
		return this;
	}
	public Vector3d constrainAll(double min, double max) {
		constrainX(min, max);
		constrainY(min, max);
		constrainZ(min, max);
		return this;
	}
	public Vector3d constrainX(double min,double max) {
		x = Math.min(max, Math.max(min, x));
		return this;
	}
	public Vector3d constrainY(double min, double max) {
		y = Math.min(max, Math.max(min, y));
		return this;
	}
	public Vector3d constrainZ(double min, double max){
		z = Math.min(max, Math.max(min, z));
		return this;
	}
	public Vector3d rotateX(double angle) {
		if (angle != 0.0) {
			double sinAngle = Math.sin(Math.PI*angle/180);
			double cosAngle = Math.cos(Math.PI*angle/180);
			set(x, (y*cosAngle - z*sinAngle), (y*sinAngle + z*cosAngle));
		}
		return this;
	}
	public Vector3d rotateY(double angle) {
		if (angle != 0.0) {
			double sinAngle = Math.sin(Math.PI*angle/180);
			double cosAngle = Math.cos(Math.PI*angle/180);
			set((x*cosAngle + z*sinAngle), y, (-x*sinAngle + z*cosAngle));
		}
		return this;
	}
	public Vector3d rotateZ(double angle) {
		if (angle != 0.0) {
			double sinAngle = Math.sin(Math.PI*angle/180);
			double cosAngle = Math.cos(Math.PI*angle/180);
			set((x*cosAngle - y*sinAngle), (x*sinAngle + y*cosAngle), z);
		}
		return this;
	}

    /**
     * <code>angleBetween</code> returns (in radians) the angle between two vectors.
     * It is assumed that both this vector and the given vector are unit vectors (iow, normalized).
     * 
     * @param otherVector a unit vector to find the angle against
     * @return the angle in radians.
     */
    public double angleBetween(Vector3d otherVector) {
        double dotProduct = dot(otherVector);
        double angle = Math.acos(dotProduct);
        return angle;
    }
    
    /**
     * Sets this vector to the interpolation by changeAmnt from this to the finalVec
     * this=(1-changeAmnt)*this + changeAmnt * finalVec
     * @param finalVec The final vector to interpolate towards
     * @param changeAmnt An amount between 0.0 - 1.0 representing a precentage
     *  change from this towards finalVec
     */
    public Vector3d interpolate(Vector3d finalVec, double changeAmnt) {
        this.x=(1-changeAmnt)*this.x + changeAmnt*finalVec.x;
        this.y=(1-changeAmnt)*this.y + changeAmnt*finalVec.y;
        this.z=(1-changeAmnt)*this.z + changeAmnt*finalVec.z;
        return this;
    }

    /**
     * Sets this vector to the interpolation by changeAmnt from beginVec to finalVec
     * this=(1-changeAmnt)*beginVec + changeAmnt * finalVec
     * @param beginVec the beging vector (changeAmnt=0)
     * @param finalVec The final vector to interpolate towards
     * @param changeAmnt An amount between 0.0 - 1.0 representing a precentage
     *  change from beginVec towards finalVec
     */
    public Vector3d interpolate(Vector3d beginVec,Vector3d finalVec, double changeAmnt) {
        this.x=(1-changeAmnt)*beginVec.x + changeAmnt*finalVec.x;
        this.y=(1-changeAmnt)*beginVec.y + changeAmnt*finalVec.y;
        this.z=(1-changeAmnt)*beginVec.z + changeAmnt*finalVec.z;
        return this;
    }

    /**
     * Check a vector... if it is null or its doubles are NaN or infinite,
     * return false.  Else return true.
     * @param vector the vector to check
     * @return true or false as stated above.
     */
    public static boolean isValidVector(Vector3d vector) {
      if (vector == null) return false;
      if (Double.isNaN(vector.x) ||
          Double.isNaN(vector.y) ||
          Double.isNaN(vector.z)) return false;
      if (Double.isInfinite(vector.x) ||
          Double.isInfinite(vector.y) ||
          Double.isInfinite(vector.z)) return false;
      return true;
    }

    public static void generateOrthonormalBasis(Vector3d u, Vector3d v, Vector3d w) {
        w.normalizeLocal();
        generateComplementBasis(u, v, w);
    }

    public static void generateComplementBasis(Vector3d u, Vector3d v,
            Vector3d w) {
        double fInvLength;

        if (Math.abs(w.x) >= Math.abs(w.y)) {
            // w.x or w.z is the largest magnitude component, swap them
            fInvLength = 1.0 / Math.sqrt(w.x * w.x + w.z * w.z);
            u.x = -w.z * fInvLength;
            u.y = 0.0f;
            u.z = +w.x * fInvLength;
            v.x = w.y * u.z;
            v.y = w.z * u.x - w.x * u.z;
            v.z = -w.y * u.x;
        } else {
            // w.y or w.z is the largest magnitude component, swap them
            fInvLength = 1.0 / Math.sqrt(w.y * w.y + w.z * w.z);
            u.x = 0.0f;
            u.y = +w.z * fInvLength;
            u.z = -w.y * fInvLength;
            v.x = w.y * u.z - w.z * u.y;
            v.y = -w.x * u.z;
            v.z = w.x * u.y;
        }
    }

    @Override
    public Vector3d clone() {
        try {
            return (Vector3d) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // can not happen
        }
    }

    /**
     * Saves this Vector3d into the given double[] object.
     * 
     * @param doubles
     *            The double[] to take this Vector3d. If null, a new double[3] is
     *            created.
     * @return The array, with X, Y, Z double values in that order
     */
    public double[] toArray(double[] doubles) {
        if (doubles == null) {
            doubles = new double[3];
        }
        doubles[0] = x;
        doubles[1] = y;
        doubles[2] = z;
        return doubles;
    }
	public boolean allLessThan(Vector3d vector) {
		return x <= vector.x && y <= vector.y && z <= vector.z;
	}
	public boolean allGreaterThan(Vector3d vector) {
		return x >= vector.x && y >= vector.y && z >= vector.z;
	}
	public boolean isZero() {
		return x == 0 && y == 0 && z == 0;
	}
	public int compareTo(Vector3d vector) {
		if (total() > vector.total()) {
			return 1;
		} else if (total() < vector.total()) {
			return -1;
		} else {
			return 0;
		}
	}

    /**
     * are these two vectors the same? they are is they both have the same x,y,
     * and z values.
     *
     * @param o
     *            the object to compare for equality
     * @return true if they are equal
     */
    public boolean equals(Object o) {
        if (!(o instanceof Vector3d)) { return false; }

        if (this == o) { return true; }

        Vector3d comp = (Vector3d) o;
        if (Double.compare(x,comp.x) != 0) return false;
        if (Double.compare(y,comp.y) != 0) return false;
        if (Double.compare(z,comp.z) != 0) return false;
        return true;
    }

    /**
     * <code>hashCode</code> returns a unique code for this vector object based
     * on it's values. If two vectors are logically equivalent, they will return
     * the same hash code value.
     * @return the hash code value of this vector.
     */
    public int hashCode() {
        long hash = 37;
        hash += 37 * hash + Double.doubleToLongBits(x);
        hash += 37 * hash + Double.doubleToLongBits(y);
        hash += 37 * hash + Double.doubleToLongBits(z);
        return (int) (hash ^ (hash >>> 32));
    }

    /**
     * <code>toString</code> returns the string representation of this vector.
     * The format is:
     *
     * org.jme.math.Vector3d [X=XX.XXXX, Y=YY.YYYY, Z=ZZ.ZZZZ]
     *
     * @return the string representation of this vector.
     */
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
	public Vector3d parse(String input) {
		String[] values = input.split(" ");
		if (values.length == 3) {
			x = Double.valueOf(values[0]);
			y = Double.valueOf(values[1]);
			z = Double.valueOf(values[2]);
		} else {
            logger.warning("Parse error wrong number of arguments.");
			set(0, 0, 0);
		}
		return this;
	}
	public static Vector3d valueOf(String input) {
		return new Vector3d().parse(input);
	}

    public void write(JmeExporter e) throws IOException {
        OutputCapsule capsule = e.getCapsule(this);
        capsule.write(x, "x", 0);
        capsule.write(y, "y", 0);
        capsule.write(z, "z", 0);
    }

    public void read(JmeImporter e) throws IOException {
        InputCapsule capsule = e.getCapsule(this);
        x = capsule.readDouble("x", 0);
        y = capsule.readDouble("y", 0);
        z = capsule.readDouble("z", 0);
    }

    public double getX() {
        return x;
    }

    public Vector3d setX(double x) {
        this.x = x;
        return this;
    }

    public double getY() {
        return y;
    }

    public Vector3d setY(double y) {
        this.y = y;
        return this;
    }

    public double getZ() {
        return z;
    }

    public Vector3d setZ(double z) {
        this.z = z;
        return this;
    }
    
    /**
     * @param index
     * @return x value if index == 0, y value if index == 1 or z value if index ==
     *         2
     * @throws IllegalArgumentException
     *             if index is not one of 0, 1, 2.
     */
    public double get(int index) {
        switch (index) {
            case 0:
                return x;
            case 1:
                return y;
            case 2:
                return z;
        }
        throw new IllegalArgumentException("index must be either 0, 1 or 2");
    }
    
    /**
     * @param index
     *            which field index in this vector to set.
     * @param value
     *            to set to one of x, y or z.
     * @throws IllegalArgumentException
     *             if index is not one of 0, 1, 2.
     */
    public void set(int index, double value) {
        switch (index) {
            case 0:
                x = value;
                return;
            case 1:
                y = value;
                return;
            case 2:
                z = value;
                return;
        }
        throw new IllegalArgumentException("index must be either 0, 1 or 2");
    }
}