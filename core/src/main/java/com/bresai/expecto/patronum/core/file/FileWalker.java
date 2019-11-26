package com.bresai.expecto.patronum.core.file;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/26
 * @content:
 */
public class FileWalker implements DataWalker {

    @Override
    public List<File> search(File dir, Predicate<File> predicate){
        List<File> files = new LinkedList<>();

        searchDirRecursive(dir, files, predicate);

        return files;
    }

    @Override
    public Predicate<File> excludeFiles(List<String> excludeFileNames){
        return (file) -> {
            for (String string : excludeFileNames){
                if (file.getName().contains(string)){
                    return false;
                }
            }
            return true;
        };
    }

    @Override
    public Predicate<File> searchSpecified(String fileName){
        return (file) -> fileName.equalsIgnoreCase(file.getName());
    }

    @Override
    public Predicate<File> searchWithKeyword(String fileName){
        return (file) -> fileName.contains(file.getName());
    }

    @Override
    public Predicate<File> searchJavaFiles(){
        return (file) -> file.getName().endsWith(".java");
    }

    @Override
    public Predicate<File> searchXmlFiles(){
        return (file) -> file.getName().endsWith(".xml");
    }

    private void searchDirRecursive(File dir, List<File> files, Predicate<File> function){
        File[] subFiles = dir.listFiles();
        if (subFiles == null || subFiles.length == 0){
            return;
        }

        for (File file : subFiles){
            if (file.isFile() && !function.test(file)){
                files.add(file);
            } else if (file.isDirectory()){
                searchDirRecursive(file, files ,function);
            }
        }
    }

    private void searchWithMultiCondition(File dir, Map<String, List<File>> fileMap,
                                          Map<String, Predicate<File>> functionMap){
        File[] subFiles = dir.listFiles();
        if (subFiles == null || subFiles.length == 0){
            return;
        }

        for (File file : subFiles){
            if (file.isFile()){
                if (functionMap == null || functionMap.isEmpty()){
                    addToDefault(fileMap, file);
                    continue;
                }

                functionMap.forEach((key, value) ->{
                    if (value.test(file)) {
                        addToKey(fileMap, file, key);
                    }
                });

            } else if (file.isDirectory()){
                searchWithMultiCondition(file, fileMap ,functionMap);
            }
        }
    }

    private void addToDefault(Map<String, List<File>> fileMap, File file) {
        addToKey(fileMap, file, "default");
    }

    private void addToKey(Map<String, List<File>> fileMap, File file, String key) {
        List<File> files = null;
        if (!fileMap.containsKey(key)){
            fileMap.putIfAbsent(key, new LinkedList<>());
        }
        files = fileMap.get(key);
        files.add(file);
    }

}
