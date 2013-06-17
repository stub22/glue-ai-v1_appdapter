package org.appdapter.api.trigger;



public interface IShowObjectMessageAndErrors extends UIProvider {

	/**
	 * Displays the given error message somehow
	 */
	UserResult showError(String msg, Throwable error);

	/**
	 * Opens up a GUI to show the details of the given value
	 */
	UserResult showScreenBox(Object value) throws Exception;

	/**
	 * Displays the given information message somehow
	 */
	UserResult showMessage(String string);

}
