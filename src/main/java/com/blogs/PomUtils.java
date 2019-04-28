package com.blogs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
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

  PomUtils(File projectDir) throws ParserConfigurationException, IOException, SAXException{
    File pomFile = new File(projectDir.getAbsolutePath() +  System.getProperty("file.separator") + "pom.xml");
    System.out.println(pomFile);
    DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
    //使用工程，创建一个文档解析器
    DocumentBuilder builder = fac.newDocumentBuilder();
    //使用文档解析器解析一个文件，放到document对象中
    xmldoc = builder.parse(pomFile);
  }
  public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
    System.out.println("hello");
    File f = new File("pombak.xml");
    //创建一个文档解析器工厂
    DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
    //使用工程，创建一个文档解析器
    DocumentBuilder builder = fac.newDocumentBuilder();
    //使用文档解析器解析一个文件，放到document对象中
    Document xmldoc = builder.parse(f);
    Element element = xmldoc.getDocumentElement();
    NodeList list = xmldoc.getElementsByTagName("version");

    System.out.println(xmldoc);
    System.out.println(xmldoc.getNodeType());
    System.out.println(xmldoc.getDocumentElement());
    System.out.println(xmldoc.getDocumentElement().getNodeType());
    System.out.println(xmldoc.getDocumentElement().getFirstChild().getNodeType());
    System.out.println(xmldoc.getDocumentElement().getFirstChild().getNextSibling());
    System.out.println(xmldoc.getDocumentElement().getFirstChild().getNextSibling().getNodeType());
    System.out.println(xmldoc.getDocumentElement().getFirstChild().getNextSibling().getNextSibling().getNextSibling());

    Node node = element.getFirstChild();
    while (node.getNodeType() > 0){
      System.out.println(node.getNodeName());
      node = node.getNextSibling();
      System.out.println("==");
    }
    System.out.println(xmldoc.getDocumentElement().getLastChild().getNextSibling());
    System.out.println("----------------");

    for (int i = 0;i < list.getLength();i++) {
      System.out.println(list.item(i).getTextContent());
    }
  }



  public static String getVersion(){
    System.out.println("hello");
    Element element = xmldoc.getDocumentElement();
    Node node = element.getFirstChild();
    while (node.getNodeType() > 0) {
      System.out.println(node.getNodeName());
      if ("version".equals(node.getNodeName())) {
        System.out.println(node.getTextContent());
        return node.getTextContent();
      }
      node = node.getNextSibling();
    }
    return "NotFound";
  }
}
