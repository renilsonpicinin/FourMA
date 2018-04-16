package com.general.mbts4ma.view.framework.bo;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;

import com.general.mbts4ma.EventInstance;
import com.general.mbts4ma.Parameter;
import com.general.mbts4ma.view.MainView;
import com.general.mbts4ma.view.dialog.EventPropertiesDialog;
import com.general.mbts4ma.view.framework.gson.GsonBuilderSingleton;
import com.general.mbts4ma.view.framework.util.FileUtil;
import com.general.mbts4ma.view.framework.util.MapUtil;
import com.general.mbts4ma.view.framework.util.StringUtil;
import com.general.mbts4ma.view.framework.vo.GraphProjectVO;
import com.github.eta.esg.Vertex;
import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxUtils;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.util.png.mxPngEncodeParam;
import com.mxgraph.util.png.mxPngImageEncoder;
import com.mxgraph.view.mxGraph;

public class GraphProjectBO implements Serializable {

	public static synchronized void updateGraph(GraphProjectVO graphProject, mxGraph graph) {
		graphProject.setGraphXML(mxXmlUtils.getXml(new mxCodec().encode(graph.getModel())));
	}

	public static synchronized String generateJSON(GraphProjectVO graphProject) {
		if (graphProject != null) {
			return GsonBuilderSingleton.getInstance().getGson().toJson(graphProject);
		}

		return null;
	}

	public static synchronized GraphProjectVO open(String path) {
		path = path + (path.endsWith(".mbtsma") ? "" : ".mbtsma");

		String json = FileUtil.readFile(new File(path));

		if (json == null || "".equalsIgnoreCase(json)) {
			return null;
		} else {
			GraphProjectVO graphProject = GsonBuilderSingleton.getInstance().getGson().fromJson(json, GraphProjectVO.class);
			
			//if(!graphProject.getAndroidProjectPath().isEmpty()){
			//	graphProject.setItsAndroidProject(true);
			//}
			
			if(graphProject.getFramework() == null || graphProject.getFramework().isEmpty()){
				graphProject.setFramework("other");
			}
			
			graphProject.setFileSavingPath(path);

			return graphProject;
		}
	}

	public static synchronized boolean save(String path, GraphProjectVO graphProject) {
		path = path + (path.endsWith(".mbtsma") ? "" : ".mbtsma");

		graphProject.setFileSavingPath(path);

		String graphProjectJSON = GraphProjectBO.generateJSON(graphProject);

		FileUtil.writeFile(graphProjectJSON, new File(path));

		return true;
	}

	public static synchronized boolean save(String path, String content, String extension) {
		path = path + (path.endsWith("." + extension) ? "" : "." + extension);

		FileUtil.writeFile(content, new File(path));

		return true;
	}

	public static synchronized boolean exportToPNG(mxGraph graph, mxGraphComponent graphComponent, String path) throws Exception {
		path = path + (path.endsWith(".png") ? "" : ".png");

		String xml = URLEncoder.encode(mxXmlUtils.getXml(new mxCodec().encode(graph.getModel())), "UTF-8");

		BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, Color.WHITE, graphComponent.isAntiAlias(), null, graphComponent.getCanvas());

		mxPngEncodeParam param = mxPngEncodeParam.getDefaultEncodeParam(image);
		param.setCompressedText(new String[] { "graph", xml });

		FileOutputStream outputStream = new FileOutputStream(new File(path));

		try {
			mxPngImageEncoder encoder = new mxPngImageEncoder(outputStream, param);

			if (image != null) {
				encoder.encode(image);
			}
		} finally {
			outputStream.close();
		}

		return true;
	}

	public static synchronized boolean exportToXML(mxGraph graph, String path) throws Exception {
		path = path + (path.endsWith(".xml") ? "" : ".xml");

		String xml = mxXmlUtils.getXml(new mxCodec().encode(graph.getModel()));

		FileUtil.writeFile(xml, new File(path));

		return true;
	}

	public static synchronized boolean loadGraphFromXML(mxGraph graph, String xml) throws Exception {
		Document document = mxXmlUtils.parseXml(xml);

		new mxCodec().decode(document.getDocumentElement(), graph.getModel());

		return true;
	}

	public static synchronized boolean importFromXML(mxGraph graph, String path) throws Exception {
		path = path + (path.endsWith(".xml") ? "" : ".xml");

		Document document = mxXmlUtils.parseXml(mxUtils.readFile(path));

		new mxCodec().decode(document.getDocumentElement(), graph.getModel());

		return true;
	}

	public static synchronized void generateReusedESG(mxGraph graph, GraphProjectVO graphProject) {
		if (graph != null) {
			Object[] edges = graph.getChildEdges(graph.getDefaultParent());

			if (edges != null && edges.length > 0) {
				for (Object edge : edges) {
					mxCell customEdge = (mxCell) edge;

					if (MainView.MARKED_EDGE.equals(customEdge.getStyle())) {
						mxCell source = (mxCell) customEdge.getSource();
						mxCell target = (mxCell) customEdge.getTarget();

						double newX = (source.getGeometry().getCenterX() + target.getGeometry().getCenterX()) / 2;

						double newY = (source.getGeometry().getCenterY() + target.getGeometry().getCenterY()) / 2;

						graph.getModel().beginUpdate();

						// graph.removeCells(new Object[] { customEdge });

						mxCell newVertex = (mxCell) graph.insertVertex(graph.getDefaultParent(), UUID.randomUUID().toString(), customEdge.getValue(), newX, newY, 100, 50, MainView.GENERATED_EVENT_VERTEX);

						graph.insertEdge(graph.getDefaultParent(), UUID.randomUUID().toString(), "", source, newVertex, MainView.GENERATED_EDGE);

						graph.insertEdge(graph.getDefaultParent(), UUID.randomUUID().toString(), "", newVertex, target, MainView.GENERATED_EDGE);

						graph.getModel().endUpdate();

						graph.setSelectionCell(newVertex);

						graphProject.updateMethodTemplateByVertice(newVertex.getId(), (String) newVertex.getValue());

						String methodTemplateContent = FileUtil.readFile(new File("templates" + File.separator + "robotium-templates" + File.separator + "utility-methods" + File.separator + ((String) newVertex.getValue()).replace(" ", "") + ".java"));

						List<String> values = StringUtil.getValuesWithRegEx(methodTemplateContent, "\\{\\{([a-z]+)\\}\\}");

						if (values != null && !values.isEmpty()) {
							EventPropertiesDialog dialog = new EventPropertiesDialog(graphProject, MapUtil.fromList(values));

							dialog.setVisible(true);

							graphProject.updateMethodTemplatePropertiesByVertice(newVertex.getId(), dialog.getValues());
						}
					}
				}
			}
		}
	}

	public static synchronized List<String> getAllVertices(mxGraph graph) {
		List<String> vertices = null;

		if (graph != null) {
			vertices = new LinkedList<String>();

			Object[] cells = graph.getChildVertices(graph.getDefaultParent());

			if (cells != null && cells.length > 0) {
				for (Object cell : cells) {
					vertices.add((String) ((mxCell) cell).getValue());
				}
			}
		}
		return vertices;
	}
	
	public static synchronized Map<String, String> generateMethodNamesMapFromCESs(List<List<Vertex>> cess, String... excepts) {
		Map<String, String> methodNames = null;

		if (cess != null && !cess.isEmpty()) {
			methodNames = new LinkedHashMap<String, String>();

			for (List<Vertex> ces : cess) {
				for (Vertex vertice : ces) {
					boolean insert = true;

					if (excepts != null && excepts.length > 0) {
						for (String except : excepts) {
							if (except.equalsIgnoreCase(vertice.getId())) {
								insert = false;
								break;
							}
						}
					}

					if (insert) {
						methodNames.put(vertice.getId(), StringUtil.toCamelCase(vertice.getName(), false, "[", "]"));
					}
				}
			}
		}

		return methodNames;
	}

	public static synchronized List<String> generateMethodNamesListFromCES(List<Vertex> ces) {
		List<String> methodNames = null;

		if (ces != null && !ces.isEmpty()) {
			methodNames = new LinkedList<String>();

			for (Vertex vertice : ces) {
				methodNames.add(StringUtil.toCamelCase(vertice.getName(), false, "[", "]"));
			}
		}

		return methodNames;
	}

	public static synchronized List<String> generateMethodNamesFromVertices(List<String> vertices) {
		List<String> methodNames = null;

		if (vertices != null && !vertices.isEmpty()) {
			methodNames = new LinkedList<String>();

			for (String vertice : vertices) {
				methodNames.add(StringUtil.toCamelCase(vertice, false, "[", "]"));
			}
		}

		return methodNames;
	}
	
	public static synchronized boolean containsVerticeEventInstance (GraphProjectVO graphProject, List<Vertex> ces) {
		if (ces != null && !ces.isEmpty()) {
			for (Vertex vertice : ces) {
				if (graphProject.getEventInstanceByVertice(vertice.getId()) != null) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static synchronized void generateMethodNamesEventInstanceListFromCES(GraphProjectVO graphProject, Map<String, String> parameters, List<Vertex> ces, StringBuilder testingMethodBodies) throws IOException {
		File parentPath = new File("..");
		String testingMethodTemplate = FileUtil.readFile(new File(parentPath.getCanonicalPath() + File.separator + "templates" + File.separator + "generic-templates" + File.separator + "TestingMethodTemplate.java"));
		testingMethodTemplate = StringUtil.replace(testingMethodTemplate, parameters);
		
		if(graphProject.getFramework().equals("robotium")){
			testingMethodTemplate = FileUtil.readFile(new File(parentPath.getCanonicalPath() + File.separator + "templates" + File.separator + "robotium-templates" + File.separator + "TestingMethodTemplate.java"));
		}
		
		testingMethodTemplate = StringUtil.replace(testingMethodTemplate, parameters);
		
		List<String> originalCes = null;
		List<String> stringSequence = null;
		String testingMethodBody = "";
		Map<Integer, ArrayList<EventInstance>> map = null;
		int index = 0;

		if (ces != null && !ces.isEmpty()) {
			originalCes = new LinkedList<String>();
			map = new LinkedHashMap<Integer, ArrayList<EventInstance>>();
			
			for (Vertex vertice : ces) {
				originalCes.add(StringUtil.toCamelCase(vertice.getName(), false, "[", "]"));
				if (graphProject.getEventInstanceByVertice(vertice.getId()) != null) {
					if (!graphProject.getEventInstanceByVertice(vertice.getId()).isEmpty()) map.put(index, graphProject.getEventInstanceByVertice(vertice.getId()));
				}
				index++;
			}

			Iterator<Integer> iKey = map.keySet().iterator();
			
			while (iKey.hasNext()) {
				int key = iKey.next();
				ArrayList<EventInstance> values = map.get(key);
				for (EventInstance e : values) {
					stringSequence = new LinkedList<String>(originalCes);
					String nome = stringSequence.get(key);
					stringSequence.set(key, e.getId());
					
					StringBuilder sb = new StringBuilder();
					for (Parameter p : e.getParameters()) {
						if (e.getParameters().size() - 1 == e.getParameters().lastIndexOf(p)) {
							sb.append(p.getType() + " " + p.getName());
						} else {
							sb.append(p.getType() + " " + p.getName() + ", ");
						}		
					}
					
					// METHOD TEMPLATE
					testingMethodBody = testingMethodTemplate
							.replace("{{testingmethodname}}", "EVENT" + e.getId())
							.replace("{{ces}}", StringUtil.convertListToString(stringSequence, "[", "]").replaceAll(e.getId(), nome + "(" + sb.toString() + ")") );
					
					testingMethodBodies.append(testingMethodBody);
					
					if (testingMethodBodies.length() > 0  && testingMethodBodies.indexOf("\n\n") != testingMethodBodies.length() - 1) {
						testingMethodBodies.append("\n\n");
					}
				}	
			}
		}
	}

	//TODO SEQUENCIAS COM EVENTINSTANCE
	public static synchronized boolean generateTestingCodeSnippets(GraphProjectVO graphProject, Map<String, String> parameters, File testingCodeSnippetsDirectory, List<List<Vertex>> cess) throws Exception {
		File parentPath = new File("..");
		
		String testingClassTemplate = FileUtil.readFile(new File(parentPath.getCanonicalPath() + File.separator + "templates" + File.separator + "generic-templates" + File.separator + "TestingClassTemplate.java"));
		String testingMethodTemplate = FileUtil.readFile(new File(parentPath.getCanonicalPath() + File.separator + "templates" + File.separator + "generic-templates" + File.separator + "TestingMethodTemplate.java"));
		
		if(graphProject.getFramework().equals("robotium")){
			testingClassTemplate = FileUtil.readFile(new File(parentPath.getCanonicalPath() + File.separator + "templates" + File.separator + "robotium-templates" + File.separator + "TestingClassTemplate.java"));
			testingMethodTemplate = FileUtil.readFile(new File(parentPath.getCanonicalPath() + File.separator + "templates" + File.separator + "robotium-templates" + File.separator + "TestingMethodTemplate.java"));
		}
		
		testingClassTemplate = StringUtil.replace(testingClassTemplate, parameters);
		testingMethodTemplate = StringUtil.replace(testingMethodTemplate, parameters);

		if (testingCodeSnippetsDirectory.exists()) {
			FileUtils.deleteDirectory(testingCodeSnippetsDirectory);
		}

		testingCodeSnippetsDirectory.mkdir();

		if (cess != null && !cess.isEmpty()) {
			StringBuilder testingMethodBodies = new StringBuilder("");

			int count = 1;

			for (List<Vertex> ces : cess) {
				String testingMethodBody = testingMethodTemplate.replace("{{testingmethodname}}", "CES" + count++).replace("{{ces}}", StringUtil.convertListToString(generateMethodNamesListFromCES(ces), "[", "]").replace(" ", ""));

				if (testingMethodBodies.length() > 0) {
					testingMethodBodies.append("\n\n");
				}
				
				if (containsVerticeEventInstance(graphProject, ces)) generateMethodNamesEventInstanceListFromCES(graphProject, parameters, ces, testingMethodBodies);

				testingMethodBodies.append(testingMethodBody);
			}

			testingClassTemplate = testingClassTemplate.replace("{{testingmethodtemplate}}", testingMethodBodies.toString());

			FileUtil.writeFile(testingClassTemplate, new File(testingCodeSnippetsDirectory.getAbsolutePath() + File.separator + parameters.get("{{testingclassname}}") + "Test.java"));

			copyUtilityClasses(parameters, testingCodeSnippetsDirectory);

			generateTestingAdapters(graphProject, generateMethodNamesMapFromCESs(cess, MainView.ID_START_VERTEX, MainView.ID_END_VERTEX), parameters, testingCodeSnippetsDirectory);
		}

		return true;
	}

	private static synchronized void generateTestingAdapters(GraphProjectVO graphProject, Map<String, String> methodNames, Map<String, String> parameters, File testingCodeSnippetsDirectory) throws Exception {
		File parentPath = new File("..");
		
		String testingAdapterClassTemplate = FileUtil.readFile(new File(parentPath.getCanonicalPath() + File.separator + "templates" + File.separator + "generic-templates" + File.separator + "adapter-templates" + File.separator + "TestingAdapterClassTemplate.java"));
		String testingAdapterMethodTemplate = FileUtil.readFile(new File(parentPath.getCanonicalPath() + File.separator + "templates" + File.separator + "generic-templates" + File.separator + "adapter-templates" + File.separator + "TestingAdapterMethodTemplate.java"));

		if(graphProject.getFramework().equals("robotium")){
			testingAdapterClassTemplate = FileUtil.readFile(new File(parentPath.getCanonicalPath() + File.separator + "templates" + File.separator + "robotium-templates" + File.separator + "adapter-templates" + File.separator + "TestingAdapterClassTemplate.java"));
			testingAdapterMethodTemplate = FileUtil.readFile(new File(parentPath.getCanonicalPath() + File.separator + "templates" + File.separator + "robotium-templates" + File.separator + "adapter-templates" + File.separator + "TestingAdapterMethodTemplate.java"));
		}
		
		testingAdapterClassTemplate = StringUtil.replace(testingAdapterClassTemplate, parameters);

		File testingCodeSnippetsAdaptersDirectory = new File(testingCodeSnippetsDirectory.getAbsolutePath() + File.separator + "adapters");

		if (testingCodeSnippetsAdaptersDirectory.exists()) {
			FileUtils.deleteDirectory(testingCodeSnippetsAdaptersDirectory);
		}

		testingCodeSnippetsAdaptersDirectory.mkdir();

		StringBuilder testingMethodBodies = new StringBuilder("");

		if (methodNames != null && !methodNames.isEmpty()) {
			Iterator<String> iMethodNames = methodNames.keySet().iterator();

			while (iMethodNames.hasNext()) {
				String key = iMethodNames.next();
				String value = methodNames.get(key);
												
				if(testingMethodBodies.indexOf(value) == -1){
					
					String testingMethodBody = testingAdapterMethodTemplate.replace("{{testingmethodname}}", value);


				if (graphProject.getMethodTemplatesByVertices().containsKey(key)) {
					String methodTemplateContent = FileUtil.readFile(new File(parentPath.getCanonicalPath() + File.separator + "templates" + File.separator + "robotium-methods" + File.separator + graphProject.getMethodTemplatesByVertices().get(key).replace(" ", "") + ".java"));

					if (methodTemplateContent == null || "".equalsIgnoreCase(methodTemplateContent)) {
						methodTemplateContent = FileUtil.readFile(new File(parentPath.getCanonicalPath() + File.separator + "templates" + File.separator + "utility-methods" + File.separator + graphProject.getMethodTemplatesByVertices().get(key).replace(" ", "") + ".java"));
					}
					methodTemplateContent = validateEventProperties(methodTemplateContent, graphProject.getMethodTemplatesPropertiesByVertices().get(key));
					testingMethodBody = testingMethodBody.replace("{{testingmethodtemplate}}", methodTemplateContent);
					
				} else {
					testingMethodBody = testingMethodBody.replace("{{testingmethodtemplate}}", "");
				}

				if (testingMethodBodies.length() > 0) {
					testingMethodBodies.append("\n\n");
				}
					testingMethodBodies.append(testingMethodBody);
				}							
			}
		}

		testingAdapterClassTemplate = testingAdapterClassTemplate.replace("{{testingmethodtemplate}}", testingMethodBodies.toString());

		FileUtil.writeFile(testingAdapterClassTemplate, new File(testingCodeSnippetsAdaptersDirectory.getAbsolutePath() + File.separator + parameters.get("{{testingclassname}}") + "Adapter.java"));
	}

	private static synchronized String validateEventProperties(String methodTemplateContent, Map<String, String> properties) {
		if (properties != null && !properties.isEmpty()) {
			Iterator<String> iProperties = properties.keySet().iterator();

			while (iProperties.hasNext()) {
				String key = iProperties.next();
				String value = properties.get(key);

				if (value != null && !"".equalsIgnoreCase(value)) {
					methodTemplateContent = methodTemplateContent.replace("{{" + key + "}}", value);
				}
			}
		}

		return methodTemplateContent;
	}

	private static synchronized void copyUtilityClasses(Map<String, String> parameters, File testingCodeSnippetsDirectory) throws Exception {
		File parentPath = new File("..");
		
		File[] utilityClasses = new File(parentPath.getCanonicalPath() + File.separator + "templates" + File.separator + "utility-classes").listFiles();

		if (utilityClasses != null && utilityClasses.length > 0) {
			File testingCodeSnippetsUtilityClassesDirectory = new File(testingCodeSnippetsDirectory.getAbsolutePath() + File.separator + "util");

			if (testingCodeSnippetsUtilityClassesDirectory.exists()) {
				FileUtils.deleteDirectory(testingCodeSnippetsUtilityClassesDirectory);
			}

			testingCodeSnippetsUtilityClassesDirectory.mkdir();

			for (File utilityClass : utilityClasses) {
				String utilityClassTemplate = FileUtil.readFile(utilityClass);

				utilityClassTemplate = StringUtil.replace(utilityClassTemplate, parameters);

				FileUtil.writeFile(utilityClassTemplate, new File(testingCodeSnippetsUtilityClassesDirectory.getAbsolutePath() + File.separator + utilityClass.getName()));
			}
		}
	}

	public static synchronized Map<String, String> getMethodTemplates(String framework) {
		File parentPath = new File("..");
		
		Map<String, String> methodTemplates = new LinkedHashMap<String, String>();

		File[] robotiumMethods = null;
		try {
			if(framework == "robotium"){
				robotiumMethods = new File(parentPath.getCanonicalPath() + File.separator + "templates" + File.separator + "robotium-templates" + File.separator + "robotium-methods").listFiles();
			} else {
				robotiumMethods = new File(parentPath.getCanonicalPath() + File.separator + "templates" + File.separator + "generic-templates" + File.separator + "generic-methods").listFiles();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (robotiumMethods != null && robotiumMethods.length > 0) {
			for (File robotiumMethod : robotiumMethods) {
				methodTemplates.put(StringUtil.splitCamelCase(robotiumMethod.getName().substring(0, robotiumMethod.getName().lastIndexOf("."))), "");
			}
		}

		return methodTemplates;
	}

	public static synchronized Map<String, String> getEdgeTemplates(String framework) {
		File parentPath = new File("..");
		Map<String, String> edgeTemplates = new LinkedHashMap<String, String>();

		File[] utilityMethods = null;
		try {
			if(framework == "robotium"){
				utilityMethods = new File(parentPath.getCanonicalPath() + File.separator + "templates" + File.separator + "robotium-templates" + File.separator + "utility-methods").listFiles();
			} else {
				utilityMethods = new File(parentPath.getCanonicalPath() + File.separator + "templates" + File.separator + "generic-templates" + File.separator + "generic-utility-methods").listFiles();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (utilityMethods != null && utilityMethods.length > 0) {
			for (File utilityMethod : utilityMethods) {
				edgeTemplates.put(StringUtil.splitCamelCase(utilityMethod.getName().substring(0, utilityMethod.getName().lastIndexOf("."))), "");
			}
		}

		return edgeTemplates;
	}

}
