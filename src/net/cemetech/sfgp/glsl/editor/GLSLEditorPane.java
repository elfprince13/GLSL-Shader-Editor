package net.cemetech.sfgp.glsl.editor;

import java.awt.BorderLayout;

import org.jdesktop.swingx.*;
import org.jdesktop.swingx.MultiSplitLayout.Node;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JToolBar;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import jsyntaxpane.DefaultSyntaxKit;

public class GLSLEditorPane extends JPanel implements ActionListener {

	MultiSplitPane editorUI;
	MultiSplitLayout editorLayout;
	
	JTabbedPane projectsPane;
	
	JButton newShader;
	JButton openShader;
	JButton resetPipeline;
	JButton openPipeline;

	JList assetsList;
	JList<String> targetsList;
	String[] defaultTargets = {"Display"};
	JList stagesList;
	
	String[] assemblyStages = {"vertex.vert", "geometry.geom", "fragment.frag"};
	
	File lastDir = null;
	
	public static void main(String[] args) {
		java.awt.EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				JFrame f = new JFrame("SFGP Shader Editor");
				final Container c = f.getContentPane();
				c.setLayout(new BorderLayout());
				c.add(new GLSLEditorPane(""), BorderLayout.CENTER);
				c.doLayout();


				f.setSize(1000, 700);
				f.setVisible(true);
				f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			}
		});
	}

	public GLSLEditorPane(String lastLayout) {
		setLayout(new BorderLayout());
		

		GLSLSyntaxKit.initKit();
		
		projectsPane = new JTabbedPane();
		projectsPane.setTabPlacement(JTabbedPane.TOP);
		
		newShader = new JButton("New Shader");
		newShader.addActionListener(this);
		openShader = new JButton("Open Shader");
		openShader.addActionListener(this);
		openPipeline = new JButton("Open Saved Pipeline");
		openPipeline.addActionListener(this);
		resetPipeline = new JButton("Reset Pipeline Configuration");
		resetPipeline.addActionListener(this);
		
		JPanel welcomePanel = new JPanel(new BorderLayout());
		welcomePanel.add(new JLabel("Welcome!"), BorderLayout.NORTH);
		welcomePanel.add(newShader, BorderLayout.WEST);
		welcomePanel.add(openShader, BorderLayout.CENTER);
		welcomePanel.add(openPipeline, BorderLayout.EAST);
		welcomePanel.add(resetPipeline, BorderLayout.SOUTH);
		
		projectsPane.addTab("Welcome", welcomePanel);
		/*
		projectsPane.addTab("test 1", new JPanel());
		projectsPane.setTabComponentAt(projectsPane.getTabCount() - 1, new CloseableTab());
		projectsPane.addTab("test 2", new JPanel());
		projectsPane.setTabComponentAt(projectsPane.getTabCount() - 1, new CloseableTab());
		projectsPane.addTab("test 3", new JPanel());
		projectsPane.setTabComponentAt(projectsPane.getTabCount() - 1, new CloseableTab());
		projectsPane.addTab("test 4", new JPanel());
		projectsPane.setTabComponentAt(projectsPane.getTabCount() - 1, new CloseableTab());
		projectsPane.addTab("test 5", new JPanel());
		projectsPane.setTabComponentAt(projectsPane.getTabCount() - 1, new CloseableTab());
		projectsPane.addTab("test 6", new JPanel());
		projectsPane.setTabComponentAt(projectsPane.getTabCount() - 1, new CloseableTab());
		//*/
		
		JPanel assetsPanel = new JPanel(new BorderLayout());
		assetsPanel.add(new JLabel("Assets:"),BorderLayout.NORTH);
		assetsList = new JList();
		assetsPanel.add(assetsList,BorderLayout.CENTER);
		JPanel assetsButtons = new JPanel(new FlowLayout());
		assetsButtons.add(new JButton("Model"));
		assetsButtons.add(new JButton("Texture"));
		assetsPanel.add(assetsButtons,BorderLayout.SOUTH);
		
		JPanel targetsPanel = new JPanel(new BorderLayout());
		targetsPanel.add(new JLabel("Targets:"),BorderLayout.NORTH);
		targetsList = new JList<String>(defaultTargets);
		targetsPanel.add(targetsList,BorderLayout.CENTER);
		targetsPanel.add(new JButton("Framebuffer"),BorderLayout.SOUTH);
		
		JPanel stagesPanel = new JPanel(new BorderLayout());
		stagesPanel.add(new JLabel("Stages:"),BorderLayout.NORTH);
		stagesList = new JList();
		stagesPanel.add(stagesList,BorderLayout.CENTER);
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
		JFileChooser j = new JFileChooser();
		if (lastDir != null) j.setCurrentDirectory(lastDir);
		if(e.getSource() == openShader) {
			j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int status = j.showOpenDialog(this);
			if(status == JFileChooser.APPROVE_OPTION) {
				JTabbedPane thisProject = new JTabbedPane();
				thisProject.addTab("Manage", new JPanel());
				File selected = j.getSelectedFile();
				for(int i = 0; i < assemblyStages.length; i++){
					File stagePath = new File(selected, assemblyStages[i]);
					if (stagePath.canRead()) {
						JEditorPane codeEditor = new JEditorPane();
						JScrollPane scrPane = new JScrollPane(codeEditor);
						
						thisProject.addTab(assemblyStages[i],scrPane);
						
						codeEditor.setContentType("text/glsl");
						try{
							codeEditor.setText(new String(Files.readAllBytes(stagePath.toPath())));
						} catch (IOException ioe) {
							ioe.printStackTrace();
							return;
						}
					}
					
				}
				
				try {
					projectsPane.addTab(selected.getCanonicalPath(), thisProject);
					projectsPane.setTabComponentAt(projectsPane.getTabCount() - 1, new CloseableTab());
				} catch (IOException ioe) {
					ioe.printStackTrace();
					return;
				}
				
			}
		} else if (e.getSource() == newShader) {
			
		} else if (e.getSource() == openPipeline) {
			
		} else if (e.getSource() == resetPipeline) {
			
		} else {
			throw new IllegalStateException("A mystery button was pushed!");
		}
		lastDir = j.getCurrentDirectory();
	}
}