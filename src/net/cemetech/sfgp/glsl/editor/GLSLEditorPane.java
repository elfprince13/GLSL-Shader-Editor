package net.cemetech.sfgp.glsl.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;

import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import net.cemetech.sfgp.glsl.compile.CompilerImpl;
import net.cemetech.sfgp.glsl.compile.CompilerTaskSpec;
import net.cemetech.sfgp.glsl.compile.TaskResult;

import org.jdesktop.swingx.MultiSplitLayout;
import org.jdesktop.swingx.MultiSplitLayout.Node;
import org.jdesktop.swingx.MultiSplitPane;

public class GLSLEditorPane extends JPanel implements ActionListener, DocumentListener, PropertyChangeListener {

	MultiSplitPane editorUI;
	MultiSplitLayout editorLayout;
	
	JTabbedPane projectsPane;
	
	public JButton newShader;
	public JButton openShader;
	public JButton resetPipeline;
	public JButton openPipeline;
	JPanel welcomePanel;

	JList assetsList;
	JList<String> targetsList;
	String[] defaultTargets = {"Display"};
	JList stagesList;
	
	static final String[] shaderKindStrings = {"vertex", "geometry", "fragment"};
	static final int GL_VERTEX_SHADER	= 35633;
	static final int GL_GEOMETRY_SHADER	= 36313;
	static final int GL_FRAGMENT_SHADER	= 35632;
	static final int[] shaderKindConsts = {GL_VERTEX_SHADER, GL_GEOMETRY_SHADER, GL_FRAGMENT_SHADER};
	static final String[] assemblyStages;
	static final Map<String,Integer> ext2Kinds;
	
	static{
		assemblyStages = new String[shaderKindStrings.length];
		ext2Kinds = new HashMap<String,Integer>();
		for(int i = 0; i < assemblyStages.length; i++){
			String shaderKind = shaderKindStrings[i];
			String ext = shaderKind.substring(0, 4);
			assemblyStages[i] = shaderKind + "." + ext;
			ext2Kinds.put(ext, shaderKindConsts[i]);
		}
	}

		
	public static String shaderTemplate = "#version 330 core\n\nvoid main() {\n\t// TODO auto-generated shader stub\n}\n";
	
	
	File lastDir = null;
	CompilerImpl compiler = null;
	

	public GLSLEditorPane(String lastLayout, String lastDirPath, CompilerImpl c) {
		setLayout(new BorderLayout());
		

		GLSLSyntaxKit.initKit();
		
		File ldp = new File(lastDirPath);
		if(ldp.isDirectory()){
			lastDir = ldp;
		}
		compiler = c;
		
		projectsPane = new JTabbedPane();
		projectsPane.setTabPlacement(JTabbedPane.TOP);
	    projectsPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		
		newShader = new JButton("New Shader");
		newShader.addActionListener(this);
		openShader = new JButton("Open Shader");
		openShader.addActionListener(this);
		openPipeline = new JButton("Open Saved Pipeline");
		openPipeline.addActionListener(this);
		resetPipeline = new JButton("Reset Pipeline Configuration");
		resetPipeline.addActionListener(this);
		
		welcomePanel = new JPanel(new BorderLayout());
		welcomePanel.add(new JLabel("Welcome!"), BorderLayout.NORTH);
		welcomePanel.add(newShader, BorderLayout.WEST);
		welcomePanel.add(openShader, BorderLayout.CENTER);
		welcomePanel.add(openPipeline, BorderLayout.EAST);
		welcomePanel.add(resetPipeline, BorderLayout.SOUTH);
		
		projectsPane.addTab("Welcome", welcomePanel);
		
		JPanel assetsPanel = new JPanel(new BorderLayout());
		assetsPanel.add(new JLabel("Assets:"),BorderLayout.NORTH);
		assetsList = new JList();
		assetsPanel.add(new JScrollPane(assetsList),BorderLayout.CENTER);
		JPanel assetsButtons = new JPanel(new FlowLayout());
		assetsButtons.add(new JButton("Model"));
		assetsButtons.add(new JButton("Texture"));
		assetsPanel.add(assetsButtons,BorderLayout.SOUTH);
		
		JPanel targetsPanel = new JPanel(new BorderLayout());
		targetsPanel.add(new JLabel("Targets:"),BorderLayout.NORTH);
		targetsList = new JList<String>(defaultTargets);
		targetsPanel.add(new JScrollPane(targetsList),BorderLayout.CENTER);
		targetsPanel.add(new JButton("Framebuffer"),BorderLayout.SOUTH);
		
		JPanel stagesPanel = new JPanel(new BorderLayout());
		stagesPanel.add(new JLabel("Stages:"),BorderLayout.NORTH);
		stagesList = new JList();
		stagesPanel.add(new JScrollPane(stagesList),BorderLayout.CENTER);
		stagesPanel.add(new JButton("Shader"),BorderLayout.SOUTH);
		
		editorUI = new MultiSplitPane();
		editorLayout = new MultiSplitLayout();

		String layoutDef = "(ROW (LEAF name=left weight=0.7) (COLUMN weight=0.3 (LEAF name=right.top weight=0.33) (LEAF name=right weight=0.33) (LEAF name=right.bottom weight=0.34)))";
		try {
			XMLDecoder d = 
					new XMLDecoder(new BufferedInputStream(
							new FileInputStream(lastLayout)));
			Node model = (Node)(d.readObject());
			editorLayout.setModel(model);
			editorLayout.setFloatingDividers(false);
			d.close();
		}
		catch (Exception exc) { 
			Node model = MultiSplitLayout.parseModel(layoutDef); 
			editorLayout.setModel(model);
		}
		
		editorUI.setLayout(editorLayout);
		
		editorUI.add(projectsPane,"left");
		editorUI.add(assetsPanel,"right.top");
		editorUI.add(targetsPanel,"right");
		editorUI.add(stagesPanel,"right.bottom");
		
		add(editorUI,BorderLayout.CENTER);

	}
	
	
	public void saveLayout(String filename) throws FileNotFoundException {
		XMLEncoder e = 
			    new XMLEncoder(new BufferedOutputStream(
			            new FileOutputStream(filename)));
			Node model = editorLayout.getModel();
			e.writeObject(model);
			e.close();
	}
	
	private class ProjectPanel extends JPanel {
		ShaderCheckBox[] shaderStages;
		private ProjectPanel(ShaderCheckBox[] shaderStages, LayoutManager layout) 
		{ super(layout); this.shaderStages = shaderStages; }
		
		public void setStage(int stage, ShaderCheckBox box) { shaderStages[stage] = box; }
		public ShaderCheckBox getStage(int stage) {	return shaderStages[stage]; }
	}
	
	private class ShaderCheckBox extends JCheckBox implements ItemListener {
		
		JTabbedPane parent;
		File source;
		String target;
		private ShaderCheckBox(JTabbedPane parent, File source, String target, boolean initState) {
			super(target,initState);
			this.parent = parent;
			this.source = source;
			this.target = target;
			addItemListener(this);
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			try{
				File shaderSource = new File(source, target);
				boolean canOpen = shaderSource.canRead();
				if(e.getStateChange() == ItemEvent.SELECTED) {
					int answer = JOptionPane.CANCEL_OPTION;
					if(!canOpen){
						answer = JOptionPane.showConfirmDialog(null, "Shader assembly stage '" + target + "' does not exist (or can't be read). Would you like to create it?", "Create shader assembly stage?",JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
					}
					
					if (canOpen || answer == JOptionPane.OK_OPTION){
						GLSLEditorPane.this.addCodeTab(parent, shaderSource, stageIndex(target), canOpen, !canOpen);
					} else {
						ShaderCheckBox.this.setSelected(false);
					}
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					int toRemove = parent.indexOfTab(target);
					if (toRemove > 0){
						parent.removeTabAt(toRemove);
					}
					
					int shouldDelete = JOptionPane.CANCEL_OPTION;
					String[] options = {"Yes, delete this file", "No, temporarily disable it only"};
					if (canOpen){
						shouldDelete = JOptionPane.showOptionDialog(null, "Shader assembly stage '" + target + 
								"' still exists on disk. Would you like to erase it? \n\n" +
								" \u2022 If you choose to delete it, it might not be recoverable. \n" + 
								" \u2022 If you choose not to delete it, it will be re-enabled automatically the next time this shader is opened.", 
								"Erase shader source from disk?",JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
					}
					
					if (shouldDelete == JOptionPane.OK_OPTION){
						Files.deleteIfExists(shaderSource.toPath());
					}
				} else {
					throw new IllegalStateException("An item state change must be either SELECTED or DESELECTED");
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
				return;
			}

		}
		
	}
	
	private class CloseableTab extends JPanel implements ActionListener{
		private CloseableTab(){
			super(new FlowLayout(FlowLayout.LEFT, 0, 0));
			setOpaque(false);
			JLabel titleLabel = new JLabel() {
	            public String getText() {
	                int i = GLSLEditorPane.this.projectsPane.indexOfTabComponent(CloseableTab.this);
	                if (i != -1) return GLSLEditorPane.this.projectsPane.getTitleAt(i);
	                else return "";
	            }
	        };
			titleLabel.setOpaque(false);
	        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
			add(titleLabel);
			JButton closeButton = new JButton("X");
			closeButton.setContentAreaFilled(false);
            closeButton.setFocusable(false);
            closeButton.addActionListener(this);
            closeButton.setPreferredSize(new Dimension(15,15));
			add(closeButton);
			setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
		}
		
		public void actionPerformed(ActionEvent e){
			if ((e.getModifiers() & ActionEvent.SHIFT_MASK) == 0){
				int i = GLSLEditorPane.this.projectsPane.indexOfTabComponent(this);
				if(i != -1) GLSLEditorPane.this.projectsPane.removeTabAt(i);
				else throw new IllegalStateException("An action was performed on a CloseableTab, but it does not belong to its own JTabbedPane");
			} else {
				int next = 1;
				while (GLSLEditorPane.this.projectsPane.getTabCount() > 2) {
					int i = GLSLEditorPane.this.projectsPane.indexOfTabComponent(this);
					if (i == next) next++;
					else if (i == -1) throw new IllegalStateException("An action was performed on a CloseableTab, but it does not belong to its own JTabbedPane");
					else GLSLEditorPane.this.projectsPane.removeTabAt(next);
				}
			}
		}
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		JFileChooser j = new JFileChooser();
		j.setCurrentDirectory( (lastDir == null) ? (new File(".")) : lastDir);
		if(source == openShader || source == newShader) {
			boolean opening = source == openShader;
			boolean starting = source == newShader;
			j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int status = opening ? j.showOpenDialog(this) : j.showSaveDialog(this);
			if(status == JFileChooser.APPROVE_OPTION) {
				JTabbedPane thisProjectDocs = new JTabbedPane();
				thisProjectDocs.setTabPlacement(JTabbedPane.TOP);
			    thisProjectDocs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			     
				ProjectPanel managePanel = new ProjectPanel(new ShaderCheckBox[assemblyStages.length], new BorderLayout());
				managePanel.add(new JLabel("Manage Shader Pipeline Stages:"), BorderLayout.NORTH);
				JPanel switchPanel = new JPanel(new FlowLayout());
				managePanel.add(switchPanel,BorderLayout.CENTER);
				
				thisProjectDocs.addTab("Manage", managePanel);
				
				File selected = j.getSelectedFile();
				if(starting && !selected.mkdirs()){
					System.err.println("Unable to create shader directory");
					return;
				}
				for(int i = 0; i < assemblyStages.length; i++){
					File stagePath = new File(selected, assemblyStages[i]);
					try{
						addCodeTab(thisProjectDocs, stagePath, i, opening, starting);
						ShaderCheckBox scb = new ShaderCheckBox(thisProjectDocs, selected, assemblyStages[i], stagePath.canRead());
						switchPanel.add(scb);
						managePanel.setStage(i, scb);
					} catch (IOException ioe) {
						ioe.printStackTrace();
						return;
					}
				}
				JPanel errorPanel = new JPanel(new BorderLayout());
				errorPanel.add(new JLabel("Issues:"), BorderLayout.NORTH);
				JList<String> thisProjectLog = new JList<String>();
				errorPanel.add(new JScrollPane(thisProjectLog),BorderLayout.CENTER);
				JSplitPane thisProject = new JSplitPane(JSplitPane.VERTICAL_SPLIT, thisProjectDocs, errorPanel);
				
				try {
					int newTabIndex = projectsPane.getTabCount();
					projectsPane.addTab(selected.getCanonicalPath(), thisProject);
					projectsPane.setTabComponentAt(newTabIndex, new CloseableTab());
					projectsPane.setSelectedIndex(newTabIndex);
				} catch (IOException ioe) {
					ioe.printStackTrace();
					return;
				}
				
			}
		} else if (source == openPipeline) {
			
		} else if (source == resetPipeline) {
			
		} else {
			throw new IllegalStateException("A mystery button was pushed!");
		}
		lastDir = j.getCurrentDirectory();
	}
	
	public void addCodeTab(JTabbedPane thisProjectDocs, File stagePath, int stage, boolean opening, boolean starting) throws IOException {
		if ((opening || (starting && stagePath.createNewFile())) && stagePath.canRead() && stagePath.canWrite()) {
			JEditorPane codeEditor = new JEditorPane();
			JScrollPane scrPane = new JScrollPane(codeEditor);
			
			boolean added = false;
			for(int i = 1; i < thisProjectDocs.getTabCount() && !added; i++){
				int otherStage = stageIndex(thisProjectDocs.getTitleAt(i));
				if (otherStage > stage){
					thisProjectDocs.insertTab(assemblyStages[stage], null, scrPane, "", i);
					added = true;
				}
			}
			if(!added) thisProjectDocs.addTab(assemblyStages[stage],scrPane);
			 
				
			codeEditor.setContentType("text/glsl");
			codeEditor.addPropertyChangeListener(this);
		
			if (opening){
				codeEditor.setText(new String(Files.readAllBytes(stagePath.toPath())));
			}
			if (starting){
				codeEditor.setText(GLSLEditorPane.shaderTemplate);
				Files.write(stagePath.toPath(), GLSLEditorPane.shaderTemplate.getBytes());
			}
		}
	}
	
	private String retrieveShaderSource(ShaderCheckBox scb) {
		int tabIndex = scb.parent.indexOfTab(scb.target);
		if(tabIndex < 0){
			JScrollPane jsp = (JScrollPane)(scb.parent.getComponentAt(tabIndex));
			String source = ((JEditorPane)(jsp.getViewport().getView())).getText();
			return source;
		} else {
			System.err.println("No " + scb.getText() + ", skipping.");	
			return null;
		}
	}
	
	private LinkedList<ShaderCheckBox> activeShadersForProject(ProjectPanel project) {
		LinkedList<ShaderCheckBox> activeShaders = new LinkedList<ShaderCheckBox>();
		for(int i = 0; i < assemblyStages.length; i++){
			ShaderCheckBox scb = project.getStage(i);
			if(scb.isSelected()){
				activeShaders.add(scb);
			}
		}
		return activeShaders;
	}
	
	private ProjectPanel getCurrentProject(){
		Component currentTab = (projectsPane.getSelectedComponent());
		ProjectPanel project;
		if ((currentTab instanceof JPanel) && currentTab == welcomePanel) { 
			project = null;
		} else if ((currentTab instanceof JSplitPane) && ((JSplitPane)currentTab).getTopComponent() instanceof JTabbedPane) {
			project = (ProjectPanel)(((JTabbedPane)(((JSplitPane)currentTab).getTopComponent())).getComponentAt(0));
		} else {
			throw new IllegalStateException("An unknown tab has appeared. Run away?");
		}
		return project;
	}
	
	public void saveCurrent() {
		ProjectPanel project = getCurrentProject();
		if(project != null){
			LinkedList<ShaderCheckBox> activeShaders = activeShadersForProject(project);
			for(ShaderCheckBox scb : activeShaders) {
				String src = retrieveShaderSource(scb);
				if(src != null){
					try {
						Files.write((new File(scb.source,scb.target)).toPath(), src.getBytes());
					} catch (IOException e) {
						e.printStackTrace();
						return;
					}
				}
			}
		} else {
			System.err.println("Nothing to save: no project is current.");
		}

	}
	
	public void compileCurrent() {
		if(compiler != null){
			ProjectPanel project = getCurrentProject();
			if(project != null){
				LinkedList<ShaderCheckBox> activeShaders = activeShadersForProject(project);
				for(ShaderCheckBox scb : activeShaders) {
					System.out.print("Compiling " + scb.target + " ... \t");
					String src = retrieveShaderSource(scb);
					if(src != null){
						String ext = FilenameUtils.getExtension(scb.target);
						int kind = ext2Kinds.get(ext).intValue();
						//System.out.println(ext + " -> " + kind);
						CompilerTaskSpec thisSpec = new CompilerTaskSpec(kind, src);
						thisSpec.setCompiler(compiler);
						TaskResult<CompilerTaskSpec> result = thisSpec.call();
						System.out.println("Done");
						if(result.useable()){
							System.out.println("Compilation successful!");
							compiler.cleanCompileResult(result);
						} else{
							System.err.println("Compilation Failed. Error log follows:");
							System.err.println(result.getLoggingResults());
						}
					}
				}
			} else {
				System.err.println("Nothing to save: no project is current.");
			}
			
		} else {
			System.err.println("We have no compiler!");
		}
	}
	
	public int stageIndex(String stageName){
		for(int i = 0; i < assemblyStages.length; i++) 
			if (stageName.equals(assemblyStages[i])) 
				return i;
		return -1;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		 if ("document".equalsIgnoreCase(evt.getPropertyName())) {
			    Document oldDocument = (Document)evt.getOldValue();
			    if (oldDocument != null) {
			      oldDocument.removeDocumentListener(this);
			    }
			    Document newDocument = (Document)evt.getNewValue();
			    newDocument.removeDocumentListener(this);
			    newDocument.addDocumentListener(this);
		 }
		 //System.out.println("Updated Document listener");
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
	}
}