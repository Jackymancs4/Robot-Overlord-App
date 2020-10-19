package com.marginallyclever.robotOverlord.entity;

/**
 * A convenience class for basic data types
 * @author Dan Royer
 * @since 1.6.0
 */
public class AbstractEntity<T> extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4725754267079958438L;
	
	// the data to store
	protected T t;

	protected AbstractEntity() {
		super();
		setName("AbstractEntity");
	}
	
	public AbstractEntity(String name) {
		super(name);
	}
	
	public AbstractEntity(String name,T t) {
		super(name);
    	this.t = t;
	}
	
	public AbstractEntity(T t) {
		super();
    	this.t = t;
	}
	
    public T get() {
    	return t;
    }
    
    public void set(T t) {
    	if( this.t==null || !this.t.equals(t) ) {
        	if(hasChanged()) return;
	    	setChanged();
	    	this.t = t;
	    	notifyObservers(t);
    	}
    }
	
	public void set(AbstractEntity<T> b) {
		super.setName(b.getName());
		
		set(b.get());
		
		parent = b.parent;
	}
}
