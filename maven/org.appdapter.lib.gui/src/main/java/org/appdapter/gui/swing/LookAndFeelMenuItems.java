package org.appdapter.gui.swing;

import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.browse.Utility;

import com.jidesoft.plaf.LookAndFeelFactory;

public class LookAndFeelMenuItems extends JMenu {

	private Thread findMoreLaf;
	public static boolean scanMoreClasses = false;

	public LookAndFeelMenuItems(String title) {
		super(title);
		boolean isOSGi = Utility.isOSGi();
		initGUI(isOSGi);
	}

	private void initGUI(boolean isOSGi) {
		//JIDESOFT  
		LookAndFeelFactory.installDefaultLookAndFeelAndExtension();
		final LookAndFeelMenuItems menu = this;
		menu.add(createLnfAction("Metal", "javax.swing.plaf.metal.MetalLookAndFeel"));
		menu.add(createLnfAction("System", UIManager.getSystemLookAndFeelClassName()));
		if (!isOSGi) {
			menu.add(createLnfAction("NimbusLookAndFeel", "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"));
			menu.add(createLnfAction("GTKLookAndFeel", "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"));
			menu.add(createLnfAction("MotifLookAndFeel", "com.sun.java.swing.plaf.motif.MotifLookAndFeel"));
		}
		this.findMoreLaf = new Thread() {
			@Override
			public void run() {
				addReflectiveLAndF(menu);
			}
		};

		Class lafc = null;
		if (!isOSGi)
			lafc = LookAndFeelFactory.class;

		if (lafc != null) {
			for (Field fld : lafc.getDeclaredFields()) {
				if (!Modifier.isStatic(fld.getModifiers()))
					continue;
				if (fld.getName().contains("_LNF") && fld.getType() == String.class) {
					try {
						String cn = (String) fld.get(null);
						if (!cn.contains("."))
							continue;
						menu.add(createLnfAction(fld.getName().replace("_LNF", "") + " (" + cn + ")", cn));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					continue;
				}
				if (fld.getName().contains("EXTENSION_STYLE") && fld.getType() == int.class) {
					try {
						menu.add(setStyleAction(fld.getName().replace("EXTENSION_STYLE_", ""), fld.getInt(null)));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					continue;
				}
			}
		}
		if (!isOSGi)
			findMoreLaf.start();
	}

	static void addReflectiveLAndF(JMenu menu) {

		try {
			if (!scanMoreClasses)
				return;
			Set<Class> lafs = org.appdapter.gui.util.ClassFinder.getClasses(LookAndFeel.class);
			if (lafs != null) {
				for (Class c : lafs) {
					if (Modifier.isAbstract(c.getModifiers()))
						continue;
					menu.add(createLnfAction(c.getSimpleName() + " (" + c + ")", c));
				}
			}
		} catch (Exception e1) {
			Debuggable.UnhandledException(e1);
		}

	}

	private static Action createLnfAction(String title, final String className) {
		return new AbstractAction(title) {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					UIManager.setLookAndFeel(className);
				} catch (Exception e1) {
				}
				JFrame frame = Utility.getAppFrame();
				SwingUtilities.updateComponentTreeUI(frame);
				frame.pack();
			}
		};
	}

	private static Action createLnfAction(String title, final Class clazz) {
		return new AbstractAction(title) {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					LookAndFeel laf = (LookAndFeel) clazz.newInstance();
					UIManager.setLookAndFeel(laf);
				} catch (Exception e1) {
				}
				JFrame frame = Utility.getAppFrame();
				SwingUtilities.updateComponentTreeUI(frame);
				frame.pack();
			}
		};
	}

	private static Action setStyleAction(String title, final int style) {
		return new AbstractAction(title) {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					LookAndFeelFactory.installJideExtension();
					LookAndFeelFactory.installJideExtension(style);
				} catch (Exception e1) {
				}
				JFrame frame = Utility.getAppFrame();
				SwingUtilities.updateComponentTreeUI(frame);
				frame.pack();
			}
		};
	}
}
