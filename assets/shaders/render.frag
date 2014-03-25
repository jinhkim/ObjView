/*OpenGL ES 2.0*/

precision mediump float; 

uniform sampler2D textures;
uniform vec3 vBrightness;

varying float texEnabled;
varying float lightsEnabled;
varying vec3 lightPosEye;
varying vec3 normalEye; 
varying vec3 vertEye;
varying vec2 texCoords;

void main() { 

	vec4 texel;
	vec4 texAmb;
	vec4 texDiff;
	
	if(texEnabled > 0.5){
		texel = texture2D(textures, texCoords);
	}
	else {
		texel = vec4(1.0);
	}
	
	/*Light output components*/
	vec3 Ia;
	vec3 Id;
	
	/*light source components*/
	vec3 La = vec3(0.5);
	vec3 Ld = vec3(0.5);
	/*vec3 Ls = vec3(1.0);*/
	
	vec3 Ka = vec3(0.3); /*ambient reflectance term*/
	vec3 Kd = vec3(0.8); /*diffuse term*/
	
	/*ambient light term*/
	Ia = La * Ka;
	
	texAmb = vec4(Ia, 1.0) * texel;
	
	float dotProd;
	vec3 lightToSurface;
	
	if(lightsEnabled > 0.5){
		/*diffuse light term*/
		lightToSurface = normalize(lightPosEye - vertEye);
		
		dotProd = dot(lightToSurface, normalEye);
		dotProd = max(dotProd, 0.0);
	}
	else{
		dotProd = 1.0;
	}

	if(lightsEnabled > 0.5){
		Id = (Ld + vBrightness) * Kd * dotProd;
	}
	else {
		Id = Ld * Kd * dotProd;
	}
	
	texDiff = vec4(Id, 1.0) * texel;
	
	gl_FragColor = texAmb + texDiff;
}