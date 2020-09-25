package com.marginallyclever.robotOverlord.entity.basicDataTypes;

import com.marginallyclever.robotOverlord.entity.AbstractEntity;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class StringEntity extends AbstractEntity<String> {
	public StringEntity() {
		super("","");
	}
	
	// ambiguous solution is to do both!
	public StringEntity(String t) {
		super();
		setName(t);
    	this.t = t;
	}
	
	public StringEntity(String name, String value) {
		super();
		setName(name);
		t=value;
	}

	public String toString() {
		return getName()+"="+t.toString();
	}
}
