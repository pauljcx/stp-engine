uniform mat4 g_WorldViewProjectionMatrix;
uniform mat3 g_NormalMatrix;
uniform vec4 m_Color;

attribute vec3 inPosition;
attribute vec3 inNormal;

#ifdef VERTEX_COLOR
    attribute vec4 inColor;
#endif

#ifdef TEXTURE
    attribute vec2 inTexCoord;
    varying vec2 texCoord;
#endif

varying vec4 color;
varying vec3 vNormal;

void main() {
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);	
	vNormal = normalize(g_NormalMatrix * inNormal);
	
    #ifdef TEXTURE
        texCoord = inTexCoord;
    #endif
    #ifdef VERTEX_COLOR
        color = m_Color * inColor;
    #else
        color = m_Color;
    #endif
}
