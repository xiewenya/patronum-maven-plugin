package com.bresai.expecto.patronum.core.bean;

import com.github.javaparser.ast.CompilationUnit;
import lombok.Getter;
import lombok.Setter;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/28
 * @content:
 */
@Setter
@Getter
public class JavaFileBean {

    private String packageName;

    private String className;

    public void setPackageName(CompilationUnit cu) {
        if (cu.getPackageDeclaration().isPresent()){
            this.packageName = cu.getPackageDeclaration().get().getNameAsString();
        }
    }

    public void setClassName(CompilationUnit cu) {
        if (cu.getPrimaryTypeName().isPresent()){
            this.className = cu.getPrimaryTypeName().get();
        }
    }

    @Override
    public String toString() {
        return packageName + "." + className;
    }
}
