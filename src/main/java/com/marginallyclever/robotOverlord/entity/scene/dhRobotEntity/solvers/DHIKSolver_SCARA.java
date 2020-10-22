package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector4d;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHRobotModel;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.PoseFK;
import com.marginallyclever.robotOverlord.log.Log;

/**
 * Solves Inverse Kinematics for a SCARA, serially-linked robot.
 * @author Dan Royer
 * See https://www.youtube.com/watch?v=xdOe4_WYzgU
 */
public class DHIKSolver_SCARA extends DHIKSolver {
	//public double theta0,d1,d2,theta3;

	/**
	 * @return the number of double values needed to store a valid solution from this DHIKSolver.
	 */
	public int getSolutionSize() {
		return 4;
	}

	/**
	 * Starting from a known local origin and a known local hand position, calculate the angles for the given pose.
	 * @param robot The DHRobot description. 
	 * @param targetMatrix the pose that robot is attempting to reach in this solution.
	 * @param keyframe store the computed solution in keyframe.
	 */
	@SuppressWarnings("unused")
	@Override
	public SolutionType solve(DHRobotModel robot,final Matrix4d targetMatrix,final PoseFK keyframe) {
		DHLink link4 = robot.getLink(robot.getNumLinks()-1);

		Matrix4d targetPoseAdj = new Matrix4d(targetMatrix);
		
		if(robot.getToolIndex()!=-1) {
			// there is a transform between the wrist and the tool tip.
			// use the inverse to calculate the wrist Z axis and wrist position.
			robot.getCurrentTool().refreshPoseMatrix();
			Matrix4d inverseToolPose = new Matrix4d(robot.getCurrentTool().getPose());
			inverseToolPose.invert();
			targetPoseAdj.mul(inverseToolPose);
		}
		Matrix4d m4 = new Matrix4d(targetPoseAdj);
		
		Point3d p4 = new Point3d(m4.m03,m4.m13,m4.m23);

		double a1 = robot.getLink(0).getR();
		double a2 = robot.getLink(1).getR();
		
		double b = a1;
		double a = a2;
		double c = Math.sqrt(p4.x*p4.x+p4.y*p4.y);
		if(c>a+b) c=a+b;

		// law of cosines to get the angle at the elbow
		// phi = acos( (b^2 - a^2 - c^2) / (-2*a*c) )
		double phi = Math.acos((b*b+a*a-c*c)/(2*a*b));

		keyframe.fkValues[1]=180-Math.toDegrees(phi);  // TODO explain this 180- here.
		
		// The the base rotation
		double theta2 = Math.PI - phi;
		double cc = a2 * Math.sin(theta2);
		double bb = a2 * Math.cos(theta2) + a1;
		double aa = c;
		double phi2 = Math.atan2(cc,bb);
		double tau = Math.atan2(p4.y,p4.x); 
		keyframe.fkValues[0]=Math.toDegrees(tau-phi2); 
		
		Point3d p3 = new Point3d(p4);
		p3.z = robot.getLink(0).getD();
		Point3d p2 = new Point3d(Math.cos(phi),Math.sin(phi),p3.z);
		
		// the height
		keyframe.fkValues[2]=-(p3.z-p4.z);
		
		// the rotation at the end effector
		Vector4d relativeX = new Vector4d(p3.x-p2.x,p3.y-p2.y,0,0);
		relativeX.normalize();  // normalize it
		
		Vector4d relativeY = new Vector4d(-relativeX.y,relativeX.x,0,0);

		Vector4d m4x = new Vector4d();
		m4.getColumn(1, m4x);
		
		double rX = m4x.dot(relativeX);
		double rY = m4x.dot(relativeY);
		
		keyframe.fkValues[3] = Math.toDegrees(-Math.atan2(rY,rX));
		
		if(false) {
			Log.message("solution={"+StringHelper.formatDouble(keyframe.fkValues[0])+","+
								keyframe.fkValues[1]+","+
								keyframe.fkValues[2]+","+
								StringHelper.formatDouble(keyframe.fkValues[3])+"}");
		}
		return SolutionType.ONE_SOLUTION;
	}
}
