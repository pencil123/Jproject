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
    // 设置代码父目录
    String parrentPath = "C:\\Users\\pencil\\work\\git\\jsh-bak";
        Application appObj = new Application(parrentPath);

        //遍历代码父目录下的工程目录，并判断是否为Maven工程;用于初始化操作，不能重复执行
        //appObj.builderProjects();

        //更新分支的代码
        // projectsSource 从配置文件中读取operateProjects 工程列表
        // 1 从数据库读取操作工程；2 从配置文件读取操作工程

        //appObj.checkoutBranch("master",1);
        //appObj.checkoutBranch("dev",1);

        //更新工程的tags列表到数据库 project_tags_list
       //appObj.updateTags();

        //TODO更新工程的所有远程branch 到数据库project_branchs_list
        //appObj,updateBranchs()

        //将工程的最新tag,master分支和dev分支中的version 写入到数据库publish_projects_list表
      //appObj.updateVersion();

        //遍历工程的POM文件，判断工程是父工程（依赖其他工程）或者子工程（被其他工程依赖）
       //appObj.builderProDependency();

       // 父工程中POM文件对子工程依赖的Version信息，写入到projects_dependencies表
       // appObj.updateProjectsDependencies();

        //输出要发版的工程；包含front 前端工程
       // appObj.listPublish();

      //更新DEV 分支中的POM文件，工程Version 和依赖工程的version信息  并push到gerrit
        // 前端工程的tag.xml文件，直接在master分支进行修改
      ///appObj.updateDevPom();


       //dev 分支merge到master分支,从数据库读取工程列表，包括front前端工程；并没有push 到远程
      //appObj.mergeNewtagBranch("dev","master",1);
      //appObj.mergeNewtagBranch("M3701-1","dev",2);

      // 查看工程的状态  1 从数据库读取，2 从配置文件中读取
      //appObj.projectStatus(1);

        // push master 分支到远程;1 从数据库中读取操作工程;2从配置文件中读取操作工程；包括front前端工程
       //appObj.pushBranch("master",1);

        //Master 分支打tag，并push到远程
        //appObj.masterTag();

        //master 分支merge到dev分支，从数据库读取工程列表，包括front前端工程
     //appObj.mergeNewtagBranch("master","dev",1);

        // push dev 分支到远程;1 从配置文件中读取操作工程;2从数据库中读取操作工程；包括front前端工程
        //appObj.pushBranch("dev",1);

      //  -----------------------------------------------------------------------------------------------------------------------

        /**
         * 将当前Master分支的version 更新至表publish_projects_list的master_follow字段
         * 用于从预生产到生产过程中，确认要发版的工程以及版本号
         */
        //appObj.pudateFollowMaster();


     //-------------------------------------------------------------------------------------------------------------------------

        // push master 分支到远程
        //appObj.pushBranch("M3707",2);

      //更新工程的tags列表到数据库 project_tags_list
      //appObj.updateTags();


        // 周一以后的预生产发版操作方法
       appObj.fullowPreMasterCreateTag();

        //master  merge 到 DEV
        //appObj.mergeNewtagBranch("M3701-1","dev",2);

        // master 分支push
        //appObj.pushBranch("dev",2);
    }
}