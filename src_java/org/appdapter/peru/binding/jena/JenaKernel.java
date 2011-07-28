/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.com).
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

package org.appdapter.peru.binding.jena;


import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Collections;

import org.appdapter.peru.core.handle.HandleDirectory;
import org.appdapter.peru.core.handle.Handle;
import org.appdapter.peru.core.name.Address;
import org.appdapter.peru.core.environment.Environment;

import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.Locator;


import com.hp.hpl.jena.assembler.AssemblerHelp;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.Dataset;

import com.hp.hpl.jena.assembler.Assembler;


/*
import net.peruser.module.projector.ProjectedNode;
import net.peruser.module.projector.Projector;
import net.peruser.module.projector.SimpleAxisQuery;
*/
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**   JenaKernel implements the core of the runtime relationship between Peruser and Jena.
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class JenaKernel extends Handle { //  implements Kernel {
	
	private static Log 		theLog = LogFactory.getLog(JenaKernel.class);	
	
	private static String	BOOT_CUTE = "_boot";
	private static String   ENV_HANDLE_DEF_CUTE = "JENA_DEFAULT_KERNEL_FOR_EHC_";
	private static String	ENV_HANDLE_DEF_PREFIX = "jena_kernel:";
	
	
	private FileManager			myFileManager;
	
	// This directory is entirely owned by the JenaKernel, and is NEVER the same as the "primary directory" for the runtime environment.
	private	HandleDirectory		myHandleDirectory;

	private	String				myBootModelURL;
	
	/** Construct a new kernel, configured with the contents of an RDF model loadable from bootModelURL.
		The bootModelURL may be null, but the JenaKernel returned is then useless without further java customization.<br/>
		If nonnull, the bootModel is read from this URL using the loadModel() method of a new global FileManager 
		(returned from FileManager.global()).
		<br/>Caveat:  This constructor also sets the singleton FileManager.globalFileManager to be the same as our new 
		file manager, which may	cause problems in multi-kernel JVMs if kernels do not customize FMs in the same way.  
		To be revisited.
	*/
	public JenaKernel(Environment env, String bootModelURL, String kernelCuteName, Address kernelPubAddress) throws Throwable {
		super(kernelCuteName, kernelPubAddress);

		myHandleDirectory = new HandleDirectory();
		
		// This call enures that ARQ assembler entities (datasets) are recognized in calls to  AssemblerHelp.findAssemblerRoots(m)
		com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils.init();
		
		// On reboot, this "new" file manager seems to still have cached files shared with an old one.
		myFileManager = FileManager.makeGlobal();
		
		myBootModelURL = bootModelURL;
		if (bootModelURL != null) {
			loadBootModel();
		} else {
			theLog.warn("No boot model URL supplied, JenaKernel is EMPTY!");
		}
		
		HandleDirectory		envPrimaryHD = env.getPrimaryHandleDirectory();
		
		// Really want atomic all-or-nothing semantics on these two operations as a single unit of work
		envPrimaryHD.attachHandle(this);
		FileManager.setGlobalFileManager(myFileManager);

	}
	
	/** Add a model to the Kernel's handle directory. <br/> The "cute" and "pub" names must both be unique within this kernel.  <br/>
		In simple setups, the pub Resource will always be in the boot model of this JenaKernel.
	*/
	protected ModelHandle attachModel(Model jmod, String cute, Resource pub, boolean supportsAssembly) throws Throwable {
		Address		address = new JenaAddress(pub);
		ModelHandle mh  = new ModelHandle(cute, address, jmod, supportsAssembly);
		myHandleDirectory.attachHandle(mh);
		return mh;
	}

		/** Add a dataset to the Kernel's registration lists. <br/> The "cute" and "pub" names must both be unique within this kernel.  <br/>
		In simple setups, the pub Resource will always be in the boot model of this JenaKernel.
	*/
	protected DatasetHandle attachDataset(Dataset jd, String cute, Resource pub) throws Throwable {
		Address		address = new JenaAddress(pub);
		DatasetHandle dh  = new DatasetHandle(cute, address, jd);
		myHandleDirectory.attachHandle(dh);
		return dh;
	}
	/** Assembles a dataset and attaches it to the directory.
		Assumes that the dataset has NOT been assembled already!
	 */
	 
	public Dataset assembleAndAttachDataset(String cuteName, String datasetDescURI) throws Throwable  {
		Resource desc = findAssemblyResourceForFullURI(datasetDescURI, false);
		// Mode.ANY allows the assember to reuse existing objects if possible, or create new ones where needed.
		DataSource ds = (DataSource) Assembler.general.open(desc); // , Mode.ANY);		
		attachDataset(ds, cuteName, desc);
		return ds;
	}
	
	/** Loads the "boot" model of the kernel.  No models may have already been appended to this kernel.  */
	protected void loadBootModel() throws Throwable {
		if (myHandleDirectory.getEntryCount() != 0) {
			throw new Exception("JenaKernel-loadBootModel is illegal when other handles are already loaded");
		}
		Model	bootModel = myFileManager.loadModel(myBootModelURL);
		String pubName = "local:BOOT_" + System.currentTimeMillis();
		Resource pubNameRes = bootModel.createResource(pubName);
		attachModel (bootModel, BOOT_CUTE, pubNameRes, true);
	}
	/**
	 * Searches for resources in kernel models which are bound to data such that they are recognizable to
	 * the Jena Assembler as "assemblable". 
	 */
	protected Resource findAssemblyResourceForFullURI(String fullURI, boolean allowNullResult) throws Throwable {
		if (myHandleDirectory.getEntryCount() == 0) {
			throw new Exception("JenaKernel-getAssemblyResourceForFullURI() is illegal when kernel is EMPTY, and this one is!");
		}
		ModelHandle bootModelHandle = (ModelHandle) myHandleDirectory.getHandleForCuteName(BOOT_CUTE);
		Resource r = bootModelHandle.getJenaModel().createResource(fullURI);
		Iterator modelHandleIterator = myHandleDirectory.iterateHandlesInClass(ModelHandle.class);
		while (modelHandleIterator.hasNext()) {
			ModelHandle	mh = (ModelHandle) modelHandleIterator.next();
			Set<Resource>	assemblySet = mh.getAssemblerRootSet();
			if (assemblySet.contains(r)) {
				return r;
			}
		}
		if (allowNullResult) {
			return null;
		} else {
			throw new Exception("JenaKernel-findAssemblyResourceForFullURI can't resolve " + fullURI);
		}
	}
	
	/**  Gets the "best" model for this URI or file path.
	 */
	 
	public Model getBestModelForLocation(String locationPathText) {
		// This will get fancier!
		return myFileManager.loadModel(locationPathText);
	}
 
	/** Prints information about the kernel state to the "debug" log level (via jakarta commons logging). */
	public void dumpDebug() {
		theLog.debug("[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[");
		long	startMsec =  System.currentTimeMillis();
		theLog.debug("Start Peruser-JenaKernelDump at " + startMsec + " msec = " + new java.util.Date());
		
		try {
			Iterator loci = myFileManager.locators();
			while (loci.hasNext()) {
				Locator l = (Locator) loci.next();
				theLog.debug("locator class: " + l.getClass().getName() + "  name: " + l.getName());
			}
			myHandleDirectory.dumpHandleList();
			
		} catch (Throwable t) {
			theLog.error("JenaKernel.dumpDebug() caught ", t);
		}
		
		long	endMsec =  System.currentTimeMillis();
		theLog.debug("End Peruser-JenaKernelDump at " + endMsec + " msec,  elapsed " + (endMsec-startMsec) + " msec"); 
		theLog.debug("]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]");
	}

	/**  If no JenaKernel exists in the environment, an empty one is created and returned. 
	  *
	  */
	public static JenaKernel getDefaultKernel(Environment env) throws Throwable {
		JenaKernel defKernel = null;
		if (env == null) {
			throw new Exception("Environment passed to getDefaultKernel is NULL!");
		}
		HandleDirectory		envPrimaryHD = env.getPrimaryHandleDirectory();
		List kernelHandleList = envPrimaryHD.listHandlesInClass(JenaKernel.class);
		if (kernelHandleList.size() > 0) {
			defKernel = (JenaKernel) kernelHandleList.get(0);
		}
		if (defKernel == null) {
			String cuteName =  ENV_HANDLE_DEF_CUTE + env.hashCode();
			String pubName =  ENV_HANDLE_DEF_PREFIX + cuteName;
			
			Address pubAddress = new JenaAddress(pubName);
			// The constructor is responsible for adding the new kernel into the environment's primaryHandleDirectory.
			defKernel = new JenaKernel(env, null, cuteName, pubAddress);
		}
		return defKernel;
	}
	
	/** This inner class represents the state of a single "attached" model in the kernel. 
	  */
	protected class ModelHandle extends Handle {
		private		Model		myJenaModel;
		private		boolean		mySupportsAssemblyFlag;
		// public		long		lastReadMsec;
		// public		long		lastWriteMsec;
		
		public ModelHandle (String cuteName, Address address, Model m, boolean supportsAssembly) {
			super(cuteName, address);
			myJenaModel = m;
			mySupportsAssemblyFlag = supportsAssembly;
		}
		public Model getJenaModel() {
			return myJenaModel;
		}
		public boolean supportsAssembly() {
			return mySupportsAssemblyFlag;
		}
		public void dumpDebug() throws Throwable {
			theLog.debug("{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{");
			theLog.debug("Handle type= ModelHandle");
			theLog.debug("cuteLocalName= " + getCuteName());
			theLog.debug("publishedAddresses= " + getAddress());
			theLog.debug("jenaModel= " + getJenaModel());
			theLog.debug("supportsAssembly= " + supportsAssembly());
			if (supportsAssembly()) {
				theLog.debug("assemblerRoot set:");
				Set<Resource> assemblerRootSet = getAssemblerRootSet();
				for (Resource ar : assemblerRootSet) {
					theLog.debug("    " + ar.toString());
				}
			} else {
				theLog.debug("This model handle does NOT support assembly!");
			}
			theLog.debug("}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}");
		}
		public Set<Resource> getAssemblerRootSet() throws Throwable {
			if (supportsAssembly()) {
				return AssemblerHelp.findAssemblerRoots(getJenaModel());
			} else {
				return Collections.EMPTY_SET;
			}
		}
	}
	protected class DatasetHandle extends Handle {
		private		Dataset		myJenaDataset;
		public DatasetHandle (String cuteName, Address address, Dataset d) {
			super(cuteName, address);
			myJenaDataset = d;
		}
		public Dataset getJenaDataset() {
			return myJenaDataset;
		}
		public void dumpDebug() throws Throwable {
			theLog.debug("{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{");
			theLog.debug("Handle type= DatasetHandle");
			theLog.debug("cuteLocalName= " + getCuteName());
			theLog.debug("publishedAddresses= " + getAddress());
			theLog.debug("dataset = " + getJenaDataset());
			Iterator dnit = getJenaDataset().listNames();
			while (dnit.hasNext()) {
				String	modelName = (String) dnit.next();
				theLog.debug("       model name: " + modelName);
			}	
			theLog.debug("}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}");
		}
		
	}
}

