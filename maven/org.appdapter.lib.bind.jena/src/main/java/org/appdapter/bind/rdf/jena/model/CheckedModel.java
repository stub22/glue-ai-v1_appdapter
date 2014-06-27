package org.appdapter.bind.rdf.jena.model;

import org.appdapter.core.log.Debuggable;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.enhanced.BuiltinPersonalities;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import org.appdapter.core.store.dataset.RepoDatasetFactory;

public class CheckedModel extends ModelCom implements Model {
	public static RDFNode createTypedLiteral(Model model, String cellString, RDFDatatype myDatatype) {
		try {
			if (myDatatype == XSDDatatype.XSDstring) {
				return model.createLiteral(cellString, "");
			}
			if (myDatatype == XSDDatatype.XSDboolean) {
				if (cellString.equals("TRUE")) {
					Debuggable.oldBug("createTypedLiteral", cellString, myDatatype, "use true", model);
					cellString = "true";
				}
			}
			return model.createTypedLiteral(cellString, myDatatype);
		} catch (Throwable t) {
			Debuggable.oldBug("createTypedLiteral", cellString, myDatatype, t, model);
			return model.createLiteral(cellString);//(cellString, null);
		}
	}
 
	private final Graph modelGraph;
	private String debuggingName;

	public CheckedModel(CheckedGraph modelGraph) {
		super(modelGraph, BuiltinPersonalities.model);
		this.modelGraph = getGraphNoRemove().modelGraph;
	}

	public CheckedModel(Graph modelGraph, boolean makeNonAdd, boolean makeNonDelete, boolean makeNameSpaceChecked) {
		this(CheckedGraph.ensure(modelGraph, makeNonAdd, makeNonDelete, makeNameSpaceChecked));
	}

	/**
	 * Create a Statement instance. (Creating a statement does not add it to the set of statements in the model; see Model::add). This method may return an existing Statement with the correct components and model, or it may construct a fresh one, as it sees fit.
	 * <p>
	 * Subsequent operations on the statement or any of its parts may modify this model.
	 * 
	 * @param s
	 *            the subject of the statement
	 * @param p
	 *            the predicate of the statement
	 * @param o
	 *            the object of the statement
	 * @return the new statement
	 */
	public Statement createStatement(Resource s, Property p, RDFNode o) {
		return super.createStatement(s, p, o);
	}

	@Override public Literal createTypedLiteral(Object v, RDFDatatype dtype) {
		// TODO Auto-generated method stub
		return createTypedLiteralObject(v, dtype);
	}

	public Literal createTypedLiteralObject(Object value, RDFDatatype dtype) {
		Literal lit = super.createTypedLiteral(value, dtype);
		return checkDataType(lit, dtype);
	}

	@Override public Literal createTypedLiteral(String lex, RDFDatatype dtype) throws DatatypeFormatException {
		Literal lit = null;
		try {
			lit = super.createTypedLiteral(lex, dtype);
		} catch (Throwable e) {
			Debuggable.oldBug("createTypedLiteral", lex, dtype, e);
		}
		if (lit == null) {
			try {
				lit = createTypedLiteralObject(lex, dtype);
			} catch (Throwable e) {
				Debuggable.oldBug("createTypedLiteralObject", lex, dtype, e);
			}
		}
		return checkDataType(lit, dtype);
	}

	private Literal checkDataType(Literal lit, RDFDatatype dtype) {
		if (dtype.getClass() == BaseDatatype.class) {

		}
		if (!dtype.isValidValue(lit.getValue())) {
			Debuggable.oldBug(lit + " is not a valid " + dtype);
		} else {
			if (dtype.getClass() == BaseDatatype.class) {
				Debuggable.oldBug(lit + " is faked " + dtype);
			}
		}
		return lit;

	}

	@Override public String toString()
	{
		return "<CheckedModel=" + debuggingName + " " + getGraph() + " | " + reifiedToString() + ">";
	}

	private CheckedGraph getGraphNoRemove() {
		return (CheckedGraph) getGraph();

	}

	@Override public Resource createResource(String uri) {
		uri = RepoDatasetFactory.fixURI(uri);
		Resource r = super.createResource(uri);
		return r;
	}

	@Override public Resource getResource(String uri) {
		uri = RepoDatasetFactory.fixURI(uri);
		Resource r = super.getResource(uri);
		return r;
	}

	@Override public void close() {
		modelGraph.close();
	}

	@Override public boolean isClosed() {
		return modelGraph.isClosed();
	}

	@Override public boolean isEmpty() {
		return modelGraph.isEmpty();
	}

	@Override public long size() {
		return modelGraph.size();
	}

	public void setName(String n) {
		this.debuggingName = n;
	}
}