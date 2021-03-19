package jext.test;

import jext.*;

import jext.test.ext.*;
import org.assertj.core.api.ListAssert;
import org.junit.Test;

import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class TestExtensionManager {

    static {
        TestExtensionManager.class.getModule()
            .addUses(ExtensionPointA.class)
            .addUses(ExtensionA.class)
        ;
    }

    private final ExtensionManager extensionManager = new ExtensionManager();

    @Test
    public void canRetrieveASingleExtension() {
        assertThat(extensionManager.getExtension(ExtensionPointA.class))
            .isNotEmpty()
            .containsInstanceOf(ExtensionPointA.class)
            .containsInstanceOf(ExtensionA.class);
    }

    @Test
    public void canRetrieveMultipleExtensions() {
        assertThat(extensionManager.getExtensions(ExtensionPointB.class))
            .anyMatch(ExtensionB1.class::isInstance)
            .anyMatch(ExtensionB2.class::isInstance);
    }

    @Test
    public void filterExtensionsWithIncorrectVersion() {
        assertThat(extensionManager.getExtensions(ExtensionPointC.class))
            .hasSize(1)
            .allMatch(ExtensionC_2_1.class::isInstance);
    }

    @Test
    public void filterExtensionsOverriden() {
        assertThat(extensionManager.getExtensions(ExtensionPointD.class))
           .hasSize(1)
           .allMatch(ExtensionDOverrider.class::isInstance)
           .noneMatch(ExtensionDOverriden.class::isInstance);
    }

    @Test
    public void extensionsAreRetrievedInPriorityOrder() {
       Stream<Class<?>>  extensionClasses = extensionManager
           .getExtensions(ExtensionPointE.class).map(Object::getClass);
       assertThat(extensionClasses)
           .hasSize(5)
           .containsExactly(
              ExtensionEHighestPriority.class,
              ExtensionEHigherPriority.class,
              ExtensionENormalPriority.class,
              ExtensionELowerPriority.class,
              ExtensionELowestPriority.class
           );
    }

    @Test
    public void scopeIsHonouredAlongMultipleCalls() {
        var globalFirstCall = extensionManager.getExtensions(ExtensionPointF.class)
            .filter(ExtensionFGlobal.class::isInstance).findAny().orElseThrow();
        var globalSecondCall = extensionManager.getExtensions(ExtensionPointF.class)
           .filter(ExtensionFGlobal.class::isInstance).findAny().orElseThrow();
        var localFirstCall = extensionManager.getExtensions(ExtensionPointF.class)
           .filter(ExtensionFLocal.class::isInstance).findAny().orElseThrow();
        var localSecondCall = extensionManager.getExtensions(ExtensionPointF.class)
           .filter(ExtensionFLocal.class::isInstance).findAny().orElseThrow();

        assertThat(globalFirstCall).isSameAs(globalSecondCall);
        assertThat(localFirstCall).isNotSameAs(localSecondCall);

    }


    @Test
    public void canRetrieveExtensionUsingExternalLoader() {
        assertThat(extensionManager.getExtension(ExtensionPointG.class)).isNotEmpty();
    }


    @Test
    public void singleExtensionCanBeInjectedIntoAnotherExtension() {
        var extension = extensionManager.getExtension(ExtensionPointH1.class);
        var assertion = assertThat(extension)
            .containsInstanceOf(ExtensionH1.class)
            .map(ExtensionH1.class::cast);
        assertion.map(ExtensionH1::injectedExtension).isNotEmpty();
        assertion.map(ExtensionH1::nonInjectedExtension).isEmpty();
    }


    @Test
    public void severalExtensionCanBeInjectedIntoAnotherExtension() {
        var extension = extensionManager.getExtension(ExtensionPointH1.class).orElseThrow();
        assertThat(extension).isInstanceOf(ExtensionH1.class);
        ExtensionH1 extensionH1 = (ExtensionH1) extension;
        assertThat(extensionH1.injectedList())
            .hasSize(2)
            .anyMatch(ExtensionH5_1.class::isInstance)
            .anyMatch(ExtensionH5_2.class::isInstance);
        assertThat(extensionH1.injectedSet())
            .hasSize(2)
            .anyMatch(ExtensionH5_1.class::isInstance)
            .anyMatch(ExtensionH5_2.class::isInstance);
        assertThat(extensionH1.injectedCollection())
            .hasSize(2)
            .anyMatch(ExtensionH5_1.class::isInstance)
            .anyMatch(ExtensionH5_2.class::isInstance);
        assertThat(extensionH1.nonParametrizedList())
            .isNull();
    }


    @Test
    public void extensionInjectionAcceptsDependencyLoops() {
        var extension = extensionManager.getExtension(ExtensionPointH3.class);
        assertThat(extension).containsInstanceOf(ExtensionH3.class);
        ExtensionH3 injection = (ExtensionH3) extension.orElseThrow();
        assertThat(injection.injectedExtension()).isInstanceOf(ExtensionH4.class);
        ExtensionH4 injected = (ExtensionH4) injection.injectedExtension();
        assertThat(injected.injectedExtension()).isSameAs(injection);
    }


    @Test
    public void extensionCanInjectListWithItself() {
        var extension = extensionManager.getExtension(ExtensionPointI.class);
        assertThat(extension).containsInstanceOf(ExtensionI1.class);
        ExtensionI1 injection = (ExtensionI1) extension.orElseThrow();
        System.out.println(injection.injectedList());
        assertThat(injection.injectedList())
            .hasSize(2)
            .anyMatch(ExtensionI1.class::isInstance)
            .anyMatch(ExtensionI2.class::isInstance);
        ;
    }
}
