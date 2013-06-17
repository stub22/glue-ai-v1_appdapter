package org.appdapter.gui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.api.trigger.NamedObjectCollection;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.Utility;
import org.appdapter.gui.rimpl.TriggerPopupMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A panel containing a very minimal GUI for an object
 * <p>
 * 
 * 
 */
public class SmallObjectView<BoxType extends Box>

extends ObjectView<Box>

implements PropertyChangeListener, MouseListener, ActionListener, DragGestureListener, Transferable, DragSourceListener, DropTargetListener {

	class PropertyButton extends JButton {
		public PropertyButton() {
			super();
			try {
				setIcon(new ImageIcon(IconView.class.getResource("PropertyButton.gif")));
			} catch (Throwable err) {
				setText("...");
			}
			//setFont(new Font("Serif", Font.PLAIN, 8));
			setToolTipText("Open a property window for this object");
		}

		@Override public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		@Override public Dimension getPreferredSize() {
			return new Dimension(16, 16);
		}
	}

	class RemoveButton extends JButton {
		public RemoveButton() {
			super();
			try {
				setIcon(new ImageIcon(IconView.class.getResource("RemoveButton.gif")));
			} catch (Throwable err) {
				setText("x");
				setForeground(Color.red);
			}
			//setFont(new Font("Serif", Font.PLAIN, 8));
			setToolTipText("Removes this object from its parent collection");
		}

		@Override public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		@Override public Dimension getPreferredSize() {
			return new Dimension(16, 16);
		}
	}

	static public interface RemoveListener {
		public void objectRemoved(Object object, Collection parent);
	}

	private final static Color normalColor = Color.black;
	private final static Color selectedColor = Color.blue;
	private static Logger theLogger = LoggerFactory.getLogger(SmallObjectView.class);
	DisplayContext context;
	DragSource dragSource;

	//Invisible panel in front that captures menu events and drag/drop events
	JPanel frontGlass;

	//public Object objectValue;

	IconView iconView;

	JLabel label;

	NamedObjectCollection maybeCoupled;
	Collection parent;
	JButton propButton;
	JButton removeButton;

	RemoveListener removeListener;
	boolean showIcon;

	boolean showLabel;

	boolean showPropButton;

	public SmallObjectView(DisplayContext context, NamedObjectCollection col, Object object, boolean showLabel, boolean showIcon, boolean showPropButton) {
		this(context, col, object, showLabel, showIcon, showPropButton, null);
	}

	/**
	 * @param parent if a parent is provided, a "remove" button will be added allowing you to remove this object from the given collection
	 */
	public SmallObjectView(DisplayContext context, NamedObjectCollection col, Object object, boolean showLabel, boolean showIcon, boolean showPropButton, Collection parent) {
		super(false);
		objectValue = object;

		if (col == null) {
			col = context.getLocalBoxedChildren();
		}
		this.maybeCoupled = col;
		this.context = context;
		this.showLabel = showLabel;
		this.showIcon = showIcon;
		this.showPropButton = showPropButton;
		this.parent = parent;
		initGUI();
		this.addMouseListener(this);
		checkColor();

		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
	}

	public SmallObjectView(NamedObjectCollection col, Object object) {
		this(col, object, true, true, true);
	}

	public SmallObjectView(NamedObjectCollection col, Object object, boolean showLabel, boolean showIcon, boolean showPropButton) {
		this(Utility.getCurrentContext(), col, object, showLabel, showIcon, showPropButton);
	}

	@Override public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == propButton) {
			if (context != null) {
				try {
					context.showScreenBox(getValue());
				} catch (Throwable err) {
					Utility.showError(context, "An error occurred while creating an interface for " + getValue(), err);
				}
			}
		} else if (evt.getSource() == removeButton) {
			parent.remove(getValue());
			if (removeListener != null) {
				removeListener.objectRemoved(getValue(), parent);
			}
		}
	}

	public void actionRemove() {
		parent.remove(getValue());
		if (removeListener != null) {
			removeListener.objectRemoved(getValue(), parent);
		}
	}

	//==== Drag/drop methods ==========================

	public void actionShowProperties() {
		if (context != null) {
			try {
				context.showScreenBox(getValue());
			} catch (Throwable err) {
				context.showError("An error occurred while creating an interface for " + getValue(), err);
			}
		}
	}

	private void checkColor() {
		//if (getPOJO().isSelected()) {
		//  label.setForeground(selectedColor);
		//} else {
		if (label != null) {
			label.setForeground(normalColor);
		}
		//}
	}

	public void dragDropEnd(DragSourceDropEvent dsde) {
	}

	public void dragEnter(DragSourceDragEvent dsde) {
	}

	@Override public void dragEnter(DropTargetDragEvent dtde) {
		Debuggable.notImplemented();

	}

	public void dragExit(DragSourceEvent dse) {
	}

	@Override public void dragExit(DropTargetEvent dte) {
		Debuggable.notImplemented();

	}

	//==== Drag/drop methods ==========================

	public void dragGestureRecognized(DragGestureEvent event) {
		theLogger.debug("source dragGestureRecognized");
		dragSource.startDrag(event, DragSource.DefaultMoveDrop, this, this);
	}

	public void dragOver(DragSourceDragEvent dsde) {
	}

	@Override public void dragOver(DropTargetDragEvent dtde) {
		Debuggable.notImplemented();

	}

	public void drop(DropTargetDropEvent event) {
		Transferable t = event.getTransferable();
		try {
			Object o = t.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType));
			setObject(o);
		} catch (Exception err) {
			new ErrorDialog("An error occurred while handling a drop operation", err).show();
		}

	}

	public void dropActionChanged(DragSourceDragEvent dsde) {
	}

	@Override public void dropActionChanged(DropTargetDragEvent dtde) {
		Debuggable.notImplemented();

	}

	@Override public void focusOnBox(Box b) {
		setObject(b);
	}

	@Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		return getValue();
	}

	@Override public DataFlavor[] getTransferDataFlavors() {
		try {
			return new DataFlavor[] { new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType) };
		} catch (ClassNotFoundException err) {
			theLogger.error("An error occurred", err);
			return new DataFlavor[0];
		}
	}

	@Override public Object getValue() {
		return objectValue;
	}

	public boolean initGUI() {

		setLayout(new OverlayLayout(this));

		JPanel panel = new JPanel();
		frontGlass = new JPanel();
		frontGlass.setOpaque(false);

		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		Object object = getValue();
		if (object != null) {
			if (showIcon) {
				try {
					BeanInfo info = Utility.getBeanInfo(object.getClass());
					Image image = info.getIcon(BeanInfo.ICON_COLOR_16x16);
					if (image != null) {
						JLabel label = new JLabel(new ImageIcon(image));
						panel.add(label);
					}
				} catch (IntrospectionException err) {
				}
				//iconView = new IconView(object.getIcon());
				//add(iconView);
			}
			if (showLabel) {
				if (context == null) {
					label = new JLabel(object.toString());
				} else {
					label = new JLabel(maybeCoupled.getTitleOf(object));
				}
				panel.add(label);
			}
			if (showPropButton && !object.getClass().isPrimitive()) {
				propButton = new PropertyButton();
				panel.add(propButton);
				propButton.addActionListener(this);
			}
			if (parent != null) {
				removeButton = new RemoveButton();
				panel.add(removeButton);
				removeButton.addActionListener(this);
			}
		} else {
			panel.add(new JLabel("null"));
		}
		add(frontGlass);
		add(panel);
		return true;
	}

	//=================================================================

	@Override public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.getMimeType().equals(DataFlavor.javaJVMLocalObjectMimeType);
	}

	@Override public void mouseClicked(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showMenu(e.getX() + 5, e.getY() + 5);
		} else {
			/* try {
			   collection.setSelected(getPOJO());
			 } catch (Exception err) {}*/
		}
	}

	@Override public void mouseEntered(MouseEvent e) {
		//label.setForeground(Color.blue);
	}

	@Override public void mouseExited(MouseEvent e) {
		//label.setForeground(Color.black);
	}

	@Override public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showMenu(e.getX() + 5, e.getY() + 5);
		} else {
		}
	}

	@Override public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showMenu(e.getX() + 5, e.getY() + 5);
		}
		//setFont(new Font("Serif", Font.PLAIN, 8));
		setToolTipText("Removes this object from its parent collection");
	}

	//=================================================================

	@Override public void objectValueChanged(Object oldBean, Object newBean) {
		removeAll();
		objectValue = newBean;
		initGUI();
	}

	@Override public void propertyChange(PropertyChangeEvent evt) {
		if (label != null) {
			if (context == null) {
				label.setText(getValue().toString());
			} else {
				label.setText(maybeCoupled.getTitleOf(getValue()));
			}
		}
		checkColor();
	}

	@Override protected void reallySetBean(Object bean) {
		objectValue = bean;

		Debuggable.notImplemented();

	}

	public void setRemoveListener(RemoveListener l) {
		this.removeListener = l;
	}

	private void showMenu(int x, int y) {
		Object object = getValue();
		if (object == null) {
			object = this;
		}
		if (!object.getClass().isPrimitive()) {
			TriggerPopupMenu menu = new TriggerPopupMenu(context, null, object);
			frontGlass.add(menu);
			menu.show(frontGlass, x, y);
		}
	}

	public void valueChanged(Object oldObject, Object newObject) {
		removeAll();
		initGUI();
	}
}
