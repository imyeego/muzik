package com.liuzhao.ioc_complier;

import com.google.auto.service.AutoService;
import com.liuzhao.ioc_annotations.BindView;
import com.liuzhao.ioc_annotations.OnClick;


import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 *
 * @author liuzhao
 */
@AutoService(Processor.class)
public class IocProcessor extends AbstractProcessor {

    private Filer mFileUtils;

    private Elements mElementUtils;
    private Messager mMessager;

    private Types mTypeUtils;
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationTypes = new LinkedHashSet<>();
        annotationTypes.add(BindView.class.getCanonicalName());
        annotationTypes.add(OnClick.class.getCanonicalName());
        return annotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mTypeUtils = processingEnv.getTypeUtils();
        mFileUtils = processingEnv.getFiler();
        mElementUtils = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
    }

    private Map<String, ProxyInfo> mProxyMap = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        mProxyMap.clear();

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BindView.class);
        for (Element element : elements) {
            if (!checkAnnotationValid(element, BindView.class)) {
                return false;
            }
            VariableElement variableElement = ((VariableElement) element);
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            String qualifiedName = typeElement.getQualifiedName().toString();
            ProxyInfo proxyInfo = mProxyMap.get(qualifiedName);
            if (proxyInfo == null) {
                proxyInfo = new ProxyInfo(mElementUtils, typeElement);
                mProxyMap.put(qualifiedName, proxyInfo);
            }
            BindView annotation = variableElement.getAnnotation(BindView.class);
            int id = annotation.value();
            proxyInfo.injectVariables.put(id, variableElement);


        }

        elements = roundEnv.getElementsAnnotatedWith(OnClick.class);
        for (Element element : elements){
            ExecutableElement executableElement = (ExecutableElement) element;
            if (!checkAnnotationOnClick(executableElement))
                return false;
            TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();
            String qualifiedName = typeElement.getQualifiedName().toString();
            ProxyInfo proxyInfo = mProxyMap.get(qualifiedName);
            if (proxyInfo == null) {
                proxyInfo = new ProxyInfo(mElementUtils, typeElement);
                mProxyMap.put(qualifiedName, proxyInfo);
            }
            OnClick annotation = executableElement.getAnnotation(OnClick.class);
            int id = annotation.value();
            proxyInfo.injectMethods.put(id, executableElement);
        }

        for (String key : mProxyMap.keySet()) {
            ProxyInfo proxyInfo = mProxyMap.get(key);
            try {
                JavaFileObject jfo = processingEnv.getFiler().createSourceFile(
                        proxyInfo.getProxyClassFullName(),
                        proxyInfo.getTypeElement());
                Writer writer = jfo.openWriter();
                writer.write(proxyInfo.generateJavaCode());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                error(proxyInfo.getTypeElement(),
                        "Unable to write injector for type %s: %s",
                        proxyInfo.getTypeElement(), e.getMessage());
            }

        }
        return true;
    }

    private boolean checkAnnotationValid(Element annotatedElement, Class clazz) {
        if (annotatedElement.getKind() != ElementKind.FIELD) {
            error(annotatedElement, "%s must be declared on field.", clazz.getSimpleName());
            return false;
        }
        if (ClassValidator.isPrivate(annotatedElement)) {
            error(annotatedElement, "%s() must can not be private.", annotatedElement.getSimpleName());
            return false;
        }
        return true;
    }

    private boolean checkAnnotationOnClick(ExecutableElement executableElement){
        TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();

        List<? extends VariableElement> methodParameters = executableElement.getParameters();
        if (methodParameters.size() > 1) {
            error(executableElement, "@%s methods can have at most 1 parameter(s). (%s.%s)",
                    OnClick.class.getSimpleName(), typeElement.getQualifiedName(), executableElement.getSimpleName());
            return false;
        }
        TypeMirror returnType = executableElement.getReturnType();
        if (returnType instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable) returnType;
            returnType = typeVariable.getUpperBound();
        }
        if (returnType.getKind() != TypeKind.VOID) {
            error(executableElement, "@%s methods must have a 'void' return type. (%s.%s)",
                    OnClick.class.getSimpleName(), typeElement.getQualifiedName(), executableElement.getSimpleName());
            return false;
        }


        return true;
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        mMessager.printMessage(Diagnostic.Kind.NOTE, message, element);
    }

}
