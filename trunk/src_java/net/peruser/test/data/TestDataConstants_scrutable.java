package net.peruser.test.data;

import net.peruser.core.name.Address;
import net.peruser.core.name.CoreAbbreviator;

import java.util.Map;
import java.util.HashMap;

/** Unit testing default data contents, which are sometimes overridden with dynamic configuration data.
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class TestDataConstants_scrutable {

	public static class AssemblerUnitTestConstants {
		// public static final  String	testAssemblerModelDescURL = "file:app/picky/rdf/picky_sql_model_assembly.ttl";
		// public static final  String	comboAssemblerFileURL = "file:app/picky/rdf/picky_combo_dataset_assembly.ttl";
		public static final String	AT_pickyPrefixURI = "http://www.peruser.net/picky#";
		public static final String  AT_cameraKeysModelURI = AT_pickyPrefixURI + "camera_keys_model";
		public static final String  AT_cameraComboDatasetURI = AT_pickyPrefixURI + "combo_camera_dataset";
		
		public static final String AT_cameraQueryLoc = "file:app/picky/sparql/picky_cameras_named_new.sparql";
		
		public static final String  AT_deltaDocPath = "file:app/picky/test_xml/amazonCameraFeatureModel_bigger_sample.xml";
	}
	/*
	public static class ModelUtilsUnitTestConstants {
		public static final String dhuOntURI 	= "http://loki.cae.drexel.edu/~how/upper/upper";
		public static final String dhuOntURL 	= "file:/D:/_mount/prj/peruser_trunk/substrate/owl/drexel_hydro_upper.owl";
		public static final String dhdOntURI 	= "http://loki.cae.drexel.edu/~how/HydrologicUnits/hu";
		// String dhdOntURL 	= "file:/D:/_mount/prj/peruser_trunk/substrate/owl/drexel_hydro_data.owl";
		public static final String dhdOntURL 	= "file:/D:/_mount/prj/peruser_trunk/substrate/owl/hydro/dhd_protege_sixteenth.owl";
		public static final String subOntURI 	= "http://www.peruser.net/substrate.owl";
		public static final String subOntURL 	= "file:/D:/_mount/prj/peruser_trunk/substrate/owl/substrate.owl";
	}
	*/
	
	/*
	private static	Address parseShortAddress(String shortForm) {
		return getAbbreviator().makeAddressFromShortForm(shortForm);
	}
	private static CoreAbbreviator	theAbbreviator = null;
	public static CoreAbbreviator getAbbreviator() {
		if (theAbbreviator == null) {
			HashMap<String,String> prefixMap = new HashMap<String,String> ();
			// prefixMap.put(SubstrateAddressConstants.substrateAlias, SubstrateAddressConstants.substrateNS);
			theAbbreviator =  new CoreAbbreviator(prefixMap,  ":", null);  
		}
		return theAbbreviator;
	}
	*/
}
