package com.ubertome.objview;

import java.util.Vector;

public class Quaternion {
	private final float pi = 3.141592f;
	private final float DEG2RAD = pi / 180.0f;
	private final float RAD2DEG = 180.0f / pi;
	private final float ROUNDING_TOLERANCE = 0.00001f;

	public float w, x, y, z;

	/**
	 * Constructs a identity quaternion
	 */
	public Quaternion() {
		w = 1.0f;
		x = 0;
		y = 0;
		z = 0;
	}

	/**
	 * Create a quaternion object at coordinates (x, y, z, w)
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param w
	 */
	public Quaternion(float x, float y, float z, float w) {
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Create a quaternion rotation about vector with angle degrees of rotation
	 * 
	 * @param vector
	 *            - axis to rotate about
	 * @param angle
	 *            - in degrees
	 */
	public Quaternion(Vector<Float> vector, float angle) {
		buildFromAxisAngle(vector, angle);
	}

	/**
	 * Sets <code>this</code> equal to <code>rhs</code>. Returns
	 * <code>this</code> to chain operations
	 * 
	 * @param rhs
	 * @return <code>this</code>
	 */
	public Quaternion setEqualTo(Quaternion rhs) {
		w = rhs.w;
		x = rhs.x;
		y = rhs.y;
		z = rhs.z;
		return this;
	}

	/**
	 * Multiplies <code>this</code> with rhs like so: <blockquote>
	 * <code>this</code> * <code>rhs</code></blockquote> OpenGL is column-major
	 * operator-on-the-left, so <code>this</code> would be the operator and
	 * <code>rhs</code> would be the object we want to transform. Does not
	 * modify original Quaternion. Returns a new Quaternion object.
	 * 
	 * @param rhs
	 * @return a new Quaternion object
	 */
	public Quaternion multiplyWith(Quaternion rhs) {
		float qw = w, qx = x, qy = y, qz = z;
		float ww, xx, yy, zz;
		ww = qw * rhs.w - qx * rhs.x - qy * rhs.y - qz * rhs.z;
		xx = qw * rhs.x + qx * rhs.w - qy * rhs.z + qz * rhs.y;
		yy = qw * rhs.y + qx * rhs.z + qy * rhs.w - qz * rhs.x;
		zz = qw * rhs.z - qx * rhs.y + qy * rhs.x + qz * rhs.w;
		return new Quaternion(ww, xx, yy, zz);
	}
	
	/**
	 * Does the same thing as {@link #multiplyWith(Quaternion) multiplyWith}, but instead modifies this
	 * Quaternion and returns itself
	 * @param rhs
	 * @return this
	 */
	public Quaternion multiplyThisWith(Quaternion rhs) {
		float qw = w, qx = x, qy = y, qz = z;
		float ww, xx, yy, zz;
		ww = qw * rhs.w - qx * rhs.x - qy * rhs.y - qz * rhs.z;
		xx = qw * rhs.x + qx * rhs.w - qy * rhs.z + qz * rhs.y;
		yy = qw * rhs.y + qx * rhs.z + qy * rhs.w - qz * rhs.x;
		zz = qw * rhs.z - qx * rhs.y + qy * rhs.x + qz * rhs.w;
		
		w = ww; 
		x = xx;
		y = yy;
		z = zz;
		return this;
	}

	/**
	 * Normalizes <code>this</code> vector.
	 */
	public void normalize() {
		float magnitude = (float) Math.sqrt(w * w + x * x + y * y + z * z);
		if (Math.abs(magnitude - 1) > ROUNDING_TOLERANCE
				&& Math.abs(magnitude) > ROUNDING_TOLERANCE) {
			w /= magnitude;
			x /= magnitude;
			y /= magnitude;
			z /= magnitude;
		}
	}

	/**
	 * Returns a new Quaternion that is a conjugate of <code>this</code>
	 * 
	 * @return
	 */
	public Quaternion conjugate() {
		return new Quaternion(-x, -y, -z, w);
	}

	/**
	 * Builds quaternion from an axis of rotation and an angle
	 * 
	 * @param axis
	 *            as a <code>Vector{@literal <}Float{@literal >}</code>
	 * @param angle
	 *            in degrees
	 */
	public void buildFromAxisAngle(Vector<Float> axis, float angle) {
		angle *= DEG2RAD;
		angle *= 0.5;
		float sinAngle = (float) Math.sin(angle);
		Quaternion quat = new Quaternion(axis.get(0), axis.get(1), axis.get(2),
				0.0f);
		quat.normalize();

		x = (quat.x * sinAngle);
		y = (quat.y * sinAngle);
		z = (quat.z * sinAngle);
		w = (float) Math.cos(angle);
		normalize();
	}

	public void buildFromAxisAngle(float[] axis, float angle) {
		Vector<Float> vec = new Vector<Float>();
		vec.add(axis[0]);
		vec.add(axis[1]);
		vec.add(axis[2]);
		buildFromAxisAngle(vec, angle);
	}

	/**
	 * Creates quaternion from Euler angles
	 * 
	 * @param xRot
	 *            rotation about X axis in degrees
	 * @param yRot
	 *            rotation about Y axis in degrees
	 * @param zRot
	 *            rotation about Z axis in degrees
	 */
	public void buildFromEuler(float xRot, float yRot, float zRot) {
		float pitch = xRot * DEG2RAD / 2.0f;
		float yaw = yRot * DEG2RAD / 2.0f;
		float roll = zRot * DEG2RAD / 2.0f;

		float sinp = (float) Math.sin(pitch), siny = (float) Math.sin(roll), sinr = (float) Math
				.sin(yaw), cosp = (float) Math.cos(pitch), cosy = (float) Math
				.cos(roll), cosr = (float) Math.cos(yaw);

		x = sinr * cosp * cosy - cosr * sinp * siny;
		y = cosr * sinp * cosy + sinr * cosp * siny;
		z = cosr * cosp * siny - sinr * sinp * cosy;
		w = cosr * cosp * cosy + sinr * sinp * siny;

		normalize();
	}

	public void buildFromMatrix(float[] m) {

		float s = 0.0f;
		float q[] = new float[] { 0.0f, 0.0f, 0.0f, 0.0f };
		float trace = m[0] + m[5] + m[10];

		if (trace > 0.0f) {
			s = (float) Math.sqrt(trace + 1.0f);
			q[3] = s * 0.5f;
			s = 0.5f / s;
			q[0] = (m[6] - m[9]) * s;
			q[1] = (m[8] - m[2]) * s;
			q[2] = (m[1] - m[4]) * s;
		} else {
			int[] nxt = new int[] { 1, 2, 0 };
			int i = 0, j = 0, k = 0;

			if (m[5] > m[0])
				i = 1;

			if (m[10] > m[i * 5])
				i = 2;

			j = nxt[i];
			k = nxt[j];
			s = (float) Math.sqrt((m[i * 5] - (m[j * 5] + m[k * 5])) + 1.0f);

			q[i] = s * 0.5f;
			s = 0.5f / s;
			q[3] = (m[j * 4 + k] - m[k * 4 + j]) * s;
			q[j] = (m[i * 4 + j] + m[j * 4 + i]) * s;
			q[k] = (m[i * 4 + k] + m[k * 4 + i]) * s;
		}

		x = q[0];
		y = q[1];
		z = q[2];
		w = q[3];
		normalize();
	}

	public float[] getMatrix() {
		float x2 = x * x;
		float y2 = y * y;
		float z2 = z * z;
		float xy = x * y;
		float xz = x * z;
		float yz = y * z;
		float wx = w * x;
		float wy = w * y;
		float wz = w * z;

		return new float[] { 1.0f - 2.0f * (y2 + z2), 2.0f * (xy - wz),
				2.0f * (xz + wy), 0.0f, 2.0f * (xy + wz),
				1.0f - 2.0f * (x2 + z2), 2.0f * (yz - wx), 0.0f,
				2.0f * (xz - wy), 2.0f * (yz + wx), 1.0f - 2.0f * (x2 + y2),
				0.0f, 0.0f, 0.0f, 0.0f, 1.0f };
	}

	/**
	 * Converts <code>this</code> into a rotation about a vector. Returns a
	 * 4-component <code>Vector{@literal <}Float{@literal >}</code> with the
	 * first 3 components set to an axis <code>(x, y, z)</code>, and the last
	 * component to the angle
	 * 
	 * @return <code>Vector{@literal <}Float{@literal >}</code> with axis and
	 *         angle data
	 */
	public Vector<Float> getAxisAngle() {
		Vector<Float> vector = new Vector<Float>();
		float magnitude = (float) Math.sqrt(x * x + y * y + z * z);
		vector.set(0, x / magnitude);
		vector.set(1, y / magnitude);
		vector.set(2, z / magnitude);
		float temp = (float) Math.acos(w) * 2.0f;
		vector.set(3, temp * RAD2DEG);
		return vector;
	}

	public float getMagnitude() {
		return (float) Math.sqrt(x * x + y * y + z * z + w * w);
	}
}
