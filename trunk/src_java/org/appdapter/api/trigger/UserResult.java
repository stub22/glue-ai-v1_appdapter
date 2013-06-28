package org.appdapter.api.trigger;

import javax.swing.JPanel;

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
