package org.appdapter.gui.util;

public interface HRKRefinement extends Ontologized {
	public static interface AskIfEqual {
		public boolean same(Object obj);
	}

	public static interface DontAdd extends OntoPriority {
	}

	public static interface UseLast extends OntoPriority {
	}

	public static interface UseFirst extends OntoPriority {
	}
}
