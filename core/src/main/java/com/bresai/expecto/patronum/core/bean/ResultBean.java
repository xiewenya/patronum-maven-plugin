package com.bresai.expecto.patronum.core.bean;

import lombok.Data;

import java.util.List;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/12/8
 * @content:
 */
@Data
public class ResultBean<T> {

    private int size;

    private List<String> simpleList;

    private List<T> completeList;

    public ResultBean(List<T> completeList) {
        this.completeList = completeList;
        setSimpleList(completeList);
        setSize(completeList.size());
    }

    public void setSimpleList(List<T> completeList) {
        completeList.forEach(bean -> simpleList.add(bean.toString()));
    }
}
