/*
 *  Copyright 2012 by The Appdapter Project (www.appdapter.org).
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
package org.appdapter.registry.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.appdapter.api.registry.Description;
import org.appdapter.api.registry.Finder;
import org.appdapter.api.registry.Pattern;
import org.appdapter.api.registry.SimpleFinder;
import org.appdapter.api.registry.VerySimpleRegistry;
import org.appdapter.core.log.BasicDebugger;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BasicRegistry extends BasicDebugger implements VerySimpleRegistry {
	private	Map<Description, Object> myObjectsByDesc;
	
	public BasicRegistry() {
		myObjectsByDesc = new HashMap<Description, Object>();
	}

	@Override public void registerObject(Object o, Description d) {
		myObjectsByDesc.put(d, o);
	}


	@Override public <OT> Finder<OT> getFinder(Class<OT> objClaz) {
		return new BasicFinder(this, objClaz);
	}
	protected <OT> BasicFinder<OT> getBasicFinder(Class<OT> objClaz) {
		Finder<OT> f = getFinder(objClaz);
		return (BasicFinder<OT>) f;
	}


	@Override public <OT> OT findRequiredUniqueMatch(Class<OT> objClaz, Pattern p) throws Exception {
		BasicFinder<OT> bf = getBasicFinder(objClaz);
		return bf.findFirstMatch(p, 1, 1);
	}
	@Override public <OT> OT findOptionalUniqueMatch(Class<OT> objClaz, Pattern p) throws Exception {
		BasicFinder<OT> bf = getBasicFinder(objClaz);
		return bf.findFirstMatch(p, 0, 1);		
	}

	@Override public <OT> OT findOptionalFirstMatch(Class<OT> objClaz, Pattern p) {
		BasicFinder<OT> bf = getBasicFinder(objClaz);
		try { 
			return bf.findFirstMatch(p, 0, SimpleFinder.MAX_MATCHES);
		} catch (Throwable t) {
			t.printStackTrace();
			throw new RuntimeException("Got unexpected exception of type " + t.getClass());
		}
	}

	@Override public <OT> List<OT> findAllMatches(Class<OT> objClaz, Pattern p, int minAllowed, int maxAllowed)
			throws Exception {
		BasicFinder<OT> bf = getBasicFinder(objClaz);
		return bf.findAllMatches(p, minAllowed, maxAllowed);
	}
	protected <OT> List<OT> brutishlyCollectAllMatches (Class<OT> objClz, Pattern p) {
		List<OT> resultList = new ArrayList<OT>();
		if (p instanceof BasicPattern) {
			// Use optimized search built into the map
			BasicPattern bp = (BasicPattern) p;
			Description bpd = bp.getDescription();
			Object candidate = myObjectsByDesc.get(bpd);
			if (candidate != null) {
				if (objClz.isInstance(candidate)) {
					resultList.add((OT) candidate);
				} else {
					// TODO : print warning if so configured
				}
			}
		} else {
			throw new UnsupportedOperationException("Cannot use non-BasicPattern " + p + " with BasicRegistry.");
		}
		return resultList;
	}
	public void registerNamedObject(Object o, String objName) {
		BasicDescription bd = new BasicDescription(objName);
		registerObject(o, bd);
	}
	@Override public <OT> OT findOptionalUniqueNamedObject(Class<OT> objClaz, String objName) throws Exception {
		BasicDescription bd = new BasicDescription(objName);
		Pattern p = new BasicPattern(bd);
		return findOptionalUniqueMatch(objClaz, p);
	}

	@Override public <OT> OT findRequiredUniqueNamedObject(Class<OT> objClaz, String objName) throws Exception {
		BasicDescription bd = new BasicDescription(objName);
		Pattern p = new BasicPattern(bd);
		return findRequiredUniqueMatch(objClaz, p);
	}

}
