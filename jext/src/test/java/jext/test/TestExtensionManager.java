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
    public void scopeIsHonouredInMultipleCalls() {
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
}
