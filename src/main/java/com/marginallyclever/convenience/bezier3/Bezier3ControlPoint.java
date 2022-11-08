package com.marginallyclever.convenience.bezier3;

import com.jogamp.opengl.GL2;

import javax.vecmath.Vector3d;

/**
 * 3D Bezier curve implementation
 * @See <a href='https://en.wikipedia.org/wiki/B%C3%A9zier_curve'>Wikipedia</a>
 * @author Dan Royer
 */
public class Bezier3ControlPoint {
	public Bezier3 position = new Bezier3();
	
	/**
	 * visualize the line in opengl
	 * @param gl2
	 */
	public void render(GL2 gl2) {
		//Vector3d u,v,w;
		
		//MatrixHelper.drawMatrix(gl2, position.interpolate(0), u, v, w);
		//MatrixHelper.drawMatrix(gl2, position.interpolate(1), u, v, w);
		boolean isLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
		boolean isCM =  gl2.glIsEnabled(GL2.GL_COLOR_MATERIAL);
		boolean isDepth =  gl2.glIsEnabled(GL2.GL_DEPTH_TEST);

		gl2.glEnable(GL2.GL_DEPTH_TEST);
		gl2.glDisable(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_COLOR_MATERIAL);
		
		//*
		gl2.glColor4f(0, 0, 1, 1);
		gl2.glBegin(GL2.GL_LINES);
		gl2.glVertex3d(position.p0.x,position.p0.y,position.p0.z);
		gl2.glVertex3d(position.p1.x,position.p1.y,position.p1.z);
		
		gl2.glVertex3d(position.p2.x,position.p2.y,position.p2.z);
		gl2.glVertex3d(position.p3.x,position.p3.y,position.p3.z);
		gl2.glEnd();
		//*/
		
		gl2.glColor4f(0, 1, 0, 1);
		gl2.glBegin(GL2.GL_LINE_STRIP);
		final float NUM_STEPS=20;
		for(float i=0;i<=NUM_STEPS;++i) {
			Vector3d ipos = position.interpolate(i/NUM_STEPS);
			gl2.glVertex3d(ipos.x,ipos.y,ipos.z);
		}
		gl2.glEnd();
		
		if(isLit) gl2.glEnable(GL2.GL_LIGHTING);
		if(isCM) gl2.glEnable(GL2.GL_COLOR_MATERIAL);
		if(!isDepth) gl2.glDisable(GL2.GL_DEPTH_TEST);
	}
}
