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

void main()
{
    gl_FragColor = texture2D (u_texture, v_texCoords);
}