package com.bresai.expecto.patronum.core.parser;

import com.bresai.expecto.patronum.core.bean.ConfigBean;
import com.bresai.expecto.patronum.core.bean.JavaFileBean;
import com.bresai.expecto.patronum.core.bean.NacosValueBean;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.comments.Comment;
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
    public List<ConfigBean> parser(String code) {
        return parse(StaticJavaParser.parse(code), null);
    }

    @Override
    public List<ConfigBean> parser(File file) {
        try {
            log.info(file.getAbsolutePath());
            return parse(StaticJavaParser.parse(file), file);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
            return new LinkedList<>();
        }
    }

    private List<ConfigBean> parse(CompilationUnit cu, File file) {
        NodeList<BodyDeclaration<?>> nodeList = new NodeList<>();

        JavaFileBean fileBean = new JavaFileBean();
        fileBean.setPackageName(cu);
        fileBean.setClassName(cu);
        fileBean.setFile(file);

        if (cu.getPrimaryType().isPresent()) {
            nodeList = cu.getPrimaryType().get().getMembers();
        }

        List<AnnotationExpr> annotations = findAnnotations(nodeList, "NacosValue");

        List<ConfigBean> configBeans = buildAnnotationBeans(annotations, fileBean);

        fileBean.setConfigBeanList(configBeans);

        return configBeans;
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

        if (annotation.isNormalAnnotationExpr()) {
            NodeList<MemberValuePair> valuePairs = annotation.asNormalAnnotationExpr().getPairs();
            valuePairs.forEach(memberValuePair -> {
                if ("value".equalsIgnoreCase(memberValuePair.getName().asString())) {
                    getValues(bean, memberValuePair.getValue().asStringLiteralExpr().asString());
                }

                if ("autoRefreshed".equalsIgnoreCase(memberValuePair.getName().asString())) {
                    bean.setAutoRefreshed("true".equalsIgnoreCase(memberValuePair.getValue().toString()));
                }
            });
        }

        if (annotation.isSingleMemberAnnotationExpr()) {
            String expr = annotation.asSingleMemberAnnotationExpr().getMemberValue().asStringLiteralExpr().asString();
            getValues(bean, expr);
        }

        bean.setFileMeta(fileBean);

        parseComments(annotation, bean);

        return bean;
    }

    private void parseComments(AnnotationExpr annotation, NacosValueBean bean) {
        annotation.getParentNode().ifPresent(node -> {
            List<Comment> comments = new LinkedList<>();
            node.getComment().ifPresent(comments::add);
            comments.addAll(node.getAllContainedComments());
            bean.setComments(comments);
        });
    }

    private void getValues(NacosValueBean bean, String expr) {
        String[] rs = valueResolver.resolve(expr);

        if (rs.length == 1) {
            bean.setConfigName(rs[0]);
            bean.setDefaultValue("");
        }

        if (rs.length == 2) {
            bean.setConfigName(rs[0]);
            bean.setDefaultValue(rs[1]);
        }
    }


}
