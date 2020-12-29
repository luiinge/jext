package jext;

import com.google.testing.compile.*;
import com.google.testing.compile.Compiler;
import org.junit.Test;

import java.io.IOException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCompilation {

    @Test
    public void testCompilation() throws IOException {
        Compilation compilation =
            Compiler.javac()
                .withProcessors(new ExtensionProcessor())
                .compile(
                    JavaFileObjects.forResource("CompilableExtensionPoint.java"),
                    JavaFileObjects.forResource("CompilableExtension.java")
                );
        System.out.println(compilation.status());
        System.out.println(compilation.diagnostics().stream().map(Object::toString).collect(Collectors.joining("\n")));
        assertThat(compilation.status()).isEqualTo(Compilation.Status.SUCCESS);
        assertThat(compilation.generatedFiles()).hasSize(3);

        var generated = compilation.generatedFiles().get(0);
        assertThat(generated.toUri()).hasPath("/CLASS_OUTPUT/META-INF/services/jext.CompilableExtensionPoint");
        assertThat(generated.getCharContent(true)).isEqualTo("jext.CompilableExtension\n");

    }
}
