package com.blogs;

import com.sun.xml.internal.ws.wsdl.parser.MemberSubmissionAddressingWSDLParserExtension;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author lyzhang
 * @since 2019/4/25 19:55
 */
public class Application {
  JdbcUtils mysqlAPI;
  File parrentPath;
  PomUtils pomObj;

  public Application(String parrentPath) throws SQLException {
    this.mysqlAPI = new JdbcUtils();
    this.parrentPath = new File(parrentPath);
  }

  public boolean builderProjects() throws  SQLException{
    String stringSQL;
    LinkedList<File> list = FileUtils.getSubFolders(this.parrentPath);
    for (File path : list) {
      if (FileUtils.isMaven(path)) {
        stringSQL = String.format("insert into publish_projects_list (name,isMaven) values(\"%s\",%d)",path.getName(),1);
      } else {
        stringSQL = String.format("insert into publish_projects_list (name,isMaven) values(\"%s\",%d)",path.getName(),0);
      }
      this.mysqlAPI.executeSql(stringSQL);
    }
    return true;
  }

  public boolean checkoutBranch(String branchName) throws GitAPIException ,IOException,SQLException,SAXException,ParserConfigurationException{
    String stringSQL = "select name from publish_projects_list";
    String projectName;
    File projectPath;
    ResultSet proSet = this.mysqlAPI.executeQuery(stringSQL);
    while (proSet.next()) {
      projectName = proSet.getString("name");
      projectPath = new File(this.parrentPath.getAbsolutePath() + System.getProperty("file.separator") + projectName);
      GitUtils objGit = new GitUtils(projectPath);
      objGit.branchPull(branchName);
    }
    return true;
  }

  public boolean updateVersion() throws GitAPIException ,IOException,SQLException,SAXException,ParserConfigurationException{
    String stringSQL = "select name,isMaven from publish_projects_list";
    String projectName;
    String version;
    File projectPath;
    File pomFile;
    LinkedList<File> list = FileUtils.getSubFolders(this.parrentPath);
    ResultSet proSet = this.mysqlAPI.executeQuery(stringSQL);
    while (proSet.next()) {
      projectName = proSet.getString("name");
      projectPath = new File(this.parrentPath.getAbsolutePath() + System.getProperty("file.separator") + projectName);
      GitUtils objGit = new GitUtils(projectPath);
      objGit.branchPull("master");
      TagsUtils objTag = objGit.getLastTag();
      stringSQL = String.format("update publish_projects_list set tag = \"%s\" where name=\"%s\"",objTag.name,projectName);
      System.out.println(stringSQL);
      this.mysqlAPI.executeSql(stringSQL);
      if(!proSet.getBoolean("isMaven")) {
        continue;
      }
      pomFile = new File(projectPath.getAbsolutePath() + System.getProperty("file.separator") + "pom.xml");
      if (objGit.branchPull("master")) {
        pomObj = new PomUtils(pomFile);
        version = pomObj.getVersion();
        stringSQL = String.format("update publish_projects_list set master =\"%s\" where name=\"%s\"",version,projectName);
        System.out.println(stringSQL);
        this.mysqlAPI.executeSql(stringSQL);
      }
      if (objGit.branchPull("dev")) {
        pomObj = new PomUtils(pomFile);
        version = pomObj.getVersion();
        stringSQL = String.format("update publish_projects_list set dev =\"%s\" where name=\"%s\"",version,projectName);
        System.out.println(stringSQL);
        this.mysqlAPI.executeSql(stringSQL);
      }
    }
    return true;
  }

  public boolean builderProDependency() throws SQLException, IOException, SAXException, ParserConfigurationException {
    Map<String,String> dependencies = new HashMap<String,String>();
    List<String> projects = new ArrayList<String>();
    String stringSQL = "select name from publish_projects_list where isMaven = 1";
    boolean isParrent = false;

    ResultSet proSet = this.mysqlAPI.executeQuery(stringSQL);
    proSet.beforeFirst();
    while (proSet.next()) {
      projects.add(proSet.getString("name"));
    }
    proSet.beforeFirst();
    while (proSet.next()) {
      File pomFile = new File (this.parrentPath.getAbsolutePath() + System.getProperty("file.separator") +
              proSet.getString("name") + System.getProperty("file.separator") + "pom.xml");
      pomObj = new PomUtils(pomFile);
      isParrent = false;
      dependencies = pomObj.getDependency();
      for (Map.Entry<String,String> entry: dependencies.entrySet()) {
        String dependencyStringName = entry.getKey().replace(".version","");
        if (projects.contains(dependencyStringName)) {
          stringSQL = String.format("update publish_projects_list set isChild =1 where name = \"%s\"",dependencyStringName);
          this.mysqlAPI.executeSql(stringSQL);
          isParrent = true;
        }
      }
      if (isParrent) {
        stringSQL = String.format(("update publish_projects_list set isParrent =1 where name=\"%s\""),proSet.getString("name"));
        this.mysqlAPI.executeSql(stringSQL);
      }
    }
    return true;
  }

  public boolean updateProjectsDependencies() throws GitAPIException ,IOException,SQLException,SAXException,ParserConfigurationException{
    String projectName;
    Map<String,String> dependencies = new HashMap<String,String>();
    List<String> projects = new ArrayList<String>();
    String stringSQL = "select name from publish_projects_list where isChild = 1";
    ResultSet proSet = this.mysqlAPI.executeQuery(stringSQL);
    stringSQL = "select * from projects_dependencies limit 1";
    ResultSet columnSet = this.mysqlAPI.executeQuery(stringSQL);
    while(proSet.next()) {
      projectName = proSet.getString("name").replace("-","_");
      projects.add(projectName);
      if (!this.mysqlAPI.isExistColumn(columnSet,projectName)) {
        stringSQL = String.format("alter table projects_dependencies add %s varchar(20) default null after name",projectName);
        System.out.println(stringSQL);
        this.mysqlAPI.execute(stringSQL);
      }
    }

    stringSQL = "select name from publish_projects_list where isParrent = 1";
    proSet = this.mysqlAPI.executeQuery(stringSQL);
    while (proSet.next()) {
      projectName = proSet.getString("name");
      stringSQL = String.format("select name from projects_dependencies where name = \"%s\"",projectName);
      if (this.mysqlAPI.count(stringSQL) == 0) {
        stringSQL = String.format("insert into projects_dependencies (name) values(\"%s\")",projectName);
        this.mysqlAPI.executeSql(stringSQL);
      }
      File pomFile = new File (this.parrentPath.getAbsolutePath() + System.getProperty("file.separator") +
              proSet.getString("name") + System.getProperty("file.separator") + "pom.xml");
      pomObj = new PomUtils(pomFile);
      dependencies = pomObj.getDependency();
      for (Map.Entry<String,String> entry: dependencies.entrySet()) {
        String dependencyStringName = entry.getKey().replace(".version","");
        dependencyStringName = dependencyStringName.replace("-","_");
        if (projects.contains(dependencyStringName)) {
          stringSQL = String.format("update projects_dependencies set %s = \"%s\" where name=\"%s\"",dependencyStringName,entry.getValue(),proSet.getString("name"));
          System.out.println(stringSQL);
          this.mysqlAPI.executeSql(stringSQL);
        }
      }

    }
    //projects_dependencies

    return true;
  }

  public Map<String,String> listPublish() throws GitAPIException ,IOException,SQLException,SAXException,ParserConfigurationException{
    Map<String,String> publisher = new HashMap<String,String>();
    String stringSQL = "select name,isChild,dev from publish_projects_list where dev like \"%-SNAPSHOT\"";
    ResultSet proSet = this.mysqlAPI.executeQuery(stringSQL);
    while (proSet.next()) {
      //System.out.println("发现工程：" + proSet.getString("name") + proSet.getString("dev"));
      publisher.put(proSet.getString("name"),proSet.getString("dev"));
      if (proSet.getBoolean("isChild")) {
        stringSQL = String.format("select name from projects_dependencies where %s is not null",proSet.getString("name").replace("-","_"));
        System.out.println("发现库工程：" + proSet.getString("name"));
        ResultSet depPro = this.mysqlAPI.executeQuery(stringSQL);
        while (depPro.next()) {
          if (!publisher.containsKey(depPro.getString("name"))) {
            publisher.put(depPro.getString("name"),"not Setting");
            //System.out.println(depPro.getString("name"));
          }
        }
      }
    }
    return publisher;
  }

  public static void main(String[] args) throws IOException, GitAPIException, IOException, SAXException, ParserConfigurationException, TransformerException, SQLException {

    LinkedList<File> list = FileUtils.getSubFolders(new File("D:\\git\\jsh-bak"));
    //File file = new File("D:\\jsh\\jsh\\jsh-service-log-provider");
    JdbcUtils mysqlAPI = new JdbcUtils();
    //String stringSQL = "insert into test (id) values(123)";
    //String stringSQL = "update test set id=234;";
    String stringSQL = "select * from test";
    System.out.println(mysqlAPI.executeQuery(stringSQL));

    //mysqlAPI.executeSql(stringSQL);
/*    for (File file:list) {

      File pomFile = new File(file.getAbsolutePath() +  System.getProperty("file.separator") + "pom.xml");
      if (pomFile.exists()) {

        //System.out.println(file.getName() + " ---"  +obj.getDependency());
        System.out.println(obj.getVersion());
        System.out.println(obj.setVersion("2.0.0"));
        System.out.println(obj.getVersion());

        Map<String,String> dependencies = new HashMap<String,String>();
        dependencies.put("dubbo.version","3.0.0");
        System.out.println(obj.setDependency(dependencies));
      } else {
        System.out.println(file.getName() + " is not maven project");
      }
      break;
    }*/
/*      GitUtils gu = new GitUtils(file);
      System.out.println("Project 获取最新的Tag 信息");
      System.out.println(file.getName() + "----" + gu.getLastTag().name);


      *//*System.out.println("DEV分支从远程拉去代码并合并");
      System.out.println(gu.branchPull("dev"));
      System.out.println("Master分支从远程拉去代码并合并");
      System.out.println(gu.branchPull("master"));*//*


      System.out.println("将dev分支的代码合并到master分支");
      System.out.println(gu.mergeBranch("dev","master"));
      System.out.println("将master分支的代码合并到dev分支");
      System.out.println(gu.mergeBranch("master","dev"));
    }*/



  }
}
