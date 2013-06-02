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

/**
 * @author Stu B. <www.texpedient.com>
 */
// / Dmiles needed something in java to cover Dmiles's Scala blindspots
public interface AnyOper {

    @Retention(RetentionPolicy.RUNTIME)
    static public @interface UISalient {
    }

    @Retention(RetentionPolicy.RUNTIME)
    static public @interface SalientVoidCall {
        /**
         *  "" = use the splitted of camelcase for methodname
         * @return 
         */
        public String Named() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    static public @interface ToStringResult {
    }

    @Retention(RetentionPolicy.RUNTIME)
    static public @interface Named {
        /**
         *  "" = use the splitted of camelcase for methodname
         * @return 
         */
        public String MenuName() default ""; 
    }
}
