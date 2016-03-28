// ***************************************************************************
// Assignment: 4
// Team : 1
// Team Members: Ryan Wiles, Erik Shreve
//
// Code reuse/attribution notes:
// args4j (for command line parsing) based on example code from:
// https://github.com/kohsuke/args4j/blob/master/args4j/examples/SampleMain.java
//
// parseFile and setParserConfiguration taken from/based on seers.astvisitortest.MainVisitor
// ***************************************************************************
package edu.utdallas.cs6301_502;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;


import edu.utdallas.cs6301_502.Runner.Requester.CUSourcePair;
import edu.utdallas.cs6301_502.UnusedVisitor.VarFieldInfo;


public class Runner {
	
	private boolean debug = false;



	
	public static void main(String... args) throws Exception {
		Runner r = new Runner();
		
		r.run();
	}



	public Runner() {
		super();
	}

	private void debug(String msg)
	{
		if (debug)	System.out.println(msg);
	}
	
	public void run() {
		try {
			//File file = new File(source);
			
			//CompilationUnit compUnit = parseFile(file);

			// create and accept the visitor
			//UnusedVisitor visitor = new UnusedVisitor();
			//visitor.setDebug(debug);
			
			
			//compUnit.accept(visitor);
			
			Requester r = new Requester();
			
			processProjects(r);

			
			for (CUSourcePair pair : r.compilationUnits)
			{
				processSource(pair.source, pair.ast);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void runOnProject(IJavaProject p) {
		try {
			
			Requester r = new Requester();
			
			processProject(r, p);

		//	System.out.println("Project path: " + p.getResource().getRawLocation().makeAbsolute());
		//	p.getResource().getName();
			
			
			for (CUSourcePair pair : r.compilationUnits)
			{
				processSource(pair.source, pair.ast);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void processSource(ICompilationUnit source, CompilationUnit ast)
	{		
		UnusedVisitor visitor = new UnusedVisitor();
		visitor.setDebug(debug);
		
		ast.accept(visitor);
		
		boolean somethingUnread = false;
		StringBuilder findings = new StringBuilder();
		
		for (String k : visitor.varRead.keySet())
		{
			if (!visitor.varRead.get(k).wasRead)
			{
				VarFieldInfo info = visitor.varRead.get(k);
				if (info.varBinding != null)
				{
					somethingUnread = true;
					String type = "variable";
					if (info.varBinding.isField()) {type = "field";}
					findings.append("* The [" + type + "] ["+ info.varBinding.getName() + "] is declared but never read in the code (line:[" + info.lineNumber + "])" + System.lineSeparator());
				}
			}
		}
		
		if (somethingUnread)
		{
			System.out.println("File: " + source.getPath());
			System.out.print(findings.toString());
		}
		
	}
	
	public class Requester extends ASTRequestor
	{

		public class CUSourcePair
		{
			ICompilationUnit source;
			CompilationUnit ast;
		}
		
		ArrayList<CUSourcePair> compilationUnits = new ArrayList<>();
		
		@Override
		public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
			// TODO Auto-generated method stub
			CUSourcePair pair = new CUSourcePair();
			pair.source = source;
			pair.ast = ast;
			compilationUnits.add(pair);
			
			debug("Accepted AST for: " + source.getPath());
			
			super.acceptAST(source, ast);
		}

		@Override
		public void acceptBinding(String bindingKey, IBinding binding) {
			// TODO Auto-generated method stub
			super.acceptBinding(bindingKey, binding);
		}
		
	}
	
	void processProject(Requester requester, IJavaProject p)
	{
		try
		{
			ASTParser parser = ASTParser.newParser(AST.JLS8);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setProject(p);

			@SuppressWarnings("unchecked")
			Map<String, String> options = JavaCore.getOptions();
			options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
			options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
			options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
			JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);

			parser.setCompilerOptions(options);
			parser.setResolveBindings(true);

			String[] bindings = new String[0];

			ArrayList<ICompilationUnit> compilationUnits = new ArrayList<>();

			IPackageFragment[] packageFragments = p.getPackageFragments();
			for (IPackageFragment aPackageFragment : packageFragments)
			{
				if (aPackageFragment.getKind() == IPackageFragmentRoot.K_SOURCE)
				{
					compilationUnits.addAll(Arrays.asList(aPackageFragment.getCompilationUnits()));
				}
			}


			parser.createASTs((ICompilationUnit[]) compilationUnits.toArray(new ICompilationUnit[compilationUnits.size()]), bindings, requester, null);


		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}
	
	void processProjects(Requester requester)
	{
        // Get all the projects in the workspace. NOTE: This code assumes
		// only Java projects are in the workspace.
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IJavaModel javaModel = JavaCore.create(workspace.getRoot());
        IJavaProject[] projects = null;
		try {
			projects = javaModel.getJavaProjects();
		} catch (JavaModelException e1) {
			e1.printStackTrace();
		}
        
        for (IJavaProject p : projects)
        {
        	try
        	{
        		if (p.getProject().getName().equals("eclipse.jdt.core"))
        		{
        			processProject(requester, p);
        		}
        	}
        	catch (Exception e)
        	{
        		e.printStackTrace();
        	}
        }
	}
	
	/**
	 * Parses a java file
	 * 
	 * @param file
	 *            the file to parse
	 * @return the CompilationUnit of a java file (i.e., its AST)
	 * @throws IOException
	 */
//	private static CompilationUnit parseFile(File file) throws IOException {
//
//		// read the content of the file
//		char[] fileContent = FileUtils.readFileToString(file).toCharArray();
//
//		// create the AST parser
//		ASTParser parser = ASTParser.newParser(AST.JLS8);
//		parser.setUnitName(file.getName());
//		parser.setSource(fileContent);
//		parser.setKind(ASTParser.K_COMPILATION_UNIT);
//
//		// set some default configuration
//		setParserConfiguration(parser);
//
//		// parse and return the AST
//		return (CompilationUnit) parser.createAST(null);
//
//	}

	/**
	 * Sets the default configuration of an AST parser
	 * 
	 * @param parser
	 *            the AST parser
	 */
	public static void setParserConfiguration(ASTParser parser) {
		@SuppressWarnings("unchecked")
		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);

		parser.setCompilerOptions(options);
		parser.setResolveBindings(true);

		parser.setEnvironment(null, null, null, true);
		//parser.setEnvironment(classPaths, sourceFolders, encodings, true);
	}
	
}