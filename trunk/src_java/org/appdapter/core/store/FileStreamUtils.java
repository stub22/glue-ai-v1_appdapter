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

package org.appdapter.core.store;

import org.appdapter.bind.rdf.jena.query.JenaArqQueryFuncs;
import org.appdapter.bind.rdf.jena.query.JenaArqResultSetProcessor;

import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.core.name.Ident;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class FileStreamUtils {

	public static InputStream openInputStream(String srcPath,
			java.util.List<ClassLoader> cls) {
	
		File file = new File(srcPath);
		if (file.exists()) {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		for (Iterator iterator = cls.iterator(); iterator.hasNext();) {
			ClassLoader classLoader = (ClassLoader) iterator.next();
			URL url = classLoader.getResource(srcPath);
			if (url != null)
				try {
					return url.openStream();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		}
		if (!srcPath.contains(":")) srcPath = "file:" + srcPath;
		try {
			return new URL(srcPath).openStream();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
