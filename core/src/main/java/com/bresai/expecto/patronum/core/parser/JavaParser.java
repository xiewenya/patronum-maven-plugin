package com.bresai.expecto.patronum.core.parser;

import com.bresai.expecto.patronum.core.bean.ConfigBean;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.util.List;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/28
 * @content:
 */
public abstract class JavaParser implements Parser {

    {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
    }

    public abstract List<ConfigBean> parser(String code);
}
