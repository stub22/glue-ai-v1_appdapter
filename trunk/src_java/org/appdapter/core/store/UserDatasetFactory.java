package org.appdapter.core.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sparql.ARQException;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.core.assembler.DatasetAssembler;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import com.hp.hpl.jena.sparql.util.DatasetUtils;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils;
import com.hp.hpl.jena.util.FileManager;

public interface UserDatasetFactory {

	public class JenaSDBWrappedDatasetFactory extends JenaDatasetFactory implements UserDatasetFactory {

		/** Create a dataset
		 * @param dataset
		 * @return Dataset
		 */
		public Dataset create(Dataset dataset) {
			return RepoOper.linkWithShared(dataset);
		}

	}

	public class JenaDatasetFactory extends AbstractDatasetFactory {

		@Override public Dataset createDefault() {
			return createMemFixed();
		}

		/** Create an in-memory, modifiable Dataset */
		public Dataset createMem() {
			return create(DatasetGraphFactory.createMem());
		}

		/** Create an in-memory, modifiable Dataset.
		 * New graphs must be explicitly added using .addGraph.
		 */
		public Dataset createMemFixed() {
			return create(DatasetGraphFactory.createMemFixed());
		}

		/** Create an in-memory, modifable Dataset
		 * @deprecated Use createMem
		 */
		@Deprecated public Dataset create() {
			// This may not be a defaultJena model - during testing,
			// we use a graph that is not value-aware for xsd:String vs plain literals.
			return new DatasetImpl(ModelFactory.createModelForGraph(GraphFactory.createDefaultGraph()));
		}

		/** Create a dataset with the given model as the default graph
		 * @param model
		 * @return Dataset
		 */
		public Dataset create(Model model) {
			return new DatasetImpl(model);
		}

		/** Create a dataset
		 * @param dataset
		 * @return Dataset
		 */
		public Dataset create(Dataset dataset) {
			return new DatasetImpl(dataset);
		}

		/** Wrap a datasetgraph to make a mutable dataset
		 * @param dataset DatasetGraph
		 * @return Dataset
		 */
		public Dataset create(DatasetGraph dataset) {
			return DatasetImpl.wrap(dataset);
		}

		@Override public boolean canCreateType(String typeOf, String sharedNameIgnoredPresently) {
			if (typeOf != null && registeredUserDatasetFactoryByName.get(typeOf) == this)
				return true;
			if (sharedNameIgnoredPresently != null && registeredUserDatasetFactoryByName.get(sharedNameIgnoredPresently) == this)
				return true;
			return false;
		}

		@Override public Dataset createType(String typeOf, String sharedNameIgnoredPresently) {
			return createDefault();
		}

	}

	public abstract class AbstractDatasetFactory implements UserDatasetFactory {

		@Override public Dataset createDefault() {
			return createMemFixed();
		}

		/** Create an in-memory, modifiable Dataset */
		public Dataset createMem() {
			return create(DatasetGraphFactory.createMem());
		}

		/** Create an in-memory, modifiable Dataset.
		 * New graphs must be explicitly added using .addGraph.
		 */
		public Dataset createMemFixed() {
			return create(DatasetGraphFactory.createMemFixed());
		}

		/** Create an in-memory, modifable Dataset
		 * @deprecated Use createMem
		 */
		@Deprecated public Dataset create() {
			// This may not be a defaultJena model - during testing,
			// we use a graph that is not value-aware for xsd:String vs plain literals.
			return create(ModelFactory.createModelForGraph(GraphFactory.createDefaultGraph()));
		}

		/** Create a dataset with the given model as the default graph
		 * @param model
		 * @return Dataset
		 */
		public Dataset create(Model model) {
			return create(new DatasetImpl(model));
		}

		/** Wrap a datasetgraph to make a mutable dataset
		 * @param dataset DatasetGraph
		 * @return Dataset
		 */
		public Dataset create(DatasetGraph dataset) {
			Dataset ds = DatasetImpl.wrap(dataset);
			return create(ds);
		}

		/** Create a named graph container based on two list of URIs.
		 *  The first is used to create the background (unnamed graph), the
		 *  second is used to create the collection of named graphs.
		 *
		 *  (Jena calls graphs "Models" and triples "Statements")
		 *
		 * @param uriList          graphs to be loaded into the unnamed, default graph
		 * @param namedSourceList  graphs to be atatched as named graphs
		 * @param fileManager
		 * @param baseURI          baseURI for relative URI expansion
		 * @return Dataset
		 */

		//		abstract public Dataset create(List<String> uriList, List<String> namedSourceList, FileManager fileManager, String baseURI);

		public Dataset create(List<String> uriList, List<String> namedSourceList, FileManager fileManager, String baseURI) {
			// Fixed dataset - any GRAPH <notThere> in a query must return no match.
			Dataset ds = createDefault();
			DatasetUtils.addInGraphs(ds, uriList, namedSourceList, fileManager, baseURI);
			return ds;
		}

		/** Create a dataset based on a list of URIs : these are merged into the default graph of the dataset.
		 *
		 * @param uriList   URIs merged to form the default dataset
		 * @return Dataset
		 */

		public Dataset create(List<String> uriList) {
			return create(uriList, null, null, null);
		}

		/** Create a dataset with a default graph and no named graphs
		 *
		 * @param uri   URIs merged to form the default dataset
		 * @return Dataset
		 */

		public Dataset create(String uri) {
			return create(uri, null, null, null);
		}

		/** Create a dataset based on a list of URIs : these are merged into the default graph of the dataset.
		 *
		 * @param uriList   URIs merged to form the default dataset
		 * @param fileManager
		 * @return Dataset
		 */

		public Dataset create(List<String> uriList, FileManager fileManager) {
			return create(uriList, null, fileManager, null);
		}

		/** Create a dataset based on a list of URIs : these are merged into the default graph of the dataset.
		 *
		 * @param uri              graph to be loaded into the unnamed, default graph
		 * @param fileManager
		 * @return Dataset
		 */

		public Dataset create(String uri, FileManager fileManager) {
			return create(uri, null, fileManager, null);
		}

		/** Create a named graph container of graphs based on a list of URIs.
		 *
		 * @param namedSourceList
		 * @param fileManager
		 * @return Dataset
		 */

		public Dataset createNamed(List<String> namedSourceList, FileManager fileManager) {
			return create((List<String>) null, namedSourceList, fileManager, null);
		}

		/** Create a dataset based on two list of URIs.
		 *  The first lists is used to create the background (unnamed graph) by merging, the
		 *  second is used to create the collection of named graphs.
		 *
		 *  (Jena calls graphs "Models" and triples "Statements")
		 *
		 * @param uriList          graphs to be loaded into the unnamed, default graph
		 * @param namedSourceList  graphs to be atatched as named graphs
		 * @return Dataset
		 */

		public Dataset create(List<String> uriList, List<String> namedSourceList) {
			return create(uriList, namedSourceList, null, null);
		}

		/** Create a dataset container based on two list of URIs.
		 *  The first is used to create the background (unnamed graph), the
		 *  second is used to create the collection of named graphs.
		 *
		 *  (Jena calls graphs "Models" and triples "Statements")
		 *
		 * @param uri              graph to be loaded into the unnamed, default graph
		 * @param namedSourceList  graphs to be attached as named graphs
		 * @return Dataset
		 */

		public Dataset create(String uri, List<String> namedSourceList) {
			return create(uri, namedSourceList, null, null);
		}

		/** Create a named graph container based on two list of URIs.
		 *  The first is used to create the background (unnamed graph), the
		 *  second is used to create the collection of named graphs.
		 *
		 *  (Jena calls graphs "Models" and triples "Statements")
		 *
		 * @param uri              graph to be loaded into the unnamed, default graph
		 * @param namedSourceList  graphs to be atatched as named graphs
		 * @param fileManager
		 * @param baseURI          baseURI for relative URI expansion
		 * @return Dataset
		 */

		public Dataset create(String uri, List<String> namedSourceList, FileManager fileManager, String baseURI) {
			List<String> uriList = new ArrayList<String>();
			uriList.add(uri);
			return create(uriList, namedSourceList, fileManager, baseURI);
		}

		//	     public  Dataset make(Dataset ds)
		//	    {
		//	        DataSourceImpl ds2 = new DataSourceImpl(ds) ;
		//	        return ds2 ;
		//	    }

		//	     public  Dataset make(Dataset ds, Graph defaultGraph)
		//	    {
		//	        DataSourceImpl ds2 = new DataSourceImpl(ds) ;
		//	        ds2.setDefaultGraph(defaultGraph) ;
		//	        return ds2 ;
		//	    }

		public Dataset make(Dataset ds, Model defaultModel) {
			Dataset ds2 = new DatasetImpl(ds);
			ds2.setDefaultModel(defaultModel);
			return ds2;
		}

		// Assembler.
		/** Assembler a dataset from the model in a file
		 *
		 * @param filename      The filename
		 * @return Dataset
		 */
		public Dataset assemble(String filename) {
			Model model = FileManager.get().loadModel(filename);
			return assemble(model);
		}

		/** Assembler a dataset from the model in a file
		 *
		 * @param filename      The filename
		 * @param  resourceURI  URI for the dataset to assembler
		 * @return Dataset
		 */
		public Dataset assemble(String filename, String resourceURI) {
			Model model = FileManager.get().loadModel(filename);
			Resource r = model.createResource(resourceURI);
			return assemble(r);
		}

		/** Assembler a dataset from the model
		 *
		 * @param model
		 * @return Dataset
		 */
		public Dataset assemble(Model model) {
			Resource r = GraphUtils.findRootByType(model, DatasetAssembler.getType());
			if (r == null)
				throw new ARQException("No root found for type <" + DatasetAssembler.getType() + ">");

			return assemble(r);
		}

		/** Assembler a dataset from a resource
		 *
		 * @param resource  The resource for the dataset
		 * @return Dataset
		 */

		public Dataset assemble(Resource resource) {
			Dataset ds = (Dataset) Assembler.general.open(resource);
			return ds;
		}

	}

	UserDatasetFactory jenaDatasetFactory = new JenaDatasetFactory();
	UserDatasetFactory jenaSDBDatasetFactory = new JenaSDBWrappedDatasetFactory();
	Map<String, UserDatasetFactory> registeredUserDatasetFactoryByName = new HashMap<String, UserDatasetFactory>();
	List<UserDatasetFactory> registeredUserDatasetFactorys = new ArrayList<UserDatasetFactory>();

	Object lock = new Object() {
		{
			RepoOper.registerDatasetFactory("default", jenaDatasetFactory);
			RepoOper.registerDatasetFactory("jena", jenaDatasetFactory);
			RepoOper.registerDatasetFactory("shared", jenaSDBDatasetFactory);
		}
	};

	/** Create an in-memory, modifiable Dataset */
	public Dataset createMem();

	/** Create an in-memory, modifiable Dataset.
	 * New graphs must be explicitly added using .addGraph.
	 */
	public Dataset createMemFixed();

	/** Create a dataset with the given model as the default graph
	 * @param model
	 * @return Dataset
	 */
	public Dataset create(Model model);

	/** Create a dataset
	 * @param dataset
	 * @return Dataset
	 */
	public Dataset create(Dataset dataset);

	/** Wrap a datasetgraph to make a mutable dataset
	 * @param dataset DatasetGraph
	 * @return Dataset
	 */
	public Dataset create(DatasetGraph dataset);

	/** Create a dataset based on a list of URIs : these are merged into the default graph of the dataset.
	 *
	 * @param uriList   URIs merged to form the default dataset
	 * @return Dataset
	 */

	public Dataset create(List<String> uriList);

	/** Create a dataset with a default graph and no named graphs
	 *
	 * @param uri   URIs merged to form the default dataset
	 * @return Dataset
	 */

	public Dataset create(String uri);

	/** Create a dataset based on a list of URIs : these are merged into the default graph of the dataset.
	 *
	 * @param uriList   URIs merged to form the default dataset
	 * @param fileManager
	 * @return Dataset
	 */

	public Dataset create(List<String> uriList, FileManager fileManager);

	public Dataset createDefault();

	public boolean canCreateType(String typeOf, String sharedNameIgnoredPresently);

	public Dataset createType(String typeOf, String sharedNameIgnoredPresently);

}
