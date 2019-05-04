package com.blogs;

public class Utils {
    public static String versionAddOne(String version) {
        String[] versionArray = version.split("\\.");
        String newVersion = null;
        if (versionArray.length >= 3) {
            int versionInt = Integer.parseInt(versionArray[2]);
            versionArray[2] = String.valueOf(versionInt + 1);
            newVersion = String.join(".",versionArray);
        }
        return newVersion;
    }
}