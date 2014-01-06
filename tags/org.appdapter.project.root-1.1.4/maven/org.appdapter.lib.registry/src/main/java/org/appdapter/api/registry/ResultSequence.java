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
 * Maintains state of a particular query result stream from some finder.
 * 
 * @author Stu B. <www.texpedient.com>
 */
public class ResultSequence<OT> extends Object {
	
	public	Receiver<OT>	myReceiver;
	public	Finder<OT>		myFinder;
	public	Pattern			myPattern;
	// 0 = nothing delivered or delivering yet
	// 1 = delivering or delivered first result
	// 2 = delivering or delivered second result
	private long			mySeqNum = 0;
	
	private Receiver.Status	myLastStatus = Receiver.Status.SEEKING;
	
	public synchronized Receiver.Status deliverResult(OT obj) {
		return myReceiver.receiveMatch(obj, this, ++mySeqNum);
	}
	public synchronized long getLastResultCount() { 
		return mySeqNum;
	}
	
	public synchronized Receiver.Status getLastReceiverStatus() { 
		return myLastStatus;
	}
	
}
