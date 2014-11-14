/*
 *  Copyright 2014 by The Appdapter Project (www.appdapter.org).
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.appdapter.bind.rdf.jena.model;
import java.io.IOException;
import java.io.Serializable;
import org.appdapter.core.item.JenaResourceItem;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

import org.appdapter.core.name.SerIdent;

/**
 * @author Stu B. <www.texpedient.com>
 * http://www.jguru.com/faq/view.jsp?EID=251942
 *  The only requirement on the constructor for a class that implements Serializable is that the first 
 * non-serializable superclass in its inheritence hierarchy must have a no-argument constructor
 */

public class SerialJenaResItem extends JenaResourceItem implements SerIdent {
	
	private	String	myAbsUri;
	
	public SerialJenaResItem(Resource r) {
		super(r);
		myAbsUri = r.getURI();
	}
	@Override protected JenaResourceItem makeItemOfMyJClazz(Resource res) {
		SerialJenaResItem noob = new SerialJenaResItem(res);
		return noob;
	}		
	@Override public Resource getJenaResource() {
		Resource r = super.getJenaResource();
		if (r == null) {
			if (myAbsUri == null) {
				throw new RuntimeException("super.getJenaResource() and myAbsUri are both null!");
			}
			// This resource will return null from getModel()
			r = ResourceFactory.createResource(myAbsUri);
			setJenaResource(r);
		}
		return r;
	}
	
	@Override public String getAbsUriString() {
		if (myAbsUri != null) {
			return myAbsUri;
		} else {
			return super.getAbsUriString();
		}
	}
	@Override protected void setJenaResource(Resource r) {
		super.setJenaResource(r);
		myAbsUri = r.getURI();
	}
}
	/*
    private void writeObject(ObjectOutputStream output)
            throws IOException {
		if (myResource != null) {
			
		} 
	//	if (myAbsUri == null)
//		String absUri = getAbsUriString();
       // stream.writeObject(absUri);
    }

    private void readObject(ObjectInputStream input)
            throws IOException, ClassNotFoundException {
		input.defaultReadObject();
		
		// Options:  
		// 1) Use a "registered" Model, from wherever.  
		// Use ResourceFactory.
    }
	*/
