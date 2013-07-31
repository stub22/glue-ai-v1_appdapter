package org.appdapter.gui.trigger;

import java.awt.event.ActionEvent;
import java.beans.FeatureDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;

import javax.swing.AbstractButton;

import org.appdapter.api.trigger.AnyOper.UISalient;
import org.appdapter.api.trigger.Box;
import org.appdapter.core.name.Ident;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.WrapperValue;
import org.appdapter.gui.browse.KMCTrigger;

abstract public class KMCTriggerImpl extends TriggerForMember implements KMCTrigger {

	public KMCTriggerImpl(String menuName, DisplayContext ctx, Class cls, WrapperValue obj, Member fd, boolean isDeclNonStatic0, FeatureDescriptor feature, boolean hasNoSideEffects) {
		super(menuName, ctx, cls, obj, fd, isDeclNonStatic0, feature, hasNoSideEffects);
	}

	public abstract Object valueOf(Box targetBox, ActionEvent actevt, boolean wantSideEffect, boolean isPaste) throws InvocationTargetException;
}
