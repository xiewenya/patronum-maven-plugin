package com.bresai.expecto.patronum.core.enums;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/12/15
 * @content:
 */
public enum FileDiffEnum {
    ADD,
    MODIFY,
    DELETE,
    RENAME;


    public static FileDiffEnum of(String type){
        if ("D".equalsIgnoreCase(type)){
            return FileDiffEnum.DELETE;
        }

        if ("A".equalsIgnoreCase(type)){
            return FileDiffEnum.ADD;
        }

        if ("M".equalsIgnoreCase(type)){
            return FileDiffEnum.MODIFY;
        }

        if (type.startsWith("R") || type.startsWith("r")){
            return FileDiffEnum.RENAME;
        }

        return null;
    }
}
