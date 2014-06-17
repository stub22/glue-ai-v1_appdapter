package org.appdapter.bind.log4j;

import java.net.URL;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorMakeSTAppender extends AppenderSkeleton {

	private static Logger theLogger;

	public static Logger testLoggerSetup(URL propertiesURL) {
		try {
			if (theLogger == null) {
				//System.setProperty("log4j.defaultInitOverride", "true");
				System.setProperty("log4j.debug", "true");
				String loc = "log4j.properties";
				if (propertiesURL != null) {
					loc = propertiesURL.toExternalForm();
				}
				System.setProperty("log4j.configuration", loc);
				theLogger = LoggerFactory.getLogger(ErrorMakeSTAppender.class);
			}
			theLogger.warn("You can see warnings");
			theLogger.error("You can see error");
			theLogger.debug("You can see debug");
			theLogger.trace("You can see trace");
			System.out.println("Did you see trace?");
		} catch (Throwable t) {
			t.printStackTrace();
		}
		Log4jFuncs.forceLog4jConfig(propertiesURL);
		return theLogger;
	}

	protected void append(LoggingEvent event) {
		switch (event.getLevel().toInt()) {
		case Level.INFO_INT:
			break;
		case Level.DEBUG_INT:
			break;
		case Level.ERROR_INT:
			(new Error("LOGGER GOT ERROR NOTICE")).fillInStackTrace().printStackTrace();
			break;
		case Level.WARN_INT:
			break;
		case Level.TRACE_INT:
			break;
		default:
			break;
		}
		String message = null;
		if (event.locationInformationExists()) {
			StringBuilder formatedMessage = new StringBuilder();
			formatedMessage.append(event.getLocationInformation().getClassName());
			formatedMessage.append(".");
			formatedMessage.append(event.getLocationInformation().getMethodName());
			formatedMessage.append(":");
			formatedMessage.append(event.getLocationInformation().getLineNumber());
			formatedMessage.append(" - ");
			formatedMessage.append(event.getMessage().toString());
			message = formatedMessage.toString();
		} else {
			message = event.getMessage().toString();
		}
		System.err.println(message);
	}

	@Override public void close() {
		// TODO Auto-generated method stub

	}

	@Override public boolean requiresLayout() {
		// TODO Auto-generated method stub
		return false;
	}

}