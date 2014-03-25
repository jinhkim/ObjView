package com.ubertome.objview;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Square {

	private FloatBuffer vertexBuffer;
	private ShortBuffer drawListBuffer;

	// number of coords per array
	static final int COORDS_PER_VERTEX = 3;
	static float squareCoords[] = { 
			-0.5f, 0.5f, 0.0f, // top left
			-0.5f, -0.5f, 0.0f, // bottom left
			0.5f, -0.5f, 0.0f, // bottom right
			0.5f, 0.5f, 0.0f }; // top right
	
	//triangles drawn in counter clockwise order
	private short drawOrder[] =  {0, 1, 2, 0, 2, 3};

//	float color[] = {0.88f, 0.423f, 0.9f, 1.0f};
	
	public Square() {
		//space allocated = # of floats X 4
		ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
		
		bb.order(ByteOrder.nativeOrder());
		
		//create float buffer 
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(squareCoords);
		vertexBuffer.position(0);
		
		//space allocated = # of shorts X 2
		ByteBuffer dd = ByteBuffer.allocateDirect(drawOrder.length * 2);
		
		//create drawlist buffer
		drawListBuffer = dd.asShortBuffer();
		drawListBuffer.put(drawOrder);
		drawListBuffer.position(0);
	}
}
