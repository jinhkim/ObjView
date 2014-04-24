package com.ubertome.objview;

public class Matrix3 {

	private float[] data;

	public Matrix3() {
		data = new float[9];
	}

	public Matrix3(float[] mat) {
		data = new float[9];
		setMatrix3(mat);
	}

	/**
	 * Fills this 3x3 matrix with values from another matrix of the 
	 * same size or greater
	 * @param mat
	 */
	public void setMatrix3(float[] mat) {
		int size = mat.length;
		int order;
		double temp;

		try {
			// check if # of elements is a perfect square number
			if (size < 9 || ((temp = Math.sqrt(size)) % 1) != 0) {
				throw new Exception("Invalid Matrix size");
			}
			order = (int) temp;
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					if (size >= 9) {
						data[3 * i + j] = mat[order * i + j];
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public float get(int num) {
		if (num >= 0 && num < data.length)
			return data[num];
		else
			throw new IndexOutOfBoundsException(
					"Index out of bounds for Matrix3");
	}
}
