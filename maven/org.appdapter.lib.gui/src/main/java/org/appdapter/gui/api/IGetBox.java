package org.appdapter.gui.api;

import org.appdapter.api.trigger.Box;


public interface IGetBox {

	public BT getBT();
	
	public interface NotWrapper {

		public Box asBox();

	}


}
