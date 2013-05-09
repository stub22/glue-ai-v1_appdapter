package org.appdapter.gui.util;

public interface Ontologized {

	public interface OntoPriority extends Ontologized {

	}

	public interface HRKAdded extends Ontologized {

	}

	static public interface NamedClassObservable extends Ontologized {

	}

	public interface NamedClassService extends Ontologized {
	}

	public interface NamedClassValue extends Ontologized {

	}

	public interface NamedClassServiceFactory extends NamedClassService {

	}

	public interface LegacyClass extends Ontologized {

	}

}
