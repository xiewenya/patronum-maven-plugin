package com.bresai.expecto.patronum.core.parser;

import com.bresai.expecto.patronum.core.bean.ConfigBean;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/27
 * @content:
 */
public class testJavaFileParser {

    @Test
    public void testJavaFileParser(){
        JavaFileParser javaFileParser = new JavaFileParser(null, new NacosValueResolver());
        URL url = this.getClass().getClassLoader().getResource("NacosValueTest.java");
        List<ConfigBean> beans = javaFileParser.parser(new File(url.getFile()));
        Assert.assertEquals(beans.size(), 9);

        url = this.getClass().getClassLoader().getResource("NacosValueTestEmpty.java");
        beans = javaFileParser.parser(new File(url.getFile()));
        Assert.assertEquals(beans.size(), 0);
    }
}