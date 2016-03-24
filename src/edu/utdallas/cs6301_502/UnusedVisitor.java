package edu.utdallas.cs6301_502;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * Visitor for Identifying Unused Fields and Variables in Java Source Code
 *
 * Design Notes:
 * Fields and Vars are set to values in VariableDeclarationFragment and Assignment nodes
 * FieldDeclaration for field declarations
 * SingleVariableDeclaration for parameter declarations (but these have no initializer)
 * VariableDeclarationFragment is used for field declarations, 
 *   local var declarations, for statement initializers, and lamda expression parameters.
 *
 * The Expression class has several subclasses of interest, such as ArrayInitializer, Assignment, etc...
 *
 * Obtaining the IVariableBinding on names will likely be useful. The IVariableBinding contains
 * getKey(), isField(), and isParameter(). The key returned by getKey() is unique for a given AST.
 *
 */
public class UnusedVisitor extends ASTVisitor {

	private boolean inLHS = false;
	private boolean inQualifier = false;
	
	public HashMap<String, VarFieldInfo> varRead;
	
	private boolean debug = false;
	
	public class VarFieldInfo
	{
		IVariableBinding varBinding;
		int lineNumber;
		boolean wasRead;
		
		public VarFieldInfo(IVariableBinding varBinding, int lineNumber) {
			super();
			this.varBinding = varBinding;
			this.lineNumber = lineNumber;
			this.wasRead = false;
		}
		
		public VarFieldInfo(boolean wasRead) {
			super();
			this.varBinding = null;
			this.lineNumber = 0;
			this.wasRead = wasRead;
		}
	}
	
	/**
	 * Default constructor
	 */
	public UnusedVisitor() {
		varRead = new HashMap<>();
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}



	private void debug(String msg)
	{
		if (debug)	System.out.println(msg);
	}
	
	
	/**
	 * Assignments are made within methods.
	 * Assignments do not include the Initializer of VariableDeclrationFragments.
	 */
	@Override
	public boolean visit(Assignment node) {
		
		// TODO: How might this work with nested assignments?
		// It actually appears to work just as Eclipse's built in "unused" finder.
		// In the statement a = b = c = d;, The values b and c will not be counted as read.
		// This works just as Eclipse does.
		node.getRightHandSide().accept(this);
	
		inLHS = true;
		node.getLeftHandSide().accept(this);
		inLHS = false;
		
		return false;
	}

	

//	@Override
//	public boolean visit(MethodInvocation node) {
//
//		Expression e1 = node.getExpression();
//		
//		if (e1 != null) {
//			e1.accept(this);
//			debug(e1.toString());
//		}
//		
//		@SuppressWarnings("unchecked")
//		List<Expression> expressions = node.arguments();
//		for (Expression e : expressions)
//		{
//			e.accept(this);
//		}
//
//		return false;
//	}




	@Override
	public boolean visit(QualifiedName node) {
		if (inQualifier)
		{
			node.getQualifier().accept(this);			
			node.getName().accept(this);
		}
		else
		{
			inQualifier = true;
			node.getQualifier().accept(this);
			inQualifier = false;
		
			node.getName().accept(this);
		}
		
		return false;
	}

	@Override
	public boolean visit(SimpleName node) {

		if (!inLHS || inQualifier)
		{
			debug("SimpleName: Key: " + node.resolveBinding().getKey());
			if (varRead.containsKey(node.resolveBinding().getKey()))
			{
				VarFieldInfo info = varRead.get(node.resolveBinding().getKey());
				info.wasRead = true;				
			}
			else
			{
				if (node.resolveBinding() instanceof IVariableBinding)
				{
				VarFieldInfo info = new VarFieldInfo(true);
				IVariableBinding binding = (IVariableBinding) node.resolveBinding();
				int modifiers = binding.getModifiers(); 
				if (!binding.isField() || Modifier.isPrivate(modifiers))
				{
					varRead.put(node.resolveBinding().getKey(), info);
				}
				}
			}
		}

		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		
		IVariableBinding binding = (IVariableBinding) node.getName().resolveBinding();
		
		CompilationUnit cu = (CompilationUnit) node.getRoot();

		if (!varRead.containsKey(node.getName().resolveBinding().getKey()))
		{
			VarFieldInfo info = new VarFieldInfo(binding, cu.getLineNumber(node.getStartPosition()));

			int modifiers = binding.getModifiers();  
			if (!binding.isField() || Modifier.isPrivate(modifiers))
			{
				varRead.put(node.getName().resolveBinding().getKey(), info);
			}
		}
		else
		{
			VarFieldInfo info = varRead.get(node.getName().resolveBinding().getKey());
			info.varBinding = binding;
			info.lineNumber = cu.getLineNumber(node.getStartPosition());
		}

		
		if (node.getInitializer() != null) {node.getInitializer().accept(this);}
		
		return false; 
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		// Use the package declaration as a place to init
		inLHS = false;
		inQualifier = false;
		return super.visit(node);
	}



}
