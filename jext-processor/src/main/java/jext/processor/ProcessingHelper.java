package jext.processor;

import java.io.*;
import java.nio.file.NoSuchFileException;
import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.element.Element;
import javax.tools.*;
import javax.tools.Diagnostic.Kind;


class ProcessingHelper {

    private final ProcessingEnvironment processingEnv;


    ProcessingHelper(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }


    void log(Diagnostic.Kind kind, String message, Object... messageArgs) {
        processingEnv.getMessager()
            .printMessage(
                kind,
                "[jext] :: " + String.format(message.replace("{}", "%s"), messageArgs)
            );
    }


    void log(Diagnostic.Kind kind, Element element, String message, Object... messageArgs) {
        processingEnv.getMessager()
            .printMessage(
                kind,
                "[jext] [" + element + "] "+ String.format(message.replace("{}", "%s"), messageArgs)
            );
    }



    void writeResource(String resource, List<String> lines) {
        FileObject resourceFile = null;
        try {
            resourceFile = getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", resource);
            try (BufferedWriter writer = new BufferedWriter(resourceFile.openWriter())) {
                for (String line : lines) {
                    writer.append(line);
                    writer.newLine();
                }
            }
        } catch (NoSuchFileException e) {
            log(Kind.NOTE, "File does not exist : {}", resourceFile);
        } catch (IOException | IllegalStateException e) {
            log(Kind.ERROR, "Cannot write file {} : {}", resourceFile, e.toString());
        }
    }



    Filer getFiler() {
        return processingEnv.getFiler();
    }




}
