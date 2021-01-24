package com.marginallyclever.convenience;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.log.Log;


public class Matrix4dTurtle {
	public class InterpolationStep {		
		public Matrix4d targetIK;
		public double duration;
		
		
		public void set(InterpolationStep b) {
			targetIK.set(b.targetIK);
			duration = b.duration;
		}
		
		public void set(Matrix4d targetIK,double waitTime) {
			this.targetIK = new Matrix4d(targetIK);
			this.duration=waitTime;
		}
		public InterpolationStep(Matrix4d targetIK,double waitTime) {
			this.targetIK = new Matrix4d(targetIK);
			this.duration=waitTime;
		}
		public InterpolationStep() {
			targetIK = new Matrix4d();
		}
	};
	protected List<InterpolationStep> steps;
	
	// targetMatrix = (endMatrix - startMatrix) * timeSoFar/timeTotal + startMatrix
	protected Matrix4d targetMatrix;
	
	// Time of entire sequence of steps
	protected double totalPlayTime;
	protected transient double playHead;
	protected transient boolean isPlaying;
	protected transient double thisStepDuration;
	protected transient double thisStepSoFar;
	
	protected transient InterpolationStep thisStepStart;
	protected transient InterpolationStep thisStepEnd;
	
	public Matrix4dTurtle() {
		thisStepStart=new InterpolationStep();
		thisStepEnd=new InterpolationStep();
		reset();
	}
	
	public Matrix4dTurtle(Matrix4dTurtle b) {
		thisStepStart=new InterpolationStep();
		thisStepEnd=new InterpolationStep();
		set(b);
	}
	
	public void reset() {
		targetMatrix = new Matrix4d();
		steps = new ArrayList<InterpolationStep>();
		playHead = 0;
		totalPlayTime = 0;
		isPlaying = false;
	}

	public void set(Matrix4dTurtle b) {
		// assumes everything provided by linkDescription
		thisStepStart.set(b.thisStepStart);
		thisStepEnd.set(b.thisStepEnd);
		targetMatrix.set(b.targetMatrix);
		steps.addAll(b.steps);
		playHead = b.playHead;
		totalPlayTime = b.totalPlayTime;
		isPlaying = b.isPlaying;
	}
	
	public void offer(Matrix4d start,Matrix4d end,double duration) {
		if(isPlaying()) return;
		
		InterpolationStep nextStep = new InterpolationStep();
		nextStep.targetIK = end;
		nextStep.duration = duration;
		steps.add(nextStep);
		
		totalPlayTime += duration;
	}
	
	public boolean isInterpolating() {
		return ( playHead < totalPlayTime );
	}
	
	/**
	 * 
	 * @param t
	 * @return if steps null, return null.  else return InterpolationStep closest to t.
	 */
	public InterpolationStep getStepTargetAtTime(double t) {
		if(steps.isEmpty()) return null;
		
		for( InterpolationStep step : steps) {
			if(t<=step.duration) return step;
			t-=step.duration;
		}
		return steps.get(steps.size()-1);
	}
	
	/**
	 * 
	 * @param t
	 * @return if steps null, return 0.  else return portion of t remaining at this step.
	 */
	public double getSoFarAtTime(double t) {
		if(steps.isEmpty()) return 0;
		
		for( InterpolationStep step : steps) {
			if(t<=step.duration) return t;
			t-=step.duration;
		}
		return t;
	}
	
	public void update(double dt,Matrix4d poseNow) {
		if(!isPlaying()) return;
		
		playHead+=dt;
		if(playHead>totalPlayTime) {
			playHead=totalPlayTime;
			setPlaying(false);
		}
		Log.message("playing "+StringHelper.formatDouble(playHead)
						+"/"+StringHelper.formatDouble(totalPlayTime)
						+" ("+(StringHelper.formatDouble(100.0*playHead/totalPlayTime))+"%)"
						+" aka "+StringHelper.formatDouble(thisStepSoFar));

		thisStepSoFar+=dt;
		if(thisStepSoFar>=thisStepDuration) {
			thisStepSoFar-=thisStepDuration;

			InterpolationStep step = getStepTargetAtTime(playHead);
			if(step!=null) {
				Log.message("\tstep "+ steps.indexOf(step) +"/"+steps.size());
				thisStepStart.set(poseNow,0);
				thisStepEnd=step;
				thisStepDuration=thisStepEnd.duration;
			}
		}
		if(playHead>=totalPlayTime) {
			Log.message(" END");
			setPlaying(false);
			// if we are a looping recording,
			//setPlayHead(0);
			//setPlaying(true);
		}
		
		// if we are single block
		//setPlaying(false);
	}

	public void render(GL2 gl2) {
		if(steps.size()==0) return;
		
		Vector3d last=null;
		for(InterpolationStep step : steps ) {
			MatrixHelper.drawMatrix(gl2, step.targetIK, 2);
			Vector3d curr = MatrixHelper.getPosition(step.targetIK);
			if(last!=null) {
				gl2.glColor3d(1, 1, 1);
				gl2.glBegin(GL2.GL_LINES);
				gl2.glVertex3d(last.x, last.y, last.z);
				gl2.glVertex3d(curr.x, curr.y, curr.z);
				gl2.glEnd();
			}
			last=curr;
		}
		Vector3d curr = MatrixHelper.getPosition(steps.get(0).targetIK);
		if(last!=null) {
			gl2.glColor3d(1, 1, 1);
			gl2.glBegin(GL2.GL_LINES);
			gl2.glVertex3d(last.x, last.y, last.z);
			gl2.glVertex3d(curr.x, curr.y, curr.z);
			gl2.glEnd();
		}
	}
	
	public Matrix4d getStartMatrix() {
		if(thisStepStart==null) return null;
		return thisStepStart.targetIK;
	}

	public Matrix4d getEndMatrix() {
		if(thisStepEnd==null) return null;
		return thisStepEnd.targetIK;
	}

	public double getTotalPlayTime() {
		return totalPlayTime;
	}

	public double getPlayHead() {
		return playHead;
	}
	public void setPlayhead(double t) {
		playHead=t;
		thisStepSoFar=getSoFarAtTime(playHead);
		InterpolationStep step = getStepTargetAtTime(playHead);
		if(step!=null) {
			thisStepEnd=step;
			thisStepDuration=thisStepEnd.duration;
		}
	}
	
	public int getQueueSize() {
		return steps.size();
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	public void setPlaying(boolean isPlaying) {
		this.isPlaying = isPlaying;
	}

	public double getStepDuration() {
		return thisStepDuration;
	}

	public double getStepSoFar() {
		return thisStepSoFar;
	}
}
