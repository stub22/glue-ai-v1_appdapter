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
package org.appdapter.core.share;

/**
 * @author Logicmoo. <www.logicmoo.org>
 *
 * Handling for a local *or* some 'remote'/'shared' model/dataset impl.
 *
 */
public interface RemoteDatasetProviderSpec {
	public RemoteDatasetProvider getRemoteDatasetProvider();

	public String getProviderBase();
}