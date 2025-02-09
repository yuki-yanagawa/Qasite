package qaservice.Build.main;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class QasiteBuild {
	private static final String BULID_DIR = "qasiteBuild";
	public static void main(String[] args) throws Exception {
		// setting pomFile
		boolean needUpdate = needPomUpdate();
		if(needUpdate) {
			backUpPomXml();
			addCompileVersionToPomxml();
		}

		// Product machine is OS = Windows
		Process process = Runtime.getRuntime().exec("cmd /c mvn compile");
		int result = process.waitFor();
		if(result != 0) {
			System.err.println("Qasite build error.");
			System.exit(-1);
		}

		// build dir
		File buildDir = new File(BULID_DIR);
		if(!buildDir.exists()) {
			if(!buildDir.mkdir()) {
				System.err.println("build dir create error.");
			}
		}

		long currentTime = System.currentTimeMillis();
		Date date = new Date(currentTime);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String buildResultDirName = BULID_DIR + File.separator +BULID_DIR + "_" + sdf.format(date);
		Path buildResultPath = Paths.get(buildResultDirName);
		if(!buildResultPath.toFile().mkdir()) {
			System.err.println("build result dir create error.");
		}

		// lib file
		String libDirStr = buildResultDirName + File.separator + "lib";
		String libAddCmd = "cmd /c mvn dependency:copy-dependencies -DoutputDirectory=" + libDirStr;
		process = Runtime.getRuntime().exec(libAddCmd);
		if(process.waitFor() != 0) {
			System.err.println("lib append failed.");
			System.exit(-1);
		}

		// Manifest file create
		StringBuilder sbw = new StringBuilder();
		StringBuilder sbl = new StringBuilder();
		sbw.append("Manifest-Version: 1.0");
		sbl.append("Manifest-Version: 1.0");
		sbw.append("\r\n");
		sbl.append("\n");
		// class path
		sbw.append("Class-Path: .");
		sbl.append("Class-Path: .");
		File libDir = new File(libDirStr);
		for(File f : libDir.listFiles()) {
			sbw.append(" lib\\" + f.getName());
			sbl.append(" lib/" + f.getName());
		}
		sbw.append("\r\n");
		sbl.append("\n");

		// Main class
		sbw.append("Main-Class: ");
		sbl.append("Main-Class: ");
		// DBServerForWindows
		String manifestDBServerForWindows = "DBServerForWindows.txt";
		File f = new File(buildResultDirName + File.separator + manifestDBServerForWindows);
		try(FileOutputStream fos = new FileOutputStream(f);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			OutputStreamWriter osw = new OutputStreamWriter(bos)) {
			osw.write(sbw.toString());
			osw.write("qaservice.DBServer.main.DBServerConsoleStart");
			osw.write("\r\n");
			osw.flush();
		}

		String manifestDBServerForLinux = "DBServerForLinux.txt";
		f = new File(buildResultDirName + File.separator + manifestDBServerForLinux);
		try(FileOutputStream fos = new FileOutputStream(f);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			OutputStreamWriter osw = new OutputStreamWriter(bos)) {
			osw.write(sbl.toString());
			osw.write("qaservice.DBServer.main.DBServerConsoleStart");
			osw.write("\n");
			osw.flush();
		}

		// WebServerForWindows
		String manifestWebServerForWindows = "WebServerForWindows.txt";
		f = new File(buildResultDirName + File.separator + manifestWebServerForWindows);
		try(FileOutputStream fos = new FileOutputStream(f);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			OutputStreamWriter osw = new OutputStreamWriter(bos)) {
			osw.write(sbw.toString());
			osw.write("qaservice.WebServer.main.WebServerConsoleEntryPoint");
			osw.write("\r\n");
			osw.flush();
		}

		String manifestWebServerForLinux = "WebServerForLinux.txt";
		f = new File(buildResultDirName + File.separator + manifestWebServerForLinux);
		try(FileOutputStream fos = new FileOutputStream(f);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			OutputStreamWriter osw = new OutputStreamWriter(bos)) {
			osw.write(sbl.toString());
			osw.write("qaservice.WebServer.main.WebServerConsoleEntryPoint");
			osw.write("\n");
			osw.flush();
		}

		String tmpClassDir = buildResultDirName + File.separator + "tmpClass";
		File src = new File("target" + File.separator + "classes");
		File target = new File(tmpClassDir);
//		Files.move(src.toPath(), target.toPath());

		// robcopy
		String copyCmd = "cmd /c robocopy " + "target" + File.separator + "classes" + " " + tmpClassDir + " /s /e";
		process = Runtime.getRuntime().exec(copyCmd);
		if(process.waitFor() != 0) {
			System.err.println("lib append failed.");
			System.exit(-1);
		}

		Files.move(Paths.get(buildResultDirName + File.separator + "DBServerForWindows.txt"), Paths.get(tmpClassDir + File.separator + "manifest.txt"));

		

		// jar create
		String jarCreateCmd = "cmd /c jar cvmf " + tmpClassDir + File.separator + "manifest.txt" 
				+ " " + buildResultDirName + File.separator + "sample1234.jar" 
				+ " " + tmpClassDir + File.separator + ".";
				
		process = Runtime.getRuntime().exec(jarCreateCmd);
		if(process.waitFor() != 0) {
			System.err.println("lib append failed.");
			System.exit(-1);
		}
	}

	private static boolean needPomUpdate() throws Exception {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(new File("pom.xml"));
		Node compileSourceNode = document.getElementsByTagName("maven.compiler.source").item(0);
		Node compileTargetNode = document.getElementsByTagName("maven.compiler.target").item(0);
		if(compileSourceNode == null || compileTargetNode == null) {
			return true;
		}
		if("1.8".equals(compileSourceNode.getTextContent()) && "1.8".equals(compileTargetNode.getTextContent())) {
			return false;
		}
		return true;
	}

	public static void main2(String[] args) {
		//addProjectFileCompileJavaVersion();
		try {
			backUpPomXml();
			addCompileVersionToPomxml();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static void backUpPomXml() throws IOException {
		long mtime = System.currentTimeMillis();
		Date date = new Date(mtime);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String dateStr = sdf.format(date);
		File f = new File("pom.xml");
		File f_bk = new File("pom.xml.org_" + dateStr);
		Files.copy(f.toPath(), f_bk.toPath());
	}

	private static void addCompileVersionToPomxml() throws Exception {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(new File("pom.xml"));
		Node projectNode = document.getElementsByTagName("project").item(0);
		NodeList nodeList = projectNode.getChildNodes();
		Node properiteNode = null;
		for(int i = 0; i < nodeList.getLength(); i++) {
			if("properties".equals(nodeList.item(i).getNodeName())) {
				properiteNode = nodeList.item(i);
			}
		}

		if(properiteNode == null) {
			properiteNode = document.createElement("properties");
			projectNode.appendChild(properiteNode);
		}
		Node compileSourceNode = document.createElement("maven.compiler.source");
		Node compileSourceText = document.createTextNode("1.8");
		compileSourceNode.appendChild(compileSourceText);
		Node compileTargetNode = document.createElement("maven.compiler.target");
		Node compileTargetText = document.createTextNode("1.8");
		compileTargetNode.appendChild(compileTargetText);

		properiteNode.appendChild(compileSourceNode);
		properiteNode.appendChild(compileTargetNode);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource domSource = new DOMSource(document);
		StreamResult streamResult = new StreamResult(new File("pom.xml"));
		transformer.transform(domSource, streamResult);
	}

	private static void addProjectFileCompileJavaVersion() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		Document document = documentBuilder.parse(new File(".project"));
		Element root = document.getDocumentElement();
		Element childElement = document.createElement("properties");
		Element grandChildElement1 = document.createElement("maven.compiler.source");
		grandChildElement1.appendChild(document.createTextNode("1.8"));
		Element grandChildElement2 = document.createElement("maven.compiler.target");
		grandChildElement2.appendChild(document.createTextNode("1.8"));
		childElement.appendChild(grandChildElement1);
		childElement.appendChild(grandChildElement2);
		root.appendChild(childElement);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource domSource = new DOMSource(document);
		StreamResult streamResult = new StreamResult(new File(".project"));
		transformer.transform(domSource, streamResult);
	}

}
