package com.general.mbts4ma.view.dialog;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;

import org.apache.commons.io.FileUtils;

import com.general.mbts4ma.view.framework.bo.GraphConverter;
import com.general.mbts4ma.view.framework.bo.GraphProjectBO;
import com.general.mbts4ma.view.framework.util.StringUtil;
import com.general.mbts4ma.view.framework.vo.GraphProjectVO;
import com.github.eta.esg.Vertex;
import com.mxgraph.view.mxGraph;

public class ExtractCESsDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();

	private JTextArea txtCESs = null;
	private JPanel buttonPane;
	private JButton btnGenerateTestingCodeSnippets;
	private JButton btnExportToTxt;

	private JScrollPane scrollPane;

	private mxGraph graph = null;
	private GraphProjectVO graphProject = null;
	private List<List<Vertex>> cess = null;
	private String cessAsString = null;

	public ExtractCESsDialog(mxGraph graph, GraphProjectVO graphProject, List<List<Vertex>> cess, String cessAsString) {
		this.graph = graph;
		this.graphProject = graphProject;
		this.cess = cess;
		this.cessAsString = cessAsString;

		this.setTitle("Extract CESs");

		this.setBounds(100, 100, 500, 500);
		this.getContentPane().setLayout(new BorderLayout());
		this.setResizable(true);
		this.setModal(true);
		this.setLocationRelativeTo(null);
		this.contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

		this.getContentPane().add(this.contentPanel, BorderLayout.CENTER);

		this.txtCESs = new JTextArea();
		this.txtCESs.setEditable(false);
		this.txtCESs.setWrapStyleWord(true);
		this.txtCESs.setLineWrap(true);
		this.txtCESs.setFont(new Font("Verdana", Font.PLAIN, 12));

		((DefaultCaret) this.txtCESs.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

		this.scrollPane = new JScrollPane(this.txtCESs);

		GroupLayout gl_contentPanel = new GroupLayout(this.contentPanel);
		gl_contentPanel.setHorizontalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING).addGroup(gl_contentPanel.createSequentialGroup().addContainerGap().addComponent(this.scrollPane, GroupLayout.DEFAULT_SIZE, 454, Short.MAX_VALUE).addContainerGap()));
		gl_contentPanel.setVerticalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING).addGroup(gl_contentPanel.createSequentialGroup().addContainerGap().addComponent(this.scrollPane, GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE).addContainerGap()));
		this.contentPanel.setLayout(gl_contentPanel);

		this.buttonPane = new JPanel();
		this.getContentPane().add(this.buttonPane, BorderLayout.SOUTH);

		this.btnGenerateTestingCodeSnippets = new JButton("");
		this.btnGenerateTestingCodeSnippets.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					ExtractCESsDialog.this.generateTestingCodeSnippets();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		});
		this.btnGenerateTestingCodeSnippets.setToolTipText("Generate testing code snippets");
		this.btnGenerateTestingCodeSnippets.setIcon(new ImageIcon(ExtractCESsDialog.class.getResource("/com/general/mbts4ma/view/framework/images/testingcodesnippets.png")));

		this.btnExportToTxt = new JButton("");
		this.btnExportToTxt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ExtractCESsDialog.this.exportToTxt();
			}
		});
		this.btnExportToTxt.setIcon(new ImageIcon(ExtractCESsDialog.class.getResource("/com/general/mbts4ma/view/framework/images/txtfile.png")));
		this.btnExportToTxt.setToolTipText("Export to TXT");
		GroupLayout gl_buttonPane = new GroupLayout(this.buttonPane);
		gl_buttonPane.setHorizontalGroup(gl_buttonPane.createParallelGroup(Alignment.LEADING).addGroup(gl_buttonPane.createSequentialGroup().addContainerGap().addComponent(this.btnGenerateTestingCodeSnippets, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.btnExportToTxt, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE).addContainerGap(378, Short.MAX_VALUE)));
		gl_buttonPane.setVerticalGroup(gl_buttonPane.createParallelGroup(Alignment.LEADING).addGroup(gl_buttonPane.createSequentialGroup().addContainerGap().addGroup(gl_buttonPane.createParallelGroup(Alignment.LEADING).addComponent(this.btnExportToTxt, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE).addComponent(this.btnGenerateTestingCodeSnippets, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		this.buttonPane.setLayout(gl_buttonPane);
		
		//ESG metrics
		Map<String,	String> metrics = GraphConverter.getMetricsFromESG(graph);
		StringBuilder metricsStr = new StringBuilder();
		metricsStr.append("ESG Metrics");
		metricsStr.append("\r\n");
		for(String metricName : metrics.keySet()) {
			metricsStr.append(metricName + ": " + metrics.get(metricName));
			metricsStr.append("\r\n");
		}
		metricsStr.append("\r\n");
		metricsStr.append("\r\n");
		metricsStr.append("CESs (Test Sequences)");
		metricsStr.append("\r\n");
		metricsStr.append("\r\n");
		
		if (cessAsString != null && !"".equalsIgnoreCase(cessAsString)) {
			this.txtCESs.setText(metricsStr.toString() + cessAsString);
		} else {
			this.txtCESs.setText("");
		}
	}

	private void generateTestingCodeSnippets() throws Exception {
		Map<String, String> parameters = new LinkedHashMap<String, String>();
	
		if(this.graphProject.getItsAndroidProject()){
			String mainTestingActivity = StringUtil.unaccent(StringUtil.stripAccents(this.graphProject.getMainTestingActivity()));
			
			parameters.put("{{projectpackage}}", this.graphProject.getApplicationPackage());
			parameters.put("{{otherimports}}", "");
			parameters.put("{{testingclassname}}", mainTestingActivity);
			parameters.put("{{activity}}", mainTestingActivity);
		} else {
			parameters.put("{{projectpackage}}", "{{package}}");
			parameters.put("{{otherimports}}", "");
			parameters.put("{{testingclassname}}", "{{ClassName}}");
			parameters.put("{{activity}}", "");
		}
		
		File testingCodeSnippetsDirectory = new File(this.graphProject.getFileSavingDirectory() + File.separator + "testing-code-snippets");

		JFileChooser fileChooser = new JFileChooser(this.graphProject.getFileSavingDirectory());
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setSelectedFile(new File("testing-code-snippets"));
		fileChooser.setDialogTitle("Specify a file to save");
			
		int result = fileChooser.showSaveDialog(null);
			
		if (result == JFileChooser.APPROVE_OPTION) {
			testingCodeSnippetsDirectory = new File(fileChooser.getSelectedFile().getPath() + File.separator + "testing-code-snippets");
		
			if (GraphProjectBO.generateTestingCodeSnippets(this.graphProject, parameters, testingCodeSnippetsDirectory, this.cess)) {
				JOptionPane.showMessageDialog(null, "Testing code snippet successfully generated.", "Attention", JOptionPane.INFORMATION_MESSAGE);
	
				try {
					File parentPath = new File("..");
					moveEventClasses(new File(parentPath.getCanonicalPath() + File.separator + "MBTS4MA-Runner"
												+ File.separator +"src"+ File.separator +"com"+ File.separator 
												+"general"+ File.separator +"mbts4ma" +File.separator+"erunner"),
												new File(testingCodeSnippetsDirectory + File.separator + "erunner"));
					
					Desktop.getDesktop().open(testingCodeSnippetsDirectory);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void exportToTxt() {
		JFileChooser fileChooser = new JFileChooser(this.graphProject.getFileSavingDirectory());

		fileChooser.setSelectedFile(new File(this.graphProject.getName() + " - CESs"));
		fileChooser.setFileFilter(new FileNameExtensionFilter("Text file (*.txt)", "txt"));
		fileChooser.setDialogTitle("Specify a file to save");

		int result = fileChooser.showSaveDialog(null);

		if (result == JFileChooser.APPROVE_OPTION) {
			String fileSavingPath = fileChooser.getSelectedFile().getAbsolutePath();

			if (fileSavingPath != null) {
				if (GraphProjectBO.save(fileSavingPath, this.cessAsString, "txt")) {
					JOptionPane.showMessageDialog(null, "Text file successfully exported.", "Attention", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		}
	}
	
	private void moveEventClasses(File source, File destiny) {
		 
		if (source.isDirectory()) {
            if (!destiny.exists()) {
            	destiny.mkdir();
            }
            
            try {
				FileUtils.copyDirectory(source, destiny);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Error when copying class from 'erunner'");
				e.printStackTrace();
			}
        }
	}
}
