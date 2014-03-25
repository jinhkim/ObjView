/*OpenGL ES 2.0*/

attribute vec3 vPosition; 
attribute vec3 vNormal; 
attribute vec2 vTexture;
uniform mat4 modelViewMatrix; 
uniform mat4 mMVPMatrix;
uniform mat4 mModelMatrix;
uniform mat4 mViewMatrix;
uniform vec4 lightPosVec;
uniform float lightingEnabled;
uniform float texturesEnabled;
uniform vec3 vBrightness;

varying float lightsEnabled;
varying float texEnabled;
varying vec3 lightPosEye;
varying vec3 normalEye; 
varying vec3 vertEye;
varying vec2 texCoords;

void main() { 

	/*Calculate normal matrix*/
	vec4 normal = vec4(vNormal, 0.0);
	
	normalEye = normalize(vec3(modelViewMatrix * normal));
	
	lightsEnabled = lightingEnabled;
	
	texEnabled = texturesEnabled;
	
	texCoords = vTexture;
	
	lightPosEye = vec3(mViewMatrix * lightPosVec);
	
	vertEye = vec3(modelViewMatrix * vec4(vPosition, 1.0));
	
	gl_Position = mMVPMatrix * vec4(vPosition, 1.0);
}