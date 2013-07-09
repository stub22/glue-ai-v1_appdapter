package org.appdapter.gui.editors;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Customizer;
import java.lang.reflect.Array;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.OverlayLayout;
import javax.swing.border.Border;

import org.appdapter.gui.api.BoxPanelSwitchableView;
import org.appdapter.gui.api.BrowserPanelGUI;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.ObjectCollectionRemoveListener;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.editors.LargeObjectView.TabPanelMaker;
import org.appdapter.gui.swing.ErrorDialog;
import org.appdapter.gui.swing.ErrorPanel;
import org.appdapter.gui.swing.JJPanel;
import org.appdapter.gui.swing.SmallObjectView;
import org.appdapter.gui.swing.VerticalLayout;

/**
 * A GUI component that shows what an array contains,
 * and lets you add and remove elements.
 *
 * 
 */
public class ArrayContentsPanel extends JJPanel implements ObjectCollectionRemoveListener, DropTargetListener, Customizer {

	public static class ArrayContentsPanelTabFramer extends TabPanelMaker {
		@Override public void setTabs(BoxPanelSwitchableView tabs, DisplayContext context, Object object, Class objClass, SetTabTo cmds) {
			if (objClass == null)
				objClass = object.getClass();
			if (!(objClass.isArray())) {
				return;
			}
			if (cmds != SetTabTo.ADD)
				return;
			try {
				ArrayContentsPanel constructors = new ArrayContentsPanel(object);
				tabs.insertTab("Array Contents", null, constructors, null, 0);
			} catch (Exception err) {
				tabs.insertTab("Array Contents", null, new ErrorPanel("Could not show ArrayContentsPanel", err), null, 0);
			}

		}
	}

	Object array;
	BrowserPanelGUI context;
	JPanel panel;
	JScrollPane scroll;
	Border defaultScrollBorder;
	JButton reloadButton;

	DropTarget dropTarget;

	//An invisible panel in front of the list of contents, which
	//captures drag/drop operations
	JPanel dropGlass;

	public ArrayContentsPanel() {
		this(null);
	}

	public ArrayContentsPanel(BrowserPanelGUI context, Object array) {
		this.array = array;
		if (context == null)
			context = Utility.getCurrentContext();
		this.context = context;
		initGUI();
	}

	public ArrayContentsPanel(Object array) {
		this(Utility.getCurrentContext(), array);
	}

	private void initGUI() {
		panel = new JPanel();
		panel.setLayout(new VerticalLayout());

		dropGlass = new JPanel();
		dropGlass.setOpaque(false);
		dropTarget = new DropTarget(dropGlass, this);

		scroll = new JScrollPane(panel);
		defaultScrollBorder = scroll.getBorder();

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		reloadButton = new JButton("Update");
		buttonPanel.add(reloadButton);
		buttonPanel.add(new JLabel("To add objects just drag them into the panel below."));
		reloadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				reloadContents();
			}
		});

		JPanel stack = new JPanel();
		stack.setLayout(new OverlayLayout(stack));
		stack.add(dropGlass);
		stack.add(scroll);

		//setBorder(new TitledBorder(new , "XYZ"));
		setLayout(new BorderLayout());
		add("North", buttonPanel);
		add("Center", stack);

		reloadContents();
	}

	public void objectRemoved(Object value, Collection parent) {
		reloadContents();
	}

	public void reloadContents() {
		panel.removeAll();
		if (array != null) {
			final int len = Array.getLength(array);
			for (int i = 0; i < len; i++) {
				final int index = i;
				final Object value = Array.get(array, index);
				SmallObjectView view = new SmallObjectView(context, null, value, null) {
					@Override public void valueChanged(Object oldValue, Object newValue) {
						Array.set(array, index, newValue);
						super.valueChanged(oldValue, newValue);
					}
				};
				view.setRemoveListener(new ObjectCollectionRemoveListener() {

					@Override public void objectRemoved(Object value, Collection parent) {
						System.arraycopy(array, index, array, index + 1, len - index - 1);

					}
				});
				panel.add(view);

			}
		} else {
		}
		invalidate();
		validate();
		repaint();

	}

	//======= Drag/Drop methods ====================================0

	public void dragEnter(DropTargetDragEvent event) {
		event.acceptDrag(DnDConstants.ACTION_MOVE);
	}

	public void dragExit(DropTargetEvent dtde) {
	}

	public void dragOver(DropTargetDragEvent dtde) {
	}

	public void drop(DropTargetDropEvent event) {
		Transferable t = event.getTransferable();
		try {
			Object o = t.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType));
			Class componentType = array.getClass().getComponentType();
			final int len = Array.getLength(array);
			Object newArray = Array.newInstance(componentType, len + 1);
			System.arraycopy(array, 0, newArray, 0, len);
			Array.set(newArray, len, o);
			setObject(newArray);
		} catch (Exception err) {
			new ErrorDialog("An error occurred while handling a drop operation", err).show();
		}

	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	@Override public void setObject(Object bean) {
		array = bean;
		reloadContents();
	}
}