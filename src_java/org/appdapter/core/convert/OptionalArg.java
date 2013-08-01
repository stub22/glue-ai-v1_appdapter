package org.appdapter.core.convert;

public interface OptionalArg {

	OptionalArg NONE = new OptionalArg() {

		public String toString() {
			return "OptionalArg.NONE";
		}

		@Override public void reset() {
		}

		@Override public Object getArg(Class pt) throws NoSuchConversionException {
			if (true)
				throw new NoSuchConversionException("no extra args of type " + pt);
			return null;
		}
	};

	OptionalArg JUST_NULLS = new OptionalArg() {

		public String toString() {
			return "OptionalArg.JUST_NULLS";
		}

		@Override public void reset() {
		}

		@Override public Object getArg(Class pt) throws NoSuchConversionException {
			return null;
		}
	};

	Object getArg(Class pt) throws NoSuchConversionException;

	void reset();
}
