/*
 * 	 * * Copied from https://github.com/EsotericSoftware/wildcard/blob/master/src/com/esotericsoftware/wildcard/*.java
	 * Collects filesystem paths using wildcards, preserving the directory structure. Copies, deletes, and zips paths.
	 * 
 */

package org.appdapter.core.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**

 */

public class ExtendedFileLoading {
	/**
	 * 
	 * 
	 
	 * */
	static public class Paths implements Iterable<String> {
		static private final Comparator<Path> LONGEST_TO_SHORTEST = new Comparator<Path>() {
			public int compare(Path s1, Path s2) {
				return s2.absolute().length() - s1.absolute().length();
			}
		};

		static class GlobScanner {
			private final File rootDir;
			private final List<String> matches = new ArrayList(128);

			public GlobScanner(File rootDir, List<String> includes, List<String> excludes, boolean ignoreCase) {
				if (rootDir == null)
					throw new IllegalArgumentException("rootDir cannot be null.");
				if (!rootDir.exists())
					throw new IllegalArgumentException("Directory does not exist: " + rootDir);
				if (!rootDir.isDirectory())
					throw new IllegalArgumentException("File must be a directory: " + rootDir);
				try {
					rootDir = rootDir.getCanonicalFile();
				} catch (IOException ex) {
					throw new RuntimeException("OS error determining canonical path: " + rootDir, ex);
				}
				this.rootDir = rootDir;

				if (includes == null)
					throw new IllegalArgumentException("includes cannot be null.");
				if (excludes == null)
					throw new IllegalArgumentException("excludes cannot be null.");

				if (includes.isEmpty())
					includes.add("**");
				List<Pattern> includePatterns = new ArrayList(includes.size());
				for (String include : includes)
					includePatterns.add(new Pattern(include, ignoreCase));

				List<Pattern> allExcludePatterns = new ArrayList(excludes.size());
				for (String exclude : excludes)
					allExcludePatterns.add(new Pattern(exclude, ignoreCase));

				scanDir(rootDir, includePatterns);

				if (!allExcludePatterns.isEmpty()) {
					// For each file, see if any exclude patterns match.
					outerLoop:
					//
					for (Iterator matchIter = matches.iterator(); matchIter.hasNext();) {
						String filePath = (String) matchIter.next();
						List<Pattern> excludePatterns = new ArrayList(allExcludePatterns);
						try {
							// Shortcut for excludes that are "**/XXX", just check file name.
							for (Iterator excludeIter = excludePatterns.iterator(); excludeIter.hasNext();) {
								Pattern exclude = (Pattern) excludeIter.next();
								if (exclude.values.length == 2 && exclude.values[0].equals("**")) {
									exclude.incr();
									String fileName = filePath.substring(filePath.lastIndexOf(File.separatorChar) + 1);
									if (exclude.matches(fileName)) {
										matchIter.remove();
										continue outerLoop;
									}
									excludeIter.remove();
								}
							}
							// Get the file names after the root dir.
							String[] fileNames = filePath.split("\\" + File.separator);
							for (String fileName : fileNames) {
								for (Iterator excludeIter = excludePatterns.iterator(); excludeIter.hasNext();) {
									Pattern exclude = (Pattern) excludeIter.next();
									if (!exclude.matches(fileName)) {
										excludeIter.remove();
										continue;
									}
									exclude.incr(fileName);
									if (exclude.wasFinalMatch()) {
										// Exclude pattern matched.
										matchIter.remove();
										continue outerLoop;
									}
								}
								// Stop processing the file if none of the exclude patterns matched.
								if (excludePatterns.isEmpty())
									continue outerLoop;
							}
						} finally {
							for (Pattern exclude : allExcludePatterns)
								exclude.reset();
						}
					}
				}
			}

			private void scanDir(File dir, List<Pattern> includes) {
				if (!dir.canRead())
					return;

				// See if patterns are specific enough to avoid scanning every file in the directory.
				boolean scanAll = false;
				for (Pattern include : includes) {
					if (include.value.indexOf('*') != -1 || include.value.indexOf('?') != -1) {
						scanAll = true;
						break;
					}
				}

				if (!scanAll) {
					// If not scanning all the files, we know exactly which ones to include.
					List matchingIncludes = new ArrayList(1);
					for (Pattern include : includes) {
						if (matchingIncludes.isEmpty())
							matchingIncludes.add(include);
						else
							matchingIncludes.set(0, include);
						process(dir, include.value, matchingIncludes);
					}
				} else {
					// Scan every file.
					for (String fileName : dir.list()) {
						// Get all include patterns that match.
						List<Pattern> matchingIncludes = new ArrayList(includes.size());
						for (Pattern include : includes)
							if (include.matches(fileName))
								matchingIncludes.add(include);
						if (matchingIncludes.isEmpty())
							continue;
						process(dir, fileName, matchingIncludes);
					}
				}
			}

			private void process(File dir, String fileName, List<Pattern> matchingIncludes) {
				// Increment patterns that need to move to the next token.
				boolean isFinalMatch = false;
				List<Pattern> incrementedPatterns = new ArrayList();
				for (Iterator iter = matchingIncludes.iterator(); iter.hasNext();) {
					Pattern include = (Pattern) iter.next();
					if (include.incr(fileName)) {
						incrementedPatterns.add(include);
						if (include.isExhausted())
							iter.remove();
					}
					if (include.wasFinalMatch())
						isFinalMatch = true;
				}

				File file = new File(dir, fileName);
				if (isFinalMatch) {
					int length = rootDir.getPath().length();
					if (!rootDir.getPath().endsWith(File.separator))
						length++; // Lose starting slash.
					matches.add(file.getPath().substring(length));
				}
				if (!matchingIncludes.isEmpty() && file.isDirectory())
					scanDir(file, matchingIncludes);

				// Decrement patterns.
				for (Pattern include : incrementedPatterns)
					include.decr();
			}

			public List<String> matches() {
				return matches;
			}

			public File rootDir() {
				return rootDir;
			}

			static class Pattern {
				String value;
				boolean ignoreCase;
				final String[] values;

				private int index;

				Pattern(String pattern, boolean ignoreCase) {
					this.ignoreCase = ignoreCase;

					pattern = pattern.replace('\\', '/');
					pattern = pattern.replaceAll("\\*\\*[^/]", "**/*");
					pattern = pattern.replaceAll("[^/]\\*\\*", "*/**");
					if (ignoreCase)
						pattern = pattern.toLowerCase();

					values = pattern.split("/");
					value = values[0];
				}

				boolean matches(String fileName) {
					if (value.equals("**"))
						return true;

					if (ignoreCase)
						fileName = fileName.toLowerCase();

					// Shortcut if no wildcards.
					if (value.indexOf('*') == -1 && value.indexOf('?') == -1)
						return fileName.equals(value);

					int i = 0, j = 0;
					while (i < fileName.length() && j < value.length() && value.charAt(j) != '*') {
						if (value.charAt(j) != fileName.charAt(i) && value.charAt(j) != '?')
							return false;
						i++;
						j++;
					}

					// If reached end of pattern without finding a * wildcard, the match has to fail if not same length.
					if (j == value.length())
						return fileName.length() == value.length();

					int cp = 0;
					int mp = 0;
					while (i < fileName.length()) {
						if (j < value.length() && value.charAt(j) == '*') {
							if (j++ >= value.length())
								return true;
							mp = j;
							cp = i + 1;
						} else if (j < value.length() && (value.charAt(j) == fileName.charAt(i) || value.charAt(j) == '?')) {
							j++;
							i++;
						} else {
							j = mp;
							i = cp++;
						}
					}

					// Handle trailing asterisks.
					while (j < value.length() && value.charAt(j) == '*')
						j++;

					return j >= value.length();
				}

				String nextValue() {
					if (index + 1 == values.length)
						return null;
					return values[index + 1];
				}

				boolean incr(String fileName) {
					if (value.equals("**")) {
						if (index == values.length - 1)
							return false;
						incr();
						if (matches(fileName))
							incr();
						else {
							decr();
							return false;
						}
					} else
						incr();
					return true;
				}

				void incr() {
					index++;
					if (index >= values.length)
						value = null;
					else
						value = values[index];
				}

				void decr() {
					index--;
					if (index > 0 && values[index - 1].equals("**"))
						index--;
					value = values[index];
				}

				void reset() {
					index = 0;
					value = values[0];
				}

				boolean isExhausted() {
					return index >= values.length;
				}

				boolean isLast() {
					return index >= values.length - 1;
				}

				boolean wasFinalMatch() {
					return isExhausted() || (isLast() && value.equals("**"));
				}
			}
			public static void main(String[] args) {
				// System.out.println(new Paths("C:\\Java\\ls", "**"));
				List<String> includes = new ArrayList();
				includes.add("website/in*");
				// includes.add("**/lavaserver/**");
				List<String> excludes = new ArrayList();
				// excludes.add("**/*.php");
				// excludes.add("website/**/doc**");
				long start = System.nanoTime();
				List<String> files = new GlobScanner(new File(".."), includes, excludes, false).matches();
				long end = System.nanoTime();
				System.out.println(files.toString().replaceAll(", ", "\n").replaceAll("[\\[\\]]", ""));
				System.out.println((end - start) / 1000000f);
			}
		}

		static class RegexScanner {
			private final File rootDir;
			private final List<Pattern> includePatterns;
			private final List<String> matches = new ArrayList(128);

			public RegexScanner(File rootDir, List<String> includes, List<String> excludes) {
				if (rootDir == null)
					throw new IllegalArgumentException("rootDir cannot be null.");
				if (!rootDir.exists())
					throw new IllegalArgumentException("Directory does not exist: " + rootDir);
				if (!rootDir.isDirectory())
					throw new IllegalArgumentException("File must be a directory: " + rootDir);
				try {
					rootDir = rootDir.getCanonicalFile();
				} catch (IOException ex) {
					throw new RuntimeException("OS error determining canonical path: " + rootDir, ex);
				}
				this.rootDir = rootDir;

				if (includes == null)
					throw new IllegalArgumentException("includes cannot be null.");
				if (excludes == null)
					throw new IllegalArgumentException("excludes cannot be null.");

				includePatterns = new ArrayList();
				for (String include : includes)
					includePatterns.add(Pattern.compile(include, Pattern.CASE_INSENSITIVE));

				List<Pattern> excludePatterns = new ArrayList();
				for (String exclude : excludes)
					excludePatterns.add(Pattern.compile(exclude, Pattern.CASE_INSENSITIVE));

				scanDir(rootDir);

				for (Iterator matchIter = matches.iterator(); matchIter.hasNext();) {
					String filePath = (String) matchIter.next();
					for (Pattern exclude : excludePatterns)
						if (exclude.matcher(filePath).matches())
							matchIter.remove();
				}
			}

			private void scanDir(File dir) {
				for (File file : dir.listFiles()) {
					for (Pattern include : includePatterns) {
						int length = rootDir.getPath().length();
						if (!rootDir.getPath().endsWith(File.separator))
							length++; // Lose starting slash.
						String filePath = file.getPath().substring(length);
						if (include.matcher(filePath).matches()) {
							matches.add(filePath);
							break;
						}
					}
					if (file.isDirectory())
						scanDir(file);
				}
			}

			public List<String> matches() {
				return matches;
			}

			public File rootDir() {
				return rootDir;
			}

			public static void main(String[] args) {
				// System.out.println(new Paths("C:\\Java\\ls", "**"));
				List<String> includes = new ArrayList();
				includes.add("core[^T]+php");
				// includes.add(".*/lavaserver/.*");
				List<String> excludes = new ArrayList();
				// excludes.add("website/**/doc**");
				long start = System.nanoTime();
				List<String> files = new RegexScanner(new File("..\\website\\includes"), includes, excludes).matches();
				long end = System.nanoTime();
				System.out.println(files.toString().replaceAll(", ", "\n").replaceAll("[\\[\\]]", ""));
				System.out.println((end - start) / 1000000f);
			}
		}

		static private List<String> defaultGlobExcludes;

		final HashSet<Path> paths = new HashSet<Path>(32);

		/** Creates an empty Paths object. */
		public Paths() {
		}

		/** Creates a Paths object and calls {@link #glob(String, String[])} with the specified arguments. */
		public Paths(String dir, String... patterns) {
			glob(dir, patterns);
		}

		/** Creates a Paths object and calls {@link #glob(String, List)} with the specified arguments. */
		public Paths(String dir, List<String> patterns) {
			glob(dir, patterns);
		}

		private Paths glob(String dir, boolean ignoreCase, String... patterns) {
			if (dir == null)
				dir = ".";
			if (patterns != null && patterns.length == 0) {
				String[] split = dir.split("\\|");
				if (split.length > 1) {
					dir = split[0];
					patterns = new String[split.length - 1];
					for (int i = 1, n = split.length; i < n; i++)
						patterns[i - 1] = split[i];
				}
			}
			File dirFile = new File(dir);
			if (!dirFile.exists())
				return this;

			List<String> includes = new ArrayList();
			List<String> excludes = new ArrayList();
			if (patterns != null) {
				for (String pattern : patterns) {
					if (pattern.charAt(0) == '!')
						excludes.add(pattern.substring(1));
					else
						includes.add(pattern);
				}
			}
			if (includes.isEmpty())
				includes.add("**");

			if (defaultGlobExcludes != null)
				excludes.addAll(defaultGlobExcludes);

			GlobScanner scanner = new GlobScanner(dirFile, includes, excludes, ignoreCase);
			String rootDir = scanner.rootDir().getPath().replace('\\', '/');
			if (!rootDir.endsWith("/"))
				rootDir += '/';
			for (String filePath : scanner.matches())
				paths.add(new Path(rootDir, filePath));
			return this;
		}

		/**
		 * Collects all files and directories in the specified directory matching the wildcard patterns.
		 * 
		 * @param dir
		 *            The directory containing the paths to collect. If it does not exist, no paths are collected. If null, "." is assumed.
		 * @param patterns
		 *            The wildcard patterns of the paths to collect or exclude. Patterns may optionally contain wildcards represented by asterisks and question marks. If empty or omitted then the dir parameter is split on the "|" character, the first element is used as the directory and remaining are used as the patterns. If null, ** is assumed (collects all paths).<br>
		 * <br>
		 *            A single question mark (?) matches any single character. Eg, something? collects any path that is named "something" plus any character.<br>
		 * <br>
		 *            A single asterisk (*) matches any characters up to the next slash (/). Eg, *\*\something* collects any path that has two directories of any name, then a file or directory that starts with the name "something".<br>
		 * <br>
		 *            A double asterisk (**) matches any characters. Eg, **\something\** collects any path that contains a directory named "something".<br>
		 * <br>
		 *            A pattern starting with an exclamation point (!) causes paths matched by the pattern to be excluded, even if other patterns would select the paths.
		 */
		public Paths glob(String dir, String... patterns) {
			return glob(dir, false, patterns);
		}

		/**
		 * Case insensitive glob.
		 * 
		 * @see #glob(String, String...)
		 */
		public Paths globIgnoreCase(String dir, String... patterns) {
			return glob(dir, true, patterns);
		}

		/**
		 * Case sensitive glob.
		 * 
		 * @see #glob(String, String...)
		 */
		public Paths glob(String dir, List<String> patterns) {
			if (patterns == null)
				throw new IllegalArgumentException("patterns cannot be null.");
			glob(dir, false, patterns.toArray(new String[patterns.size()]));
			return this;
		}

		/**
		 * Case insensitive glob.
		 * 
		 * @see #glob(String, String...)
		 */
		public Paths globIgnoreCase(String dir, List<String> patterns) {
			if (patterns == null)
				throw new IllegalArgumentException("patterns cannot be null.");
			glob(dir, true, patterns.toArray(new String[patterns.size()]));
			return this;
		}

		/**
		 * Collects all files and directories in the specified directory matching the regular expression patterns. This method is much slower than {@link #glob(String, String...)} because every file and directory under the specified directory must be inspected.
		 * 
		 * @param dir
		 *            The directory containing the paths to collect. If it does not exist, no paths are collected.
		 * @param patterns
		 *            The regular expression patterns of the paths to collect or exclude. If empty or omitted then the dir parameter is split on the "|" character, the first element is used as the directory and remaining are used as the patterns. If null, ** is assumed (collects all paths).<br>
		 * <br>
		 *            A pattern starting with an exclamation point (!) causes paths matched by the pattern to be excluded, even if other patterns would select the paths.
		 */
		public Paths regex(String dir, String... patterns) {
			if (dir == null)
				dir = ".";
			if (patterns != null && patterns.length == 0) {
				String[] split = dir.split("\\|");
				if (split.length > 1) {
					dir = split[0];
					patterns = new String[split.length - 1];
					for (int i = 1, n = split.length; i < n; i++)
						patterns[i - 1] = split[i];
				}
			}
			File dirFile = new File(dir);
			if (!dirFile.exists())
				return this;

			List<String> includes = new ArrayList();
			List<String> excludes = new ArrayList();
			if (patterns != null) {
				for (String pattern : patterns) {
					if (pattern.charAt(0) == '!')
						excludes.add(pattern.substring(1));
					else
						includes.add(pattern);
				}
			}
			if (includes.isEmpty())
				includes.add(".*");

			RegexScanner scanner = new RegexScanner(dirFile, includes, excludes);
			String rootDir = scanner.rootDir().getPath().replace('\\', '/');
			if (!rootDir.endsWith("/"))
				rootDir += '/';
			for (String filePath : scanner.matches())
				paths.add(new Path(rootDir, filePath));
			return this;
		}

		/**
		 * Copies the files and directories to the specified directory.
		 * 
		 * @return A paths object containing the paths of the new files.
		 */
		public Paths copyTo(String destDir) throws IOException {
			Paths newPaths = new Paths();
			for (Path path : paths) {
				File destFile = new File(destDir, path.name);
				File srcFile = path.file();
				if (srcFile.isDirectory()) {
					destFile.mkdirs();
				} else {
					destFile.getParentFile().mkdirs();
					copyFile(srcFile, destFile);
				}
				newPaths.paths.add(new Path(destDir, path.name));
			}
			return newPaths;
		}

		/**
		 * Deletes all the files, directories, and any files in the directories.
		 * 
		 * @return False if any file could not be deleted.
		 */
		public boolean delete() {
			boolean success = true;
			List<Path> pathsCopy = new ArrayList<Path>(paths);
			Collections.sort(pathsCopy, LONGEST_TO_SHORTEST);
			for (File file : getFiles(pathsCopy)) {
				if (file.isDirectory()) {
					if (!deleteDirectory(file))
						success = false;
				} else {
					if (!file.delete())
						success = false;
				}
			}
			return success;
		}

		/**
		 * Compresses the files and directories specified by the paths into a new zip file at the specified location. If there are no paths or all the paths are directories, no zip file will be created.
		 */
		public void zip(String destFile) throws IOException {
			Paths zipPaths = filesOnly();
			if (zipPaths.paths.isEmpty())
				return;
			byte[] buf = new byte[1024];
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(destFile));
			out.setLevel(Deflater.BEST_COMPRESSION);
			try {
				for (Path path : zipPaths.paths) {
					File file = path.file();
					out.putNextEntry(new ZipEntry(path.name.replace('\\', '/')));
					FileInputStream in = new FileInputStream(file);
					int len;
					while ((len = in.read(buf)) > 0)
						out.write(buf, 0, len);
					in.close();
					out.closeEntry();
				}
			} finally {
				out.close();
			}
		}

		public int count() {
			return paths.size();
		}

		public boolean isEmpty() {
			return paths.isEmpty();
		}

		/** Returns the absolute paths delimited by the specified character. */
		public String toString(String delimiter) {
			StringBuffer buffer = new StringBuffer(256);
			for (String path : getPaths()) {
				if (buffer.length() > 0)
					buffer.append(delimiter);
				buffer.append(path);
			}
			return buffer.toString();
		}

		/** Returns the absolute paths delimited by commas. */
		public String toString() {
			return toString(", ");
		}

		/** Returns a Paths object containing the paths that are files, as if each file were selected from its parent directory. */
		public Paths flatten() {
			Paths newPaths = new Paths();
			for (Path path : paths) {
				File file = path.file();
				if (file.isFile())
					newPaths.paths.add(new Path(file.getParent(), file.getName()));
			}
			return newPaths;
		}

		/** Returns a Paths object containing the paths that are files. */
		public Paths filesOnly() {
			Paths newPaths = new Paths();
			for (Path path : paths) {
				if (path.file().isFile())
					newPaths.paths.add(path);
			}
			return newPaths;
		}

		/** Returns a Paths object containing the paths that are directories. */
		public Paths dirsOnly() {
			Paths newPaths = new Paths();
			for (Path path : paths) {
				if (path.file().isDirectory())
					newPaths.paths.add(path);
			}
			return newPaths;
		}

		/** Returns the paths as File objects. */
		public List<File> getFiles() {
			return getFiles(new ArrayList(paths));
		}

		private ArrayList<File> getFiles(List<Path> paths) {
			ArrayList<File> files = new ArrayList(paths.size());
			for (Path path : paths)
				files.add(path.file());
			return files;
		}

		/** Returns the portion of the path after the root directory where the path was collected. */
		public List<String> getRelativePaths() {
			ArrayList<String> stringPaths = new ArrayList(paths.size());
			for (Path path : paths)
				stringPaths.add(path.name);
			return stringPaths;
		}

		/** Returns the full paths. */
		public List<String> getPaths() {
			ArrayList<String> stringPaths = new ArrayList(paths.size());
			for (File file : getFiles())
				stringPaths.add(file.getPath());
			return stringPaths;
		}

		/** Returns the paths' filenames. */
		public List<String> getNames() {
			ArrayList<String> stringPaths = new ArrayList(paths.size());
			for (File file : getFiles())
				stringPaths.add(file.getName());
			return stringPaths;
		}

		/** Adds a single path to this Paths object. */
		public Paths addFile(String fullPath) {
			File file = new File(fullPath);
			String parent = file.getParent();
			paths.add(new Path(parent == null ? "" : parent, file.getName()));
			return this;
		}

		/** Adds a single path to this Paths object. */
		public Paths add(String dir, String name) {
			paths.add(new Path(dir, name));
			return this;
		}

		/** Adds all paths from the specified Paths object to this Paths object. */
		public void add(Paths paths) {
			this.paths.addAll(paths.paths);
		}

		/** Iterates over the absolute paths. The iterator supports the remove method. */
		public Iterator<String> iterator() {
			return new Iterator<String>() {
				private Iterator<Path> iter = paths.iterator();

				public void remove() {
					iter.remove();
				}

				public String next() {
					return iter.next().absolute();
				}

				public boolean hasNext() {
					return iter.hasNext();
				}
			};
		}

		/** Iterates over the paths as File objects. The iterator supports the remove method. */
		public Iterator<File> fileIterator() {
			return new Iterator<File>() {
				private Iterator<Path> iter = paths.iterator();

				public void remove() {
					iter.remove();
				}

				public File next() {
					return iter.next().file();
				}

				public boolean hasNext() {
					return iter.hasNext();
				}
			};
		}

		static private final class Path {
			public final String dir;
			public final String name;

			public Path(String dir, String name) {
				if (dir.length() > 0 && !dir.endsWith("/"))
					dir += "/";
				this.dir = dir;
				this.name = name;
			}

			public String absolute() {
				return dir + name;
			}

			public File file() {
				return new File(dir, name);
			}

			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((dir == null) ? 0 : dir.hashCode());
				result = prime * result + ((name == null) ? 0 : name.hashCode());
				return result;
			}

			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				Path other = (Path) obj;
				if (dir == null) {
					if (other.dir != null)
						return false;
				} else if (!dir.equals(other.dir))
					return false;
				if (name == null) {
					if (other.name != null)
						return false;
				} else if (!name.equals(other.name))
					return false;
				return true;
			}
		}

		/** Sets the exclude patterns that will be used in addition to the excludes specified for all glob searches. */
		static public void setDefaultGlobExcludes(String... defaultGlobExcludes) {
			Paths.defaultGlobExcludes = Arrays.asList(defaultGlobExcludes);
		}

		/** Copies one file to another. */
		static private void copyFile(File in, File out) throws IOException {
			FileInputStream sourceStream = new FileInputStream(in);
			FileOutputStream destinationStream = new FileOutputStream(out);
			FileChannel sourceChannel = sourceStream.getChannel();
			FileChannel destinationChannel = destinationStream.getChannel();
			sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
			sourceChannel.close();
			sourceStream.close();
			destinationChannel.close();
			destinationStream.close();
		}

		/** Deletes a directory and all files and directories it contains. */
		static private boolean deleteDirectory(File file) {
			if (file.exists()) {
				File[] files = file.listFiles();
				for (int i = 0, n = files.length; i < n; i++) {
					if (files[i].isDirectory())
						deleteDirectory(files[i]);
					else
						files[i].delete();
				}
			}
			return file.delete();
		}
	}
}
