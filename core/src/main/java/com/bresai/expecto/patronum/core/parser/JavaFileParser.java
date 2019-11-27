package com.bresai.expecto.patronum.core.parser;

import com.bresai.expecto.patronum.core.bean.ConfigBean;
import com.bresai.expecto.patronum.core.bean.NacosValueBean;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import pl.project13.core.log.LoggerBridge;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/27
 * @content:
 */
public class JavaFileParser implements Parser {

    private LoggerBridge log;

    private ValueResolver valueResolver;

    public JavaFileParser(LoggerBridge loggerBridge, ValueResolver valueResolver) {
        this.log = loggerBridge;
        this.valueResolver = valueResolver;
    }

    @Override
    public List<ConfigBean> parser(File file){
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

        List<ConfigBean> beans = new LinkedList<>();

        try {
            NodeList<BodyDeclaration<?>> nodeList = new NodeList<>();

            CompilationUnit cu = StaticJavaParser.parse(file);
            if (cu.getPrimaryType().isPresent()){
                nodeList = cu.getPrimaryType().get().getMembers();
            }

            List<AnnotationExpr> annotations = new LinkedList<>();

            //get all nacosValue annotations
            nodeList.forEach(node ->
                    node.getAnnotationByName("NacosValue")
                            .ifPresent(annotationExpr -> annotations
                                    .add(annotationExpr.asAnnotationExpr())));

            //build NacosValueBean for each nacosValue annotation
            annotations.forEach(annotation -> {
                beans.add(parseNacosValueProperties(annotation));
            });
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
        }

        return beans;
    }

    private NacosValueBean parseNacosValueProperties(AnnotationExpr annotation) {
        NacosValueBean bean = new NacosValueBean();

        if (annotation.isNormalAnnotationExpr()){
            NodeList<MemberValuePair> valuePairs = annotation.asNormalAnnotationExpr().getPairs();
            valuePairs.forEach(memberValuePair -> {
                if ("value".equalsIgnoreCase(memberValuePair.getName().asString())){
                    getValues(bean, memberValuePair.getValue().asStringLiteralExpr().asString());
                }

                if ("autoRefreshed".equalsIgnoreCase(memberValuePair.getName().asString())){
                    bean.setAutoRefreshed("true".equalsIgnoreCase(memberValuePair.getValue().toString()));
                }
            });

        }

        if (annotation.isSingleMemberAnnotationExpr()){
            String expr = annotation.asSingleMemberAnnotationExpr().getMemberValue().asStringLiteralExpr().asString();
            getValues(bean, expr);
        }

        return bean;
    }

    private void getValues(NacosValueBean bean, String expr) {
        String[] rs = valueResolver.resolve(expr);

        if (rs.length == 1 ){
            bean.setConfigName(rs[0]);
            bean.setDefaultValue("");
        }

        if (rs.length == 2){
            bean.setConfigName(rs[0]);
            bean.setDefaultValue(rs[1]);
        }
    }


}
