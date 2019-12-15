package com.bresai.expecto.patronum.core.result;

import com.bresai.expecto.patronum.core.bean.ConfigBean;
import com.bresai.expecto.patronum.core.bean.FileBean;
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
public class Result<T extends ConfigBean> {

    private int size;

    private List<String> simpleList;

    private List<T> completeList;

    private Set<FileBean> fileBeanSet;

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
        if (fileBeanSet == null){
            fileBeanSet = new HashSet<>();
        }

        completeList.forEach(bean -> fileBeanSet.add(bean.getFileMeta()));
    }
}
