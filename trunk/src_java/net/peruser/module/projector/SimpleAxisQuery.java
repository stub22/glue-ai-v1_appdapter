package net.peruser.module.projector;

/**  navigation-direction-marker for a projector-module query-property. 
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class SimpleAxisQuery {
	// Extend this to handle RDF Bag,Seq?
	public static int		PARENT_POINTS_TO_CHILD=0;
	public static int		CHILD_POINTS_TO_PARENT=1;
	
	private int		myDirection;
	private String	myPropertyURI;
	
	public SimpleAxisQuery (String propertyURI, int direction) {
		myDirection = direction;
		myPropertyURI = propertyURI;
	}
	public String	getPropertyURI() {
		return myPropertyURI;
	}
	public int getDirection() {
		return myDirection;
	}
}

