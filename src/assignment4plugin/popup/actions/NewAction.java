package assignment4plugin.popup.actions;

import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import edu.utdallas.cs6301_502.Runner;

public class NewAction implements IObjectActionDelegate {

	private Shell shell;
	
	private IWorkbenchPart targetPart;
	private ISelection selection;
	
	/**
	 * Constructor for Action1.
	 */
	public NewAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
		this.targetPart = targetPart;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		
		Runner r = new Runner();
		
		if (selection instanceof TreeSelection)
		{
			TreeSelection tree = (TreeSelection) selection;
			
			if (tree.getFirstElement() instanceof IProject)
			{
				
				IProject p = (IProject) tree.getFirstElement();
				try
				{
					if (p.isNatureEnabled("org.eclipse.jdt.core.javanature"))
					{
						r.runOnProject(JavaCore.create(p));
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			
		}
		


		MessageDialog.openInformation(
			shell,
			"Assignment4Plugin",
			"Analyze was executed. " + "Title: " + targetPart.getTitle() + " Selection: " + selection.toString());
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	
	
	
}
