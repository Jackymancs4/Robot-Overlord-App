package com.marginallyclever.robotOverlord.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.Translator;
import com.marginallyclever.robotOverlord.dhRobot.DHRobot;
import com.marginallyclever.robotOverlord.dhRobot.DHTool;
import com.marginallyclever.robotOverlord.entity.Entity;

/**
 * An undoable action to add an {@link Entity} to the world.
 * @author Dan Royer
 *
 */
public class UndoableActionSetDHTool extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DHRobot robot;
	private DHTool newTool;
	private DHTool previousTool;
	
	public UndoableActionSetDHTool(DHRobot robot,DHTool newTool) {
		this.robot = robot;
		this.newTool = newTool;
		this.previousTool = robot.getCurrentTool();
		addNow();
	}
	
	@Override
	public boolean canRedo() {
		return true;
	}

	@Override
	public boolean canUndo() {
		return true;
	}

	@Override
	public String getPresentationName() {
		return Translator.get("Set tool ")+newTool.getDisplayName();
	}


	@Override
	public String getRedoPresentationName() {
		return Translator.get("Redo ") + getPresentationName();
	}

	@Override
	public String getUndoPresentationName() {
		return Translator.get("Undo ") + getPresentationName();
	}

	@Override
	public void redo() throws CannotRedoException {
		addNow();
	}

	@Override
	public void undo() throws CannotUndoException {
		removeNow();
	}

	private void addNow() {
		robot.removeTool();
		robot.addTool(newTool);
	}
	
	private void removeNow() {
		robot.removeTool();
		robot.addTool(previousTool);
	}
}