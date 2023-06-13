package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.swinginterface.UnicodeIcon;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentManagerPanel;
import com.marginallyclever.robotoverlord.swinginterface.edits.ComponentDeleteEdit;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Delete a {@link Component} from an {@link Entity}.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class ComponentDeleteAction extends AbstractAction {
	private final ComponentManagerPanel componentManagerPanel;
	private Component component;

	public ComponentDeleteAction(ComponentManagerPanel componentManagerPanel) {
		super(Translator.get("ComponentDeleteAction.name"));
		this.componentManagerPanel = componentManagerPanel;
		putValue(SMALL_ICON,new UnicodeIcon("🗑"));
		putValue(SHORT_DESCRIPTION, Translator.get("ComponentDeleteAction.shortDescription"));
	}

	public void setComponent(Component component) {
		this.component = component;
	}
	
    /**
     * select from a list of all object types.  An instance of that type is then added to the world.
     */
	@Override
	public void actionPerformed(ActionEvent event) {
		if(component==null) return;

		UndoSystem.addEvent(new ComponentDeleteEdit(componentManagerPanel,component.getEntity(),component));
    }
}
