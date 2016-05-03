package edu.utdallas.cs6301_502;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.IntersectionType;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

public class AstVisitor extends ASTVisitor {
	int depth = 0;
	BufferedWriter astDumpWriter = null;

	public static boolean DEBUGGING_ENABLED = false;

	public AstVisitor(int depth, String fileName, CompilationUnit cu) {
		try {
			if (DEBUGGING_ENABLED) {
				File dir = new File("astDumps");
				if (!dir.exists())
					dir.mkdir();
				fileName = "astDumps/" + fileName.replace(".java", ".txt");
				File astDump = new File(fileName);
				astDumpWriter = new BufferedWriter(new FileWriter(astDump));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.depth = depth;
		this.visit(cu);
	}

	public static void processTypeDeclarations(int depth, String fileName, CompilationUnit cu) {
		new AstVisitor(depth, fileName, cu);
	}

	public void logAST(int depth, String str) {
		if (DEBUGGING_ENABLED) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < depth; i++) {
				sb.append("\t");
			}
			sb.append(str);
			sb.append("\r");
			try {
				astDumpWriter.write(sb.toString());
				astDumpWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void log(int depth, String str) {
		if (DEBUGGING_ENABLED) {
			for (int i = 0; i < depth; i++) {
				System.out.print("    ");
			}
			System.out.println(str);
		}
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration n) {
		boolean retval = true;
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.getName().getIdentifier());

		depth++;
		retval = super.visit(n);
		depth--;

		return retval;
	}

	@Override
	public boolean visit(AnnotationTypeMemberDeclaration n) {
		boolean retval;
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(ArrayAccess n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(ArrayCreation n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(ArrayInitializer n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(ArrayType n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(AssertStatement n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(Assignment n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(Block n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(BlockComment n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(BooleanLiteral n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(BreakStatement n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(CastExpression n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(CatchClause n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(CharacterLiteral n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(ClassInstanceCreation n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		log(0, n.getParent().getClass().getName());

		depth++;
		retval = super.visit(n);
		depth--;

		return retval;
	}

	@Override
	public boolean visit(CompilationUnit n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(ConditionalExpression n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(ConstructorInvocation n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(ContinueStatement n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(CreationReference n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(Dimension n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(DoStatement n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(EmptyStatement n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(EnhancedForStatement n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(EnumConstantDeclaration n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(EnumDeclaration n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(ExpressionMethodReference n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(ExpressionStatement n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(FieldAccess n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(FieldDeclaration n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(ForStatement n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(IfStatement n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(ImportDeclaration n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(InfixExpression n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(Initializer n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(InstanceofExpression n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(IntersectionType n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(Javadoc n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(LabeledStatement n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(LambdaExpression n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(LineComment n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(MarkerAnnotation n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(MemberRef n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(MemberValuePair n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(MethodDeclaration n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(MethodInvocation n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(MethodRef n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(MethodRefParameter n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(Modifier n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(NameQualifiedType n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(NormalAnnotation n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(NullLiteral n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(NumberLiteral n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(PackageDeclaration n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(ParameterizedType n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(ParenthesizedExpression n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(PostfixExpression n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(PrefixExpression n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(PrimitiveType n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(QualifiedName n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(QualifiedType n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(ReturnStatement n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(SimpleName n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(SimpleType n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(SingleMemberAnnotation n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(SingleVariableDeclaration n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(StringLiteral n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(SuperConstructorInvocation n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(SuperFieldAccess n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(SuperMethodInvocation n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(SuperMethodReference n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(SwitchCase n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(SwitchStatement n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(SynchronizedStatement n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(TagElement n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(TextElement n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(ThisExpression n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(ThrowStatement n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(TryStatement n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(TypeDeclaration n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(TypeDeclarationStatement n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(TypeLiteral n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(TypeMethodReference n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(TypeParameter n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(UnionType n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(VariableDeclarationExpression n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(VariableDeclarationFragment n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(VariableDeclarationStatement n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(WhileStatement n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

	@Override
	public boolean visit(WildcardType n) {
		logAST(depth, n.getClass().getName() + "(" + n.getStartPosition() + "): " + n.toString());
		boolean retval;

		depth++;
		retval = super.visit(n);
		depth--;
		return retval;
	}

}
