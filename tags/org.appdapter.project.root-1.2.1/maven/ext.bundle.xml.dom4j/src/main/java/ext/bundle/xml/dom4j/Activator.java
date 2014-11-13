package ext.bundle.xml.dom4j;

import ext.osgi.common.ExtBundleActivatorBase;

public class Activator extends ExtBundleActivatorBase {

	@Override public void ensureExtClassesAreFindable() {
		if (isOSGIProperty("osgi-tests", true)) {
			debugLoaders(org.dom4j.swing.XMLTableDefinition.class);
		}
	}
}
