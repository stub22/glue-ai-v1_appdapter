package org.appdapter.gui.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.OverlayLayout;
import javax.swing.border.Border;

import org.appdapter.gui.pojo.ScreenBoxedPOJORefPanel;
import org.appdapter.gui.pojo.Utility;
import org.appdapter.gui.swing.impl.JVPanel;


/**
 * A GUI component that shows what an array contains,
 * and lets you add and remove elements.
 *
 * 
 */
public class ArrayContentsPanel extends JVPanel implements ScreenBoxedPOJORefPanel.RemoveListener, DropTargetListener {

  Object array;
  POJOAppContext context;
  JPanel panel;
  JScrollPane scroll;
  Border defaultScrollBorder;
  JButton reloadButton;

  DropTarget dropTarget;

  //An invisible panel in front of the list of contents, which
  //captures drag/drop operations
  JPanel dropGlass;

  public ArrayContentsPanel(Object array, Collection collection) {
    this.array = array;
    this.context = context;
    initGUI();
  }

  public ArrayContentsPanel(Collection collection) throws Exception {
    this(Utility.getCurrentContext(), collection);
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
    reloadButton.addActionListener(
      new ActionListener() {
        @Override
		public void actionPerformed(ActionEvent event) {
          reloadContents();
        }
      }
    );

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

  @Override
public void objectRemoved(Object object, Collection parent) {
    reloadContents();
  }

  public void reloadContents() {
    throw new RuntimeException("Not yet implemented");
/*
    panel.removeAll();

    Iterator it = collection.iterator();
    while (it.hasNext()) {
      Object object = it.next();
      SmallBeanView view = new SmallBeanView(context, object, true, true, true, collection);
      view.setRemoveListener(this);
      panel.add(view);
    }
    invalidate();
    validate();
    repaint();
*/
  }

//======= Drag/Drop methods ====================================0

  @Override
public void dragEnter(DropTargetDragEvent event) {
    event.acceptDrag (DnDConstants.ACTION_MOVE);
  }

  @Override
public void dragExit(DropTargetEvent dtde) {
  }
  @Override
public void dragOver(DropTargetDragEvent dtde) {
  }
  @Override
public void drop(DropTargetDropEvent event) {
    throw new RuntimeException("Not yet implemented");
  /*
    Transferable t = event.getTransferable();
    try {
      Object o = t.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType));
      collection.add(o);
      reloadContents();
    } catch (Exception err) {
      new ErrorDialog("An error occurred while handling a drop operation", err).show();
    }
    */
  }
  @Override
public void dropActionChanged(DropTargetDragEvent dtde) {
  }
}