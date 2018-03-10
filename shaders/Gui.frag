#ifdef TEXTURE
    uniform sampler2D m_Texture;
    varying vec2 texCoord;
#endif

varying vec4 color;
varying vec3 vNormal;

void main() {
	vec4 colour = color;
	vec3 ambient = vec3(0.2, 0.2, 0.2);
	
    #ifdef TEXTURE
		vec4 texVal = texture2D(m_Texture, texCoord);
		colour = texVal * color;
	#endif

	vec3 lightDir = vec3(0, -1.0, 1.0);
	lightDir = normalize(lightDir);

	float lightVal = max(0.0, dot(vNormal, lightDir));

	gl_FragColor.rgb = (ambient * colour.rgb) + (colour.rgb * vec3(lightVal));
	gl_FragColor.a = colour.a;
}
