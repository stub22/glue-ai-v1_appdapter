
package org.appdapter.gui.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.io.PrintWriter;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.appdapter.gui.pojo.Utility;
import org.appdapter.gui.swing.impl.JBox;
import org.appdapter.gui.swing.impl.JJPanel;

public class ErrorPanel extends JJPanel {
  Throwable error;
  String message;
  JButton viewButton;
  POJOAppContext context;

  public ErrorPanel(String message, Throwable error) {
    super();
    this.message = message;
    this.error = error;
    this.context = Utility.getCurrentContext();
    createGUI();
  }

  public ErrorPanel(Throwable error) {
    this("Something went wrong!", error);
  }

  public ErrorPanel(String message) {
    this(message, null);
  }

  private void createGUI() {
	  JJPanel panel = new JJPanel(new BorderLayout(5, 5));
    JLabel messageLabel = new JLabel(message);
    panel.add("North", messageLabel);
    if (error != null) {
      JPanel errorPanel = new JPanel(new BorderLayout(2, 2));

      JBox labels = new JBox(BoxLayout.Y_AXIS);
      labels.add(new JLabel("Class: " + error.getClass().getName()));
      labels.add(new JLabel("Message: " + error.getMessage()));

      JTextArea stackTrace = new JTextArea(10, 50);
      TextAreaOutputStream stream = new TextAreaOutputStream(stackTrace);
      PrintWriter writer = new PrintWriter(stream);
      error.printStackTrace(new PrintStream(stream));

      errorPanel.add("North", labels);
      errorPanel.add("Center", new JScrollPane(stackTrace));

      panel.add("Center", errorPanel);
    }

    setLayout(new BorderLayout(5, 5));
    add("Center", panel);

    JPanel buttonPanel = new JPanel(new FlowLayout());
    if (context != null) {
      viewButton = new JButton("Details...");
      buttonPanel.add(viewButton);
      viewButton.addActionListener(
        new ActionListener() {
          @Override
		public void actionPerformed(ActionEvent evt) {
            try {
              context.showScreenBox(error);
            } catch (Throwable err) {
              if (context == null) {
                new ErrorDialog(err).show();
              } else {
                context.showError(null, err);
              }
            }
          }
        }
      );
    }

    add("South", buttonPanel);
  }
}