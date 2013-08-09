package org.appdapter.gui.browse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.appdapter.bind.rdf.jena.model.JenaLiteralUtils;
import org.appdapter.bind.rdf.jena.model.ModelStuff;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class ResourceToFromString extends ToFromKeyConverter<Object, String> {

	private static Map<String, String> knownNamespaces = new HashMap();
	static Model oenhmodel;
	Model enhmodel;

	public ResourceToFromString(Model model) {
		super(Object.class, String.class);
		enhmodel = model;
		offerModel(model);
	}

	private void offerModel(Model model2) {
		if (model2 == null)
			return;
		oenhmodel = model2;
		Map map2 = model2.getNsPrefixMap();

		knownNamespaces.putAll(map2);

	}

	@Override public String toKey(Object toBecomeAString) {
		RDFNode rdfnode = null;
		Model model = getModel(toBecomeAString);
		offerModel(model);
		Object literalOrNode_URI = JenaLiteralUtils.cvtToString(toBecomeAString, model);
		String str = "" + literalOrNode_URI;
		if (false) {
			Object r = fromKey(str, RDFNode.class);
			if (!(r instanceof RDFNode)) {
				str = "" + literalOrNode_URI;
			}
		}
		return str;
	}

	public Model getModel(Object toBecomeAString) {
		if (toBecomeAString instanceof RDFNode) {
			return (((RDFNode) toBecomeAString).getModel());
		}
		Model model = enhmodel;
		if (model == null) {
			model = oenhmodel;
		}
		return model;
	}

	@Override public Object fromKey(String title, Class further) {

		try {
			Model model = enhmodel;
			if (model == null) {
				model = oenhmodel;
			}
			if (model == null) {
				model = ModelFactory.createDefaultModel();
				model.setNsPrefixes(knownNamespaces);
			}

			if (true) {
				Node node = ModelStuff.create(model, title);
				if (further.isInstance(node))
					return node;
				return new ResourceImpl(node, (EnhGraph) model);
			}
			RDFReader reader = model.getReader("N3");
			reader.read(model, new StringInputStream("<#pat> <#knows> " + title + " .", Charset.defaultCharset()), "http://noprefix.com/noprefix#");

			return model.listStatements().next().getObject();
		} catch (Throwable t) {
			//	printStackTrace(t, System.err, 3);
			return null;
		}
	}

	static public class StringInputStream extends InputStream {

		/* # of characters to buffer - must be >=2 to handle surrogate pairs */
		private static final int CHAR_CAP = 8;

		private final Queue<Byte> buffer = new LinkedList<Byte>();
		private final Writer encoder;
		private final String data;
		private int index;

		public StringInputStream(String sequence, Charset charset) {
			data = sequence;
			encoder = new OutputStreamWriter(new OutputStreamBuffer(), charset);
		}

		private int buffer() throws IOException {
			if (index >= data.length()) {
				return -1;
			}
			int rlen = index + CHAR_CAP;
			if (rlen > data.length()) {
				rlen = data.length();
			}
			for (; index < rlen; index++) {
				char ch = data.charAt(index);
				encoder.append(ch);
				// ensure data enters buffer
				encoder.flush();
			}
			if (index >= data.length()) {
				encoder.close();
			}
			return buffer.size();
		}

		@Override public int read() throws IOException {
			if (buffer.size() == 0) {
				int r = buffer();
				if (r == -1) {
					return -1;
				}
			}
			return 0xFF & buffer.remove();
		}

		private class OutputStreamBuffer extends OutputStream {

			@Override public void write(int i) throws IOException {
				byte b = (byte) i;
				buffer.add(b);
			}

		}

	}

}