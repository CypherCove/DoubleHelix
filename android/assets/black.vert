attribute vec4 a_position;
 
uniform mat4 u_modelViewProjTrans;
 
void main()
{
	gl_Position =  u_modelViewProjTrans * a_position;
}