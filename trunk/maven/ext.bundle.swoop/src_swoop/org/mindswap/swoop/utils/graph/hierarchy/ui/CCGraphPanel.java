package org.mindswap.swoop.utils.graph.hierarchy.ui;

import java.awt.BorderLayout;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;

import edu.uci.ics.jung.visualization.VisualizationViewer;

public class CCGraphPanel extends JPanel {
	protected VisualizationViewer vv;
	protected boolean hadjusting;
	protected boolean vadjusting;

	public CCGraphPanel(VisualizationViewer vv) {
		super(new BorderLayout());
		this.vv = vv;
		add(vv);
		this.setupKeyBindings();
		this.requestFocusInWindow();
	}

	private void setupKeyBindings() {
		// setting up key actions
		ActionMap amap = getActionMap();
		InputMap imap = getInputMap();
	}

}