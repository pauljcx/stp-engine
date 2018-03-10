uniform sampler2D m_Texture;
uniform sampler2D m_DepthTexture;
varying vec2 texCoord;

uniform int m_FogMode;
uniform vec4 m_FogColor;
uniform float m_FogDensity;
uniform float m_FogStartDistance;
uniform float m_FogEndDistance;
uniform bool m_ExcludeSky;

uniform float g_Time;

const float LOG2 = 1.442695;

float computeLinearFogFactor(in float depth, in vec2 frustumNear, in vec2 frustumFar) {
float depthS = (2.0 * frustumNear.x) / (frustumNear.y + frustumNear.x * (frustumNear.y - frustumNear.x));
float depthE = (2.0 * frustumFar.x) / (frustumFar.y + frustumFar.x * (frustumFar.y - frustumFar.x));
float fogDepth =	1.0/((frustumFar.y) / (frustumNear.y - depth * (frustumNear.y)));
float fogFactor;
fogFactor = (depthE - fogDepth) / (depthE - depthS);
fogFactor = clamp( fogFactor, 0.0, 1.0 );
return fogFactor;
}

float computeExp2FogFactorC2D(in float depth, in vec2 frustumNear) {
float depthS = 1.0-(2.0 * frustumNear.x) / (frustumNear.y + frustumNear.x * (frustumNear.y-frustumNear.x));
float fogDepth = ((depthS) / ((frustumNear.y) - depth * ((frustumNear.y))))*(depthS*4.0);
float fogFactor = 0.0;
if (depth < depthS)
fogFactor = exp2( -m_FogDensity * m_FogDensity * fogDepth *  fogDepth * LOG2 );
fogFactor = clamp(fogFactor, 0.0, 1.0);
return fogFactor;
}

float computeExp2FogFactorD2I(in float depth, in vec2 frustumNear) {
float depthS = 1.0-(2.0 * frustumNear.x) / (frustumNear.y + frustumNear.x * (frustumNear.y-frustumNear.x));
float fogDepth = 1.0-((depthS) /
((frustumNear.y) - depth * ((frustumNear.y))));
float fogFactor = 1.0;
if (depth > depthS)
fogFactor = exp2( -m_FogDensity * m_FogDensity * fogDepth *  fogDepth * LOG2 );
fogFactor = clamp(fogFactor, 0.0, 1.0);
return fogFactor;
}

void main() {
vec4 texVal = texture2D(m_Texture, texCoord);
float depth	= texture2D(m_DepthTexture,texCoord).r;
vec2 frustumNear = vec2(1.0,m_FogStartDistance);
vec2 frustumFar = vec2(1.0,m_FogEndDistance);

float fogFactor;

if (m_FogMode == 0) fogFactor = computeLinearFogFactor(depth, frustumNear, frustumFar);
if (m_FogMode == 1) fogFactor = computeExp2FogFactorC2D(depth, frustumNear);
if (m_FogMode == 2) fogFactor = computeExp2FogFactorD2I(depth, frustumNear);

if (m_ExcludeSky) {
if (depth < 1.0)
gl_FragColor = mix(m_FogColor,texVal,fogFactor);
else
gl_FragColor = texVal;
} else
gl_FragColor = mix(m_FogColor,texVal,fogFactor);

}