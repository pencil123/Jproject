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

  public Application() throws SQLException {
    this.mysqlAPI = new JdbcUtils();
    this.parrentPath = new File("D:\\git\\jsh-bak");
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

  public boolean updateVersion() throws GitAPIException ,IOException,SQLException,SAXException,ParserConfigurationException{
    String stringSQL = "select name from publish_projects_list where isMaven=1";
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

  public boolean builderProDependency () throws SQLException, IOException, SAXException, ParserConfigurationException {
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
      System.out.println(pomFile);
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
