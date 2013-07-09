package org.appdapter.gui.api;

public interface Ontologized {
	public interface UIProvider {

	}

	public interface OntoPriority extends Ontologized {
	}

	public interface HRKRefinement extends Ontologized {
	}

	public interface HRKAdded extends Ontologized {

	}

	public interface NamedClassObservable extends Ontologized {

	}

	public interface NamedClassService extends Ontologized {
	}

	public interface NamedClassValue extends Ontologized {

	}

	public interface UserInputComponent {

	}

	public interface NamedClassServiceFactory extends NamedClassService {

	}

	public interface LegacyClass extends Ontologized {

	}

	public interface UseLast extends OntoPriority {
	}

	public interface UseFirst extends OntoPriority {
	}

	public interface DontAdd extends OntoPriority {
	}

	public interface AskIfEqual {
		public boolean same(Object obj);
	}

}
