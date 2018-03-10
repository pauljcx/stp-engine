uniform vec4 m_Color;
uniform float m_Shininess;
uniform vec2 m_CenterPoint;
uniform sampler2D m_Texture0;
uniform sampler2D m_Texture1;
uniform sampler2D m_HeightMap;
uniform vec4 g_LightDirection;

varying vec4 AmbientSum;
varying vec4 DiffuseSum;
varying vec4 SpecularSum;

varying vec2 texCoord;
varying vec3 vPosition;
varying vec3 vCoords;
varying vec3 vNormal;
varying vec3 vViewDir;
varying vec4 vLightDir;
varying vec3 lightVec;

float tangDot(in vec3 v1, in vec3 v2){
	float d = dot(v1,v2);
	return d;
}

float lightComputeDiffuse(in vec3 norm, in vec3 lightdir, in vec3 viewdir){
    return max(0.0, dot(norm, lightdir));
}

float lightComputeSpecular(in vec3 norm, in vec3 viewdir, in vec3 lightdir, in float shiny){
	// Standard Phong
	vec3 R = reflect(-lightdir, norm);
	return pow(max(tangDot(R, viewdir), 0.0), shiny);
}
vec2 computeLighting(in vec3 wvPos, in vec3 wvNorm, in vec3 wvViewDir, in vec3 wvLightDir){
   float diffuseFactor = lightComputeDiffuse(wvNorm, wvLightDir, wvViewDir);
   float specularFactor = lightComputeSpecular(wvNorm, wvViewDir, wvLightDir, m_Shininess);

   if (m_Shininess <= 1.0) {
       specularFactor = 0.0; // should be one instruction on most cards ..
   }

   float att = vLightDir.w;

   return vec2(diffuseFactor, specularFactor) * vec2(att);
}
void main(){
	float d = distance(m_CenterPoint, vCoords.xz);
	float intensity = max(0.5, 1.0 - smoothstep(35.0, 60.0, d));

	vec4 lightDir = vLightDir; //vec4(1.0, 0.0, 1.0, 1.0); //vLightDir;
    lightDir.xyz = normalize(lightDir.xyz);
	vec2 light = computeLighting(vPosition, vNormal, vViewDir.xyz, lightDir.xyz);
	
	float light0 = max(0.0, dot(vNormal, lightDir.xyz));
	
	vec4 specularColor = vec4(1.0);

	vec4 t0 = texture2D(m_Texture0, texCoord*32);
	vec4 diffuse0 = (AmbientSum * t0) + (DiffuseSum * t0  * light0) + (SpecularSum * specularColor * light.y);

	vec4 t1 = texture2D(m_Texture1, texCoord*64);
	vec4 diffuse1 =  (AmbientSum * t1) + (DiffuseSum * t1  * light.x) + (SpecularSum * specularColor * light.y);

	float height = vCoords.y;//texture2D(m_HeightMap, vec2(texCoord.s, 1.0 - texCoord.t)).x;
	vec4 color;
	if (height >= 2) {
		color = diffuse0;
	} else if (height < 1.5) {
		color = diffuse1;
	} else {
		float difference = smoothstep(0.75, 0.25, (height-1.5)*2);
		color = mix(diffuse0, diffuse1, difference);
	}
	gl_FragColor = color;//*intensity;
}

