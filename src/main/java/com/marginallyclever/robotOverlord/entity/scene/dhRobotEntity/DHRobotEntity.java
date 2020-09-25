package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity;

import java.util.List;
import java.util.Observable;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.vecmath.Matrix4d;

import com.marginallyclever.convenience.IntersectionTester;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.entity.scene.Scene;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.dhTool.DHTool;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers.DHIKSolver;
import com.marginallyclever.robotOverlord.log.Log;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * A robot designed using D-H parameters.
 * 
 * @author Dan Royer
 */
public class DHRobotEntity extends PoseEntity {
	// a list of DHLinks describing the kinematic chain.
	public List<DHLink> links = new ArrayList<DHLink>();
	
	// The solver for this type of robot
	protected transient DHIKSolver solver;
	// only used in isPoseIKSane()
	protected PoseFK poseFKold;
	// only used in isPoseIKSane()
	protected PoseFK poseFKnew;

	// a DHTool attached to the arm.
	public DHTool dhTool;

	// more debug output, please.
	static final boolean VERBOSE=false;

	public DHRobotEntity() {
		super();
		setName("DHRobot");

		showBoundingBox.addObserver(this);
		showLocalOrigin.addObserver(this);
		showLineage.addObserver(this);
	}
	
	public DHRobotEntity(DHRobotEntity b) {
		this();
		set(b);
	}

	public void set(DHRobotEntity b) {
		super.set(b);
		// remove any exiting links from other robot to be certain.
		setNumLinks(b.getNumLinks());
		// copy my links to the next robot
		for(int i=0;i<b.getNumLinks();++i) {
			links.get(i).set(b.links.get(i));
			links.get(i).setDHRobot(this);
		}
		
		setIKSolver(b.solver);
		dhTool = b.dhTool;
		
		refreshPose();
	}
	
	/**
	 * Override this method to return the correct solver for your type of robot.
	 * 
	 * @return the IK solver for a specific type of robot.
	 */
	public DHIKSolver getIKSolver() {
		return solver;
	}

	public void setIKSolver(DHIKSolver solver0) {
		solver = solver0;
		poseFKold = solver.createPoseFK();
		poseFKnew = solver.createPoseFK();
	}

	public Matrix4d getParentMatrix() {
		if( parent == null || !(parent instanceof PoseEntity) ) {
			Matrix4d m = new Matrix4d();
			m.setIdentity();
			return m;
		} else {
			return ((PoseEntity)parent).getPose();
		}
	}
	
	/**
	 * Update the pose matrix of each DH link, then use forward kinematics to find
	 * the end position.
	 */
	public void refreshPose() {
		for( DHLink link : links ) {
			link.refreshPoseMatrix();
		}
		
		if (dhTool != null) {
			dhTool.refreshPoseMatrix();
		}
	}

	/**
	 * Adjust the number of links in this robot
	 * 
	 * @param newSize must be >= 0
	 */
	public void setNumLinks(int newSize) {
		if(newSize < 0) newSize = 0;

		links.clear();
		
		// count the number of existing children.
		Entity prev=this;
		boolean found;
		int s=0;
		while(prev.getChildren().size()>0 && s<newSize) {
			found=false;
			for( Entity c : prev.getChildren() ) {
				if(c instanceof DHLink ) {
					links.add((DHLink)c);
					prev=c;
					++s;
					found=true;
					break;
				}
			}
			// in case there are children but none are DHLinks
			if(found==false) break;
		}

		// if the number is too low, add more.
		while(s<newSize) {
			DHLink newLink = new DHLink();
			links.add(newLink);
			prev.addChild(newLink);
			prev = newLink;
			++s;
		}
		
		// if the number is too high, delete the remaining children.
		prev.getChildren().clear();
	}

	// the tool should be the child of the last link in the chain
	public void setTool(DHTool arg0) {
		removeTool();
		dhTool = arg0;
		links.get(links.size()-1).addChild(arg0);
	}

	public void removeTool() {
		if(dhTool==null) return;
		
		// the child of the last link in the chain is the tool
		links.get(links.size()-1).removeChild(dhTool);
		dhTool = null;
	}

	public DHTool getCurrentTool() {
		return dhTool;
	}

	/**
	 * checks that the keyframe is sane that no collisions occur.
	 * @param keyframe
	 * @return false if the keyframe is not sane or a collision occurs.
	 */
	public boolean sanityCheck(PoseFK keyframe) {
		if(!keyframeAnglesAreOK(keyframe)) {
			if(VERBOSE) Log.message("Bad angles");
			return false;
		}
		if(collidesWithSelf(keyframe)) {
			if(VERBOSE) Log.message("Collides with self");
			return false;
		}
		if(collidesWithWorld(keyframe))	{
			if(VERBOSE) Log.message("Collides with world");
			return false;
		}
		return true;
	}
		
	/**
	 * Test physical bounds of link N against all links &lt;N-1 and all links
	 * &gt;N+1 We're using separating Axis Theorem. See
	 * https://gamedev.stackexchange.com/questions/25397/obb-vs-obb-collision-detection
	 * 
	 * @param keyframe the angles at time of test
	 * @return true if there are no collisions
	 */
	public boolean collidesWithSelf(PoseFK futureKey) {
		PoseFK originalKey = getPoseFK();
		// move the clone to the keyframe pose
		setPoseFK(futureKey);
		
		int size = links.size();
		for (int i = 0; i < size; ++i) {
			if (links.get(i).getModel() == null)
				continue;

			for (int j = i + 2; j < size; ++j) {
				if (links.get(j).getModel() == null)
					continue;

				if (IntersectionTester.cuboidCuboid(
						links.get(i).getCuboid(),
						links.get(j).getCuboid())) {
						Log.message("Self collision between "+
									i+":"+links.get(i).getName()+" and "+
									j+":"+links.get(j).getName());

					setPoseFK(originalKey);
					return true;
				}
			}
		}

		setPoseFK(originalKey);
		return false;
	}

	/**
	 * Test physical bounds of all links with the world.
	 * We're using separating Axis Theorem. See https://gamedev.stackexchange.com/questions/25397/obb-vs-obb-collision-detection
	 * 
	 * @param keyframe the angles at time of test
	 * @return false if there are no collisions
	 */
	public boolean collidesWithWorld(PoseFK futureKey) {
		// is this robot in the world?
		Entity rootEntity = getRoot();
		if( !(rootEntity instanceof RobotOverlord) ) {
			// no!
			return false;
		}
		// yes!
		RobotOverlord ro = (RobotOverlord)rootEntity;
		// Does RobotOverlord contain a Scene?
		Scene scene = ro.getWorld();
		if(scene==null) return false;
		// yes!  We have all the prerequisites.		
		// save the original key
		PoseFK originalKey = getPoseFK();
		// move to the future key
		setPoseFK(futureKey);
		// test for intersection
		boolean result = scene.collisionTest((PoseEntity)this);
		// clean up and report results
		setPoseFK(originalKey);
		return result;
	}

	/**
	 * Perform a sanity check. Make sure the angles in the keyframe are within the
	 * joint range limits.
	 * 
	 * @param keyframe
	 * @return
	 */
	public boolean keyframeAnglesAreOK(PoseFK keyframe) {
		int j = 0;
		for( DHLink link : links ) {
			if(link.flags == LinkAdjust.NONE) continue;
			double v = keyframe.fkValues[j++];
			if ( link.rangeMax.get() < v || link.rangeMin.get() > v) {
				if(VERBOSE) {
					Log.message("FK "+ link.flags + j + ":" + v + " out (" + link.rangeMin.get() + " to " + link.rangeMax.get() + ")");
				}
				return false;
			}
		}

		return true;
	}
	
	/**
	 * Use an IK pose of the end effector to find the FK pose of the robot.  If it can be found, set that FK pose.
	 *   
	 * @param m end effector world pose matrix.
	 * @return true if sane position set.  false, nothing changed.
	 */
	public boolean setPoseIK(Matrix4d m) {
		if(isPoseIKSane(m)) {
			setPoseFK(poseFKnew);
			return true;
		}
		return false;
	}
	
	/**
	 * Verifies if the requested end effector pose of the robot is reachable and sane.
	 * Leaves the robot in the state it was found except for the newPose/oldPose keyframes, which are only used for this method. 
	 * 
	 * @param m end effector world pose matrix.
	 * @return true if sane.
	 */
	public boolean isPoseIKSane(Matrix4d m) {
		poseFKold.set(getPoseFK());

		if(VERBOSE) Log.message("\n\nold: "+poseFKold);
		
		boolean isSane = false;
		DHIKSolver.SolutionType s = solver.solveWithSuggestion(this, m, poseFKnew,poseFKold);
		if(VERBOSE) Log.message("new: "+poseFKnew + "\t"+s);
		if (s == DHIKSolver.SolutionType.ONE_SOLUTION) {
			if (sanityCheck(poseFKnew)) {
				if(VERBOSE) Log.message("Sane");
				isSane = true;
			} else if(VERBOSE) Log.message("isPoseIKSane() insane");
		} else if(VERBOSE) Log.message("isPoseIKSane() impossible");
		
		setPoseFK(poseFKold);
		
		return isSane;
	}

	/**
	 * Set the robot's FK values to the keyframe values and then refresh the pose.
	 * This method is used by others to verify for collisions, so this method
	 * cannot verify collisions itself.
	 * @param keyframe
	 */
	public void setPoseFK(PoseFK keyframe) {
		int stop=keyframe.fkValues.length;
		int j = 0;
		
		for( DHLink link : links ) {
			if(j==stop) break;
			if(link.hasAdjustableValue()) {
				link.setAdjustableValue(keyframe.fkValues[j++]);
			}
		}

		refreshPose();
	}

	/**
	 * Get the robot's FK pose.
	 * 
	 * @return keyframe of this pose
	 */
	public PoseFK getPoseFK() {
		PoseFK keyframe = solver.createPoseFK();
		assert(keyframe.fkValues.length==links.size());

		int j = 0;
		for( DHLink link : links ) {
			if(link.hasAdjustableValue()) {
				keyframe.fkValues[j++] = link.getAdjustableValue();
			}
		}
		return keyframe;
	}

	public int getNumLinks() {
		return links.size();
	}

	public DHLink getLink(int i) {
		return links.get(i);
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Dh", "DH shortcuts");
		
		view.add(showBoundingBox);
		view.add(showLocalOrigin);
		view.add(showLineage);
		
		for( DHLink link : links ) {
			view.addRange(link.theta,
					(int)Math.floor(link.rangeMax.get()),
					(int)Math.ceil(link.rangeMin.get()));
		}
		view.popStack();
		super.getView(view);
	}

	// recursively set for all children
	public void setShowBoundingBox(boolean arg0) {
		LinkedList<PoseEntity> next = new LinkedList<PoseEntity>();
		next.add(this);
		while( !next.isEmpty() ) {
			PoseEntity link = next.pop();
			link.showBoundingBox.set(arg0);
			for( Entity child : link.getChildren() ) {
				if( child instanceof PoseEntity ) {
					next.add((PoseEntity)child);
				}
			}
		}
		this.showBoundingBox.set(arg0);
	}
	
	// recursively set for all children
	public void setShowLocalOrigin(boolean arg0) {
		LinkedList<PoseEntity> next = new LinkedList<PoseEntity>();
		next.add(this);
		while( !next.isEmpty() ) {
			PoseEntity link = next.pop();
			link.showLocalOrigin.set(arg0);
			for( Entity child : link.getChildren() ) {
				if( child instanceof PoseEntity ) {
					next.add((PoseEntity)child);
				}
			}
		}
		this.showLocalOrigin.set(arg0);
	}

	// recursively set for all children
	public void setShowLineage(boolean arg0) {
		LinkedList<PoseEntity> next = new LinkedList<PoseEntity>();
		next.add(this);
		while( !next.isEmpty() ) {
			PoseEntity link = next.pop();
			link.showLineage.set(arg0);
			for( Entity child : link.getChildren() ) {
				if( child instanceof PoseEntity ) {
					next.add((PoseEntity)child);
				}
			}
		}
		this.showLineage.set(arg0);
	}
	
	/**
	 * Something this Entity is observing has changed.  Deal with it!
	 */
	@Override
	public void update(Observable o, Object arg) {
		if(o==showBoundingBox) setShowBoundingBox((boolean)arg);
		if(o==showLocalOrigin) setShowLocalOrigin((boolean)arg);
		if(o==showLineage) setShowLineage((boolean)arg);
	}
}
