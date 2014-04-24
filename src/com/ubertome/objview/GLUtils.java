package com.ubertome.objview;

import com.ubertome.objview.Vector3;

public class GLUtils {

	private GLUtils(){}
	
	public static float[] crossProduct(float ax, float ay, float az, float bx, float by, float bz){
		float[] ans = new float[3];
		ans[0] = ay*bz - az*by;
		ans[1] = az*bx - ax*bz;
		ans[2] = ax*by - ay*bx; 
		return ans;
	}
	
	public static Vector3 crossProduct(Vector3 a, Vector3 b){
		Vector3 ans = new Vector3();
		float ax = a.getX(),  ay = a.getY(),  az = a.getZ(),  
				bx = b.getX(),  by = b.getY(),  bz = b.getZ();
		
		ans.setX(ay*bz - az*by);
		ans.setY(az*bx - ax*bz);
		ans.setZ(ax*by - ay*bx);
		
		return ans;
	}
	
	public static float dotProduct(Vector3 a, Vector3 b){
		float ans = a.getX()*b.getX() 
				  + a.getY()*b.getY()
				  + a.getZ()*b.getZ();
		return ans;
	}
	
	public static Vector3 multiplyMV3(Matrix3 mat3, Vector3 vec3){
		Vector3 ans = new Vector3();
		
		ans.setX(mat3.get(0)*vec3.getX() + mat3.get(3)*vec3.getY() + mat3.get(6)*vec3.getZ());
		ans.setY(mat3.get(1)*vec3.getX() + mat3.get(4)*vec3.getY() + mat3.get(7)*vec3.getZ());
		ans.setZ(mat3.get(2)*vec3.getX() + mat3.get(5)*vec3.getY() + mat3.get(8)*vec3.getZ());
		return ans;
	}
	
	public static float[] normalize(float[] vec){
		return normalize(vec, vec.length);
	}
	
	public static float[] normalize(float[] vec, int n){
		int dimension = (n > 0 && n <= vec.length) ? n : vec.length;
		float mag, sum = 0;
		
		for(int i = 0; i < dimension; i++){
			sum += vec[i]*vec[i];
		}
		mag = (float) Math.sqrt(sum);
		
		for(int j = 0; j < dimension; j++){
			vec[j] /= mag;
		}
		
		return vec;
	}
}
