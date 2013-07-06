package org.appdapter.gui.swing;

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
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.OverlayLayout;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.api.trigger.BoxPanelSwitchableView;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.Chooser;
import org.appdapter.gui.api.ObjectCollectionRemoveListener;
import org.appdapter.gui.api.Utility;

/**
 * A GUI component that shows what a Collection contains,
 * and lets you add and remove elements.
 *
 * 
 */
public class CollectionContentsPanel

extends ScreenBoxPanel implements ObjectCollectionRemoveListener, DropTargetListener, Chooser<Object>

, ChangeListener {

	BoxPanelSwitchableView parentTabs;
	boolean wasSelected = false;

	@Override public void stateChanged(ChangeEvent evt) {
		boolean isSelected = parentTabs.getSelectedIndex() == parentTabs.indexOfComponent(this);
		if (wasSelected != isSelected) {
			if (isSelected) {
				this.reloadContents();
			}
		}
		wasSelected = isSelected;
	}

	//Collection collection;
	DisplayContext context;
	JPanel panel;
	JScrollPane scroll;
	Border defaultScrollBorder;
	JButton reloadButton;

	DropTarget dropTarget;

	//An invisible panel in front of the list of contents, which
	//captures drag/drop operations
	JPanel dropGlass;

	public CollectionContentsPanel(DisplayContext context, Collection collection, BoxPanelSwitchableView tabs) {
		this.parentTabs = tabs;
		this.objectValue = collection;
		this.context = context;
	}

	public CollectionContentsPanel(Collection collection, BoxPanelSwitchableView tabs) throws Exception {
		this(Utility.getCurrentContext(), collection, tabs);
	}

	@Override protected void initSubclassGUI() throws Throwable {
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
			@Override public void actionPerformed(ActionEvent event) {
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
	}

	public void completeSubClassGUI() {
		reloadContents();
	}

	@Override public void objectRemoved(Object object, Collection parent) {
		reloadContents();
	}

	public void reloadContents() {
		panel.removeAll();
		final Collection collection = (Collection) objectValue;
		Iterator it = collection.iterator();
		while (it.hasNext()) {
			final Object value = it.next();
			SmallObjectView view = new SmallObjectView(context, null, value, collection) {
				@Override public void valueChanged(Object oldValue, Object newValue) {
					replace(collection, oldValue, newValue);
					super.valueChanged(oldValue, newValue);
				}
			};
			view.setRemoveListener(this);
			panel.add(view);
		}
		invalidate();
		validate();
		repaint();
	}

	static private void replace(Collection collection, Object oldValue, Object newValue) {
		Iterator it = collection.iterator();
		while (it.hasNext()) {
			final Object value = it.next();
			if (value == oldValue) {
				it.remove();
				collection.add(newValue);
				return;
			}
		}

	}

	//======= Drag/Drop methods ====================================0

	@Override public void dragEnter(DropTargetDragEvent event) {
		event.acceptDrag(DnDConstants.ACTION_MOVE);
	}

	@Override public void dragExit(DropTargetEvent dtde) {
	}

	@Override public void dragOver(DropTargetDragEvent dtde) {
	}

	@Override public void drop(DropTargetDropEvent event) {
		Transferable t = event.getTransferable();
		try {
			Object o = t.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType));
			Collection collection = (Collection) this.objectValue;
			collection.add(o);
			reloadContents();
		} catch (Exception err) {
			new ErrorDialog("An error occurred while handling a drop operation", err).show();
		}
	}

	@Override public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	@Override protected boolean reloadObjectGUI(Object obj) throws Throwable {
		Debuggable.notImplemented();
		return false;
	}

}