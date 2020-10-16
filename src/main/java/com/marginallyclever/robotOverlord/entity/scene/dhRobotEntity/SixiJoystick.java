package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity;

import java.util.Observable;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantLock;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.RemoteEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2old.Sixi2;
import com.marginallyclever.robotOverlord.entity.scene.modelEntity.ModelEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;


@Deprecated
public class SixiJoystick extends ModelEntity {
	private Sixi2 target;
	
	private RemoteEntity connection = new RemoteEntity();
	private ReentrantLock lock = new ReentrantLock();

	PoseFK [] keyframeSamples = new PoseFK[10];  // more samples = slower response but smoother results.
	
	PoseFK keyframe;
	
	public SixiJoystick() {
		setName("Sixi Joystick");
		connection.addObserver(this);
	}
	
	// TODO this is trash.  if robot is deleted this link would do what, exactly?
	// What if there was more than one Sixi?  More than one joystick?
	protected Sixi2 findRobot() {
		if(parent instanceof Sixi2) {
			return (Sixi2)parent;
		}
		
		for( Entity e : getWorld().getChildren() ) {
			if(e instanceof Sixi2) {
				return (Sixi2)e;
			}
		}
		
		return null;
	}
	
	@Override
	public void update(Observable obs,Object obj) {
		if(obs == connection) {
			if(lock.isLocked()) return;
			lock.lock();
			try {
				if(target==null) {
					target = findRobot();
				}
				if(target!=null) {
					String message = (String)obj;
					//Log.message("JOY: "+message.trim());
					
					int i,j;
					
					if(keyframe==null) {
						keyframe = target.sim.createPoseFK();
						for(j=0;j<keyframeSamples.length;++j) {
							keyframeSamples[j]= target.sim.createPoseFK();
						}
					}
					// age the samples
					for(j=1;j<keyframeSamples.length;++j) {
						keyframeSamples[j-1].set(keyframeSamples[j]);
					}

					StringTokenizer tokenizer = new StringTokenizer(message);
					if(tokenizer.countTokens()<keyframe.fkValues.length) return;
					
					// update the last sample
					j=keyframeSamples.length-1;
					for(i=0;i<keyframe.fkValues.length;++i) {
						double d = StringHelper.parseNumber(tokenizer.nextToken());
						keyframeSamples[j].fkValues[i]=d;//Math.max(Math.min(d,180),-180);
					}
					keyframeSamples[j].fkValues[1]*=-1;
					keyframeSamples[j].fkValues[4]*=-1;
					keyframeSamples[j].fkValues[1]-=90;
					
					// update the average
					for(i=0;i<keyframe.fkValues.length;++i) {
						keyframe.fkValues[i] = 0;
						for(j=1;j<keyframeSamples.length;++j) {
							keyframe.fkValues[i] += keyframeSamples[j].fkValues[i];
						}
						keyframe.fkValues[i] /= keyframeSamples.length;
					}
				}
			}
			catch(NumberFormatException e) {
				e.printStackTrace();
			}
			finally {
				lock.unlock();
			}
		}
	}

	@Override
	public void update(double dt) {
		if(target==null) return;
		
		if(lock.isLocked()) return;
		lock.lock();
		try {
			target.sim.setPoseTo(keyframe);
		}
		finally {
			lock.unlock();
		}
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Sj", "Sixi Joystick");
		view.add(connection);
		view.popStack();
		super.getView(view);
	}
}
