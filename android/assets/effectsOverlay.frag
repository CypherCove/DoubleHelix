#ifdef GL_ES
    #define LOWP lowp
    precision mediump float;
#else
    #define LOWP
#endif

varying vec2 v_texCoords;
varying vec2 v_texCoordsGrain;
varying vec2 v_texCoordsScanLines;

uniform sampler2D u_texture; // the vignette texture, drawn with SpriteBatch
uniform sampler2D u_noiseTexture;
uniform sampler2D u_scanLineTexture;
uniform float u_flicker;
uniform vec4 u_color;
#ifdef BLOOM
uniform sampler2D u_bloomTexture;
const float BLOOM_SCALE = 0.4;
#endif
     
void main()
{
	LOWP float vignette = u_color.b * texture2D(u_texture, v_texCoords).a;
	LOWP float grain = u_color.r * texture2D(u_noiseTexture, v_texCoordsGrain).a;
	LOWP float scanLine = u_color.g * texture2D(u_scanLineTexture, v_texCoordsScanLines).a;

	#ifdef BLOOM
	    LOWP float bloom = BLOOM_SCALE * texture2D(u_bloomTexture, v_texCoords).r;
		float scanLineDarkening = mix(1.0, scanLine, u_color.g);
	    gl_FragColor = vec4(vec3(bloom * (1.0 - grain) * scanLineDarkening + grain), max(vignette * u_flicker + grain + scanLine, 0.0));
	#else
	    gl_FragColor = vec4(vec3(grain), (vignette * u_flicker + grain + scanLine));
	#endif
}