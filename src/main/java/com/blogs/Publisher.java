package com.blogs;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.SQLException;

public class Publisher {
    public static void main(String[] args) throws SQLException, IOException, SAXException, ParserConfigurationException, GitAPIException {
        Application appObj = new Application();
        //appObj.builderProjects();
       //appObj.builderProDependency();
        appObj.updateVersion();
    }
}