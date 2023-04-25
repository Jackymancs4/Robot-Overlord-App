package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class DoubleParameter extends AbstractParameter<Double> {

	public DoubleParameter(String s) {
		super(0.0);
		setName(s);
	}
	
	public DoubleParameter(String s, double d) {
		super(d);
		setName(s);
	}

	public DoubleParameter(String s, float d) {
		super((double)d);
		setName(s);
	}
	
	public DoubleParameter(String s, int d) {
		super((double)d);
		setName(s);
	}
	
	@Override
	public String toString() {
		return getName()+"="+StringHelper.formatDouble(t);
	}
	
	@Override
	public void getView(ComponentPanelFactory view) {
		view.add(this);
	}

	@Override
	public JSONObject toJSON() {
		JSONObject jo = super.toJSON();
		jo.put("value",get());
		return jo;
	}

	@Override
	public void parseJSON(JSONObject jo) throws JSONException {
		super.parseJSON(jo);
		set(jo.getDouble("value"));
	}
}