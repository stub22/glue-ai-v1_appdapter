package org.appdapter.core.jvm;


public interface CallableWithParameters<P, R> {

	R call(P box, Object... moreparams);

}
