package com.liuzhao.ioc_complier;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * Created by zhongyu on 2018/11/13.
 *
 * @author liuzhao
 */
public class ProxyInfo {

    private String packageName;

    private String proxyClassName;

    private TypeElement typeElement;

    public Map<Integer, VariableElement> injectVariables = new HashMap<>();
    public Map<Integer, ExecutableElement> injectMethods = new HashMap<>();

    public static final String PROXY = "Finder";

    public ProxyInfo(Elements elementUtils, TypeElement classElement) {
        this.typeElement = classElement;
        PackageElement packageElement = elementUtils.getPackageOf(classElement);
        packageName = packageElement.getQualifiedName().toString();
        proxyClassName = ClassValidator.getClassName(classElement, packageName) + "$$" + PROXY;
    }
    public String generateJavaCode() {
        StringBuilder builder = new StringBuilder();
        builder.append("// Generated code. Do not modify!\n");
        builder.append("package ").append(packageName).append(";\n\n");
        builder.append("import com.liuzhao.ioc_api.*;\n");
        builder.append('\n');
        builder.append("public class ").append(proxyClassName).append(" implements " + ProxyInfo.PROXY + "<" + typeElement.getQualifiedName() + ">");
        builder.append(" {\n");
        generateMethods(builder);
        builder.append('\n');
        builder.append("}\n");
        return builder.toString();
    }

    private void generateMethods(StringBuilder builder) {
        builder.append("\t@Override\n ");
        builder.append("\tpublic void inject(" + typeElement.getQualifiedName() + " host, Object source, Provider provider) {\n");
        for (int id : injectVariables.keySet()) {
            VariableElement element = injectVariables.get(id);

            String name = element.getSimpleName().toString();
            String type = element.asType().toString();
            builder.append("\t\thost." + name).append(" = ");
            builder.append("(" + type + ")(provider.findView( source," + id + "));\n");

        }

        for (int id : injectMethods.keySet()){
            ExecutableElement element = injectMethods.get(id);
            String methodName = element.getSimpleName().toString();
            String name = id2Name(id);
            builder.append("\t\thost." + name + ".setOnClickListener(new android.view.View.OnClickListener() {\n");
            builder.append("\t\t@Override\n");
            builder.append("\t\tpublic void onClick(android.view.View v) {\n");
            builder.append("\t\t\thost." + methodName + "(v);\n");
            builder.append("\t\t}\n");
            builder.append("\t\t});\n\n");
        }
        builder.append("\t}\n");
    }

    private String id2Name(int id){
        VariableElement element = injectVariables.get(id);
        String name = element.getSimpleName().toString();
        return name;
    }

    public String getProxyClassFullName() {
        return packageName + "." + proxyClassName;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }
}
