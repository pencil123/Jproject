package com.blogs;

import java.io.File;
import java.io.IOException;
import java.sql.Array;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.eclipse.jgit.api.errors.GitAPIException;

import org.xml.sax.SAXException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author lyzhang
 * @since 2019/4/25 19:55
 */
public class Application {
  private JdbcUtils mysqlAPI;
  private File parrentPath;
  private PomUtils pomObj;

  class Dependency {
    protected String name;
    //0：一般工程，1：库工程，2：修改pom的工程
    protected int type;
    protected String version;
  }
  public Application(String parrentPath) throws SQLException {
    this.mysqlAPI = new JdbcUtils();
    this.parrentPath = new File(parrentPath);
  }

  //建立工程初始列表：工程名  isMaven工程；
  public boolean builderProjects() {
    String stringSQL;
    int count =1 ;
    LinkedList<File> list = FileUtils.getSubFolders(this.parrentPath);
    for (File path : list) {
      if (FileUtils.isMaven(path)) {
        stringSQL = String.format("insert into publish_projects_list (name,isMaven) values(\"%s\",%d)",path.getName(),1);
      } else {
        stringSQL = String.format("insert into publish_projects_list (name,isMaven) values(\"%s\",%d)",path.getName(),0);
      }
      try {
      this.mysqlAPI.executeSql(stringSQL);
      } catch (SQLException e) {
        continue;
      }
      System.out.println("count " + count  + "检查文件夹完成" + path.getName());
      count ++;
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
      System.out.println("---------------------");
      System.out.println("拉取的工程名：" + projectName);
      GitUtils objGit = new GitUtils(projectPath);
      if (!objGit.branchPull(branchName)) {
        System.out.println("project :" + projectName + "pull false");
        return false;
      }
      objGit.getStatus();
    }
    System.out.println("true");
    return true;
  }

  public boolean updateVersion() throws GitAPIException ,IOException,SQLException,SAXException,ParserConfigurationException{
    String stringSQL = "select name,isMaven from publish_projects_list";
    String projectName;
    String version;
    File projectPath;
    File pomFile;
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
    System.out.println("工程tag、master、dev 版本信息处理完");
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
    int count =1;
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
      count ++;
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
          this.mysqlAPI.executeSql(stringSQL);
        }
      }
      System.out.println("工程 " + count + ":" + projectName);
    }
    //projects_dependencies

    return true;
  }

  public boolean listPublish() throws GitAPIException ,IOException,SQLException,SAXException,ParserConfigurationException{
    Map<String,Dependency> publisher = new HashMap<String,Dependency>();
    String stringSQL = "update publish_projects_list set newtag =null,newtag_type=null";
    this.mysqlAPI.executeSql(stringSQL);
    stringSQL = "select name,isChild,dev from publish_projects_list where dev like \"%-SNAPSHOT\"";
    ResultSet proSet = this.mysqlAPI.executeQuery(stringSQL);
    Dependency depenClass;
    while (proSet.next()) {
      depenClass = new Dependency();
      //System.out.println("发现工程：" + proSet.getString("name") + proSet.getString("dev"));
      if (proSet.getBoolean("isChild")) {
        depenClass.type =1;
        depenClass.version = proSet.getString("dev");
        publisher.put(proSet.getString("name"),depenClass);
        stringSQL = String.format("select name from projects_dependencies where %s is not null",proSet.getString("name").replace("-","_"));
        System.out.println("发现库工程：" + proSet.getString("name"));
        ResultSet depPro = this.mysqlAPI.executeQuery(stringSQL);
        while (depPro.next()) {
          if (!publisher.containsKey(depPro.getString("name"))) {
            depenClass = new Dependency();
            depenClass.type =2;
            publisher.put(depPro.getString("name"),depenClass);
          }
        }
      } else {
        depenClass.type =0;
        depenClass.version = proSet.getString("dev");
        publisher.put(proSet.getString("name"),depenClass);
      }
    }
    for (Map.Entry<String,Dependency> entry:publisher.entrySet()) {
      if (entry.getValue().type !=2 ) {
        String version = entry.getValue().version.replace("-SNAPSHOT","");
        stringSQL = String.format("select id from project_tags_list where name=\"%s\" and tag_name = \"%s\"",entry.getKey(),version);
        while ( this.mysqlAPI.count(stringSQL) > 0 ) {
          version = Utils.versionAddOne(version);
          stringSQL = String.format("select id from project_tags_list where name=\"%s\" and tag_name = \"%s\"",entry.getKey(),version);
        }
        stringSQL = String.format("update publish_projects_list set newtag = \"%s\",newtag_type=%d where name=\"%s\"",version,entry.getValue().type,entry.getKey());
        System.out.println(stringSQL);
      } else {
        stringSQL = String.format("select dev from publish_projects_list where name = \"%s\"",entry.getKey());
        ResultSet rs = this.mysqlAPI.executeQuery(stringSQL);
        rs.next();
        String newVersion = Utils.versionAddOne(rs.getString("dev"));
        stringSQL = String.format("select id from project_tags_list where name=\"%s\" and tag_name = \"%s\"",entry.getKey(),newVersion);
        while ( this.mysqlAPI.count(stringSQL) > 0 ) {
          newVersion = Utils.versionAddOne(newVersion);
          stringSQL = String.format("select id from project_tags_list where name=\"%s\" and tag_name = \"%s\"",entry.getKey(),newVersion);
        }
        stringSQL = String.format("update publish_projects_list set newtag = \"%s\",newtag_type=%s where name=\"%s\"",newVersion,2,entry.getKey());
        System.out.println(stringSQL);
      }
      this.mysqlAPI.executeSql(stringSQL);
    }
    return true;
  }

  public boolean updateTags() throws GitAPIException ,IOException,SQLException,SAXException,ParserConfigurationException {
    String projectName;
    File projectPath;
    String stringSQL = "select name from publish_projects_list";
    ResultSet proSet = this.mysqlAPI.executeQuery(stringSQL);
    while (proSet.next()) {
      projectName = proSet.getString("name");
      projectPath = new File(this.parrentPath.getAbsolutePath() + System.getProperty("file.separator") + projectName);
      GitUtils objGit = new GitUtils(projectPath);
      for (String tag : objGit.getAllTags()) {
        stringSQL = String.format("insert into project_tags_list (name,tag_name) values(\"%s\",\"%s\")",projectName,tag);
        try {
          this.mysqlAPI.executeSql(stringSQL);
        } catch (SQLException e) {
          continue;
        }
      }
    }
    System.out.println("工程tags更新至数据表 project_tags_list 完成");
    return true;
  }

  public boolean updateDevPom () throws GitAPIException ,IOException,SQLException,SAXException,ParserConfigurationException{
    String projectName;
    String newVersion;
    Map<String,String> projectAddTag = new HashMap<String,String>();
    File projectPath;
    File pomFile;
    String stringSQL = "select name,newtag,isParrent from publish_projects_list where isMaven =1 and newtag is not null";
    ResultSet proSet = this.mysqlAPI.executeQuery(stringSQL);
    while (proSet.next()) {
      projectAddTag.put(proSet.getString("name"),proSet.getString("newtag"));
    }
    proSet.beforeFirst();
    while (proSet.next()) {
      projectName = proSet.getString("name");
      System.out.println("修改工程：" + projectName + "的Pom文件");
      newVersion = proSet.getString("newtag");
      projectPath = new File(this.parrentPath.getAbsolutePath() + System.getProperty("file.separator") + projectName);
      GitUtils objGit = new GitUtils(projectPath);
      if (!objGit.branchPull("dev")) {
        System.out.println("project :" + projectName + "dev branch pull false");
        return false;
      }
      pomFile = new File (this.parrentPath.getAbsolutePath() + System.getProperty("file.separator") +
              projectName + System.getProperty("file.separator") + "pom.xml");
      pomObj = new PomUtils(pomFile);

      try {
        pomObj.setVersion(newVersion);
      } catch (TransformerException e) {
        System.out.println("project :" + projectName + "update version false");
        return false;
      }

      if (proSet.getBoolean("isParrent")) {
        for (Map.Entry<String,String> entry: pomObj.getDependency().entrySet()) {
          String dependencyStringName = entry.getKey();
          if (projectAddTag.containsKey(dependencyStringName)) {
            try {
              pomObj.setDependency(dependencyStringName,projectAddTag.get(dependencyStringName));
            } catch (TransformerException e) {
              System.out.println("project :" + projectName + "update subProject" + dependencyStringName + " false");
              return false;
            }
          }
        }
      }
      objGit.getStatus();
      objGit.commitPomChange("task4643:发版过程中API版本号统一维护");
    }
    return true;
  }

  public boolean branchDevToMaster () throws GitAPIException ,IOException,SQLException,SAXException,ParserConfigurationException{
    String stringSQL = "select name,newtag from publish_projects_list where newtag is not null";
    File projectPath;
    int count =1;
    String projectName;
    ResultSet proSet = this.mysqlAPI.executeQuery(stringSQL);
    while (proSet.next()) {
      projectName = proSet.getString("name");
      projectPath = new File(this.parrentPath.getAbsolutePath() + System.getProperty("file.separator") + projectName);
      System.out.println(count + "工程：" + projectName + " DEV分支Merge到Master");
      GitUtils objGit = new GitUtils(projectPath);
      if (objGit.mergeBranch("dev","master")) {
        System.out.println("merge sucess");
      }
      count ++;
    }
    return true;
  }

  public boolean masterTag() throws GitAPIException ,IOException,SQLException,SAXException,ParserConfigurationException{
    String stringSQL = "select name,newtag from publish_projects_list where newtag is not null";
    File projectPath;
    int count =1;
    String projectName;
    String newTag;
    ResultSet proSet = this.mysqlAPI.executeQuery(stringSQL);
    while (proSet.next()) {
      projectName = proSet.getString("name");
      newTag = proSet.getString("newtag");
      System.out.println(count + " 工程：" + projectName + "打Tag :" + newTag);
      projectPath = new File(this.parrentPath.getAbsolutePath() + System.getProperty("file.separator") + projectName);
      GitUtils objGit = new GitUtils(projectPath);
      if (!objGit.createTag(newTag)) {
        System.out.println("tag 创建失败");
      }
      count ++;
    }
    return true;
  }

  public boolean pushMaster() throws GitAPIException ,IOException,SQLException,SAXException,ParserConfigurationException{
    String stringSQL = "select name from publish_projects_list where newtag is not null";
    File projectPath;
    int count =1;
    String projectName;
    String newTag;
    ResultSet proSet = this.mysqlAPI.executeQuery(stringSQL);
    while (proSet.next()) {
      projectName = proSet.getString("name");
      System.out.println( count + " push 工程：" + projectName + "的Master分支");
      projectPath = new File(this.parrentPath.getAbsolutePath() + System.getProperty("file.separator") + projectName);
      GitUtils objGit = new GitUtils(projectPath);
      if (objGit.pushMaster()) {
        System.out.println("push master sucess");
      }
    }
    return true;
  }

  public static void main(String[] args) throws IOException, GitAPIException, IOException, SAXException, ParserConfigurationException, TransformerException, SQLException {

    LinkedList<File> list = FileUtils.getSubFolders(new File("D:\\git\\jsh-bak"));
    //File file = new File("D:\\jsh\\jsh\\jsh-service-log-provider");
    //JdbcUtils mysqlAPI = new JdbcUtils();
    //String stringSQL = "insert into test (id) values(123)";
    //String stringSQL = "update test set id=234;";
    //String stringSQL = "select * from test";
    //System.out.println(mysqlAPI.executeQuery(stringSQL));

    //mysqlAPI.executeSql(stringSQL);
    for (File file:list) {

/*      File pomFile = new File(file.getAbsolutePath() +  System.getProperty("file.separator") + "pom.xml");
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
      break;*/

      GitUtils gu = new GitUtils(file);
      gu.branchPull("dev");
/*      System.out.println("Project 获取最新的Tag 信息");
      System.out.println(file.getName() + "----" + gu.getLastTag().name);


      System.out.println("DEV分支从远程拉去代码并合并");
      System.out.println(gu.branchPull("dev"));
      System.out.println("Master分支从远程拉去代码并合并");
      System.out.println(gu.branchPull("master"));


      System.out.println("将dev分支的代码合并到master分支");
      System.out.println(gu.mergeBranch("dev","master"));
      System.out.println("将master分支的代码合并到dev分支");
      System.out.println(gu.mergeBranch("master","dev"));*/
    }



  }
}
