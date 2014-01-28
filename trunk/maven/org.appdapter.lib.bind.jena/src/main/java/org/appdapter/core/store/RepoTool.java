package org.appdapter.core.store;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.appdapter.core.matdat.OnlineSheetRepoSpec;
import org.appdapter.core.matdat.RepoSpec;
import org.appdapter.core.matdat.URLRepoSpec;

public class RepoTool {
	static String example = "--uri goog://0AmvzRRq-Hhz7dFVpSDFaaHhMWmVPRFl4RllXSHVxb2c/9/8 --test --write c:\\GluePuma_R25_TestFull\\";
	static String example1 = "--scandir ./loadAll/ --test --write c:\\GluePuma_R25_TestFull";
	static String example2 = "0AmvzRRq-Hhz7dFVpSDFaaHhMWmVPRFl4RllXSHVxb2c 9 8 --write c:\\GluePuma_R25_TestFull\\";

	public static void main(String[] args) {
		if (args.length <= 2) {
			if (args.length == 0) {
				args = example.split(" ");
				System.out.println("Not enough arguments.  USING: " + example);
			} else {
				System.out.println("Not enough arguments.  "
						+ "Expected: --uri <URI> <--test | OutputDir>"
						+ "(ex. --uri goog:/0AmvzRRq-Hhz7dFVpSDFaaHhMWmVPRFl4RllXSHVxb2c/9/8 --test )");
			}
		}
		ArrayList<String> al = new ArrayList<String>();
		for (String a0 : args) {
			String a = a0.trim();
			al.add(a);
		}

		RepoSpec spec = getRepoSpec(al);
		if (spec == null)
			return;
		String dir = getRepoDir(al);
		if (dir == null || dir.equals("--test")) {
			verifyRepo(spec);
			return;
		}
		writeRepo(spec.makeRepo(), dir);
	}

	private static void verifyRepo(RepoSpec spec) {
		spec.makeRepo();
	}

	public static RepoSpec getRepoSpec(List<String> args) {
		List<ClassLoader> fileModelCLs = Arrays.asList(ClassLoader.getSystemClassLoader());
		int argslength = args.size();
		if (argslength == 0) {
			System.out.println("Not enough arguments.  "
					+ "Expected: --uri [URI]"
					+ "(ex. --uri goog:/0AmvzRRq-Hhz7dFVpSDFaaHhMWmVPRFl4RllXSHVxb2c/9/8)");
			return null;
		}
		String key = args.get(0);
		String lkey = key.toLowerCase();
		if (lkey.equals("--uri"))
		{
			if (argslength < 2) {
				System.out.println("Not enough arguments.  "
						+ "Expected: --uri [URI]"
						+ "(ex. --uri goog:/0AmvzRRq-Hhz7dFVpSDFaaHhMWmVPRFl4RllXSHVxb2c/9/8 GluePuma_R25_TestFull)");
				return null;
			}
			String dirModelURL = args.get(1);
			removeArgs(args, 2);
			return new URLRepoSpec(dirModelURL, fileModelCLs);
		}

		if (lkey.equals("--scandir"))
		{
			if (argslength < 2) {
				System.out.println("Not enough arguments.  "
						+ "Expected: --scandir [URI]"
						+ "(ex. --uri goog:/0AmvzRRq-Hhz7dFVpSDFaaHhMWmVPRFl4RllXSHVxb2c/9/8 GluePuma_R25_TestFull)");
				return null;
			}
			String dirModelURL = args.get(1);
			removeArgs(args, 2);
			return new ScanURLRepoSpec(dirModelURL, fileModelCLs);
		}

		if (argslength < 3) {
			System.out.println("Not enough arguments.  "
					+ "Expected: [SheetKey] [Namespace Tab] [Dir Tab]"
					+ "(ex. 0AmvzRRq-Hhz7dFVpSDFaaHhMWmVPRFl4RllXSHVxb2c 9 8");
			return null;
		}

		Integer nmspc;
		Integer dir;
		try {
			nmspc = Integer.parseInt(args.get(1));
			dir = Integer.parseInt(args.get(2));
		} catch (NumberFormatException ex) {
			System.out.println("Bad Namespace or Dir number: " + args.get(1) + ", " + args.get(2));
			return null;
		}
		removeArgs(args, 3);
		return new OnlineSheetRepoSpec(key, nmspc, dir, fileModelCLs);
	}

	private static void removeArgs(List<String> args, int i) {
		while (i-- > 0)
			args.remove(0);

	}

	private static String getRepoDir(List<String> args) {
		if (args.size() == 0)
			return "SavedRepoDir";
		for (String a : args) {
			if (a.startsWith("-")) {
				continue;
			}
			return a;
		}
		return null;
	}

	private static void writeRepo(Repo repo, String dir) {
		if (!(repo instanceof Repo.WithDirectory)) {
			System.out.println("Not Repo.WithDirectory  " + repo.getClass() + " " + repo);
			return;
		}
		try {
			File d = new File(dir);
			System.out.println("Writing repo to " + d.getAbsolutePath() + " ...");
			d.delete();
			RepoOper.writeRepoToDirectory(repo, dir, false);
			System.out.println("Writing repo complete");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}