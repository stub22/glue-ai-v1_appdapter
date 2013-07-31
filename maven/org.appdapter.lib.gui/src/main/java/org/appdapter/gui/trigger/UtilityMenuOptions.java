package org.appdapter.gui.trigger;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.appdapter.api.trigger.AnyOper.UISalient;
import org.appdapter.api.trigger.AnyOper.UtilClass;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.api.trigger.TriggerImpl;
import org.appdapter.bind.rdf.jena.model.JenaModelUtils;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.core.convert.TypeAssignable;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.ScreenBox;
import org.appdapter.gui.api.WrapperValue;
import org.appdapter.gui.box.BoxImpl;
import org.appdapter.gui.box.ScreenBoxImpl;
import org.appdapter.gui.browse.KMCTrigger;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.repo.RepoManagerPanel;
import org.appdapter.gui.util.ClassFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UISalient(NonPublicMethods = false)
abstract public class UtilityMenuOptions implements UtilClass {

	protected static Logger theLogger = LoggerFactory.getLogger(UtilityMenuOptions.class);

	public static boolean addPanelClasses = false;
	public static boolean addGlobalStatics = true;
	public static boolean useBeanIcons = false;
	public static boolean usePropertyEditorManager = false;
	public static boolean useSeperateSlowThreads = false;

	public static void findAndloadMissingUtilityClasses() throws IOException {
		Utility.addClassStaticMethods(JenaModelUtils.class);
		for (Class utilClass : Utility.getCoreClasses(UtilClass.class)) {
			try {
				if (utilClass.isInterface())
					continue;
				Utility.addClassStaticMethods(utilClass);
			} catch (Throwable t) {
				Debuggable.printStackTrace(t);
			}
		}
		Utility.addClassStaticMethods(RepoManagerPanel.class);
	}

	public static void findAndloadMissingTriggers() throws IOException {
		for (Class utilClass : Utility.getCoreClasses(Trigger.class)) {
			try {
				addTriggerClass(utilClass);
			} catch (Throwable t) {
				Debuggable.printStackTrace(t);
			}

		}

	}

	static private Set<Class> allBoxTypes = new HashSet<Class>();

	public static Set<Class> getAllBoxTypes() throws IOException {
		synchronized (allBoxTypes) {
			if (allBoxTypes.size() == 0) {
				for (Class utilClass : Utility.getCoreClasses(Box.class)) {
					try {
						if (ReflectUtils.isCreatable(utilClass)) {
							allBoxTypes.add(utilClass);
						}
					} catch (Throwable t) {
						Debuggable.printStackTrace(t);
					}
				}
			}
		}
		return allBoxTypes;

	}

	static Trigger createTrigger(Class utilClass, Object... params) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException {
		for (Constructor c : utilClass.getDeclaredConstructors()) {
			Class[] pt = c.getParameterTypes();
			if (pt.length > 0 && pt.length == params.length) {
				return (Trigger) c.newInstance(params);
			}
		}
		return (Trigger) utilClass.getDeclaredConstructors()[0].newInstance();
	}

	static Collection skippedTypes = new HashSet() {
		{
			add(Box.class);
			add(ScreenBox.class);
			add(ScreenBoxImpl.class);
			add(BoxImpl.class);
			add(Trigger.class);
			add(TriggerImpl.class);
			add(Object.class);
			addAll(Arrays.asList(TriggerImpl.class.getInterfaces()));
			addAll(Arrays.asList(ScreenBoxImpl.class.getInterfaces()));
			add(java.io.Serializable.class);
		}
	};

	private static void addTriggerClass(final Class utilClass) {
		if (!ReflectUtils.isCreatable(utilClass))
			return;
		Class classOfBox;
		boolean hasNoSideEffects = false;
		Callable<Trigger> howto = new Callable<Trigger>() {
			public Trigger call() throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException {
				return createTrigger(utilClass);
			}
		};
		String menuName = ReflectUtils.getCanonicalSimpleName(utilClass);
		if (menuName.endsWith("Trigger")) {
			menuName = menuName.substring(0, menuName.length() - 7);
		}
		Member member;
		final boolean isDeclNonStatic0 = true;

		try {
			// checks for some type and claims this will be good for it
			if (TriggerForClass.class.isAssignableFrom(utilClass)) {
				Utility.addTriggerForClassInst((TriggerForClass) utilClass.newInstance());
				return;
			}
		} catch (Throwable e) {
			// otherwise look for another registration method
		}

		try {
			// checks for a static final field of some type and claims this will be good for it
			classOfBox = (Class) utilClass.getField("boxTargetClass").get(null);
			member = classOfBox.getDeclaredConstructors()[0];
			addTriggerForClass(menuName, classOfBox, member, howto, isDeclNonStatic0, hasNoSideEffects);
			return;
		} catch (Throwable e) {
			// otherwise look for another registration method
		}

		try {
			// checks for a fire(WhatNotBox box) and claims this will be good for it
			Method method = ReflectUtils.getDeclaredMethod(utilClass, "fire", false, false, 1);
			if (method != null) {
				classOfBox = ReflectUtils.getTypeClass(method.getParameterTypes()[0], new ArrayList(skippedTypes));
				if (classOfBox != null) {
					member = method;
					addTriggerForClass(menuName, classOfBox, member, howto, isDeclNonStatic0, hasNoSideEffects);
					return;
				}
			}
		} catch (Throwable e) {
			// otherwise look for another registration method
		}

		try {
			// checks for a SomeTrigger(WhatNot classOfBox) and claims this will be good for it
			Constructor method = ReflectUtils.getDeclaredConstructor(utilClass, TypeAssignable.ANY, 1);
			if (method != null) {
				classOfBox = ReflectUtils.getTypeClass(method.getParameterTypes()[0], new ArrayList(skippedTypes));
				if (classOfBox != null) {
					member = method;
					addTriggerForClass(menuName, classOfBox, member, howto, isDeclNonStatic0, hasNoSideEffects);
					return;
				}
			}
		} catch (Throwable e) {
			// otherwise look for another registration method
		}

		try {
			// checks for some type and claims this will be good for it
			classOfBox = ReflectUtils.getTypeClass(utilClass.getTypeParameters(), new ArrayList(skippedTypes));
			if (classOfBox != null) {
				member = classOfBox.getDeclaredConstructors()[0];
				//@todo ?
				addTriggerForClass(menuName, classOfBox, member, howto, isDeclNonStatic0, hasNoSideEffects);
			}
		} catch (Throwable e) {
			// otherwise look for another registration method
		}

		theLogger.warn("Unable to register triggers from: " + utilClass);
	}

	private static void addTriggerForClass(final String menuName, final Class classOfBox, final Member member, final Callable<Trigger> valueOf, final boolean isDeclNonStatic0,
			final boolean hasNoSideEffects) {

		Utility.addTriggerForClassInst(new TriggerForClass() {

			@Override public String toString() {
				return Debuggable.toInfoStringF(this);
			}

			@Override public KMCTrigger createTrigger(String menuFmt, DisplayContext ctx, WrapperValue poj) {
				return new KMCTriggerImpl(menuName, ctx, classOfBox, poj, member, isDeclNonStatic0, null, hasNoSideEffects) {
					@Override public void fire(Box targetBox) {
						super.fire(targetBox);
					}

					@Override public Object valueOf(Box targetBox, ActionEvent actevt, boolean wantSideEffect, boolean isPaste) throws InvocationTargetException {
						try {
							if (!wantSideEffect)
								return null;
							valueOf.call().fire(targetBox);
							return null;
						} catch (InvocationTargetException ite) {
							throw ite;
						} catch (Exception e) {
							throw new InvocationTargetException(e);
						}
					}
				};
			}

			@Override public boolean appliesTarget(Class cls, Object anyObject) {
				return ReflectUtils.convertsTo(anyObject, cls, classOfBox);
			}
		});
	}
}
