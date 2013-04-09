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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class FileStreamUtils {

	public static String getFileExt(String srcPath) {
		int at = srcPath.lastIndexOf('.');
		if (at < 0)
			return null;
		return srcPath.substring(at + 1).toLowerCase();
	}

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
					InputStream is = url.openStream();
					if (is != null)
						return is;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		if (!srcPath.contains(":"))
			srcPath = "file:" + srcPath;
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
