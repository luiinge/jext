/**
 * @author Luis Iñesta Gelabert - luiinge@gmail.com
 */
package jext.processor;


import static java.util.stream.Collectors.*;
import java.util.*;
import java.util.stream.*;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.element.ModuleElement.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import jext.*;

/**
 * An extension processor that validate and publish the provided extensions
 */
@SupportedAnnotationTypes({
    "jext.Extension",
    "jext.ExtensionPoint"
})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class JextProcessor extends AbstractProcessor {


    private static class ExtensionInfo {

        private final TypeElement extensionElement;
        private final String extensionName;
        private final TypeElement extensionPointElement;
        private final String extensionPointName;

        public ExtensionInfo(
            TypeElement extensionElement,
            String extensionName,
            TypeElement extensionPointElement,
            String extensionPointName
        ) {
            this.extensionElement = extensionElement;
            this.extensionName = extensionName;
            this.extensionPointElement = extensionPointElement;
            this.extensionPointName = extensionPointName;
        }
    }


    private ProcessingHelper helper;



    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        this.helper = new ProcessingHelper(processingEnv);

        if (!annotations.isEmpty()) {
            Map<String, List<String>> serviceImplementations = new LinkedHashMap<>();
            validateAndRegisterExtensions(roundEnv, serviceImplementations);
            validateExtensionPoints(roundEnv);
            if (!validateModule(roundEnv, serviceImplementations)) {
                return false;
            }
        } else {
            new ManifestGeneratorProcessor().process(processingEnv, roundEnv);
        }
        return true;
    }






    private void validateAndRegisterExtensions(
        RoundEnvironment roundEnv,
        Map<String, List<String>> serviceImplementations
    ) {
        for (Element extensionElement : roundEnv.getElementsAnnotatedWith(Extension.class)) {
            if (validateElementKindIsClass(extensionElement)) {
                validateAndRegisterExtension(
                    (TypeElement) extensionElement, serviceImplementations
                );
            }
        }
    }


    private void validateExtensionPoints(RoundEnvironment roundEnv) {
        var extensionPoints = roundEnv.getElementsAnnotatedWith(ExtensionPoint.class);
        for (Element extensionPointElement : extensionPoints) {
            validateExtensionPoint(extensionPointElement);
        }
    }


    private void validateExtensionPoint(Element extensionPointElement) {
        if (extensionPointElement.getKind() != ElementKind.CLASS &&
            extensionPointElement.getKind() != ElementKind.INTERFACE
        ) {
            helper.log(
                Kind.ERROR,
                extensionPointElement,
                "@ExtensionPoint not valid for {} (only processed for classes or interfaces)",
                extensionPointElement.getSimpleName()
            );
        } else {
            var annotation = extensionPointElement.getAnnotation(ExtensionPoint.class);
            validateVersionFormat(
                annotation.version(), extensionPointElement, "version"
            );
        }
    }



    private boolean validateModule(
        RoundEnvironment roundEnv,
        Map<String, List<String>> serviceImplementations
    ) {
        var elements = roundEnv.getElementsAnnotatedWithAny(Set.of(ExtensionPoint.class,Extension.class));
        if (elements.isEmpty()) {
            return true;
        }
        var module = processingEnv.getElementUtils().getModuleOf(
            elements.iterator().next()
        );
        boolean ok = true;

        var exports = directives(module, ExportsDirective.class);
        for (var extensionPoint : roundEnv.getElementsAnnotatedWith(ExtensionPoint.class)) {
            ok = ok && validateModuleExtensionPoint(extensionPoint,exports);
        }

        var provides = directives(module, ProvidesDirective.class);
        for (var entry : serviceImplementations.entrySet()) {
            ok = ok && validateModuleExtension(entry.getKey(), entry.getValue(), provides);
        }

        return ok;
    }



    private boolean validateModuleExtensionPoint(
        Element extensionPoint,
        List<ExportsDirective> exports
    ) {
        var extensionPointPackage = processingEnv.getElementUtils().getPackageOf(extensionPoint);
        if (exports.stream().map(ExportsDirective::getPackage).noneMatch(extensionPointPackage::equals)) {
            var message = new StringBuilder();
            message.append("Extension point package {} must be declared in the module-info.java file:\n\n");
            message.append("\texports {};\n");
            helper.log(Kind.ERROR, message.toString(),extensionPointPackage,extensionPointPackage);
            return false;
        }
        return true;
    }




    private boolean validateModuleExtension(
        String extensionPoint,
        List<String> extensions,
        List<ProvidesDirective> provides
    ) {
        var implementations = provides.stream()
            .filter(directive -> nameEquals(directive.getService(),extensionPoint))
            .flatMap(directive -> directive.getImplementations().stream())
            .map(TypeElement::getQualifiedName)
            .map(Object::toString)
            .collect(Collectors.toList());

        var nonDeclared = extensions.stream()
            .filter(extension -> !implementations.contains(extension))
            .collect(toList());

        if (!nonDeclared.isEmpty()) {
            var message = new StringBuilder();
            message.append("Extensions implementing extension point {} must be declared in the module-info.java file:\n\n");
            message.append("\tprovides {} with ");
            if (implementations.isEmpty() && nonDeclared.size() == 1) {
                message.append(extensions.get(0)).append(";\n\n");
            } else {
                message.append(
                    Stream.concat(implementations.stream(),nonDeclared.stream())
                    .collect(joining(",\n\t\t", "\n\t\t", ";\n\n"))
                );
            }
            helper.log(Kind.ERROR, message.toString(),extensionPoint,extensionPoint);
            return false;
        } else {
            return true;
        }
    }


    private boolean nameEquals(TypeElement element, String name) {
        return element.getQualifiedName().contentEquals(name);
    }





    private <T extends Directive> List<T> directives(ModuleElement module, Class<T> type) {
        return module.getDirectives()
            .stream()
            .filter(type::isInstance)
            .map(type::cast)
            .collect(Collectors.toList());
    }




    private void validateAndRegisterExtension(
        TypeElement extensionElement,
        Map<String, List<String>> serviceImplementations
    ) {

        boolean ignore = false;
        var extensionAnnotation = extensionElement.getAnnotation(Extension.class);

        // not handling externally managed extensions
        //ignore = extensionAnnotation.externallyManaged();
        ignore = ignore || !validateVersionFormat(
            extensionAnnotation.extensionPointVersion(),
            extensionElement,
            "extensionPointVersion"
        );

        if (ignore) {
            return;
        }

        String extensionPointName = computeExtensionPointName(
            extensionElement, extensionAnnotation
        );
        String extensionName = extensionElement.getQualifiedName().toString();
        TypeElement extensionPointElement = processingEnv.getElementUtils()
            .getTypeElement(extensionPointName);
        ExtensionInfo extensionInfo = new ExtensionInfo(
            extensionElement,
            extensionName,
            extensionPointElement,
            extensionPointName
        );

        ignore = !validateExtensionPointClassExists(extensionInfo);
        ignore = ignore || !validateExtensionPointAnnotation(extensionInfo);
        ignore = ignore || !validateExtensionPointAssignableFromExtension(extensionInfo);

       if (!ignore) {
            serviceImplementations
                .computeIfAbsent(extensionPointName, x -> new ArrayList<>())
                .add(extensionName);
        }

    }




    private boolean validateExtensionPointAssignableFromExtension(ExtensionInfo extensionInfo) {
        if (!isAssignable(
            extensionInfo.extensionElement.asType(), extensionInfo.extensionPointElement.asType()
        )) {
            helper.log(
                Kind.ERROR,
                extensionInfo.extensionElement,
                "{} must implement or extend the extension point type {}",
                extensionInfo.extensionName,
                extensionInfo.extensionPointName
            );
            return false;
        }
        return true;
    }


    private boolean validateExtensionPointAnnotation(ExtensionInfo extensionInfo) {
        if (extensionInfo.extensionPointElement.getAnnotation(ExtensionPoint.class) == null) {
            helper.log(
                Kind.ERROR,
                extensionInfo.extensionElement,
                "Expected extension point type '{}' is not annotated with @ExtensionPoint",
                extensionInfo.extensionPointName
            );
            return false;
        }
        return true;
    }


    private boolean validateExtensionPointClassExists(ExtensionInfo extensionInfo) {
        if (extensionInfo.extensionPointElement == null) {
            helper.log(
                Kind.ERROR,
                extensionInfo.extensionElement,
                "Cannot find extension point class '{}'",
                extensionInfo.extensionPointName
            );
            return false;
        }
        return true;
    }


    private String computeExtensionPointName(
        TypeElement extensionClassElement,
        Extension extensionAnnotation
    ) {
        String extensionPoint = extensionAnnotation.extensionPoint();
        if (extensionPoint.isEmpty()) {
            for (TypeMirror implementedInterface : extensionClassElement.getInterfaces()) {
                extensionPoint = implementedInterface.toString();
                // remove the <..> part in case it is a generic class
                extensionPoint = extensionPoint.replaceAll("\\<[^\\>]*\\>", "");
            }
        }
        return extensionPoint;
    }


    boolean validateElementKindIsClass(Element element) {
        if (element.getKind() != ElementKind.CLASS) {
            helper.log(
                Kind.WARNING,
                element,
                "@Extension ignored for {} (only processed for classes)",
                element.getSimpleName()
            );
            return false;
        }
        return true;
    }


    private boolean validateVersionFormat(String version, Element element, String fieldName) {
        boolean valid = SemanticVersion.validate(version);
        if (!valid) {
            helper.log(
                Kind.ERROR,
                element,
                "Content of field {} ('{}') must be in form of semantic version: '<major>.<minor>[.<patch>]'",
                fieldName,
                version
            );
        }
        return valid;
    }


    private boolean isAssignable(TypeMirror type, TypeMirror typeTo) {
        if (nameWithoutGeneric(type).equals(nameWithoutGeneric(typeTo))) {
            return true;
        }
        for (TypeMirror superType : processingEnv.getTypeUtils().directSupertypes(type)) {
            if (isAssignable(superType, typeTo)) {
                return true;
            }
        }
        return false;
    }


    private String nameWithoutGeneric(TypeMirror type) {
        int genericPosition = type.toString().indexOf('<');
        return genericPosition<0 ? type.toString() : type.toString().substring(0, genericPosition);
    }







}
