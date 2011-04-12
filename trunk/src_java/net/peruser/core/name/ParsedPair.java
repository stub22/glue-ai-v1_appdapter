package net.peruser.core.name;

/**
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class ParsedPair {
	public String	left, right;

	public static ParsedPair parsePair(String unparsed, String splitter) {
		ParsedPair pp = new ParsedPair();
		int		splitterIndex = unparsed.indexOf(splitter);
		String prefix = null, ident;
		if (splitterIndex != -1) {
			pp.left = unparsed.substring(0, splitterIndex);
			pp.right = unparsed.substring(splitterIndex + splitter.length());
		} else {
			pp = null;
			// throw new Exception ("pair-string " + unparsed + " does not contain expected splitter " + splitter);
		}
		return pp;
	}
}

