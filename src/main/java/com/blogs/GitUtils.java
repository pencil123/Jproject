package com.blogs;

import jdk.nashorn.internal.objects.Global;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;

import javax.swing.text.html.HTML;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.lang.System.in;

/**
 * @author lyzhang
 * @since 2019/4/25 21:39
 */
public class GitUtils {
  private Git git;
  private Repository repository;
  private RevWalk walk;

  GitUtils(File parrentFolder) throws IOException, GitAPIException{
   // File parrentFolder = new File("D:\\codes\\jsh-tally-jobs");
   // System.out.println(parrentFolder.getAbsolutePath() + System.getProperty("file.separator") + ".git");
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    try (Repository repository = builder
            .setGitDir(new File(parrentFolder.getAbsolutePath() + System.getProperty("file.separator") + ".git"))
            .readEnvironment()
            .findGitDir()
            .build()) {
      this.git = new Git(repository);
      this.walk = new RevWalk(repository);
      this.repository = repository;
    }
  }
  public List<String> getAllTags() throws GitAPIException,IOException {
    List<Ref> call;
    List<String> tags = new ArrayList<String>();
    call = this.git.tagList().call();
    for (Ref ref :call ) {
      tags.add(ref.getName().split("/")[2]);
    }
    return tags;
  }

  public TagsUtils getLastTag() throws GitAPIException,IOException {
    List<Ref> call;
    call = this.git.tagList().call();
    int sizeRef = call.size();
    LinkedList<TagsUtils> tagsList =  new LinkedList<>();
    for (Ref ref : call) {
      TagsUtils tag = new TagsUtils();
      String tagName = ref.getName().split("/")[2];
      //System.out.println("Tag:" + ref + "  " + ref.getName().split("/")[2] + "  " + ref.getObjectId());
      /*System.out.println(ref.getClass() + " - " + ref.getStorage().getDeclaringClass().getClass());
      System.out.println(ref.getClass() + " - " + ref.getStorage().getClass().getFields());*/
      //对象类型转换 将Tag 的ObjectId 转为 Commit 的 RevCommit
      ObjectId id =  ref.getObjectId().toObjectId();
      RevCommit commit = this.walk.parseCommit(id);
      int tagTime = commit.getCommitTime();
      tag.name = tagName;
      tag.timestamp = tagTime;
/*    System.out.println(ref.getName());
      System.out.println(commit.getCommitTime() + "---" + commit.getFullMessage());
      System.out.println(ref.getObjectId().toObjectId());
      long time = this.walk.parseTag(ref.getObjectId()).getTaggerIdent().getWhen().getTime();*/
      tagsList.add(tag);
    }

    //冒泡排序
    int i;
    int j;
    TagsUtils tmp;
    for (i = 0;i < sizeRef - 1;i++) {
      //System.out.println(tagsList.get(i).timestamp);
      for (j = i + 1;j < sizeRef;j++) {
        if (tagsList.get(j).timestamp == tagsList.get(i).timestamp &&
        !tagsList.get(j).name.equals(tagsList.get(i).name)) {
          //tag 如果以#结束，说明此Commit 上打了过多Tag
          String tagsName1 = tagsList.get(j).name;
          String tagsName2 = tagsList.get(i).name;
          if (tagsName1.compareTo(tagsName2) > 0) {
            tagsList.get(i).name = tagsName1 + "#";
            tagsList.get(j).name = tagsName1 + "#";
          } else {
            tagsList.get(i).name = tagsName2 + "#";
            tagsList.get(j).name = tagsName2 + "#";
          }
        }
        if (tagsList.get(j).timestamp > tagsList.get(i).timestamp) {
          tmp = tagsList.get(i);
          tagsList.set(i,tagsList.get(j));
          tagsList.set(j,tmp);
        }
      }
    }

    return tagsList.get(0);
  }

  public boolean createTagAndPush(String tagName)throws GitAPIException,IOException {
    String currentBranch = this.repository.getBranch();
    if (currentBranch.equals("master")) {
        Ref tag = this.git.tag().setName(tagName).call();
        this.git.push().setPushTags().call();
        return true;
    } else {
      System.out.println("当前分支有问题");
      return false;
    }
  }

  public boolean branchPull(String branchName) throws GitAPIException,IOException {
    String currentBranch = this.repository.getBranch();
    git.reset().setMode(ResetCommand.ResetType.HARD).call();
    System.out.println("强制回滚分支" + currentBranch + "到HEAD");
    List<Ref> call = this.git.branchList().call();
    boolean branchExist = false;
    for (Ref ref : call) {
      //System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
      if (ref.getName().equals("refs/heads/" + branchName)) {
        branchExist = true;
      }
    }
    if (branchExist) {
      this.git.checkout().setName(branchName).call();
    } else {
      git.branchCreate()
              .setName(branchName)
              .setStartPoint("origin/" + branchName)
              .call();
      this.git.checkout().setName(branchName).call();
    }
    //this.git.fetch().setCheckFetchedObjects(true).call();
    PullResult pull = this.git.pull().call();
    if (pull.isSuccessful()) {
      /*      System.out.println(pull.getFetchResult(true).call());
      System.out.println(pull.getMergeResult().getConflicts());
      System.out.println(pull.isSuccessful());*/
      System.out.println("工程拉取分支" + branchName + "完成");
      return true;
    } else {
      return false;
    }
  }

  public boolean mergeBranch(String fromBranch,String toBranch) throws IOException, GitAPIException {
    List<Ref> call = this.git.branchList().call();
    int branchExist =0;
    for (Ref ref : call) {
      //System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
      if (ref.getName().equals("refs/heads/" + toBranch)) {
        branchExist += 1;
      }
      if (ref.getName().equals("refs/heads/" + fromBranch)) {
        branchExist += 1;
      }
    }
    //checkout 到目标分支
    if (branchExist == 2) {
      this.git.checkout().setName(toBranch).call();
    } else {
      return false;
    }
    //合并分支
    ObjectId mergeBase = this.repository.resolve(fromBranch);
    // perform the actual merge, here we disable FastForward to see the
    // actual merge-commit even though the merge is trivial
    MergeResult merge = this.git.merge()
            .include(mergeBase)
            .setCommit(true)
            .setFastForward(MergeCommand.FastForwardMode.NO_FF)
            //.setSquash(false)
            .setMessage(fromBranch +" merge to " + toBranch)
            .call();

    if (merge.getMergeStatus().isSuccessful()) {
      System.out.println("工程 Merge 完成");
      return true;
    } else {
      System.out.println(merge.getMergeStatus().toString());
      for (Map.Entry<String,int[][]> entry : merge.getConflicts().entrySet()) {
        System.out.println("Key: " + entry.getKey());
        for(int[] arr : entry.getValue()) {
          System.out.println("value: " + Arrays.toString(arr));
        }
      }
      return false;
    }

  /*  getMergeStatus()
      ABORTED
              ALREADY_UP_TO_DATE
      CHECKOUT_CONFLICT
      Status representing a checkout conflict, meaning that nothing could be merged, as the pre-scan for the trees already failed for certain files (i.e.
              CONFLICTING
      FAILED
              FAST_FORWARD
      FAST_FORWARD_SQUASHED
              MERGED
      MERGED_NOT_COMMITTED
              MERGED_SQUASHED
      MERGED_SQUASHED_NOT_COMMITTED
              NOT_SUPPORTED*/
  }

  public boolean getStatus() throws IOException, GitAPIException {
    Status status = this.git.status().call();
    boolean ifsuccess = true;
    if (!status.getAdded().isEmpty()) {
      System.out.println("Added: " + status.getAdded());
      ifsuccess =false;
    }
    if (!status.getChanged().isEmpty()) {
      System.out.println("Changed: " + status.getChanged());
      ifsuccess =false;
    }
    if (!status.getConflicting().isEmpty()) {
      System.out.println("Conflicting: " + status.getConflicting());
      ifsuccess =false;
    }
    if (!status.getConflictingStageState().isEmpty()) {
      System.out.println("ConflictingStageState: " + status.getConflictingStageState());
      ifsuccess =false;
    }
    if (!status.getMissing().isEmpty()) {
      System.out.println("Missing: " + status.getMissing());
      ifsuccess =false;
    }

/*    System.out.println("IgnoredNotInIndex: " + status.getIgnoredNotInIndex());*/

    if (!status.getModified().isEmpty() ){
      System.out.println("Modified: " + status.getModified());
      ifsuccess =false;
    }

    if (!status.getRemoved().isEmpty()) {
      System.out.println("Removed: " + status.getRemoved());
      ifsuccess =false;
    }

    if (!status.getUntracked().isEmpty()) {
      System.out.println("Untracked: " + status.getUntracked());
      ifsuccess =false;
    }

    if (!status.getUntrackedFolders().isEmpty()) {
      System.out.println("UntrackedFolders: " + status.getUntrackedFolders());
      ifsuccess =false;
    }

    if (ifsuccess) {
      System.out.println("工程状态OK");
    } else {
      System.out.println("工程状态异常");
    }
    return ifsuccess;
  }

    public boolean commitPomChange(String pomFile,String branchName,String commitMes) throws IOException,GitAPIException{
    this.git.add()
            .addFilepattern(pomFile)
            .call();
    RevCommit revCommit = this.git.commit()
            .setMessage(commitMes)
            .setInsertChangeId(true)
            .call();
    Iterable<PushResult> pushResults = this.git.push().setRefSpecs(new RefSpec("HEAD:refs/for/dev%submit")).call();
    pushResults.iterator();
    for (PushResult pushResult : pushResults) {
      for (RemoteRefUpdate update : pushResult.getRemoteUpdates()) {
        if (!(update.getStatus().toString().equals("OK") | update.getStatus().toString().equals("UP_TO_DATE"))) {
          System.out.println(update.getStatus() + update.getMessage());
        }
      }
    }
    return true;
  }

  public boolean commitPomChange(String CommitMes) throws NoFilepatternException,GitAPIException {
    //this.git.commit().setMessage("123").setHookOutputStream();
        this.git.add()
            .addFilepattern("pom.xml")
            .call();

    RevCommit revCommit = this.git.commit()
            .setMessage(CommitMes)
            .setInsertChangeId(true)
            .call();

    Iterable<PushResult> pushResults = this.git.push().setRefSpecs(new RefSpec("HEAD:refs/for/dev%submit")).call();
    pushResults.iterator();
    for (PushResult pushResult : pushResults) {
      for (RemoteRefUpdate update : pushResult.getRemoteUpdates()) {
        if (!(update.getStatus().toString().equals("OK") | update.getStatus().toString().equals("UP_TO_DATE"))) {
          System.out.println(update.getStatus() + update.getMessage());
        }
      }
    }
    return true;
  }

  public boolean pushMaster()  throws NoFilepatternException,GitAPIException,IOException{
    String currentBranch = this.repository.getBranch();
    if (currentBranch.equals("master")) {
      Iterable<PushResult> pushResults = this.git.push().setRefSpecs(new RefSpec("HEAD:refs/heads/master")).call();
      pushResults.iterator();
      for (PushResult pushResult : pushResults) {
        for (RemoteRefUpdate update : pushResult.getRemoteUpdates()) {
          if (!(update.getStatus().toString().equals("OK") | update.getStatus().toString().equals("UP_TO_DATE"))) {
            System.out.println(update.getStatus() + update.getMessage());
          }
        }
      }
      return true;
    } else {
      System.out.println("当前分支有问题");
      return false;
    }
  }

  public void getLog()  throws GitAPIException,IOException{
    Iterable<RevCommit> logs = this.git.log().all().call();
    int count = 0;

    //  logs.iterator()
    //当前分支的提交日志
    for (RevCommit rev : logs) {
      /*     System.out.println("Commit: " + rev */
      /* + ", name: " + rev.getName() + ", id: " + rev.getId().getName() */
      /*);*/
/*
      System.out.println(rev.getCommitTime() + " -  " +rev.getName() + " --" + rev.getFullMessage() + rev.getClass());
*/
      Ref tag = git.tag().call();
      System.out.println(tag);
    }
  }

  public void commitTag () throws GitAPIException,IOException{
    ObjectId id = this.repository.resolve("HEAD^");
    RevCommit commit = walk.parseCommit(id);
    System.out.println(commit);

    String tag = git.tag().setObjectId(commit).getName();
    System.out.println(tag);
  }

}
