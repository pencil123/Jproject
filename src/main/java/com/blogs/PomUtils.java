package com.blogs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
  public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
    System.out.println("hello");
    File f = new File("../pom.xml");
    //创建一个文档解析器工厂
    DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
    //使用工程，创建一个文档解析器
    DocumentBuilder builder = fac.newDocumentBuilder();
    //使用文档解析器解析一个文件，放到document对象中
    Document doc = builder.parse(f);
    doc.getDocumentElement();

  }
}
