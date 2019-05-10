package com.blogs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author lyzhang
 * @since 2019/4/28 17:46
 */
public class PomUtils {
  protected static Document xmldoc;
  protected static File f;

  /**
   * 初始化对象
   *
   * @param pomFile
   * @throws ParserConfigurationException
   * @throws IOException
   * @throws SAXException
   */
  PomUtils(File pomFile) throws ParserConfigurationException, IOException, SAXException {
    //System.out.println(pomFile);
    DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
    //使用工程，创建一个文档解析器
    DocumentBuilder builder = fac.newDocumentBuilder();
    //使用文档解析器解析一个文件，放到document对象中
    f = pomFile;
    xmldoc = builder.parse(pomFile);
    xmldoc.setXmlStandalone(true);
  }

  public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
    File f = new File("pombak.xml");
    //创建一个文档解析器工厂
    DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
    //使用工程，创建一个文档解析器
    DocumentBuilder builder = fac.newDocumentBuilder();
    //使用文档解析器解析一个文件，放到document对象中
    Document xmldoc = builder.parse(f);
    Element element = xmldoc.getDocumentElement();
    NodeList list = xmldoc.getElementsByTagName("version");

/*    System.out.println(xmldoc);
    System.out.println(xmldoc.getNodeType());
    System.out.println(xmldoc.getDocumentElement());
    System.out.println(xmldoc.getDocumentElement().getNodeType());
    System.out.println(xmldoc.getDocumentElement().getFirstChild().getNodeType());
    System.out.println(xmldoc.getDocumentElement().getFirstChild().getNextSibling());
    System.out.println(xmldoc.getDocumentElement().getFirstChild().getNextSibling().getNodeType());
    System.out.println(xmldoc.getDocumentElement().getFirstChild().getNextSibling().getNextSibling().getNextSibling());*/

    Node node = element.getFirstChild();
    while (node.getNodeType() > 0) {
      System.out.println(node.getNodeName());
      node = node.getNextSibling();
      System.out.println(node.getNodeType());
      System.out.println("==");
    }
    System.out.println(xmldoc.getDocumentElement().getLastChild().getNextSibling());
    System.out.println("----------------");

    for (int i = 0; i < list.getLength(); i++) {
      System.out.println(list.item(i).getTextContent());
    }
  }

  /**
   * 获取XML文件中，第一层嵌套中的version
   *
   * @return
   */
  public static String getVersion() {
    Element element = xmldoc.getDocumentElement();
    Node node = element.getFirstChild();
    while (node.getNodeType() > 0) {
      //System.out.println(node.getNodeName());
      if ("version".equals(node.getNodeName())) {
        //System.out.println(node.getTextContent());
        return node.getTextContent();
      }
      node = node.getNextSibling();
    }
    return "NotFound";
  }

  /**
   * 设置当前文件version
   * @param projectVersion
   * @return
   * @throws TransformerException
   */
  public static boolean setVersion(String projectVersion) throws TransformerException {
    Element element = xmldoc.getDocumentElement();
    Node node = element.getFirstChild();
    boolean change = false;
    while (node != null) {
      //System.out.println(node.getNodeName());
      if ("version".equals(node.getNodeName())) {
        //System.out.println(node.getTextContent());
        node.setTextContent(projectVersion);
        change = true;
        break;
      }
      node = node.getNextSibling();
    }
    if (change) {
      TransformerFactory factor = TransformerFactory.newInstance();
      Transformer former = factor.newTransformer();
      former.transform(new DOMSource(xmldoc), new StreamResult(f));
    }
    return true;
  }

  /**
   * 获取POM文件中，工程依赖的工程信息
   *
   * @return
   */
  public static Map<String, String> getDependency() {
    Map<String, String> dependencies = new HashMap<String, String>();
    NodeList nodesDepend = xmldoc.getElementsByTagName("properties");
    //System.out.println(nodesDepend.getLength());
    Node dependency = nodesDepend.item(0).getFirstChild();
    while (dependency != null) {
      dependencies.put(dependency.getNodeName().replace(".version", ""), dependency.getTextContent());
      dependency = dependency.getNextSibling();
    }
    return dependencies;
  }

  /**
   * 设置工程POM文件中的依赖关系
   * @param projectName
   * @param projectVersion
   * @return
   * @throws TransformerException
   */
  public static boolean setDependency (String projectName,String projectVersion) throws TransformerException{
    NodeList nodesDepend = xmldoc.getElementsByTagName("properties");
    projectName = projectName + ".version";
    //System.out.println(nodesDepend.getLength());
    Node dependency = nodesDepend.item(0).getFirstChild();
    boolean change = false;
    while (dependency != null) {
      String elementName = dependency.getNodeName();
      if (elementName.equals(projectName)) {
        dependency.setTextContent(projectVersion);
        change = true;
        break;
      }
      dependency = dependency.getNextSibling();
    }
    if (change) {
      TransformerFactory factor = TransformerFactory.newInstance();
      Transformer former = factor.newTransformer();
      former.transform(new DOMSource(xmldoc),new StreamResult(f));
    }
    return true;
  }
}
