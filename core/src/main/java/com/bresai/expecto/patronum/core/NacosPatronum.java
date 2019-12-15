package com.bresai.expecto.patronum.core;

import com.bresai.expecto.patronum.core.bean.Config;
import com.bresai.expecto.patronum.core.bean.result.FileDiff;
import com.bresai.expecto.patronum.core.enums.FileDiffEnum;
import com.bresai.expecto.patronum.core.utils.ProjectFileUtils;
import com.bresai.expecto.patronum.core.walker.FileWalker;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import pl.project13.core.log.LoggerBridge;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/12/15
 * @content:
 */
@Getter
public class NacosPatronum extends Patronum {
    /**
     * contains all the config of current commit
     */
    private List<Config> configList;

    /**
     * contains all files that have target configs
     */
    private Map<String, List<Config>> configBeanSortByFile;

    /**
     * count how many times the config used in current commit
     */
    private Map<Config, Integer> configCount;

    /**
     * contains all config in the newly added files of current commit;
     * contains all config added from modification of current commit;
     */
    private List<Config> configToAdd;

    /**
     * We count the times this config newly appears in current commit,
     * so it can be compare to the global config count of current commit.
     */
    private Map<Config, Integer> configToAddCount;

    /**
     * contains all config in the newly added files of current commit;
     * contains all config added from modification of current commit;
     */
    private List<Config> configToDelete;

    /**
     * We count the times this config removed from current commit,
     * so it can be compare to the global config count of current commit.
     */
    private Map<Config, Integer> configToDeleteCount;

    /**
     * the branch name we used as baseline, to compare with current commit;
     */
    // TODO: 2019/12/15 as improvement, we should support tags too
    private String targetRemoteBranch;

    private FileWalker fileWalker;

    public NacosPatronum(FileWalker fileWalker, String targetRemoteBranch, LoggerBridge log, File projectDir) {
        super(log,projectDir);
        this.fileWalker = fileWalker;
        this.targetRemoteBranch = targetRemoteBranch;
        configList = new LinkedList<>();
        configBeanSortByFile = new HashMap<>();
        initCounts();

        //get complete config map;
        findCompleteConfigMap();

        //get file changes using git
        String fileChanges = fileWalker.getGitOperator()
                .diffBranchNameStatus("dev", targetRemoteBranch);
        Map<FileDiffEnum, List<FileDiff>> fileDiffMap = fileWalker.searchGitFileDiff(fileChanges);

        //count config changes
        countConfigChanges(fileDiffMap);
    }

    public void findCompleteConfigMap(){
        configBeanSortByFile = fileWalker.walkThrough(getProjectDir());
        configBeanSortByFile.forEach((key, configBeans) -> configList.addAll(configBeans));
        countConfig(configList);
    }

    private void initCounts() {
        configCount = new HashMap<>();
        configToAdd = new LinkedList<>();
        configToAddCount = new ConcurrentHashMap<>(100);
        configToDelete = new LinkedList<>();
        configToDeleteCount = new ConcurrentHashMap<>(100);
    }

    private void countConfig(List<Config> configList){
        configList.forEach(config -> configCount.merge(config, 1, Integer::sum));
    }

    public void countConfigChanges(Map<FileDiffEnum, List<FileDiff>> fileChanges){
        List<FileDiff> fileDiffList = fileChanges.get(FileDiffEnum.ADD);
        countFileAdded(fileDiffList);

        fileDiffList = fileChanges.get(FileDiffEnum.DELETE);
        countFileDeleted(fileDiffList);

        fileDiffList = fileChanges.get(FileDiffEnum.MODIFY);
        countFileModify(fileDiffList);

    }

    @Override
    public List<Config> getNewConfig() {
        List<Config> newConfig = new LinkedList<>();

        configToAddCount.forEach((config, count) ->{
            int globalCount = configCount.getOrDefault(config, 0);

            if (globalCount < count){
                //this should not happened
                log.error("the global count {} should not smaller than count {} for config {}", globalCount, count, config.getConfigName());
                return;
            }

            if (globalCount == count){
                //the global count of this config counts the total used times in this commit.
                //if the times the config added to this commit equals to the global count,
                //then this config must be newly added to this commit
                newConfig.add(config);
            }
        });

        return newConfig;
    }

    @Override
    public List<Config> getRemovedConfig() {
        List<Config> removedConfig = new LinkedList<>();

        configToDeleteCount.forEach((config, count) ->{
            int globalCount = configCount.getOrDefault(config, 0);

            if (globalCount > count){
                //this should not happened
                log.error("the global count {} should not greater than count {} for config {}", globalCount, count, config.getConfigName());
                return;
            }

            if (globalCount == count){
                //the global count of this config counts the total used times in this commit.
                //if the times the config added to this commit equals to the global count,
                //then this config must be newly added to this commit
                removedConfig.add(config);
            }
        });

        return removedConfig;
    }

    private void countFileAdded(List<FileDiff> fileDiffList){

        if (CollectionUtils.isEmpty(fileDiffList)){
            return;
        }

        fileDiffList.forEach(fileDiff -> {
            // since the file is new in current commit, it does not exist in remote branch
            // so we just get the configs from current commit
            String targetFile = ProjectFileUtils.getAbsolutePath(getProjectDir(), fileDiff.getFilepath());
            List<Config> beans = configBeanSortByFile.getOrDefault(targetFile, new LinkedList<>());

            recordConfigFromLocalNewFile(beans);
        });
    }

    private void recordConfigFromLocalNewFile(List<Config> beans) {
        beans.forEach(configBean -> {
            // We count the times this config appears in the new commit,
            // so it can be compare to the global config count of this new commit.
            // If the global config count of this config is bigger,
            // then this config is not new to the current commit
            configToAdd.add(configBean);
            configToAddCount.merge(configBean, 1, Integer::sum);
        });
    }

    private void countFileDeleted(List<FileDiff> fileDiffList){
        if (CollectionUtils.isEmpty(fileDiffList)){
            return;
        }

        fileDiffList.forEach(fileDiff -> {
            List<Config> beans = fileWalker.parseFromRemoteFile(targetRemoteBranch, fileDiff.getFilepath());
            recordConfigFromRemoteOldFile(beans);
        });
    }

    private void recordConfigFromRemoteOldFile(List<Config> beans) {
        beans.forEach(configBean -> {
            // We count the times this config appears in the new commit,
            // so it can be compare to the global config count of this new commit.
            // If the global config count of this config is bigger,
            // then this config is not new to the current commit
            configToDelete.add(configBean);
            configToDeleteCount.merge(configBean, 1, Integer::sum);
        });
    }

    private void countFileModify(List<FileDiff> fileDiffList){
        if (CollectionUtils.isEmpty(fileDiffList)){
            return;
        }

        fileDiffList.forEach(fileDiff -> {
            String targetFile = ProjectFileUtils.getAbsolutePath(getProjectDir(), fileDiff.getFilepath());
            List<Config> beansLocal = configBeanSortByFile.getOrDefault(targetFile, new LinkedList<>());

            List<Config> beansRemote = fileWalker.parseFromRemoteFile(targetRemoteBranch, fileDiff.getFilepath());

            List<Config> localNew = new LinkedList<>(beansLocal);
            localNew.removeAll(beansRemote);

            List<Config> remoteNew = new LinkedList<>(beansRemote);
            localNew.removeAll(beansLocal);

            recordConfigFromLocalNewFile(localNew);
            recordConfigFromRemoteOldFile(remoteNew);
        });
    }
}
