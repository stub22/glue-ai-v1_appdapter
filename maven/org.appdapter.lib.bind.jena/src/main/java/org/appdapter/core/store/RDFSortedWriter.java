package org.appdapter.core.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.appdapter.core.store.dataset.CheckedGraph;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.n3.N3JenaWriter;
import com.hp.hpl.jena.n3.N3JenaWriterPP;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

public final class RDFSortedWriter extends N3JenaWriterPP {
	private boolean skipWritingPrefixes = false;

	{
		useWellKnownPropertySymbols = false;
	}

	public RDFSortedWriter(boolean writePfxs) {
		this.skipWritingPrefixes = !writePfxs;
	}

	protected ResIterator listSubjects(Model model) {
		return model.listSubjects();
	}

	@Override protected void writePrefixes(Model model) {
		Graph graph = model.getGraph();
		while (graph instanceof CheckedGraph) {
			graph = ((CheckedGraph) graph).getDataGraph();
		}
		if (!(graph instanceof GraphMem)) {
			out.println("# " + graph.getClass());
		}
		if (!skipWritingPrefixes) {
			for (Iterator<String> pIter = prefixMap.keySet().iterator(); pIter.hasNext();)
			{
				String p = pIter.next();
				String u = prefixMap.get(p);

				// BaseURI - <#>            
				//	            // Special cases: N3 handling of base names.
				//	            if (doAbbreviatedBaseURIref && p.equals(""))
				//	            {
				//	                if (baseURIrefHash != null && u.equals(baseURIrefHash))
				//	                    u = "#";
				//	                if (baseURIref != null && u.equals(baseURIref))
				//	                    u = "";
				//	            }

				String tmp = "@prefix " + p + ": ";
				out.print(tmp);
				out.print(pad(16 - tmp.length()));
				// NB Starts with a space to ensure a gap.
				out.println(" <" + u + "> .");
			}
		}
	}

	protected void writeOneGraphNode(Resource subject)
	{
		// New top level item.
		// Does not take effect until newline.
		out.incIndent(indentProperty);
		writeSubject(subject);
		ClosableIterator<Property> iter = preparePropertiesForSubject(subject);
		writePropertiesForSubject(subject, iter);
		out.decIndent(indentProperty);
		out.println(" .");
	}

	protected void writePropertiesForSubject(Resource subj, ClosableIterator<Property> iter0)
	{
		List<Property> sortMe = new ArrayList();
		// For each property.
		while (iter0.hasNext())
		{
			Property property = iter0.next();
			sortMe.add(property);
		}
		iter0.close();

		Collections.sort(sortMe, NumericStringComparitor.resourceComparator);

		Iterator<Property> iter = sortMe.iterator();
		while (iter.hasNext())
		{
			Property property = iter.next();
			// Object list
			writeObjectList(subj, property);

			if (iter.hasNext())
				out.println(" ;");
		}
	}

	protected void writeModel(Model model)
	{
		List<Resource> sortMe = new ArrayList();
		// Needed only for no prefixes, no blank first line. 
		boolean doingFirst = true;
		ResIterator rIter = listSubjects(model);
		for (; rIter.hasNext();)
		{
			// Subject:
			// First - it is something we will write out as a structure in an object field?
			// That is, a RDF list or the object of exactly one statement.
			Resource subject = (Resource) rIter.next();
			sortMe.add(subject);
		}
		rIter.close();

		Collections.sort(sortMe, NumericStringComparitor.resourceComparator);

		for (Resource subject : sortMe) {
			if (skipThisSubject(subject))
			{
				if (N3JenaWriter.DEBUG)
					out.println("# Skipping: " + formatResource(subject));
				continue;
			}

			// We really are going to print something via writeTriples
			if (doingFirst)
				doingFirst = false;
			else
				out.println();

			writeOneGraphNode(subject);
		}

	}
}