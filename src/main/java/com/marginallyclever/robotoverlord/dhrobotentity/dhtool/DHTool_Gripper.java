package com.marginallyclever.robotoverlord.dhrobotentity.dhtool;

import java.util.List;
import java.util.StringTokenizer;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.memento.Memento;
import com.marginallyclever.convenience.memento.MementoOriginator;
import com.marginallyclever.robotoverlord.PoseEntity;
import com.marginallyclever.robotoverlord.dhrobotentity.DHLink;
import com.marginallyclever.robotoverlord.swinginterface.InputManager;


/**
 * @author Dan Royer
 */
@Deprecated
public class DHTool_Gripper extends DHTool implements MementoOriginator {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1544140469103301389L;
	public static final double ANGLE_MAX=55;
	public static final double ANGLE_MIN=10;
	
	protected DHLink[] subComponents = new DHLink[6];

	protected double gripperServoAngle;
	protected double interpolatePoseT;
	protected double startT,endT;

	protected transient boolean wasGripping;
	protected transient PoseEntity subjectBeingHeld;
	
	public DHTool_Gripper() {
		super();
		setLetter("T");
		setName("Gripper");
		refreshDHMatrix();
		
		gripperServoAngle=90;
		interpolatePoseT=1;
		startT=endT=gripperServoAngle;
		
		setShapeFilename("/Sixi2/beerGripper/base.stl");
		shapeEntity.setShapeScale(0.1f);
		shapeEntity.setShapeOrigin(-1,0,4.15);
		shapeEntity.setShapeRotation(0,180,90);
		

		Matrix3d r = new Matrix3d();
		r.setIdentity();
		r.rotX(Math.toRadians(180));
		Matrix3d r2 = new Matrix3d();
		r2.setIdentity();
		r2.rotZ(Math.toRadians(90));
		r.mul(r2);
		this.setRotation(r);
		
		// 4 bars
		addChild(subComponents[0]=new DHLink());
		addChild(subComponents[1]=new DHLink());
		addChild(subComponents[2]=new DHLink());
		addChild(subComponents[3]=new DHLink());
		subComponents[0].setShapeFilename("/Sixi2/beerGripper/linkage.stl");
		subComponents[0].setShapeScale(0.1);
		subComponents[1].set(subComponents[0]);
		subComponents[2].set(subComponents[0]);
		subComponents[3].set(subComponents[0]);
		subComponents[0].setPosition(new Vector3d(2.7/2, 0, 4.1));
		subComponents[1].setPosition(new Vector3d(1.1/2, 0, 5.9575));
		subComponents[2].setPosition(new Vector3d(-2.7/2, 0, 4.1));
		subComponents[3].setPosition(new Vector3d(-1.1/2, 0, 5.9575));
		
		// 2 finger tips
		addChild(subComponents[4]=new DHLink());
		subComponents[4].setShapeFilename("/Sixi2/beerGripper/finger.stl");
		subComponents[4].setShapeScale(0.1);
		addChild(subComponents[5]=new DHLink());
		subComponents[5].set(subComponents[4]);
		
		wasGripping=false;
	}
	
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
/*
		material.render(gl2);
		
		gl2.glPushMatrix();
			gl2.glRotated(90, 0, 0, 1);
		
			double v = -180-this.gripperServoAngle;
		
			gl2.glPushMatrix();
			gl2.glRotated(v, 0, 1, 0);
			gl2.glPopMatrix();
			
			gl2.glPushMatrix();
			gl2.glRotated(v, 0, 1, 0);
			gl2.glPopMatrix();
			
			gl2.glPushMatrix();
			gl2.glRotated(-v, 0, 1, 0);
			gl2.glPopMatrix();
			
			gl2.glPushMatrix();
			gl2.glRotated(-v, 0, 1, 0);
			gl2.glPopMatrix();

			double c=Math.cos(Math.toRadians(v));
			double s=Math.sin(Math.toRadians(v));
			gl2.glPushMatrix();
			gl2.glTranslated(-2.7/2-s*4.1, 0, 4.1+c*4.1);
			gl2.glScaled(1,1,-1);
			//gl2.glTranslated(s*4.1, 2.7/2, 4.1+c*4.1);
			finger.render(gl2);
			gl2.glPopMatrix();
			
			gl2.glPushMatrix();
			gl2.glTranslated(2.7/2+s*4.1, 0, 4.1+c*4.1);
			gl2.glScaled(-1,1,-1);
			finger.render(gl2);
			gl2.glPopMatrix();
		
		gl2.glPopMatrix();
		*/
	}

	/**
	 * Read HID device to move target pose.  Currently hard-coded to PS4 joystick values. 
	 * @return true if targetPose changes.
	 */
	@Override
	public boolean directDrive() {
		boolean isDirty=false;
		final double scaleGrip=1.8;
		
		if(InputManager.isOn(InputManager.Source.STICK_CIRCLE) && !wasGripping) {
			wasGripping=true;
			Matrix4d poseWorld = getPoseWorld();
			// grab release
			if(subjectBeingHeld==null) {
				//Log.message("Grab");
				// Get the object at the targetPos.
				Vector3d target = new Vector3d();
				poseWorld.get(target);
				List<PoseEntity> list = this.getWorld().findPhysicalObjectsNear(target, 10);
				if(!list.isEmpty()) {
					subjectBeingHeld = list.get(0);
					// A new subject has been acquired.
					// The subject is being held by the gripper.  Subtract the gripper's world pose from the subject's world pose.
					Matrix4d m = subjectBeingHeld.getPose();
					Matrix4d iposeWorld = new Matrix4d(poseWorld);
					iposeWorld.invert();
					m.mul(iposeWorld);
					subjectBeingHeld.setPose(m);
				}
			} else {
				//Log.message("Release");
				// The subject is being held relative to the gripper.  Add the gripper's world pose to the subject's pose.
				Matrix4d m = subjectBeingHeld.getPose();
				m.mul(poseWorld);
				subjectBeingHeld.setPose(m);
				// forget the subject.
				subjectBeingHeld=null;
			}
		}
		if(InputManager.isOff(InputManager.Source.STICK_CIRCLE)) wasGripping=false;
		
        if(InputManager.isOn(InputManager.Source.STICK_OPTIONS)) {
			if(gripperServoAngle<ANGLE_MAX) {
				gripperServoAngle+=scaleGrip;
				if(gripperServoAngle>ANGLE_MAX) gripperServoAngle=ANGLE_MAX;
				isDirty=true;
			}
        }
        if(InputManager.isOn(InputManager.Source.STICK_SHARE)) {
			if(gripperServoAngle>ANGLE_MIN) {
				gripperServoAngle-=scaleGrip;
				if(gripperServoAngle<ANGLE_MIN) gripperServoAngle=ANGLE_MIN;
				isDirty=true;
			}
        }

        return isDirty;
	}

	@Override
	public String getCommand() {
		return getLetter()+StringHelper.formatDouble(this.gripperServoAngle);
	}
	
	@Override
	public void sendCommand(String str) {
		StringTokenizer tok = new StringTokenizer(str);
		while(tok.hasMoreTokens()) {
			String token = tok.nextToken();
			try {
				if(token.startsWith("T")) {
					startT = gripperServoAngle;
					endT = StringHelper.parseNumber(token.substring(1));
				}
			} catch(NumberFormatException e) {
				e.printStackTrace();
			}
		}
		gripperServoAngle=endT;
		interpolatePoseT=0;
	}
	
	@Override
	public void interpolate(double dt) {
		super.interpolate(dt);
		
		if(interpolatePoseT<1) {
			interpolatePoseT+=dt;
			if(interpolatePoseT>=1) {
				interpolatePoseT=1;
			}
			gripperServoAngle=((endT-startT)*interpolatePoseT + startT);
			refreshDHMatrix();
		}
	}
	
	@Override
	public double getAdjustableValue() {
		return gripperServoAngle;
	}
	
	@Override
	public void setAdjustableValue(double v) {
		v = Math.max(Math.min(v, rangeMax.get()), rangeMin.get());
		gripperServoAngle=v;
	}

	@Override
	public Memento getState() {
		return new GripperMemento(gripperServoAngle);
	}

	@Override
	public void setState(Memento arg0) {
		gripperServoAngle = ((GripperMemento)arg0).gripperAngle;
	}
}
