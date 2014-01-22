package net.cemetech.sfgp.glsl.editor;

import java.awt.BorderLayout;

import org.jdesktop.swingx.*;
import org.jdesktop.swingx.MultiSplitLayout.Node;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import java.nio.file.Files;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import jsyntaxpane.DefaultSyntaxKit;

public class GLSLEditorPane extends JPanel implements ActionListener, DocumentListener, PropertyChangeListener {

	MultiSplitPane editorUI;
	MultiSplitLayout editorLayout;
	
	JTabbedPane projectsPane;
	
	JButton newShader;
	JButton openShader;
	JButton resetPipeline;
	JButton openPipeline;
	JPanel welcomePanel;

	JList assetsList;
	JList<String> targetsList;
	String[] defaultTargets = {"Display"};
	JList stagesList;
	
	String[] assemblyStages = {"vertex.vert", "geometry.geom", "fragment.frag"};
	
	public static String shaderTemplate = "#version 330 core\n\nvoid main() {\n\t// TODO auto-generated shader stub\n}\n";
	
	File lastDir = null;
	
	public static void main(String[] args) {
		java.awt.EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				JMenuBar mb = new JMenuBar();
				JMenu file, edit, run;
				final JMenuItem save, open, newMenu, openPipe, resetPipe, compile;
				
				file = new JMenu("File");
				edit = new JMenu("Edit");
				run = new JMenu("Run");
				
				newMenu = new JMenuItem("New");
				newMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,ActionEvent.META_MASK));
				file.add(newMenu);
				
				open = new JMenuItem("Open");
				open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,ActionEvent.META_MASK));
				file.add(open);
				
				save = new JMenuItem("Save");
				save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.META_MASK));
				file.add(save);
				
				file.add(new JSeparator());
				
				resetPipe = new JMenuItem("Reset Pipeline");
				resetPipe.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,ActionEvent.META_MASK | ActionEvent.SHIFT_MASK));
				file.add(resetPipe);
				
				openPipe = new JMenuItem("Open Pipeline");
				openPipe.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,ActionEvent.META_MASK | ActionEvent.SHIFT_MASK));
				file.add(openPipe);
				
				compile = new JMenuItem("Compile");
				compile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, ActionEvent.META_MASK));
				run.add(compile);
				
				mb.add(file);
				mb.add(edit);
				mb.add(run);
				
				JFrame f = new JFrame("SFGP Shader Editor");
				f.setJMenuBar(mb);
				final Container c = f.getContentPane();
				c.setLayout(new BorderLayout());
				final GLSLEditorPane editor = new GLSLEditorPane("");
				c.add(editor, BorderLayout.CENTER);
				c.doLayout();


				f.setSize(1000, 700);
				f.setVisible(true);
				f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				
				ActionListener menuListener = new ActionListener() {
					public void actionPerformed(ActionEvent e){
						if (e.getSource() == save) {
							editor.saveCurrent();
						} else if (e.getSource() == compile) {
							editor.compileCurrent();
						} else if (e.getSource() == open) {
							ActionEvent relay = new ActionEvent(editor.openShader, e.getID(), e.getActionCommand());
							editor.actionPerformed(relay);
						} else if (e.getSource() == newMenu) {
							ActionEvent relay = new ActionEvent(editor.newShader, e.getID(), e.getActionCommand());
							editor.actionPerformed(relay);
						} else if (e.getSource() == openPipe) {
							ActionEvent relay = new ActionEvent(editor.openPipeline, e.getID(), e.getActionCommand());
							editor.actionPerformed(relay);
						} else if (e.getSource() == resetPipe) {
							ActionEvent relay = new ActionEvent(editor.resetPipeline, e.getID(), e.getActionCommand());
							editor.actionPerformed(relay);
						} else {
							throw new IllegalStateException("Imaginary menu item registered an ActionEvent: " + e.getSource());
						}
					}
				};
				
				compile.addActionListener(menuListener);
				newMenu.addActionListener(menuListener);
				open.addActionListener(menuListener);
				save.addActionListener(menuListener);
				resetPipe.addActionListener(menuListener);
				openPipe.addActionListener(menuListener);

			}
		});
	}

	public GLSLEditorPane(String lastLayout) {
		setLayout(new BorderLayout());
		

		GLSLSyntaxKit.initKit();
		
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
		if (lastDir != null) j.setCurrentDirectory(lastDir);
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
	
	public void saveCurrent() {
		Component currentTab = (projectsPane.getSelectedComponent());
		if ((currentTab instanceof JPanel) && currentTab == welcomePanel) { 
			return;
		} else if ((currentTab instanceof JSplitPane) && ((JSplitPane)currentTab).getTopComponent() instanceof JTabbedPane) {
			ProjectPanel project = (ProjectPanel)(((JTabbedPane)(((JSplitPane)currentTab).getTopComponent())).getComponentAt(0));
			for(int i = 0; i < assemblyStages.length; i++){
				ShaderCheckBox scb = project.getStage(i);
				int tabIndex = scb.parent.indexOfTab(scb.target);
				JScrollPane jsp = (JScrollPane)(scb.parent.getComponentAt(tabIndex));
				try {
					Files.write((new File(scb.source,scb.target)).toPath(), ((JEditorPane)(jsp.getViewport().getView())).getText().getBytes());
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		} else {
			throw new IllegalStateException("An unknown tab has appeared. Run away?");
		}
	}
	
	public void compileCurrent() {
		
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