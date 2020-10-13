#ifdef GL_ES 
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

varying MED vec2 v_texCoords;
 
uniform sampler2D u_texture;
uniform LOWP vec3 u_color;
uniform LOWP float u_ambient;

void main()
{
    LOWP vec4 texture = texture2D (u_texture, v_texCoords);
    gl_FragColor = vec4(texture.r + (texture.g + u_ambient) * u_color, 1.0);
}