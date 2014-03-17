package org.appdapter.core.store;

import java.util.ArrayList;
import java.util.Comparator;

import org.appdapter.core.log.Debuggable;

import com.hp.hpl.jena.rdf.model.Resource;

public final class NumericStringComparitor implements Comparator {
	final public static Comparator<Resource> resourceComparator = new NumericStringComparitor();
	final public static Comparator<String> stringComparator = new NumericStringComparitor();

	public int compare(String s1, String s2) {
		return compareStrings(s1, s2);
	}

	@Override public int compare(Object o1, Object o2) {
		if (o1 instanceof Resource || o2 instanceof Resource) {
			return compare((Resource) o1, (Resource) o2);
		}
		return compare((String) o1, (String) o2);
	}

	public int compare(Resource o1, Resource o2) {
		int c = compare(mostImportant(o1), mostImportant(o2), true);
		if (c != 0)
			return c;
		if (!o1.equals(o2)) {
			Debuggable.notImplemented("compare", o1, o2);
		}
		return 1;
	}

	public static int compare(String[] s1, String[] s2, boolean mayDemposeString) {
		int i = 0;
		while (true) {
			if (i == s1.length) {
				if (s1.length == s2.length) {
					return 0;
				}
				return -1;
			} else {
				if (i == s2.length) {
					return 1;
				}
			}
			if (mayDemposeString) {
				int comp = compareStrings(s1[i], s2[i]);
				if (comp != 0)
					return comp;
			} else {
				int comp = compareNoDecomp(s1[i], s2[i]);
				if (comp != 0)
					return comp;
			}
			i++;
		}
	}

	public static int compareNoDecomp(String s1, String s2) {
		if (s1 == null) {
			if (s2 == null) {
				return 0;
			}
			return 1;
		}
		if (s2 == null) {
			return -1;
		}
		if (s1.length() == 0) {
			if (s2.length() == 0) {
				return 0;
			}
			return -1;
		}
		if (s2.length() == 0) {
			return 1;
		}
		if (s1.length() > 0) {
			if (Character.isDigit(s1.charAt(0)) && Character.isDigit(s2.charAt(0))) {
				Double d1 = calcValue(s1);
				Double d2 = calcValue(s2);
				int mcomp = (int) Math.signum(d1.compareTo(d2));
				if (mcomp != 0)
					return mcomp;
			}
		}
		int comp = s1.compareToIgnoreCase(s2);
		if (comp == 0)
			comp = s1.compareTo(s2);
		if (comp == 0)
			return 0;
		return comp;
	}

	public static Double calcValue(String s2) {
		try {
			return Double.parseDouble(s2);
		} catch (NumberFormatException nfe) {
			return Double.NaN;
		}
	}

	public static int compareStrings(String s1, String s2) {
		if (s1 == null) {
			if (s2 == null) {
				return 0;
			}
			return 1;
		}
		if (s2 == null) {
			return -1;
		}
		if (s1.length() == 0) {
			if (s2.length() == 0) {
				return 0;
			}
			return -1;
		}
		if (s2.length() == 0) {
			return 1;
		}
		String tokens1[] = tokens(s1);
		String tokens2[] = tokens(s2);
		return compare(tokens1, tokens2, false);
	}

	public static String[] tokens(String s2) {
		ArrayList<String> al = new ArrayList<String>();
		StringBuffer sofar = new StringBuffer();
		boolean wasDigit = Character.isDigit(s2.charAt(0));
		char[] charArray = s2.toCharArray();
		for (int j = 0; j < charArray.length; j++) {
			char c = charArray[j];
			boolean isDigit = Character.isDigit(c);
			if (wasDigit != isDigit) {
				wasDigit = isDigit;
				al.add(sofar.toString());
				sofar = new StringBuffer();
				sofar.append(c);
			} else {
				sofar.append(c);
			}
		}
		if (sofar.length() > 0) {
			al.add(sofar.toString());
		}
		return (String[]) al.toArray(new String[al.size()]);
	}

	public static void main(String[] args) {
		showCompare("abc223", "223abc");
		showCompare("a22", "a2");
		showCompare("abcd", "a2");
		showCompare("M1L3P1ForTablet_control_12", "M1L3P1ForTablet_control_13");
		showCompare("M1L3P1ForTablet_control_12", "M1L3P1ForTablet_control_11");
		showCompare("M1L3P1ForTablet_control_12", "M1L3P1ForTablet_control_2");
	}

	static String[] sgn = new String[] { "<", "==", ">" };

	private static void showCompare(String s1, String s2) {
		int comp = compareStrings(s1, s2);
		System.out.println(s1 + " " + sgn[(int) Math.signum(comp) + 1] + " " + s2);
	}

	private String[] mostImportant(Resource o2) {
		if (o2.isAnon()) {
			return new String[] { "", "" + o2.getId() };
		}
		if (o2.isURIResource()) {
			String ns = o2.getNameSpace();
			if (ns != null && ns.contains("w3.")) {
				// push W3C higher 
				return new String[] { "", o2.getLocalName() };
			}
			return new String[] { o2.getLocalName(), ns };
		}
		//if (o2.isLiteral()) {
		return new String[] { null, "\"" + o2 };
		//}
	}

}