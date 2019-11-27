package com.bresai.expecto.patronum.core.parser;

import com.bresai.expecto.patronum.core.bean.NacosValueBean;
import org.apache.commons.lang3.ArrayUtils;

import java.util.regex.Pattern;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/27
 * @content:
 */
public class NacosValueResolver extends ValueResolver<NacosValueBean> {

    @Override
    public String[] resolve(String value){
        if (!match(value)) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }

        String content = value.substring(2, value.length() - 1);
        return content.split(":");

//        if (split.length == 2){
//            return Pair.of(split[0], split[1]);
//        }else{
//            return Pair.of(split[0], "");
//        }
    }

    @Override
    public Pattern getPattern() {
        String patternStr = "^(\\$)(\\{).*?(\\})$";
        return Pattern.compile(patternStr,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    }


}
