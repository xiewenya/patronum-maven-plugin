package com.bresai.expecto.patronum.core.parser;

import com.bresai.expecto.patronum.core.bean.ConfigBean;
import com.bresai.expecto.patronum.core.bean.JavaFileBean;
import com.bresai.expecto.patronum.core.bean.NacosValueBean;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
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
public class JavaFileParser extends JavaParser {

    private LoggerBridge log;

    private ValueResolver valueResolver;

    public JavaFileParser(LoggerBridge loggerBridge, ValueResolver valueResolver) {
        this.log = loggerBridge;
        this.valueResolver = valueResolver;
    }

    @Override
    public List<ConfigBean> parser(File file){
        try {

            log.info(file.getAbsolutePath());

            NodeList<BodyDeclaration<?>> nodeList = new NodeList<>();

            CompilationUnit cu = StaticJavaParser.parse(file);

            JavaFileBean fileBean = new JavaFileBean();
            fileBean.setPackageName(cu);
            fileBean.setClassName(cu);

            if (cu.getPrimaryType().isPresent()){
                nodeList = cu.getPrimaryType().get().getMembers();
            }

            List<AnnotationExpr> annotations = findAnnotations(nodeList, "NacosValue");

            return buildAnnotationBeans(annotations, fileBean);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
            return new LinkedList<>();
        }
    }

    private List<ConfigBean> buildAnnotationBeans(List<AnnotationExpr> annotations, JavaFileBean fileBean) {
        List<ConfigBean> beans = new LinkedList<>();
        //build NacosValueBean for each nacosValue annotation
        annotations.forEach(annotation -> {
            beans.add(parseNacosValueProperties(annotation, fileBean));
        });

        return beans;
    }

    private List<AnnotationExpr> findAnnotations(NodeList<BodyDeclaration<?>> nodeList, String annotationName) {
        List<AnnotationExpr> annotations = new LinkedList<>();

        //get all nacosValue annotations
        nodeList.forEach(node ->
                node.getAnnotationByName(annotationName)
                        .ifPresent(annotationExpr -> annotations
                                .add(annotationExpr.asAnnotationExpr())));

        return annotations;
    }

    private NacosValueBean parseNacosValueProperties(AnnotationExpr annotation, JavaFileBean fileBean) {
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

        bean.setFileMeta(fileBean);

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
