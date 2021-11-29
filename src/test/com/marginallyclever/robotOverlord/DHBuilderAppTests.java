package com.marginallyclever.robotOverlord;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import com.marginallyclever.robotOverlord.dhRobotEntity.DHBuilderApp;
import com.marginallyclever.robotOverlord.dhRobotEntity.DHLink;

@Deprecated
@Ignore
public class DHBuilderAppTests {
	final static String TEST_FOLDER = "src\\\\main\\resources\\Sixi";
	@Test
	public void testSave() throws IOException {
		DHBuilderApp app = new DHBuilderApp();
		app.saveToFolder(new File(TEST_FOLDER));
	}

	@Test
	public void testLoad() throws IOException {
		DHBuilderApp app = new DHBuilderApp();
		app.loadFromFolder(new File(TEST_FOLDER));
	}

	@Test
	public void testSaveAndLoad() throws IOException {
		try {
			for(int j=0;j<50;++j) {
				DHBuilderApp app1 = new DHBuilderApp();
				for( int i=0;i< app1.getNumLinks();++i) {
					DHLink bone = app1.getLink(i);
					bone.d    .set(Math.random()*360.0-180.0);
					bone.theta.set(Math.random()*360.0-180.0);
					bone.r    .set(Math.random()*360.0-180.0);
					bone.alpha.set(Math.random()*360.0-180.0);
				}
				app1.saveToFolder(new File(TEST_FOLDER));
				
				DHBuilderApp app2 = new DHBuilderApp();
				app2.loadFromFolder(new File(TEST_FOLDER));
				assert(app2.getNumLinks()==app1.getNumLinks());
				Double d1,d2;
				for(int i=0;i<app1.getNumLinks();++i) {
					DHLink bone1 = app1.getLink(i);
					DHLink bone2 = app2.getLink(i);
					
					d1 = (bone1.d.get()); 
					d2 = (bone2.d.get()); 
					assert(Math.abs(d1-d2)<1e-6);
					d1 = (bone1.theta.get()); 
					d2 = (bone2.theta.get()); 
					assert(Math.abs(d1-d2)<1e-6);
					d1 = (bone1.r.get()); 
					d2 = (bone2.r.get()); 
					assert(Math.abs(d1-d2)<1e-6);
					d1 = (bone1.alpha.get()); 
					d2 = (bone2.alpha.get()); 
					assert(Math.abs(d1-d2)<1e-6);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@After
	public void cleanup() {
		File f = new File(TEST_FOLDER);
		f.delete();
	}
}
