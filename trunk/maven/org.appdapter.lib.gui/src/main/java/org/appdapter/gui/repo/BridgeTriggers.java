/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.gui.repo;

import java.net.URL;
import java.util.*;
import java.util.Set;

import org.appdapter.api.trigger.AnyOper.UISalient;
import org.appdapter.api.trigger.AnyOper.UtilClass;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.api.trigger.TriggerImpl;
import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.bind.rdf.jena.model.JenaFileManagerUtils;
import org.appdapter.core.convert.NoSuchConversionException;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.store.Repo;
import org.appdapter.demo.DemoResources;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.trigger.TriggerForClass;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BridgeTriggers implements UtilClass {

	@UISalient public static List<Model> getModelsFoundIn(Repo repo) {
		return getModelsFoundIn(repo.getMainQueryDataset());
	}

	@UISalient public static List<Model> getModelsFoundIn(Dataset mainQueryDataset) {
		ArrayList<Model> models = new ArrayList<Model>();
		for (String s : ReflectUtils.toList(mainQueryDataset.listNames())) {
			models.add(mainQueryDataset.getNamedModel(s));
		}
		return models;
	}

	public static class MountSubmenuFromTriplesTrigger<BT extends Box<TriggerImpl<BT>>> extends TriggerImpl<BT> implements TriggerForClass {

		@UISalient(MenuName = "triplesURLParam")
		public static Class<URL> boxTargetClass = URL.class;

		@Override public boolean appliesTarget(Class cls, Object anyObject) {
			return ReflectUtils.convertsTo(anyObject, cls, boxTargetClass);
		}

		@Override public Trigger createTrigger(String menuFmt, DisplayContext ctx, Object poj) {
			try {
				return new MountSubmenuFromTriplesTrigger(ReflectUtils.recast(poj, boxTargetClass));
			} catch (NoSuchConversionException e) {
				throw Debuggable.reThrowable(e);
			}
		}

		String triplesURL;

		public MountSubmenuFromTriplesTrigger(URL triplesURLParam) {
			triplesURL = triplesURLParam.toExternalForm();
		}

		public MountSubmenuFromTriplesTrigger(String triplesURLParam) {
			triplesURL = triplesURLParam;
		}

		@Override public void fire(BT targetBox) {
			logInfo(toString() + ".fire()");
			BoxContext bc = targetBox.getBoxContext();
			JenaFileManagerUtils.ensureClassLoaderRegisteredWithDefaultJenaFM(DemoResources.class.getClassLoader());
			logInfo("Loading triples from URL: " + triplesURL);
			try {
				Set<Object> loadedStuff = AssemblerUtils.buildAllObjectsInRdfFile(triplesURL);
				logInfo("Loaded " + loadedStuff.size() + " objects");
				for (Object o : loadedStuff) {
					if (o instanceof MutableBox) {
						MutableBox loadedMutableBox = (MutableBox) o;
						bc.contextualizeAndAttachChildBox(targetBox, loadedMutableBox);
						logInfo("Loaded mutable box: " + loadedMutableBox);
					} else {
						logInfo("Loaded object which is not a mutable box: " + o);
					}
				}
			} catch (Exception e) {

			}
		}

		@Override public Object getIdentityObject() {
			return (getClass() + ":" + triplesURL).intern();
		}
	}
}
