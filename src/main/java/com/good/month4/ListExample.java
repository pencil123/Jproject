package com.good.month4;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lyzhang
 * @since 2019/4/17 13:15
 */
public class ListExample {
  public static void main(String[] args) {
    //
      List<Integer> words = new ArrayList<Integer>();
      for (int i=0;i<10;i++){
          words.add(i);
      }


  //    words.forEach(System.out::println);

    //遍历list 方法1
      for(Integer j:words){
        System.out.println(j);
    }

      //遍历list 方法2
      int size = words.size();
      for (int j=0;j<size;j++){
      System.out.println(words.get(j));
      }


  }
}
