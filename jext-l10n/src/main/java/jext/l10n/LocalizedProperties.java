package jext.l10n;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;

public class LocalizedProperties {

    private static Map<Pattern, String> messageQuoteTransformation = Map.of(
        Pattern.compile("([^}])'([^{])"),"$1''$2",
        Pattern.compile("^'([^{])"),     "''$1",
        Pattern.compile("([^}])'$"),     "$1''"
    );


    private final Map<Locale, Properties> cache = new HashMap<>();

    private final String resource;
    private final Module module;
    private final Localization localization;
    private final Map<Locale,Map<String,Optional<MessageFormat>>> formats = new HashMap<>();


    public LocalizedProperties(Module module, String resource, Localization localization) {
        this.module = module;
        this.resource = resource;
        this.localization = localization;
    }


    public Optional<String> property(Locale locale, String key) {
        return Optional.ofNullable(
            cache.computeIfAbsent(locale, this::obtainProperties).getProperty(key)
        );
    }


    public Optional<String> message(Locale locale, String key, Object... formatArgs) {
        return formats
            .computeIfAbsent(locale, it -> new HashMap<>())
            .computeIfAbsent(key, it -> messageFormat(locale, key))
            .map(it -> it.format(formatArgs));
    }



    public Properties obtainProperties(Locale locale) {
        try {
            Properties properties = new Properties();
               properties.load(
                new LocalizedResource(module, resource, locale, localization).reader()
            );
            return properties;
        } catch (IOException e) {
            return new Properties();
        }
    }


    private Optional<MessageFormat> messageFormat(Locale locale, String key) {
        return property(locale,key)
           .map(this::transformMessageQuotes)
           .map(MessageFormat::new);
    }

    private String transformMessageQuotes(String message) {
        StringBuilder output = new StringBuilder(message);
        messageQuoteTransformation.forEach((pattern, replace) ->
           output.replace(0,output.length(), pattern.matcher(output).replaceAll(replace))
        );
        return output.toString();
    }
}
