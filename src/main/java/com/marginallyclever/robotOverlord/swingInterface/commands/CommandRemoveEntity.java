package com.marginallyclever.robotOverlord.swingInterface.commands;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.RemovableEntity;
import com.marginallyclever.robotOverlord.log.Log;
import com.marginallyclever.robotOverlord.swingInterface.actions.ActionEntityRemove;
import com.marginallyclever.robotOverlord.swingInterface.translator.Translator;

/**
 * @author Dan Royer
 */
public class CommandRemoveEntity extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	
	public CommandRemoveEntity(RobotOverlord ro) {
		super(Translator.get("Remove Entity"));
        putValue(AbstractAction.SHORT_DESCRIPTION, Translator.get("Remove the selected entity from the world."));
        //putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, ActionEvent.CTRL_MASK));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_DELETE));
        
		this.ro = ro;
	}

	public void actionPerformed(ActionEvent event) {
		ArrayList<Entity> entityList = ro.getSelectedEntities();
		if(entityList.size()==0) {
			Log.error("RemoveEntity with no entity selected.");
			return;
		}
		for(Entity e : entityList) {
			if(e instanceof RemovableEntity) {
				ro.undoableEditHappened(new UndoableEditEvent(this,new ActionEntityRemove(ro,e) ) );
			} else {
				Log.error("Entity "+e.getFullPath()+" is not a RemovableEntity.");
			}
		}			
	}
}
