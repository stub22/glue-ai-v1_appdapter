package org.appdapter.gui.util;

public class ClassNotFoundExceptionF extends ClassNotFoundException {

	public ClassNotFoundExceptionF(String m, Throwable e) {
		super(m, e);
	}

	public ClassNotFoundExceptionF(String m) {
		super(m);
	}

	@Override public String getMessage() {
		String m = super.getMessage();
		if (m != null)
			return "Felix: " + m;
		return null;
	}

}
