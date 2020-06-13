module jext {

    exports jext;
    requires transitive java.compiler;
    requires org.slf4j;
    uses javax.annotation.processing.Processor;
    uses jext.ExtensionLoader;
    provides javax.annotation.processing.Processor with jext.ExtensionProcessor;
}