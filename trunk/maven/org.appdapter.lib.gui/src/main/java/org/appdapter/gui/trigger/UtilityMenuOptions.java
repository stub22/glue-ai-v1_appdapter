package org.appdapter.gui.trigger;

import static org.appdapter.core.log.Debuggable.*;
import java.awt.event.ActionEvent;
import java.beans.Customizer;
import java.beans.PropertyEditor;
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

import org.appdapter.api.trigger.AnyOper;
import org.appdapter.api.trigger.AnyOper.Autoload;
import org.appdapter.api.trigger.AnyOper.UISalient;
import org.appdapter.api.trigger.AnyOper.UtilClass;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.api.trigger.TriggerImpl;
import org.appdapter.bind.rdf.jena.model.JenaModelUtils;
import org.appdapter.core.component.KnownComponent;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.core.convert.TypeAssignable;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.ScreenBox;
import org.appdapter.gui.box.BoxImpl;
import org.appdapter.gui.box.ScreenBoxImpl;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.editors.ObjectPanel;
import org.appdapter.gui.repo.RepoManagerPanel;
import org.appdapter.gui.util.ClassFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.ReificationStyle;

@UISalient(NonPublicMethods = false)
abstract public class UtilityMenuOptions implements UtilClass {

	protected static Logger theLogger = LoggerFactory.getLogger(UtilityMenuOptions.class);

	public static boolean addPanelClasses = true;
	public static boolean addGlobalStatics = true;
	public static boolean allTriggersAreGlobal = true;
	public static boolean useBeanIcons = false;
	public static boolean usePropertyEditorManager = true;
	public static boolean separateSlowThreads = true;

	interface VoidFunc<A> {
		void call(A r);
	}

	public static void findAndloadMissingUtilityClasses() throws IOException {
		Utility.addClassMethods(JenaModelUtils.class);
		VoidFunc fw = new VoidFunc<Class>() {
			@Override public void call(Class utilClass) {
				//if (!utilClass.isInterface())
				Utility.addClassMethods(utilClass);
			}
		};

		withSubclasses(KnownComponent.class, fw);
		withSubclasses(Box.class, fw);
		withSubclasses(Trigger.class, fw);
		withSubclasses(ObjectPanel.class, fw);
		withSubclasses(UtilClass.class, fw);
		withSubclasses(AnyOper.class, fw);
		withSubclasses(Customizer.class, fw);
		withSubclasses(PropertyEditor.class, fw);
		withSubclasses(ModelFactory.class, fw);
		useScannedClasses("com.hp.hpl.jena.rdf.model.", fw);
		createEnumClass(ReificationStyle.class);

		loadAutoloads();
		Utility.addClassMethods(RepoManagerPanel.class);
	}

	private static void createEnumClass(Class ec) {
		Utility.addClassMethods(ec);
	}

	private static <T> void withSubclasses(Class<T> sc, VoidFunc<Class<? extends T>> func) {

		for (Class utilClass : Utility.getCoreClasses(sc)) {
			try {
				func.call(utilClass);
			} catch (Throwable t) {
				printStackTrace(t);
			}
		}
	}

	private static <T> void useScannedClasses(String packagePrefix, VoidFunc<Class<? extends T>> func) {

		try {
			for (Class utilClass : ClassFinder.getClasses(packagePrefix, Object.class)) {
				try {
					func.call(utilClass);
				} catch (Throwable t) {
					printStackTrace(t);
				}
			}
		} catch (IOException e) {
			printStackTrace(e);
		}
	}

	static @UISalient() public void loadAutoloads() {
		VoidFunc fw = new VoidFunc<Class>() {
			@Override public void call(Class utilClass) {
				if (!utilClass.isInterface())
					Utility.addClassMethods(utilClass);
			}
		};
		withSubclasses(Autoload.class, fw);
	}

	public static void findAndloadMissingTriggers() throws IOException {
		VoidFunc fw = new VoidFunc<Class>() {
			@Override public void call(Class utilClass) {
				if (!utilClass.isInterface())
					Utility.addClassMethods(utilClass);
			}
		};
		withSubclasses(Trigger.class, fw);
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
				classOfBox = ReflectUtils.getTypeClass(method.getGenericParameterTypes()[0], null, new ArrayList(skippedTypes));
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
				classOfBox = ReflectUtils.getTypeClass(method.getGenericParameterTypes()[0], null, new ArrayList(skippedTypes));
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
			classOfBox = ReflectUtils.getTypeClass(utilClass.getTypeParameters(), null, new ArrayList(skippedTypes));
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

			@Override public boolean isSideEffectSafe() {
				return false;
			}

			@Override public boolean isFavorited() {
				return true;
			}

			@Override public Object getIdentityObject() {
				return member;
			}

			@Override public String toString() {
				return Debuggable.toInfoStringF(this);
			}

			@Override public KMCTrigger createTrigger(String menuFmt, DisplayContext ctx, Object poj) {
				return new KMCTriggerImpl(menuName, ctx, classOfBox, poj, member, isDeclNonStatic0, null, hasNoSideEffects) {
					@Override public void fire(Box targetBox) {
						super.fire(targetBox);
					}

					@Override public Object getIdentityObject() {
						return member;
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

			@Override public String getMenuPath() {
				return "" + member;
			}

		});
	}
}
