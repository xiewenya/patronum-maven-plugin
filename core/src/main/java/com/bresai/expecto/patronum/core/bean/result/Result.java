package com.bresai.expecto.patronum.core.bean.result;

import com.bresai.expecto.patronum.core.bean.Config;
import com.bresai.expecto.patronum.core.bean.FileMeta;
import lombok.Data;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/12/8
 * @content:
 */
@Data
public class Result<T extends Config> {

    private int size;

    private List<String> simpleList;

    private List<T> completeList;

    private Set<FileMeta> fileMetaSet;

    public Result(List<T> completeList) {
        this.completeList = completeList;
        setSimpleList(completeList);
        setSize(completeList.size());
        setFileSet(completeList);
    }

    public void setSimpleList(List<T> completeList) {
        simpleList = new LinkedList<>();
        completeList.forEach(bean -> simpleList.add(bean.toString()));
    }

    private void setFileSet(List<T> completeList) {
        if (fileMetaSet == null){
            fileMetaSet = new HashSet<>();
        }

        completeList.forEach(bean -> fileMetaSet.add(bean.getFileMeta()));
    }
}
