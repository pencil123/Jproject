package com.blogs;

import jdk.nashorn.internal.objects.Global;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.FetchResult;

import javax.swing.text.html.HTML;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

  public boolean branchPull(String branchName) throws GitAPIException {
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
      /*System.out.println(pull.getFetchResult(true).call());
      System.out.println(pull.getMergeResult().getConflicts());
      System.out.println(pull.isSuccessful());*/
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
            .setMessage("Merged changes")
            .call();

    if (merge.getMergeStatus().isSuccessful()) {
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
