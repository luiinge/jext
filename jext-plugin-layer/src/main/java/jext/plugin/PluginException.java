package jext.plugin;

public class PluginException extends RuntimeException {

    private static final long serialVersionUID = 2029625435486652147L;

    public PluginException(String message, Object... args) {
        super(String.format(message.replace("{}", "%s"), args));
    }


    public PluginException(Exception e) {
        super(e);
    }


}
