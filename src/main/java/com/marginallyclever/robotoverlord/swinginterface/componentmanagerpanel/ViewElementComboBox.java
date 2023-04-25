package com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel;

import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.ComboBoxEdit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.undo.AbstractUndoableEdit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ViewElementComboBox extends ViewElement implements ActionListener, PropertyChangeListener {
	private final JComboBox<String> field;
	private final IntParameter parameter;
	
	public ViewElementComboBox(IntParameter parameter, String [] listOptions) {
		super();
		this.parameter = parameter;
		
		parameter.addPropertyChangeListener(this);
		
		field = new JComboBox<>(listOptions);
		field.setSelectedIndex(parameter.get());
		field.addActionListener(this);
		field.addFocusListener(this);

		JLabel label=new JLabel(parameter.getName(),JLabel.LEADING);
		label.setLabelFor(field);

		this.setLayout(new BorderLayout());
		this.setBorder(new EmptyBorder(0,0,0,1));
		this.add(label,BorderLayout.LINE_START);
		this.add(field,BorderLayout.LINE_END);
	}
	
	public String getValue() {
		return field.getItemAt(parameter.get());
	}

	/**
	 * I have changed.  poke the entity
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		int newIndex = field.getSelectedIndex();
		if(newIndex != parameter.get()) {
			AbstractUndoableEdit event = new ComboBoxEdit(parameter, parameter.getName(), newIndex);
			UndoSystem.addEvent(this,event);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		field.setSelectedIndex((Integer)evt.getNewValue());
	}

	@Override
	public void setReadOnly(boolean arg0) {
		field.setEnabled(!arg0);
	}
}