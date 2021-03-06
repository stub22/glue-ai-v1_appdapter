/*
 * Created on Aug 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.graph.hierarchy.popup;

import org.mindswap.swoop.SwoopModel;
import org.semanticweb.owl.io.ShortFormProvider;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLObject;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ClassAxiomContainer implements AxiomContainer {

	private OWLClassAxiom myAxiom = null;
	private String myString = null;

	public ClassAxiomContainer(OWLClassAxiom axe, ShortFormProvider provider, SwoopModel model) {
		myAxiom = axe;
		ConcisePlainVisitor visitor = new ConcisePlainVisitor(provider, model);
		visitor.reset();
		try {
			myAxiom.accept(visitor);
			myString = visitor.result();// + " " + axe.getClass();
		} catch (OWLException ex) {
			ex.printStackTrace();
		}
	}

	public OWLObject getAxiom() {
		return myAxiom;
	}

	public String toString() {
		return myString;
	}

}
