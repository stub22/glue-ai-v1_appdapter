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
package org.appdapter.api.trigger;



import org.appdapter.core.component.KnownComponent;
import org.appdapter.core.debug.UIAnnotations;
import org.appdapter.core.name.Ident;

/**
 * @author Stu B. <www.texpedient.com>
 */
// / Dmiles needed something in java to cover Dmiles's Scala blindspots
public interface AnyOper extends UIAnnotations {


	@UISalient(ApplyToClass = "HASIDENT")
	static public interface HasIdent extends ApplyToClassInterfaces {
		@UISalient(MenuName = "Show Ident", ToValueMethod = "toString") Ident getIdent();

		Class HASIDENT = KnownComponent.class;
	}

}
