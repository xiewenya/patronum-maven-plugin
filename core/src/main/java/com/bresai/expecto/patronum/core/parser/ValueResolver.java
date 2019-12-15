package com.bresai.expecto.patronum.core.parser;

import com.bresai.expecto.patronum.core.bean.Config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/27
 * @content:
 */
public abstract class ValueResolver<T extends Config> {

    private Pattern pattern;

    public ValueResolver() {
        this.pattern = getPattern();
    }

    boolean match(String value){
        Matcher matcher = pattern.matcher(value);
        // 字符串是否与正则表达式相匹配
        return matcher.matches();
    }


    public abstract String[] resolve(String value);

    /**
     * define pattern
     * @return Value Pattern
     */
    public abstract Pattern getPattern();
}
