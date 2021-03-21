package jext.processor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import javax.tools.*;
import org.junit.Test;
import com.google.testing.compile.*;
import com.google.testing.compile.Compiler;

public class TestProcessor {


   @Test
   public void extensionPointWithoutModuleFileShouldRaiseCompilationError() throws IOException {
       compilationWithoutModules("extensionPointWithoutModuleFile")
       .hadErrorContaining(
           "[jext] :: Extension point package jext.test.api must be declared in the module-info.java file:\n"+
           "  \n"+
           "  \texports jext.test.api;"
       );
   }

   @Test
   public void extensionPointWithInvalidVersionShouldRaiseCompilationError() throws IOException {
       compilation("extensionPointWithInvalidVersion")
       .hadErrorContaining(
           "[jext] [jext.test.api.StuffProvider] Content of field version ('A.0') must be in form of semantic version: '<major>.<minor>[.<patch>]'"
       );
   }

   @Test
   public void extensionsNotDeclaredInModuleShouldRaiseCompilationError() throws IOException {
       compilation("extensionNotDeclaredInModuleFile")
       .hadErrorContaining(
           "[jext] :: Extensions implementing extension point jext.test.api.StuffProvider must be declared in the module-info.java file:\n"+
           "  \n"+
           "  \tprovides jext.test.api.StuffProvider with \n"+
           "  \t\tjext.test.api.StuffProviderB,\n"+
           "  \t\tjext.test.api.StuffProviderA;"
       );
   }


   @Test
   public void projectWithExtensionsGenerateManifest() throws IOException {
       compilation("projectWithExtensions")
       .generatedFile(StandardLocation.CLASS_OUTPUT, "", "META-INF/MANIFEST.MF")
       .contentsAsString(StandardCharsets.UTF_8)
       .isEqualTo(
           "Manifest-Version: 1.0\n"+
           "Archiver-Version: Plexus Archiver\n"+
           "Plugin-Vendor: io.github.luiinge\n"+
           "Plugin-Name: jext-processor\n"+
           "Plugin-Version: 2.0.0-SNAPSHOT\n"+
           "Plugin-Dependencies: io.github.luiinge:jext:2.0.0-SNAPSHOT \n" +
           " javax.annotation:javax.annotation-api:1.3.2 \n" +
           " org.apache.maven:maven-model-builder:3.6.3 \n" +
           " org.codehaus.plexus:plexus-utils:3.2.1\n");
   }


   @Test
   public void extensionImplementingNotAnnotatedExtensionPointShouldRaiseCompilationError() throws IOException {
       var compilation = compilation("extensionPointNotAnnotated");
       compilation.hadErrorCount(2);
       compilation.hadErrorContaining(
           "[jext] [jext.test.api.StuffProviderB] Expected extension point type 'jext.test.api.StuffProvider' is not annotated with @ExtensionPoint"
       );
       compilation.hadErrorContaining(
           "[jext] [jext.test.api.StuffProviderA] Expected extension point type 'jext.test.api.StuffProvider' is not annotated with @ExtensionPoint"
       );
   }


   @Test
   public void extensionNotImplementingExtensionPointShouldRaiseCompilationError() throws IOException {
       compilation("extensionNotImplementingExtensionPoint")
       .hadErrorContaining(
           "[jext] [jext.test.api.StuffProviderA] jext.test.api.StuffProviderA must implement or extend the extension point type jext.test.api.StuffProvider"
       );
   }


   private CompilationSubject compilation(String sourceFolder) throws IOException {
       return compilation(sourceFolder, true);
   }


   private CompilationSubject compilationWithoutModules(String sourceFolder) throws IOException {
       return compilation(sourceFolder, false);
   }

   private CompilationSubject compilation(String sourceFolder, boolean withModules) throws IOException {
       String option = withModules ? "--module-path" : "--class-path";
       System.out.println("------------------------------");
       System.out.println("Compiling "+sourceFolder);
       Compilation compilation = Compiler.javac()
           .withProcessors(new JextProcessor())
           .withOptions(new Object[] {option+"=src/test/resources/jext.jar"})
           .compile(source(sourceFolder));
       System.out.println(compilation.status());
       System.out.println(compilation.diagnostics().stream().map(Object::toString).collect(Collectors.joining("\n")));
       return CompilationSubject.assertThat(compilation);
   }


   private JavaFileObject[] source(String sourceFolder) throws IOException {
       List<JavaFileObject> files = new ArrayList<>();
       Path path = Path.of("src", "test", "resources", sourceFolder);
       Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
           @Override
           public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
               if (Files.isRegularFile(file)) {
                   files.add(JavaFileObjects.forResource(file.toUri().toURL()));
               }
               return super.visitFile(file, attrs);
        }
       });
       return files.toArray(JavaFileObject[]::new);
   }


}
