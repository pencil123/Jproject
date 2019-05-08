package com.blogs;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

public class Publisher {
    public static void main(String[] args) throws SQLException, IOException, SAXException, ParserConfigurationException, GitAPIException, TransformerException {
        //设置代码父目录
        String parrentPath = "D:\\git\\jsh-bak";
        Application appObj = new Application(parrentPath);

        //遍历代码父目录下的工程目录，并判断是否为Maven工程;用于初始化操作，不能重复执行
        //appObj.builderProjects();

        //更新分支的代码
       // appObj.checkoutBranch("master");
        //appObj.checkoutBranch("dev");

        //更新工程的tags列表到数据库 project_tags_list
        //appObj.updateTags();

        //将工程的最新tag,master分支和dev分支中的version 写入到数据库publish_projects_list表
        //appObj.updateVersion();

        //遍历工程的POM文件，判断工程是父工程（依赖其他工程）或者子工程（被其他工程依赖）
       //appObj.builderProDependency();

        //父工程中POM文件对子工程依赖的Version信息，写入到projects_dependencies表
        //appObj.updateProjectsDependencies();

        //输出要发版的工程；不包含front 前端工程
         //appObj.listPublish();

      //更新DEV 分支中的POM文件，工程Version 和依赖工程的version信息
       //appObj.updateDevPom();

       //dev 分支merge到master分支
      //appObj.branchDevToMaster();

        //Master 分支打tag
       // appObj.masterTag();

        // push master 分支到远程
        //appObj.pushMaster();


        // 周一以后的预生产发版操作方法
        appObj.fullowPreMasterCreateTag();
    }
}