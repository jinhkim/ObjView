package com.ubertome.objview;

import java.util.Arrays;

public class Vector3 {
	private float[] data;

	public Vector3() {
		data = new float[3];
	}

	public Vector3(float val) {
		data = new float[] { val, val, val };
	}

	public Vector3(float x, float y, float z) {
		data = new float[] { x, y, z };
	}

	public Vector3(float[] vec) {
		setNewVector(vec);
	}

	public Vector3(Vector3 vec) {
		setVector(vec);
	}

	public void normalize() {
		GLUtils.normalize(data);
	}

	public void normalize(int dimension) {
		GLUtils.normalize(data, dimension);
	}

	public float magnitude() {
		return (float) Math.sqrt(data[0] * data[0] + data[1] * data[1]
				+ data[2] * data[2]);
	}

	public void setVector(Vector3 vec) {
		if (vec.size() < 3)
			throw new IndexOutOfBoundsException(
					"Vector3 requires at least a 3-element array as input");
		data = vec.data.clone();
	}
	
	public void setVector(float value){
		data[0] = data[1] = data[2] = value;
	}
	
	public void setVector(float x, float y, float z){
		data[0] = x;
		data[1] = y;
		data[2] = z;
	}

	public void setNewVector(float[] vec) {
		if (vec.length < 3)
			throw new IndexOutOfBoundsException(
					"Vector3 requires at least a 3-element array as input");
		data = new float[] { vec[0], vec[1], vec[2] };
	}
	
	public float getX() {
		return data[0];
	}

	public float getY() {
		return data[1];
	}

	public float getZ() {
		return data[2];
	}

	public float setX(float x) {
		data[0] = x;
		return x;
	}

	public float setY(float y) {
		data[1] = y;
		return y;
	}

	public float setZ(float z) {
		data[2] = z;
		return z;
	}

	public float[] getDataArray() {
		return data;
	}

	public float[] getVector4(int w) {
		return new float[] { data[0], data[1], data[2], w };
	}
	
	public int size(){
		return data.length;
	}
}