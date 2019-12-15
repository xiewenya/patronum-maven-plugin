package com.bresai.expecto.patronum.core.utils;

import java.io.File;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/12/15
 * @content:
 */
public class ProjectFileUtils {

    public static String getAbsolutePath(File baseProject, String relativeName){
        if (relativeName == null){
            return "";
        }

        if(relativeName.startsWith("/")){
            relativeName = relativeName.substring(1);
        }

        return baseProject.getAbsolutePath() + relativeName;

    }

    public static String getRelativePath(File projectDir, File file){
        if (file == null){
            return "";
        }

        return projectDir.toURI().relativize(file.toURI()).getPath();
    }


}
