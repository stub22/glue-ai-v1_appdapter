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
package org.appdapter.gui.assembly;
import org.appdapter.core.item.Ident;
import org.appdapter.core.item.Item;
import org.appdapter.core.item.Item.SortKey;
import org.appdapter.core.item.JenaResourceItem;
import org.appdapter.core.item.ModelIdent;
import org.appdapter.gui.box.KnownComponent;
import org.appdapter.gui.box.MutableKnownComponent;
import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class ComponentCache<MKC extends MutableKnownComponent>  {
	static Logger theLogger = LoggerFactory.getLogger(ComponentCache.class);
	private	Map<Ident, MKC> myCompCache = new HashMap<Ident, MKC>();	
	protected MKC getCachedComponent(Ident id) {
		return myCompCache.get(id);
	}
	protected void putCachedComponent(Ident id, MKC comp) {
		myCompCache.put(id, comp);
	}	
}
