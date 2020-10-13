#ifdef GL_ES
	#define LOWP lowp
	precision mediump float;
#else
	#define LOWP 
#endif

varying LOWP vec4 v_color;
varying LOWP vec4 v_channelStrength;

uniform sampler2D u_texture;
     
void main()
{
	vec4 texture = texture2D(u_texture, gl_PointCoord);
	gl_FragColor = dot(v_channelStrength, texture) * v_color;
}