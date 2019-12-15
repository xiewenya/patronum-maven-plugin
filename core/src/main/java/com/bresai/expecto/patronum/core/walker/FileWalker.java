package com.bresai.expecto.patronum.core.walker;

import com.bresai.expecto.patronum.core.bean.Config;
import com.bresai.expecto.patronum.core.bean.result.FileDiff;
import com.bresai.expecto.patronum.core.enums.FileDiffEnum;
import com.bresai.expecto.patronum.core.git.GitOperator;
import com.bresai.expecto.patronum.core.parser.Parser;
import lombok.Getter;
import lombok.Setter;
import org.codehaus.plexus.util.StringUtils;
import pl.project13.core.log.LoggerBridge;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.HashMap;
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
@Setter
@Getter
public abstract class FileWalker implements DataWalker {

    protected LoggerBridge log;

    protected File projectDir;

    protected File dotGitRepo;

    protected GitOperator gitOperator;

    protected Parser parser;

    public FileWalker(LoggerBridge log,File dotGitRepo,  File projectDir) {
        this.log = log;
        this.projectDir = projectDir;
        this.gitOperator = setGitOperator(log, dotGitRepo);
        this.parser = setParser(log);
    }

    public abstract GitOperator setGitOperator(LoggerBridge log, File dotGitRepo);

    public abstract Parser setParser(LoggerBridge log);


    @Override
    public Map<FileDiffEnum, List<FileDiff>> searchGitFileDiff(String result){
        Map<FileDiffEnum, List<FileDiff>> map = new HashMap<>();
        map.put(FileDiffEnum.ADD, new LinkedList<>());
        map.put(FileDiffEnum.DELETE, new LinkedList<>());
        map.put(FileDiffEnum.MODIFY, new LinkedList<>());

        String[] lines = result.split(System.lineSeparator());
        for (String line : lines) {
            FileDiff fileDiff = buildFileDiff(line);
            if (fileDiff == null){
                continue;
            }

            if (fileDiff.getDiffEnum().equals(FileDiffEnum.RENAME)){
                /*
                we treat rename as remove the old file and add a new file
                newFilepath is the name of the new file,
                and filepath is the file removed
                 */
                FileDiff fileDiffAdd = new FileDiff();
                fileDiffAdd.setDiffEnum(FileDiffEnum.ADD);
                fileDiffAdd.setFilepath(fileDiff.getNewFilepath());

                FileDiff fileDiffRemove = new FileDiff();
                fileDiffRemove.setDiffEnum(FileDiffEnum.DELETE);
                fileDiffRemove.setFilepath(fileDiff.getFilepath());

                map.get(FileDiffEnum.ADD).add(fileDiffAdd);
                map.get(FileDiffEnum.DELETE).add(fileDiffRemove);
            } else{
                map.get(fileDiff.getDiffEnum()).add(fileDiff);
            }
        }

        return map;
    }

    private FileDiff buildFileDiff(String line) {
        if (StringUtils.isBlank(line)){
            return null;
        }

        String[] elements = line.split("\t");
        if (elements.length < 2){
            log.warn("the format of the line is not correct for {}", line);
            return null;
        }

        FileDiffEnum diffEnum = FileDiffEnum.of(elements[0]);

        FileDiff bean = new FileDiff();
        bean.setDiffEnum(diffEnum);
        bean.setFilepath(elements[1]);

        if (FileDiffEnum.RENAME.equals(diffEnum)){
            if (elements.length > 3){
                log.warn("the format of the rename line is not correct for {}", line);
                return null;
            }

            bean.setNewFilepath(elements[2]);
        }

        return bean;
    }

    @Override
    public List<File> search(File dir, Predicate<File> predicate){
        List<File> files = new LinkedList<>();

        log.debug("search dir {}", dir.getAbsolutePath());
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
            if (file.isFile() && function.test(file)){
                log.info("file {} found", file.getAbsolutePath());
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

    @Override
    public List<Config> parseFromRemoteFile(String targetRemoteBranch, String filepath) {
        // get the remote File and parse it
        String fileContent = gitOperator.getFileFromRemote(targetRemoteBranch, filepath);
        return parser.parser(fileContent);
    }

    public Map<String, List<Config>> walkThrough(@Nonnull File gitDir){
        Map<String, List<Config>> configBeanSortByFile = new HashMap<>();

        Predicate<File> predicate = this.searchJavaFiles();

        log.info("search dir {}", gitDir.getAbsolutePath());

        List<File> fileList = search(gitDir, predicate);
        fileList.forEach(file -> {
            List<Config> list = parser.parser(file);
            if (list != null && list.size() != 0){
                configBeanSortByFile.put(file.getAbsolutePath(), list);
            }
        });



        return configBeanSortByFile;
    }
}
