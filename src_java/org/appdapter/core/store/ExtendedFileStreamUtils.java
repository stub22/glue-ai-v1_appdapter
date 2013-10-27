/*
 *  Copyright 2013 by The Appdapter Project (www.appdapter.org).
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

package org.appdapter.core.store;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import org.appdapter.fileconv.FileStreamUtils;

import org.appdapter.api.trigger.AnyOper.UISalient;
import org.appdapter.bind.rdf.jena.model.JenaFileManagerUtils;
import org.appdapter.core.boot.ClassLoaderUtils;
import org.appdapter.core.log.Debuggable;
import static org.appdapter.fileconv.FileStreamUtils.getModelIfAvailable;


/**
 * @author Stu B. <www.texpedient.com>
 */

public class ExtendedFileStreamUtils extends FileStreamUtils {
	@UISalient
	
	public static Model getModelIfAvailable(String sheetLocation, String sheetName, java.util.Map nsMap, java.util.List<ClassLoader> fileModelCLs) {
		FileManager fm = JenaFileManagerUtils.getDefaultJenaFM();

		for (ClassLoader cl : fileModelCLs)
			fm.addLocatorClassLoader(cl);

		Model m = getModelIfAvailable(sheetLocation + sheetName, fm);
		if (m != null)
			return m;
		m = getModelIfAvailable(sheetName, fm);
		if (m != null)
			return m;
		try {
			return fm.loadModel(sheetName, sheetLocation);
		} catch (Exception e) {
			return null;
		}
	}	
	@Override public InputStream openInputStream(String srcPath, java.util.List<ClassLoader> cls) throws IOException {

		if (cls == null) {
			cls = ClassLoaderUtils.getCurrentClassLoaderList();
		}
		if (srcPath == null)
			throw new MalformedURLException("URL = NULL");
		IOException ioe = null;
		File file = new File(srcPath);
		if (file.exists()) {
			try {
				return new FileInputStream(file);
			} catch (IOException io) {
				// It existed so this might be legit
				ioe = io;
			}
		}
		if (srcPath.contains(":")) {
			try {
				return new URL(srcPath).openStream();
			} catch (MalformedURLException maf) {
				if (ioe == null)
					ioe = maf;
			} catch (IOException e) {
				ioe = e;
			}
		}
		for (Iterator iterator = cls.iterator(); iterator.hasNext();) {
			ClassLoader classLoader = (ClassLoader) iterator.next();
			InputStream is = null;
			URL url = classLoader.getResource(srcPath);
			if (url != null) {
				try {
					is = url.openStream();
				} catch (IOException e) {
					ioe = e;
				}
			} else {
				is = classLoader.getResourceAsStream(srcPath);
			}
			if (is != null)
				return is;
		}

		if (!srcPath.contains(":")) {
			InputStream is = ClassLoader.getSystemResourceAsStream(srcPath);
			if (is != null)
				return is;
			srcPath = "file:" + srcPath;
			try {
				return new URL(srcPath).openStream();
			} catch (MalformedURLException maf) {
				if (ioe == null)
					ioe = maf;
			} catch (IOException e) {
				ioe = e;
			}
			if (ioe != null) {
				throw ioe;
			}
		}

		return null;
	}
	@Override public InputStream openInputStreamOrNull(String srcPath, java.util.List<ClassLoader> cls) {
		try {
			return openInputStream(srcPath, cls);
		} catch (Throwable e) {
			getLogger().error("Bad srcPath={}", srcPath, e);
			return null;
		}
	}	
}
