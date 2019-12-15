package com.bresai.expecto.patronum.core.bean;

import com.github.javaparser.ast.CompilationUnit;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/28
 * @content:
 */
@Setter
@Getter
public class JavaFileMeta extends FileMeta {

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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JavaFileMeta that = (JavaFileMeta) o;

        return new EqualsBuilder()
                .append(packageName, that.packageName)
                .append(className, that.className)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(packageName)
                .append(className)
                .toHashCode();
    }

    @Override
    public String toString() {
        return packageName + "." + className;
    }
}
