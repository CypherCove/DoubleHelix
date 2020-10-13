#ifdef GL_ES 
#define LOWP lowp
precision mediump float;
#else
#endif

varying vec2 v_texCoordsR;
varying vec2 v_texCoordsG;
varying vec2 v_texCoordsB;

uniform sampler2D u_texture;

void main()
{
    LOWP float r = texture2D(u_texture, v_texCoordsR).r;
    LOWP float g = texture2D(u_texture, v_texCoordsG).g;
    LOWP float b = texture2D(u_texture, v_texCoordsB).b;
    gl_FragColor = vec4(r, g, b, 1.0);
}