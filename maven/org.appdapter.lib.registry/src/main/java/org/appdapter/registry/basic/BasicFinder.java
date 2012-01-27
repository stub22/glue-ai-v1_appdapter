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
package org.appdapter.registry.basic;

import java.util.ArrayList;
import java.util.List;
import org.appdapter.api.registry.Pattern;
import org.appdapter.api.registry.Receiver;
import org.appdapter.api.registry.Receiver.Status;
import org.appdapter.api.registry.ResultSequence;
import org.appdapter.api.registry.SimpleFinder;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BasicFinder<OT> implements SimpleFinder<OT> {
	private BasicRegistry	myBasicRegistry;
	private	Class<OT>		myObjClz;
	
	public BasicFinder(BasicRegistry br, Class<OT> otClz) {
		myBasicRegistry = br;
		myObjClz = otClz;
	}
	protected ResultSequence<OT> makeResultSequence(Pattern p, Receiver<OT> r) {
		ResultSequence<OT> rseq = new ResultSequence<OT>();
		rseq.myFinder = this;
		rseq.myPattern = p;
		rseq.myReceiver = r;
		return rseq;
	}
	
	@Override public ResultSequence<OT> deliverMatchesUntilDone(Pattern p, Receiver<OT> r) {
		ResultSequence<OT> rseq = makeResultSequence(p, r);
		List<OT> brutishMatches = myBasicRegistry.brutishlyCollectAllMatches(myObjClz, p);
		Receiver.Status stat = Receiver.Status.SEEKING;
		for (OT obj : brutishMatches) {
			stat = rseq.deliverResult(obj);
			if (stat.equals(Receiver.Status.DONE)) {
				break;
			}
		}
		return rseq;
	}

	protected List<OT> collectMatches(Pattern p) {
		final List<OT> matches = new ArrayList<OT>();
		Receiver<OT> collector = new Receiver<OT>() {
			@Override public Status receiveMatch(OT match, ResultSequence<OT> seq, long seqIndex) {
				matches.add(match);
				return Status.SEEKING;
			}
			
		};
		deliverMatchesUntilDone(p, collector);
		return matches;
	}
	@Override public OT findFirstMatch(Pattern p, int minAllowed, int maxAllowed) throws Exception {
		// TODO:  Do not need to iterate further than maxAllowed, which is usually not higher than 2.
		// TODO:  Sanity checks on minAllowed + maxAllowed args.
		List<OT> matches = collectMatches(p);
		int matchCount = matches.size();
		if ((matchCount >= minAllowed) && (matchCount <= maxAllowed)) {
			return (matchCount > 0) ?  matches.get(0) : null;
		} else {
			throw new Exception("Expected between " + minAllowed + " and " + maxAllowed + " matches for " + p + ", but got " + matchCount);
		}
	}


	@Override public List<OT> findAllMatches(Pattern p) {
		List<OT> matches = collectMatches(p);
		return matches;
	}

	@Override public long countMatches(Pattern p) {
		// TODO:  this can be done more efficiently by delivering to a null receiver (for starters!)
		List<OT> matches = collectMatches(p);
		return matches.size();
	}

	@Override public void killDeliverySequence(ResultSequence resultSeq) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
}
