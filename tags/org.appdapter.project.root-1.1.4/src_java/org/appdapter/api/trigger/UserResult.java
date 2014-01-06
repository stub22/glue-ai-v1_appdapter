package org.appdapter.api.trigger;


import javax.swing.JPanel;

// TODO:  Move this to o.a.lib.gui.    Classes that use Swing belong in the GUI packages.
public interface UserResult {

	UserResult SUCCESS = new UserResult() {
		public String toString() {
			return "SUCCESS";
		}

		@Override public JPanel getPropertiesPanel() {
			return null;
		};
	};

	JPanel getPropertiesPanel();

}
