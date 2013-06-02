package org.appdapter.gui.browse;

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

import org.appdapter.gui.pojo.Utility;
import org.appdapter.gui.util.ClassFinder;
import org.appdapter.gui.util.Debuggable;

import com.jidesoft.plaf.LookAndFeelFactory;

public class LookAndFeelMenuItems extends JMenu {

	public LookAndFeelMenuItems(String title) {
		super(title);
		LookAndFeelFactory.installDefaultLookAndFeelAndExtension();
		final LookAndFeelMenuItems menu = this;
		menu.add(createLnfAction("NimbusLookAndFeel", "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"));
		menu.add(createLnfAction("Metal", "javax.swing.plaf.metal.MetalLookAndFeel"));
		menu.add(createLnfAction("System", UIManager.getSystemLookAndFeelClassName()));
		menu.add(createLnfAction("GTKLookAndFeel", "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"));
		menu.add(createLnfAction("MotifLookAndFeel", "com.sun.java.swing.plaf.motif.MotifLookAndFeel"));
		(new Thread() {
			@Override public void run() {
				// TODO Auto-generated method stub
				addReflectiveLAndF(menu);
			}
		}).start();

		for (Field fld : LookAndFeelFactory.class.getDeclaredFields()) {
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
		// TODO Auto-generated constructor stub
	}

	static void addReflectiveLAndF(JMenu menu) {

		try {
			Set<Class> lafs = ClassFinder.getClasses(LookAndFeel.class);
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
			@Override public void actionPerformed(ActionEvent e) {
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
			@Override public void actionPerformed(ActionEvent e) {
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
			@Override public void actionPerformed(ActionEvent e) {
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
