package com.marginallyclever.convenience;

public class Units {
	public double to_inch(double x) {	return x/25.4;	}
	public double to_mm  (double x) {	return x*25.4;	}
	
	public double to_color  (double x) {	return x*255;	}
	public double from_color(double x) {	return x/255;	}
}
