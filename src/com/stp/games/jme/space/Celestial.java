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
package com.stp.games.jme.space;
// JME3 Dependencies
import com.jme3.export.Savable;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.binary.ByteUtils;
// Java Dependencies
import java.io.IOException;
import java.util.ArrayList;
// Internal Dependencies
import com.stp.games.jme.util.Vector3d;
import com.stp.games.jme.util.MathUtils;
import com.stp.games.jme.controls.GameControl;

public class Celestial implements Savable {
	public static final double G = 0.0000000000667408; // Gravitational Constant G = 6.674*10−11 N-m2/kg
	public static final double PARSEC = 	30841882000000.0; // 30.8 trillion kilometers
	public static final double LIGHTYEAR = 9460700000000.0; // 9.5 trillion kilometers
	public static final double AU =  149597870.7; // 150 million kilometers
	public static final double ME = 5.972 * Math.pow(10, 24); // Earth Mass in kg

	public enum Classification {
		Void,
		Universe,
		Galaxy,
		System,
		Star,
		Nebula,
		Planet,
		Moon,
		Comet,
		Asteroid,
		Meteoroid;
	}
	
	public enum SubClass {
		None,
		Rock,
		Terrestrial,
		GasGiant,
		ProtoStar,
		YSO,
		PMS,
		MainSequence,
		SubDwarf,
		SubGiant,
		Giant,
		BrightGiant,
		SuperGiant,
		HyperGiant,
		BlackHole,
		BrownDwarf,
		WhiteDwarf;
	}
	
	// Identifiers
	protected long uid;
	protected String name;
	protected long parent_id;
	protected Classification classification = Classification.Void;
	protected SubClass subclass = SubClass.None;

	// Physical parameters
	protected final Vector3d coordinates = new Vector3d();
	protected double radius;
	protected double mass;
	
	// References
	protected Celestial parent;
	protected GameControl object;
	
	// Orbit
	protected double semiMajorAxis;
	protected float eccentricity;
	protected float trueAnomaly;
	protected float inclination;
	protected float longitudeOfAscendingNode;
	protected float argumentOfPeriapsis;

	// Calculated Parameters
	protected double meanMotion;
	protected double period;

	public Celestial() {
	}
	public void setUniqueId(long uid) {
		this.uid = uid;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setParentId(long parent_id) {
		this.parent_id = parent_id;
	}
	public void setClassification(Classification classification) {
		this.classification = classification;
	}
	public void setSubClass(SubClass subclass) {
		this.subclass = subclass;
	}
	public void setX(double x) {
		coordinates.setX(x);
	}
	public void setY(double y) {
		coordinates.setY(y);
	}
	public void setZ(double z) {
		coordinates.setZ(z);
	}
	public void setCoordinates(double x, double y, double z) {
		coordinates.set(x, y, z);
	}
	public void setRadius(double radius) {
		this.radius = radius;
	}
	public void setMass(double mass) {
		this.mass = mass;
		updateMeanMotion();
	}
	public void setParent(Celestial parent) {
		this.parent = parent;
		updateMeanMotion();
	}
	public void setGameObject(GameControl object) {
		this.object = object;
	}
	public void setSemiMajorAxis(double semiMajorAxis) {
		this.semiMajorAxis = semiMajorAxis;
		updateMeanMotion();
	}
	public void setEccentricity(float eccentricity) {
		this.eccentricity = eccentricity;
	}
	public void setTrueAnomaly(float trueAnomaly) {
		this.trueAnomaly = trueAnomaly;
	}
	public void setInclination(float inclination) {
		this.inclination = inclination;
	}
	public void setLongitudeOfAscendingNode(float longitudeOfAscendingNode) {
		this.longitudeOfAscendingNode = longitudeOfAscendingNode;
	}
	public void setArgumentOfPeriapsis(float argumentOfPeriapsis) {
		this.argumentOfPeriapsis = argumentOfPeriapsis;
	}
	public boolean hasParent() {
		return parent_id > 0;
	}
	public long getUniqueId() {
		return uid;
	}
	public String getName() {
		return name;
	}
	public long getParentId() {
		return parent_id;
	}
	public Classification getClassification() {
		return classification;
	}
	public SubClass getSubClass() {
		return subclass;
	}
	public double getX() {
		return coordinates.getX();
	}
	public double getY() {
		return coordinates.getY();
	}
	public double getZ() {
		return coordinates.getZ();
	}
	public Vector3d getCoordinates() {
		return coordinates;
	}
	public Vector3d getAbsoluteCoordinates(Vector3d store) {
		if (parent != null) {
			parent.getAbsoluteCoordinates(store);
		} 
		return store.addLocal(coordinates);
	}
	public double getRadius() {
		return radius;
	}
	public double getMass() {
		return mass;
	}
	public Celestial getParent() {
		return parent;
	}
	public GameControl getGameObject() {
		return object;
	}
	public double getSemiMajorAxis() {
		return semiMajorAxis;
	}
	public float getEccentricity() {
		return eccentricity;
	}
	public float getInclination() {
		return inclination;
	}
	public float getTrueAnomaly() {
		return trueAnomaly;
	}
	public float getLongitudeOfAscendingNode() {
		return longitudeOfAscendingNode;
	}
	public float getArgumentOfPeriapsis() {
		return argumentOfPeriapsis;
	}
	public void setNamedValue(String nodeName, String value) {
		if (nodeName.equals("uid")) {
			setUniqueId(Long.valueOf(value));
		} else if (nodeName.equals("name")) {
			setName(value);
		}else if (nodeName.equals("parent_id")) {
			setParentId(Long.valueOf(value));
		} else if (nodeName.equals("classification")) {
			setClassification(Classification.valueOf(value));
		} else if (nodeName.equals("subclass")) {
			setSubClass(SubClass.valueOf(value));
		} else if (nodeName.equals("x_coordinate")) {
			setX(Double.valueOf(value));
		} else if (nodeName.equals("y_coordinate")) {
			setY(Double.valueOf(value));
		} else if (nodeName.equals("z_coordinate")) {
			setZ(Double.valueOf(value));
		} else if (nodeName.equals("radius")) {
			setRadius(Double.valueOf(value));
		} else if (nodeName.equals("mass")) {
			setMass(Double.valueOf(value));
		} else if (nodeName.equals("axis")) {
			setSemiMajorAxis(Double.valueOf(value));
		} else if (nodeName.equals("eccentricity")) {
			setEccentricity(Float.valueOf(value));
		} else if (nodeName.equals("anomaly")) {
			setTrueAnomaly(Float.valueOf(value));
		} else if (nodeName.equals("inclination")) {
			setInclination(Float.valueOf(value));
		} else if (nodeName.equals("longitude")) {
			setLongitudeOfAscendingNode(Float.valueOf(value));
		} else if (nodeName.equals("periapsis")) {
			setArgumentOfPeriapsis(Float.valueOf(value));
		} 
	}
	@Override
	// Write all unique parameter values to the JmeExporter
	public void write(JmeExporter e) throws IOException {
		OutputCapsule capsule = e.getCapsule(this);
		capsule.write(uid, "uid", 0L);
		capsule.write(name, "name", "void");
		capsule.write(parent_id, "parent_id", 0L);
		capsule.write(classification.name(), "classification", "Void");
		capsule.write(subclass.name(), "subclass", "None");
		capsule.write(coordinates.getX(), "x_coordinate", 0.0);
		capsule.write(coordinates.getY(), "y_coordinate", 0.0);
		capsule.write(coordinates.getZ(), "z_coordinate", 0.0);
		capsule.write(radius, "radius", 0.0);
		capsule.write(mass, "mass", 0.0);
		capsule.write(semiMajorAxis, "axis", 0.0);
		capsule.write(eccentricity, "eccentricity", 0.0f);
		capsule.write(trueAnomaly, "anomaly", 0.0f);
		capsule.write(inclination, "inclination", 0.0f);
		capsule.write(longitudeOfAscendingNode, "longitude", 0.0f);
		capsule.write(argumentOfPeriapsis, "periapsis", 0.0f);
	}
	@Override
	// Read all unique parameter values from the JmeImporter
	public void read(JmeImporter e) throws IOException {
		InputCapsule capsule = e.getCapsule(this);
		setUniqueId(capsule.readLong("uid", 0L));
		setName(capsule.readString("name", "void"));
		setParentId(capsule.readLong("parent_id", 0L));
		setClassification(Classification.valueOf(capsule.readString("classification", "Void")));
		setSubClass(SubClass.valueOf(capsule.readString("subclass", "None")));
		setX(capsule.readDouble("x_coordinate", 0.0));
		setY(capsule.readDouble("y_coordinate", 0.0));
		setZ(capsule.readDouble("z_coordinate", 0.0));
		setRadius(capsule.readDouble("radius", 0.0));
		setMass(capsule.readDouble("mass", 0.0));
		setSemiMajorAxis(capsule.readDouble("axis", 0.0));
		setEccentricity(capsule.readFloat("eccentricity", 0.0f));
		setTrueAnomaly(capsule.readFloat("anomaly", 0.0f));
		setInclination(capsule.readFloat("inclination", 0.0f));
		setLongitudeOfAscendingNode(capsule.readFloat("longitude", 0.0f));
		setArgumentOfPeriapsis(capsule.readFloat("periapsis", 0.0f));
	}
	public void setTime(double timeInSeconds) {
		// Only calculate the position if the object is in motion
		if (meanMotion > 0) {
			// A standard eliptical orbit as defined using the six kepler elements
			double meanAnomaly = (trueAnomaly*MathUtils.DEG_TO_RAD) + (meanMotion * timeInSeconds); // Calculate the current angle using the average motion and the current time in seconds
			double eccentricityAnomaly = getEccentricityAnomaly(eccentricity, meanAnomaly, 5); // Use Newtons Method to get the eccentricity anomaly
			coordinates.setX(semiMajorAxis*(Math.cos(eccentricityAnomaly)-eccentricity));
			coordinates.setY(0);
			coordinates.setZ(semiMajorAxis*Math.sqrt(1.0-eccentricity*eccentricity)*Math.sin(eccentricityAnomaly));
			coordinates.rotateY(argumentOfPeriapsis); // Rotate the orbital path by the argumentOfPeriapsis
			coordinates.rotateZ(inclination); // Rotate the orbital path by the inclination
			coordinates.rotateY(longitudeOfAscendingNode); // Rotate the orbital path by the longitude of ascending node
		}
	}
	// Recalculates the period and mean motion when they are updated
	private void updateMeanMotion() {
		if (parent != null && mass > 0 && semiMajorAxis > 0) {
			double u = G*(parent.getMass() + mass); // the standard gravitational parameter in m3/s2
			this.period = MathUtils.TWO_PI*Math.sqrt(Math.pow(semiMajorAxis*1000.0, 3)/u); // total time in seconds to complete on full revolution
			this.meanMotion = MathUtils.TWO_PI/period; // the amount moved in angular motion for each second
		}
	}
	// Returns the calculated time to complete one full orbital revolution in seconds
	public double getPeriod() {
		return period;
	}
	// Returns the average angular motion per second in radians
	public double getMeanMotion() {
		return meanMotion;
	}

	/* Static Methods */
	public static Vector3d convertToAU(Vector3d in, Vector3d out) {
		return out.set(in.x/AU, in.y/AU, in.z/AU);
	}
	public static Vector3d convertToLightYears(Vector3d in, Vector3d out) {
		return out.set(in.x/LIGHTYEAR, in.y/LIGHTYEAR, in.z/LIGHTYEAR);
	}
	public static Vector3d convertToParsecs(Vector3d in, Vector3d out) {
		return out.set(in.x/PARSEC, in.y/PARSEC, in.z/PARSEC);
	}
	// Calculates the Eccentricity Anomaly for Orbits using Newton's method
	public static double getEccentricityAnomaly(float eccentricity, double meanAnomaly, int precision) {
		int maxIter = 30;
		int i = 0;
		double delta = Math.pow(10,-precision);
		double m = meanAnomaly % MathUtils.TWO_PI; // normalize the mean anomaly
		
		double E = (eccentricity < 0.8f) ? m : Math.PI;
		double F = E - eccentricity*Math.sin(m) - m;

		while ((Math.abs(F)>delta) && (i < maxIter)) {
			E = E - F/(1.0-eccentricity*Math.cos(E));
			F = E - eccentricity*Math.sin(E) - m;
			i++;
		}
		return Math.round(E*Math.pow(10, precision))/Math.pow(10, precision);
	}
}