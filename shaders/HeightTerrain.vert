uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldViewMatrix;
uniform mat4 g_WorldMatrix;
uniform mat3 g_NormalMatrix;
uniform mat4 g_ViewMatrix;
uniform vec4 g_LightColor;
uniform vec4 g_LightPosition;
uniform vec4 g_AmbientLightColor;

attribute vec3 inPosition;
attribute vec3 inNormal;
attribute vec2 inTexCoord;

varying vec2 texCoord;
varying vec3 vPosition;
varying vec3 vCoords;
varying vec3 vNormal;
varying vec3 vViewDir;
varying vec4 vLightDir;
varying vec3 lightVec;

varying vec4 AmbientSum;
varying vec4 DiffuseSum;
varying vec4 SpecularSum;

// JME3 lights in world space
void lightComputeDir(in vec3 worldPos, in vec4 color, in vec4 position, out vec4 lightDir){
    float posLight = step(0.5, color.w);
    vec3 tempVec = position.xyz * sign(posLight - 0.5) - (worldPos * posLight);
    lightVec.xyz = tempVec;  
    float dist = length(tempVec);
    lightDir.w = clamp(1.0 - position.w * dist * posLight, 0.0, 1.0);
    lightDir.xyz = tempVec / vec3(dist);
}

void main() {
	vec4 modelSpacePos = vec4(inPosition, 1.0);

    texCoord = inTexCoord;

	vCoords = (g_WorldMatrix * modelSpacePos).xyz;
	vPosition = (g_WorldViewMatrix * modelSpacePos).xyz;
	vNormal  = normalize(g_NormalMatrix * inNormal);
	vViewDir = normalize(-vPosition);

	vec4 wvLightPos = (g_ViewMatrix * vec4(g_LightPosition.xyz,clamp(g_LightColor.w,0.0,1.0)));
    wvLightPos.w = g_LightPosition.w;

	lightComputeDir(vPosition, g_LightColor, wvLightPos, vLightDir);

	AmbientSum  = g_AmbientLightColor;
	DiffuseSum  = g_LightColor;
	SpecularSum = g_LightColor;

	gl_Position = g_WorldViewProjectionMatrix * modelSpacePos;
}
