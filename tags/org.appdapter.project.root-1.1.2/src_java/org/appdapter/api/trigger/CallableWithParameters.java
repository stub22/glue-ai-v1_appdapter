package org.appdapter.api.trigger;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface CallableWithParameters<P, R> {

	R call(P box, Object... moreparams);

}