
module jext.spring {

    requires jext;
    requires org.slf4j;

    requires spring.context;
    requires spring.beans;
    requires spring.core;

    uses jext.ExtensionLoader;

}