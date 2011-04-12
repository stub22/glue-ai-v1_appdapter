package net.peruser.binding.jena;

import net.peruser.core.name.Address;
import net.peruser.core.name.Abbreviator;

import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
 
public class JenaAddress extends Address {
	Resource	myJenaResource;
	
	public JenaAddress(Resource r) {
		myJenaResource = r;
	}
	public JenaAddress(String longForm) {
		myJenaResource = ModelUtils.makeUnattachedResource(longForm);
	}
	public String getLongFormString() {
		// Will return null if myJenaResource is a bnode.
		return myJenaResource.getURI();
	}
	public Resource getJenaResource() {
		return myJenaResource;
	}
	/*
	public JenaAddress (Abbreviator abb, Resource jenaResource) {
		super(abb, jenaResource.getURI());
		myJenaResource = jenaResource;
	}
	*/
	/*
	public int unresolvedHashCode() {
		return myJenaResource.hashCode();
	}
	public boolean unresolvedEquals(Address a) {
		if (a instanceof JenaAddress) {
			return myJenaResource.equals(((JenaAddress) a).myJenaResource);
		} else {
			return false;
		}
	}
	public String unresolvedDebugString() {
		return myJenaResource.toString();
	}
	*/	
}
