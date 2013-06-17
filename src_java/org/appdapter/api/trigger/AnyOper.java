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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.appdapter.core.component.KnownComponent;

/**
 * @author Stu B. <www.texpedient.com>
 */
// / Dmiles needed something in java to cover Dmiles's Scala blindspots
public interface AnyOper {

	/**
		 * @author Administrator
		 *
		 */
	@UISalient
	public static interface Reloadable {

		void reload();

	}

	/**
		 * @author Administrator
		 *
		 */
	@Retention(RetentionPolicy.RUNTIME)
	public @interface UIHidden {

	}

	@Retention(RetentionPolicy.RUNTIME)
	static public @interface UISalient {
		/**
		 * "" == do nothing to the result 
		 * "toString" .. call the toString method on Result
		 * Before return the result call the method name
		 * @return 
		 */
		public String ToValueMethod() default "";

		/**
		 *  "" = use the splitted of camelcase for Menu Item Name
		 * @return 
		 */
		public String MenuName() default "";

		/**
		 *  true if the last argument is the dropped/pasted item
		 * @return 
		 */
		public boolean PasteDropTarget() default false;

		/**
		 *  "" = use the splitted of camelcase for methodname
		 * @return 
		 */
		public String CastingMethod() default "";

		/**
		 *  true if the first argument into target method will be the menuSourceItem
		 * @return 
		 */
		public boolean TreatLikeStatic() default false;

		/**
		 *  "" = use the splitted of camelcase for methodname
		 * @return 
		 */
		public String ApplyToClass() default "";
	}

	static interface ApplyToClassInterfaces {

	}

	@UISalient(ApplyToClass = "HASIDENT")
	static public interface HasIdent extends ApplyToClassInterfaces {
		@UISalient(MenuName = "Show Ident", ToValueMethod = "toString") void getIdent();

		Class HASIDENT = KnownComponent.class;
	}
}
