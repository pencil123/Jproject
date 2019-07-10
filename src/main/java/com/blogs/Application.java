package com.blogs;

import java.io.File;
import java.io.IOException;
import java.sql.Array;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import com.good.codepublish.FilePathUtil;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private static Logger logger = LoggerFactory.getLogger(FilePathUtil.class);


  class Dependency {
    protected String name;
    //0：一般工程，1：库工程，2：修改pom的工程
    protected int type;
    protected String version;
  }

  /**
   * 初始化对象
   *
   * @param parrentPath
   * @throws SQLException
   */
  public Application(String parrentPath) throws SQLException {
    this.mysqlAPI = new JdbcUtils();
    this.parrentPath = new File(parrentPath);
  }

  /**
   * 建立工程初始列表：工程名  isMaven工程；
   * @return
   */
  public boolean builderProjects() {
    String stringSQL;
    int count = 1;
    LinkedList<File> list = FileUtils.getSubFolders(this.parrentPath);
    for (File path : list) {
      if (FileUtils.isMaven(path)) {
        stringSQL =
                String.format(
                        "insert into publish_projects_list (name,isMaven) values(\"%s\",%d)",
                        path.getName(), 1);
      } else {
        stringSQL =
                String.format(
                        "insert into publish_projects_list (name,isMaven) values(\"%s\",%d)",
                        path.getName(), 0);
      }
      try {
        this.mysqlAPI.executeSql(stringSQL);
      } catch (SQLException e) {
        continue;
      }
      System.out.println("count " + count + "检查文件夹完成" + path.getName());
      count++;
    }
    return true;
  }

  /**
   * 周一预生产发版后，后续预生产发版操作功能方法
   * @return
   * @throws GitAPIException
   * @throws IOException
   * @throws TransformerException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  public boolean fullowPreMasterCreateTag()
      throws GitAPIException, IOException, TransformerException, SAXException,
          ParserConfigurationException,SQLException {
    boolean ifSucess;
    File pomFile;
    File projectPath;
    String tagVersion;
    String stringSQL;
    String newVersion;
    List<String> FrontProjects = Utils.frontProjects();
    List<String> projectsList = Utils.publishProjects();
    for (String projectName : projectsList) {
      projectPath =
          new File(
              this.parrentPath.getAbsolutePath()
                  + System.getProperty("file.separator")
                  + projectName);
      logger.info("预生产发版的工程:{}和version",projectName);
      GitUtils objGit = new GitUtils(projectPath);
      objGit.branchPull("master");
      updateOneTags(projectName);
      if (FrontProjects.contains(projectName)) {
        pomFile =
            new File(
                this.parrentPath.getAbsolutePath()
                    + System.getProperty("file.separator")
                    + projectName
                    + System.getProperty("file.separator")
                    + "tag.xml");
        pomObj = new PomUtils(pomFile);
        newVersion =pomObj.getVersion();

        stringSQL =
                String.format(
                        "select id from project_tags_list where name=\"%s\" and tag_name = \"%s\"",
                        projectName, newVersion);
        while (this.mysqlAPI.count(stringSQL) > 0) {
          newVersion = Utils.versionAddOne(newVersion);
          stringSQL =
                  String.format(
                          "select id from project_tags_list where name=\"%s\" and tag_name = \"%s\"",
                          projectName, newVersion);
        }

        pomObj.setVersion(newVersion);
        ifSucess = objGit.branchCreateCommit("master", "tag.xml", "task4643:发版过程中API版本号统一维护");
        if (ifSucess) {
          ifSucess = objGit.branchPushCommit("master");
        }
        if (ifSucess) {
          objGit.createTagAndPush(newVersion);
        } else {
          logger.error("创建Commit,提交Commit,创建Tag并提交：失败");
          return false;
        }
        logger.info("工程：{} {}",projectName,newVersion);
      } else {
        pomFile =
            new File(
                this.parrentPath.getAbsolutePath()
                    + System.getProperty("file.separator")
                    + projectName
                    + System.getProperty("file.separator")
                    + "pom.xml");
        pomObj = new PomUtils(pomFile);
        tagVersion = pomObj.getVersion();
        objGit.createTagAndPush(tagVersion);
        logger.info(projectName + " " + tagVersion);
      }
    }
    return true;
  }

  /**
   * 遍历所有工程，拉取相应的分支
   * @param branchName 分支名
   * @param projectsSource 需要操作的工程列表获取方式
   *                       1 从数据库读取所有列表
   *                       2 从配置文件中，获取临时操作列表operateProjects
   * @return
   * @throws GitAPIException
   * @throws IOException
   * @throws SQLException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  public boolean checkoutBranch(String branchName,int projectsSource)
      throws GitAPIException, IOException, SQLException{
    String projectName;
    int count = 1;
    if (1 == projectsSource) {
      String stringSQL = "select name from publish_projects_list";
      ResultSet proSet = this.mysqlAPI.executeQuery(stringSQL);
      while (proSet.next()) {
        projectName = proSet.getString("name");
        logger.info("--------{}:拉取的工程名:{}",count,projectName);
        checkoutOneBranch(branchName,projectName);
        count ++;
      }
    } else {
      List<String> projectsList = Utils.operateProjects();
      for (String project : projectsList) {
        logger.info("--------{}:拉取的工程名:{}",count,project);
        checkoutOneBranch(branchName,project);
        count ++;
      }
    }
    System.out.println("true");
    return true;
  }

  private boolean checkoutOneBranch(String branchName,String projectName) throws GitAPIException, IOException{
    File projectPath;
    projectPath =
            new File(
                    this.parrentPath.getAbsolutePath()
                            + System.getProperty("file.separator")
                            + projectName);
    GitUtils objGit = new GitUtils(projectPath);
    if (!objGit.branchPull(branchName)) {
      logger.error("工程：{} 拉取失败",projectName);
      return false;
    }
    objGit.getStatus();
    return true;
  }

  /**
   * 将当前Master分支的version 更新至表publish_projects_list的master_follow字段
   * 用于从预生产到生产过程中，确认要发版的工程以及版本号
   *
   * @return
   * @throws GitAPIException
   * @throws IOException
   * @throws SQLException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  public boolean pudateFollowMaster()
          throws GitAPIException, IOException, SQLException, SAXException,
          ParserConfigurationException {
    String projectName;
    String version;
    File projectPath;
    File pomFile;
    String stringSQL = "select name,isMaven from publish_projects_list where newtag is not null";
    ResultSet proSet = this.mysqlAPI.executeQuery(stringSQL);
    while (proSet.next()) {
      projectName = proSet.getString("name");
      projectPath =
              new File(
                      this.parrentPath.getAbsolutePath()
                              + System.getProperty("file.separator")
                              + projectName);
      GitUtils objGit = new GitUtils(projectPath);
      if (objGit.branchPull("master")) {
        TagsUtils objTag = objGit.getLastTag();
        stringSQL =
                String.format(
                        "update publish_projects_list set master_follow =\"%s\" where name=\"%s\"",
                        objTag.name, projectName);
        System.out.println(stringSQL);
        this.mysqlAPI.executeSql(stringSQL);
      }
      logger.info("工程:{}master版本信息处理完", projectName);
    }
    return true;
  }

  /**
   * 更新所有工程的最新tag,master分支和dev分支Version
   *
   * @return
   * @throws GitAPIException
   * @throws IOException
   * @throws SQLException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  public boolean updateVersion()
          throws GitAPIException, IOException, SQLException, SAXException,
          ParserConfigurationException {
    String projectName;
    String version;
    File projectPath;
    File pomFile;
    String stringSQL = "select name,isMaven from publish_projects_list where skip = 0";
    ResultSet proSet = this.mysqlAPI.executeQuery(stringSQL);
    while (proSet.next()) {
      projectName = proSet.getString("name");
      projectPath =
              new File(
                      this.parrentPath.getAbsolutePath()
                              + System.getProperty("file.separator")
                              + projectName);
      GitUtils objGit = new GitUtils(projectPath);
      objGit.branchPull("master");
      TagsUtils objTag = objGit.getLastTag();
      stringSQL =
              String.format(
                      "update publish_projects_list set tag = \"%s\" where name=\"%s\"",
                      objTag.name, projectName);
      this.mysqlAPI.executeSql(stringSQL);
      logger.info("更新工程:{}的最新tag:{}", projectName, objTag.name);
      if (proSet.getBoolean("isMaven")) {
        pomFile =
                new File(
                        projectPath.getAbsolutePath() + System.getProperty("file.separator") + "pom.xml");
      } else {
        pomFile =
                new File(
                        projectPath.getAbsolutePath() + System.getProperty("file.separator") + "tag.xml");
      }
      if (objGit.branchPull("master")) {
        pomObj = new PomUtils(pomFile);
        version = pomObj.getVersion();
        stringSQL =
                String.format(
                        "update publish_projects_list set master =\"%s\" where name=\"%s\"",
                        version, projectName);
        System.out.println(stringSQL);
        this.mysqlAPI.executeSql(stringSQL);
      }
      if (objGit.branchPull("dev")) {
        pomObj = new PomUtils(pomFile);
        version = pomObj.getVersion();
        stringSQL =
                String.format(
                        "update publish_projects_list set dev =\"%s\" where name=\"%s\"",
                        version, projectName);
        System.out.println(stringSQL);
        this.mysqlAPI.executeSql(stringSQL);
      }
      logger.info("工程:{}tag、master、dev 版本信息处理完", projectName);
    }
    return true;
  }

  /**
   * 工程的依赖属性；
   * 被其他工程依赖
   * 依赖其他工程
   *
   * @return
   * @throws SQLException
   * @throws IOException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  public boolean builderProDependency()
          throws SQLException, IOException, SAXException, ParserConfigurationException {
    Map<String, String> dependencies = new HashMap<String, String>();
    List<String> projects = new ArrayList<String>();
    String stringSQL = "select name from publish_projects_list where isMaven = 1 and skip = 0";
    boolean isParrent = false;
    ResultSet proSet = this.mysqlAPI.executeQuery(stringSQL);
    proSet.beforeFirst();
    while (proSet.next()) {
      projects.add(proSet.getString("name"));
    }
    proSet.beforeFirst();
    while (proSet.next()) {
      File pomFile =
              new File(
                      this.parrentPath.getAbsolutePath()
                              + System.getProperty("file.separator")
                              + proSet.getString("name")
                              + System.getProperty("file.separator")
                              + "pom.xml");
      pomObj = new PomUtils(pomFile);
      isParrent = false;
      dependencies = pomObj.getDependency();
      for (Map.Entry<String, String> entry : dependencies.entrySet()) {
        String dependencyStringName = entry.getKey().replace(".version", "");
        if (projects.contains(dependencyStringName)) {
          stringSQL =
                  String.format(
                          "update publish_projects_list set isChild =1 where name = \"%s\"",
                          dependencyStringName);
          this.mysqlAPI.executeSql(stringSQL);
          isParrent = true;
        }
      }
      if (isParrent) {
        stringSQL =
                String.format(
                        ("update publish_projects_list set isParrent =1 where name=\"%s\""),
                        proSet.getString("name"));
        this.mysqlAPI.executeSql(stringSQL);
      }
    }
    return true;
  }

  /**
   * 将工程间依赖关系导入到数据表 projects_dependencies
   *
   * @return
   * @throws GitAPIException
   * @throws IOException
   * @throws SQLException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  public boolean updateProjectsDependencies()
          throws IOException, SQLException, SAXException,
          ParserConfigurationException {
    String projectName;
    int count = 1;
    Map<String, String> dependencies = new HashMap<String, String>();
    List<String> projects = new ArrayList<String>();
    String stringSQL = "select name from publish_projects_list where isChild = 1";
    ResultSet proSet = this.mysqlAPI.executeQuery(stringSQL);
    stringSQL = "select * from projects_dependencies limit 1";
    ResultSet columnSet = this.mysqlAPI.executeQuery(stringSQL);
    while (proSet.next()) {
      projectName = proSet.getString("name").replace("-", "_");
      projects.add(projectName);
      if (!this.mysqlAPI.isExistColumn(columnSet, projectName)) {
        stringSQL =
                String.format(
                        "alter table projects_dependencies add %s varchar(20) default null after name",
                        projectName);
        System.out.println(stringSQL);
        this.mysqlAPI.execute(stringSQL);
      }
    }

    stringSQL = "select name from publish_projects_list where isParrent = 1";
    proSet = this.mysqlAPI.executeQuery(stringSQL);
    while (proSet.next()) {
      count++;
      projectName = proSet.getString("name");
      stringSQL =
              String.format("select name from projects_dependencies where name = \"%s\"", projectName);
      if (this.mysqlAPI.count(stringSQL) == 0) {
        stringSQL =
                String.format("insert into projects_dependencies (name) values(\"%s\")", projectName);
        this.mysqlAPI.executeSql(stringSQL);
      }
      File pomFile =
              new File(
                      this.parrentPath.getAbsolutePath()
                              + System.getProperty("file.separator")
                              + proSet.getString("name")
                              + System.getProperty("file.separator")
                              + "pom.xml");
      pomObj = new PomUtils(pomFile);
      dependencies = pomObj.getDependency();
      for (Map.Entry<String, String> entry : dependencies.entrySet()) {
        String dependencyStringName = entry.getKey().replace(".version", "");
        dependencyStringName = dependencyStringName.replace("-", "_");
        if (projects.contains(dependencyStringName)) {
          stringSQL =
                  String.format(
                          "update projects_dependencies set %s = \"%s\" where name=\"%s\"",
                          dependencyStringName, entry.getValue(), proSet.getString("name"));
          this.mysqlAPI.executeSql(stringSQL);
        }
      }
      System.out.println("工程 " + count + ":" + projectName);
    }
    return true;
  }

  /**
   * 计算出工程下次发版的tag version
   * 从dev pom文件中获取verson，检查此version tag是否已经存在
   * TODO (Front 工厂没有处理)生成下次预生产批量发版的tag
   * new_type : '0：一般工程，1：库工程，2：修改pom的工程 3：前端front 工程'
   *  不再对库工程做特殊处理（查找其依赖工程）
   * @return
   * @throws SQLException
   */
  public boolean listPublish()
          throws SQLException {
    Map<String, Dependency> publisher = new HashMap<String, Dependency>();
    String stringSQL = "update publish_projects_list set newtag =null,newtag_type=null";
    this.mysqlAPI.executeSql(stringSQL);

    //处理Maven 工程
    stringSQL = "select name,isChild,dev from publish_projects_list where dev like \"%-SNAPSHOT\"";
    ResultSet proSet = this.mysqlAPI.executeQuery(stringSQL);
    Dependency depenClass;
    while (proSet.next()) {
      depenClass = new Dependency();
      // System.out.println("发现工程：" + proSet.getString("name") + proSet.getString("dev"))
        depenClass.type = 0;
        depenClass.version = proSet.getString("dev");
        publisher.put(proSet.getString("name"), depenClass);
    }

    //处理前端工程
    stringSQL = "select name,master from publish_projects_list where isMaven = 0";
    proSet = this.mysqlAPI.executeQuery(stringSQL);
    while (proSet.next()) {
      depenClass = new Dependency();
      depenClass.type = 3;
      depenClass.version = proSet.getString("master");
      publisher.put(proSet.getString("name"), depenClass);
    }

    // 生成最新的tag 写入到数据库
    for (Map.Entry<String, Dependency> entry : publisher.entrySet()) {
      if (entry.getValue().type != 2) {
        String version = entry.getValue().version.replace("-SNAPSHOT", "");
        stringSQL =
                String.format(
                        "select id from project_tags_list where name=\"%s\" and tag_name = \"%s\"",
                        entry.getKey(), version);
        while (this.mysqlAPI.count(stringSQL) > 0) {
          version = Utils.versionAddOne(version);
          stringSQL =
                  String.format(
                          "select id from project_tags_list where name=\"%s\" and tag_name = \"%s\"",
                          entry.getKey(), version);
        }
        stringSQL =
                String.format(
                        "update publish_projects_list set newtag = \"%s\",newtag_type=%d where name=\"%s\"",
                        version, entry.getValue().type, entry.getKey());
        logger.info("设置工程：{} 下个Tag: {}", entry.getKey(), version);
      }
      this.mysqlAPI.executeSql(stringSQL);
    }
    return true;
  }

  /**
   * 更新所有的工程tag到数据表中
   *
   * @return
   * @throws GitAPIException
   * @throws IOException
   * @throws SQLException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  public boolean updateTags()
          throws GitAPIException, IOException, SQLException, SAXException,
          ParserConfigurationException {
    String projectName;
    File projectPath;
    int count = 1;
    String stringSQL = "select name from publish_projects_list";
    ResultSet proSet = this.mysqlAPI.executeQuery(stringSQL);
    while (proSet.next()) {
      projectName = proSet.getString("name");
      projectPath =
              new File(
                      this.parrentPath.getAbsolutePath()
                              + System.getProperty("file.separator")
                              + projectName);
      GitUtils objGit = new GitUtils(projectPath);
      logger.info("{}.project:{} update tag",count,projectName);
      for (String tag : objGit.getAllTags()) {
        stringSQL =
                String.format(
                        "insert into project_tags_list (name,tag_name) values(\"%s\",\"%s\")",
                        projectName, tag);
        try {
          this.mysqlAPI.executeSql(stringSQL);
        } catch (SQLException e) {
          continue;
        }
      }
      count ++;
    }
    logger.info("工程所有的tag已更新至数据表project_tags_list 完成");
    return true;
  }

  /**
   * 当个工程更新tag 信息到数据库
   * @param projectName 工程名
   * @return
   * @throws GitAPIException
   * @throws IOException
   * @throws SQLException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  private boolean updateOneTags(String projectName) throws GitAPIException, IOException, SQLException, SAXException,
          ParserConfigurationException {
    File projectPath;
    String stringSQL;
    projectPath =
            new File(
                    this.parrentPath.getAbsolutePath()
                            + System.getProperty("file.separator")
                            + projectName);
    GitUtils objGit = new GitUtils(projectPath);
    logger.info("project:{} update tag",projectName);
    for (String tag : objGit.getAllTags()) {
      stringSQL =
              String.format(
                      "insert into project_tags_list (name,tag_name) values(\"%s\",\"%s\")",
                      projectName, tag);
      try {
        this.mysqlAPI.executeSql(stringSQL);
      } catch (SQLException e) {
        continue;
      }
    }
    return true;
  }

  /**
   * 修改DEV 分支中POM文件中信息
   * @return
   * @throws GitAPIException
   * @throws IOException
   * @throws SQLException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  public boolean updateDevPom()
      throws GitAPIException, IOException, SQLException, SAXException,
          ParserConfigurationException {
    String projectName;
    String newVersion;
    Map<String, String> projectAddTag = new HashMap<String, String>();
    File projectPath;
    File pomFile;
    String stringSQL =
        "select name,newtag,isParrent from publish_projects_list where isMaven =1 and newtag is not null";
    ResultSet proSet = this.mysqlAPI.executeQuery(stringSQL);
    while (proSet.next()) {
      projectAddTag.put(proSet.getString("name"), proSet.getString("newtag"));
    }
    proSet.beforeFirst();
    while (proSet.next()) {
      projectName = proSet.getString("name");
      System.out.println("修改工程：" + projectName + "的Pom文件");
      newVersion = proSet.getString("newtag");
      projectPath =
          new File(
              this.parrentPath.getAbsolutePath()
                  + System.getProperty("file.separator")
                  + projectName);
      GitUtils objGit = new GitUtils(projectPath);
      if (!objGit.branchPull("dev")) {
        System.out.println("project :" + projectName + "dev branch pull false");
        return false;
      }
      pomFile =
          new File(
              this.parrentPath.getAbsolutePath()
                  + System.getProperty("file.separator")
                  + projectName
                  + System.getProperty("file.separator")
                  + "pom.xml");
      pomObj = new PomUtils(pomFile);

      try {
        pomObj.setVersion(newVersion);
      } catch (TransformerException e) {
        System.out.println("project :" + projectName + "update version false");
        return false;
      }

      // 修改Maven工程依赖中带有-SNAPSHOT 字符的
        for (Map.Entry<String, String> entry : pomObj.getDependency().entrySet()) {
          String dependencyStringName = entry.getKey();
          String dependencyVersion = entry.getValue();
          if (dependencyVersion.endsWith("-SNAPSHOT")) {
            String newDependencyVersion = dependencyVersion.replace("-SNAPSHOT","");
            System.out.println(dependencyStringName + "+  version : " + dependencyVersion + " update set " + newDependencyVersion);
            try {
              pomObj.setDependency(dependencyStringName,newDependencyVersion);
            } catch (TransformerException e) {
              System.out.println(
                      "project :"
                              + projectName
                              + "update subProject"
                              + dependencyStringName
                              + " false");
              return false;
            }
          }
/*          if (projectAddTag.containsKey(dependencyStringName)) {
            try {
              pomObj.setDependency(dependencyStringName, projectAddTag.get(dependencyStringName));
            } catch (TransformerException e) {
              System.out.println(
                  "project :"
                      + projectName
                      + "update subProject"
                      + dependencyStringName
                      + " false");
              return false;
            }
          }*/
      }
      objGit.getStatus();
      objGit.branchCreateCommit("dev", "pom.xml", "task4643:发版过程中API版本号统一维护");
      objGit.branchPushCommit("dev");
    }
    return true;
  }

  /**
   * 将publish_projects_list 表中存在newtag的工程,进行Merge操作
   * @param fromBranch 代码源分支
   * @param toBranch 代码目标分支
   * @param projectsSource 需要操作的工程列表获取方式
   *                       1 从数据库读取所有列表
   *                       2 从配置文件中，获取临时操作列表operateProjects
   * @return
   * @throws GitAPIException
   * @throws IOException
   * @throws SQLException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  public boolean mergeNewtagBranch(String fromBranch,String toBranch,int projectsSource)
          throws GitAPIException, IOException, SQLException {
    File projectPath;
    int count = 1;
    String projectName;
    if (1 == projectsSource) {
      String stringSQL = "select name,newtag from publish_projects_list where newtag is not null";
      ResultSet proSet = this.mysqlAPI.executeQuery(stringSQL);
      while (proSet.next()) {
        projectName = proSet.getString("name");
        projectPath = new File(this.parrentPath.getAbsolutePath() + System.getProperty("file.separator") + projectName);
        System.out.println(count + "工程：" + projectName + " " + fromBranch + "分支Merge到" + toBranch);
        GitUtils objGit = new GitUtils(projectPath);
        if (objGit.mergeBranch(fromBranch,toBranch)) {
          System.out.println("merge sucess");
        }
        count++;
      }
    } else {
      List<String> projectsList = Utils.operateProjects();
      for (String project : projectsList) {
        projectPath = new File(this.parrentPath.getAbsolutePath() + System.getProperty("file.separator") + project);
        System.out.println(count + "工程：" + project + " " + fromBranch + "分支Merge到" + toBranch);
        GitUtils objGit = new GitUtils(projectPath);
        if (objGit.mergeBranch(fromBranch,toBranch)) {
          System.out.println("merge sucess");
        }
        count++;
      }
    }
    return true;
  }

  /**
   * 查看工程的状态，
   * @param projectsSource  1 从数据库读取，2 从配置文件中读取
   * @return
   * @throws GitAPIException
   * @throws IOException
   * @throws SQLException
   */
  public boolean projectStatus(int projectsSource) throws GitAPIException, IOException, SQLException{
    File projectPath;
    int count = 1;
    String projectName;
    if (1 == projectsSource) {
      String stringSQL = "select name from publish_projects_list where newtag is not null";
      ResultSet proSet = this.mysqlAPI.executeQuery(stringSQL);
      while (proSet.next()) {
        projectName = proSet.getString("name");
        projectPath = new File(this.parrentPath.getAbsolutePath() + System.getProperty("file.separator") + projectName);
        GitUtils objGit = new GitUtils(projectPath);
        objGit.getStatus();
        count++;
        System.out.println(count +  projectName + " Status");
      }
    } else {
      List<String> projectsList = Utils.operateProjects();
      for (String project : projectsList) {
        projectPath = new File(this.parrentPath.getAbsolutePath() + System.getProperty("file.separator") + project);
        GitUtils objGit = new GitUtils(projectPath);
        objGit.getStatus();
        count++;
        System.out.println(count +  project + " Status");
      }
    }
    return true;
  }

  /**
   * 将publish_projects_list 表中存在newtag的工程,在Master分支创建TAG并push
   *
   * @return
   * @throws GitAPIException
   * @throws IOException
   * @throws SQLException
   */
  public boolean masterTag() throws GitAPIException, IOException, SQLException {
    String stringSQL =
        "select name,newtag from publish_projects_list where newtag is not null";
    File projectPath;
    int count = 1;
    String projectName;
    String newTag;
    ResultSet proSet = this.mysqlAPI.executeQuery(stringSQL);
    while (proSet.next()) {
      projectName = proSet.getString("name");
      newTag = proSet.getString("newtag");
      System.out.println(count + " 工程：" + projectName + "打Tag :" + newTag);
      projectPath = new File(this.parrentPath.getAbsolutePath() + System.getProperty("file.separator") + projectName);
      GitUtils objGit = new GitUtils(projectPath);
      if (!objGit.createTagAndPush(newTag)) {
        System.out.println("tag 创建失败");
      }
      count++;
    }
    return true;
  }

  /**
   * 将publish_projects_list 表中存在newtag的工程,Push 相应的分支到远程
   *
   * @param branchName 要push的分支
   * @param projectsSource 需要操作的工程列表获取方式
   *                       1 从数据库读取所有列表
   *                       2 从配置文件中，获取临时操作列表operateProjects
   * @return
   * @throws GitAPIException
   * @throws IOException
   * @throws SQLException
   */
  public boolean pushBranch(String branchName,int projectsSource) throws GitAPIException, IOException, SQLException {
    File projectPath;
    int count = 1;
    String projectName;
    if (1 == projectsSource) {
      String stringSQL = "select name from publish_projects_list where newtag is not null";
      ResultSet proSet = this.mysqlAPI.executeQuery(stringSQL);
      while (proSet.next()) {
        projectName = proSet.getString("name");
        System.out.println(count + " push 工程：  " + projectName + "的" + branchName + "分支");
        projectPath = new File(this.parrentPath.getAbsolutePath() + System.getProperty("file.separator") + projectName);
        GitUtils objGit = new GitUtils(projectPath);
        if (objGit.branchPushCommit(branchName)) {
          System.out.println("push sucess");
        }
      }
    } else {
      List<String> projectsList = Utils.operateProjects();
      for (String project : projectsList) {
        projectPath = new File(this.parrentPath.getAbsolutePath() + System.getProperty("file.separator") + project);
        System.out.println(count + " push 工程：" + project + "的" + branchName + "分支");
        GitUtils objGit = new GitUtils(projectPath);
        if (objGit.branchPushCommit(branchName)) {
          System.out.println("push sucess");
        }
        count++;
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
