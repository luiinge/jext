import jext.test.ext.*;

module test {

    requires jext;
    requires junit;
    requires org.assertj.core;
    requires org.slf4j;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;

    exports jext.test to jext, junit;
    exports jext.test.ext to jext, junit;

    provides ExtensionPointA with ExtensionA;
    provides ExtensionPointB with ExtensionB1, ExtensionB2;
    provides ExtensionPointC with ExtensionC_1, ExtensionC_2_1, ExtensionC_3;
    provides ExtensionPointD with ExtensionDOverriden, ExtensionDOverrider;
    provides ExtensionPointE with
        ExtensionELowerPriority,
        ExtensionELowestPriority,
        ExtensionENormalPriority,
        ExtensionEHighestPriority,
        ExtensionEHigherPriority;
    provides ExtensionPointF with ExtensionFGlobal, ExtensionFLocal;
    provides ExtensionPointG with ExtensionGWithExternalLoader;


}