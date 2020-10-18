package com.marginallyclever.robotOverlord.swingInterface.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.RemovableEntity;
import com.marginallyclever.robotOverlord.swingInterface.translator.Translator;

/**
 * An undoable action to remove an {@link Entity} from the world.
 * @author Dan Royer
 *
 */
public class ActionEntityRemove extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Entity entity;
	private Entity parent;
	private RobotOverlord ro;
	
	public ActionEntityRemove(RobotOverlord ro,Entity entity) {
		super();
		
		this.entity = entity;
		this.ro = ro;
		this.parent = entity.getParent();

		doIt();
	}
	
	@Override
	public String getPresentationName() {
		return Translator.get("Remove ")+entity.getName();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		doIt();
	}
	
	protected void doIt() {
		if(entity instanceof RemovableEntity) {
			((RemovableEntity)entity).beingRemoved();
		}
		if(parent!=null) parent.removeChild(entity);
		ro.updateEntityTree();
		ro.pickEntity(null);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if(parent!=null) parent.addChild(entity);
		ro.updateEntityTree();
		ro.pickEntity(entity);
	}
}
