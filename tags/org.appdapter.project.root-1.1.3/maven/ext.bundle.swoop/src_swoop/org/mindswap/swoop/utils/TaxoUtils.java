package org.mindswap.swoop.utils;

import java.io.PrintWriter;

import org.mindswap.pellet.KnowledgeBase;
import org.mindswap.pellet.debug.Taxonomy;
import org.mindswap.pellet.debug.output.OutputFormatter;

public class TaxoUtils {

	public static void printTax(Taxonomy t) {
		printTax(t, new PrintWriter(System.out));
	}

	public static void printTax(Taxonomy t, PrintWriter output) {
		output.print("TODO PRINT " + t);

	}

	public static void printTax(org.mindswap.pellet.taxonomy.Taxonomy t) {
		System.out.println("TODO PRINT " + t);
	}

	public static void printTax(org.mindswap.pellet.taxonomy.Taxonomy t, OutputFormatter output) {
		output.print("TODO PRINT " + t);

	}

	public static org.mindswap.pellet.taxonomy.Taxonomy cloneTax(org.mindswap.pellet.taxonomy.Taxonomy t) {
		System.out.println("TODO CLONE " + t);
		// TODO Auto-generated method stub
		return t;
	}

	private static String compareTaxonomy(Taxonomy original, Taxonomy fullTaxonomy) {
		if (true)
			throw new RuntimeException("//TODO Auto-generated method stub");
		return null;
	}

	private static Taxonomy merge(Taxonomy tax1, Taxonomy tax2) {
		if (true)
			throw new RuntimeException("//TODO Auto-generated method stub");
		return null;
	}

	public static org.mindswap.pellet.taxonomy.Taxonomy clone(org.mindswap.pellet.taxonomy.Taxonomy auxTax) {
		// TODO Auto-generated method stub
		if (true)
			throw new RuntimeException("//TODO Auto-generated method stub");
		return null;
	}

	public static org.mindswap.pellet.taxonomy.Taxonomy merge(org.mindswap.pellet.taxonomy.Taxonomy tax1, org.mindswap.pellet.taxonomy.Taxonomy tax2) {
		// TODO Auto-generated method stub
		if (true)
			throw new RuntimeException("//TODO Auto-generated method stub");
		return null;
	}

	public static void printTax(org.mindswap.pellet.taxonomy.Taxonomy fullTaxonomy, org.mindswap.pellet.output.OutputFormatter output2) {
		// TODO Auto-generated method stub
		if (true)
			throw new RuntimeException("//TODO Auto-generated method stub");
	}

	public static String compareTaxonomy(org.mindswap.pellet.taxonomy.Taxonomy original, org.mindswap.pellet.taxonomy.Taxonomy fullTaxonomy) {
		// TODO Auto-generated method stub
		if (true)
			throw new RuntimeException("//TODO Auto-generated method stub");
		return null;
	}

	public static void setOntology(KnowledgeBase kb, String string) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		if (true)
			throw new RuntimeException("//TODO Auto-generated method stub");
	}
}
