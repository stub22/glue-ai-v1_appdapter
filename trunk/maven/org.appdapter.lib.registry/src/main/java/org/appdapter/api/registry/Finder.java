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

package org.appdapter.api.registry;


/**
 * @author Stu B. <www.texpedient.com>
 * A finder must make fair delivery on all current sequences
 */
public interface Finder<OC> {
	// TODO: Add bounds estimation 
	// public  MatchEstimateBounds	estimateMatchCount(Pattern<OC> p);
	
	// This thread will block, returning ResultSequence only after exhausting
	// finder's index, or after Receiver returns Status=DONE.
	public ResultSequence deliverMatchesUntilDone(Pattern p, Receiver<OC> r);
	
	public void killDeliverySequence(ResultSequence resultSeq);
}
