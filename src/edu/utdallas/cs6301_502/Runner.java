package edu.utdallas.cs6301_502;


import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class Runner {

	private boolean debug = false;

	private String[] sources = new String[] { "/Users/rwiles/github/jabref", "/Users/rwiles/github/eclipse.jdt.core" };

	public static void main(String... args) throws Exception {
		Runner r = new Runner();
		r.doMain(args);
		r.run();
	}

	public void doMain(String[] args) {
	}
	
	public Runner() {
		super();
	}

	private void debug(String msg) {
		if (debug)
			System.out.println(msg);
	}

	public void run() {
		try {
			for (String source : sources) {
				System.out.println("Scanning: " + source);
				processProject(source);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void processProject(String source) {
		try {
			long startTime = System.currentTimeMillis();
			
			List<File> files = new ArrayList<File>();
			if (source != null) {
				files.addAll(walkFolder(Paths.get(source), true));
			}
			for (File file : files) {
				CompilationUnit compUnit = parseFile(file);
				AstVisitor visitor = new AstVisitor(0, file.getName(), compUnit);
				compUnit.accept(visitor);

			}

			System.out.println("\tEclipse JDT, total time to parse AST: " + (System.currentTimeMillis() - startTime) + "ms");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private CompilationUnit parseFile(File file) throws IOException {
		debug("Attempting to parse: " + file.getAbsolutePath());
		// read the content of the file
		char[] fileContent = FileUtils.readFileToString(file).toCharArray();

		// create the AST parser
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setUnitName(file.getName());
		parser.setSource(fileContent);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		// set some default configuration
		setParserConfiguration(parser);

		// parse and return the AST
		return (CompilationUnit) parser.createAST(null);

	}

	/**
	 * Sets the default configuration of an AST parser
	 * 
	 * @param parser
	 *            the AST parser
	 */
	public void setParserConfiguration(ASTParser parser) {
		@SuppressWarnings("unchecked")
		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.CORE_INCOMPLETE_CLASSPATH, JavaCore.WARNING);
		options.put(JavaCore.COMPILER_PB_ENUM_IDENTIFIER, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.IGNORE);
		options.put(JavaCore.CORE_CIRCULAR_CLASSPATH, JavaCore.WARNING);
		options.put(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.WARNING);
		options.put(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_NULL_ANNOTATION_INFERENCE_CONFLICT, JavaCore.IGNORE);
		options.put(JavaCore.CORE_OUTPUT_LOCATION_OVERLAPPING_ANOTHER_SOURCE, JavaCore.IGNORE);
		options.put(JavaCore.CORE_JAVA_BUILD_DUPLICATE_RESOURCE, JavaCore.WARNING);
		options.put(JavaCore.CODEASSIST_DEPRECATION_CHECK, JavaCore.DISABLED);
		options.put(JavaCore.COMPILER_CODEGEN_UNUSED_LOCAL, JavaCore.PRESERVE);
		options.put(JavaCore.COMPILER_PB_SUPPRESS_WARNINGS, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_MISSING_SERIAL_VERSION, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_DEPRECATION, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_STATIC_ACCESS_RECEIVER, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_EMPTY_STATEMENT, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_UNUSED_WARNING_TOKEN, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_UNUSED_TYPE_ARGUMENTS_FOR_METHOD_INVOCATION, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE, JavaCore.IGNORE);

		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);
		parser.setResolveBindings(true);

		List<File> jars = new ArrayList<File>();

		String[] classpath = new String[jars.size()];
		for (int i = 0; i < jars.size(); i++) {
			classpath[i] = jars.get(i).getAbsolutePath();
		}

		String[] sourceFolders = null;

		parser.setEnvironment(classpath, sourceFolders, null, true);
	}

	private List<File> walkFolder(Path path, boolean javaNotJar) throws IOException {

		final ExecutorService executor = Executors.newFixedThreadPool(4); // FIXME: make number of threads configurable

		List<File> files = new ArrayList<File>();

		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				try {
					if (javaNotJar) {
						if (file.toString().endsWith(".java")) {
							debug("processing " + file.toString());

							File f = file.toFile();
							files.add(f);
						}
					} else {
						if (file.toString().endsWith(".jar")) {
							debug("processing " + file.toString());
							File f = file.toFile();
							files.add(f);
						}
					}
				} finally {}
				return FileVisitResult.CONTINUE;
			}
		});

		executor.shutdown();
		while (!executor.isTerminated()) {}

		return files;
	}

}