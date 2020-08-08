/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package jext;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * An extension processor that validate and publish the provided extensions
 */
@SupportedAnnotationTypes("jext.Extension")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class ExtensionProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<String, List<String>> serviceImplementations = new LinkedHashMap<>();
        for (Element extensionElement : roundEnv.getElementsAnnotatedWith(Extension.class)) {
            if (validateElementKindIsClass(extensionElement)) {
                validateAndRegisterExtension(
                    (TypeElement) extensionElement, serviceImplementations
                );
            }
        }
        for (Element extensionPointElement : roundEnv
            .getElementsAnnotatedWith(ExtensionPoint.class)) {
            validateExtensionPoint(extensionPointElement);
        }
        writeMetaInfServiceDeclarations(serviceImplementations);
        return false;
    }


    private void validateExtensionPoint(Element extensionPointElement) {
        if (extensionPointElement.getKind() != ElementKind.CLASS
                        && extensionPointElement.getKind() != ElementKind.INTERFACE) {
            log(
                Kind.ERROR,
                extensionPointElement,
                "@ExtensionPoint not valid for {} (only processed for classes or interfaces)",
                extensionPointElement.getSimpleName()
            );
        } else {
            var extensionPointAnnotation = extensionPointElement
                .getAnnotation(ExtensionPoint.class);
            validateVersionFormat(
                extensionPointAnnotation.version(), extensionPointElement, "version"
            );
        }
    }


    private void validateAndRegisterExtension(
        TypeElement extensionElement,
        Map<String, List<String>> serviceImplementations
    ) {

        boolean ignore;
        var extensionAnnotation = extensionElement.getAnnotation(Extension.class);

        // not handling externally managed extensions
        ignore = extensionAnnotation.externallyManaged();
        ignore = ignore || !validateVersionFormat(
            extensionAnnotation.version(),
            extensionElement,
            "version"
        );
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
        ignore = ignore || !validateExtensionDeclaredInModuleInfo(extensionInfo);

        if (!ignore) {
            serviceImplementations
                .computeIfAbsent(extensionPointName, x -> new ArrayList<>())
                .add(extensionName);
        }

    }


    private boolean validateExtensionDeclaredInModuleInfo(ExtensionInfo extensionInfo) {
        var module = this.processingEnv.getElementUtils()
            .getModuleOf(extensionInfo.extensionElement);
        if (module.isUnnamed()) {
            return true;
        }
        boolean declaredInModule = module.getDirectives()
            .stream()
            .filter(directive -> directive.getKind() == ModuleElement.DirectiveKind.PROVIDES)
            .map(ModuleElement.ProvidesDirective.class::cast)
            .filter(provides -> provides.getService().equals(extensionInfo.extensionPointElement))
            .flatMap(provides -> provides.getImplementations().stream())
            .anyMatch(extensionInfo.extensionElement::equals);
        if (!declaredInModule) {
            log(
                Kind.MANDATORY_WARNING,
                extensionInfo.extensionElement,
                "{} must be declared with the directive 'provides' in module-info.java in order to be used properly",
                extensionInfo.extensionName,
                extensionInfo.extensionPointName
            );
        }
        return true;
    }


    private boolean validateExtensionPointAssignableFromExtension(ExtensionInfo extensionInfo) {
        if (!isAssignable(
            extensionInfo.extensionElement.asType(), extensionInfo.extensionPointElement.asType()
        )) {
            log(
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
            log(
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
            log(
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
            log(
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
        boolean valid = version.matches("\\d+\\.\\d+(\\..*)?");
        if (!valid) {
            log(
                Kind.ERROR,
                element,
                "Content of field {} ('{}') must be in form '<major>.<minor>(.<patch>)'",
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


    private void writeMetaInfServiceDeclarations(Map<String,List<String>> serviceImplementations) {
        Filer filer = this.processingEnv.getFiler();
        for (Entry<String, List<String>> mapEntry : serviceImplementations.entrySet()) {
            String extension = mapEntry.getKey();
            String resourcePath = "META-INF/services/" + extension;
            try {
                writeFile(filer, resourcePath, mapEntry);
            } catch (IOException e) {
                log(Kind.ERROR, "UNEXPECTED ERROR: {}", e.toString());
            }
        }
    }


    private void writeFile(
        Filer filer,
        String resourcePath,
        Entry<String, List<String>> entry
    ) throws IOException {
        FileObject resourceFile = filer
            .getResource(StandardLocation.CLASS_OUTPUT, "", resourcePath);
        List<String> oldExtensions = resourceFile.getLastModified() == 0 ?
            List.of() :
            readLines(resourceFile)
        ;
        Set<String> allExtensions = new LinkedHashSet<>();
        allExtensions.addAll(oldExtensions);
        allExtensions.addAll(entry.getValue());
        resourceFile = filer.createResource(StandardLocation.CLASS_OUTPUT, "", resourcePath);
        writeLines(allExtensions, resourceFile);
        System.out.println(
            "[jext] :: Generated service declaration file " + resourceFile.getName()
        );
    }


    private List<String> readLines(FileObject resourceFile) {
        return readLines(resourceFile, false);
    }


    private List<String> readLines(FileObject resourceFile, boolean ignoreMissing) {
        List<String> lines = new ArrayList<>();
        try {
            try (BufferedReader reader = new BufferedReader(resourceFile.openReader(true))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            if (!(e instanceof NoSuchFileException && ignoreMissing)) {
                log(Kind.ERROR, "Cannot read file {} : {}", resourceFile.toUri(), e.toString());
            }
        }
        return lines;
    }


    private void writeLines(Set<String> lines, FileObject resourceFile) {
        try {
            try (BufferedWriter writer = new BufferedWriter(resourceFile.openWriter())) {
                for (String line : lines) {
                    writer.append(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            log(Kind.ERROR, "error writing {} : {}", resourceFile.toUri(), e.toString());
        }
    }


    private void log(Kind kind, String message, Object... messageArgs) {
        processingEnv.getMessager()
            .printMessage(
                kind,
                "[jext] :: " + String.format(message.replace("{}", "%s"), messageArgs)
            );
    }


    private void log(Kind kind, Element element, String message, Object... messageArgs) {
        processingEnv.getMessager()
            .printMessage(
                kind,
                "[jext] at " + element.asType().toString() + " :: " + String
                    .format(message.replace("{}", "%s"), messageArgs)
            );
    }


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
}
