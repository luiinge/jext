package jext.l10n;

import java.io.*;
import java.net.URL;
import java.nio.charset.*;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class LocalizedResource {

    private final String resource;
    private final Locale requestedLocale;

    protected final Module effectiveModule;
    protected final Locale effeciveLocale;
    protected final String effectiveResource;
    protected final URL url;


    public LocalizedResource(Module baseModule, String resource, Locale locale) {
        this(baseModule, resource, locale, Localization.DEFAULT);
    }


    public LocalizedResource(Module baseModule, String resource, Locale locale, Localization localization) {

        this.resource = resource;
        this.requestedLocale = locale;

        var l10nModule = extensionModule(baseModule,".l10n");
        var localeModule = extensionModule(baseModule, ".l10n."+locale.toLanguageTag());
        var candidateModules = Stream.of(localeModule, l10nModule, baseModule)
            .filter(Objects::nonNull)
            .collect(toList());

        Module effectiveModuleTmp = null;
        Locale effeciveLocaleTmp = null;
        String effectiveResourceTmp = null;
        URL urlTmp = null;


        for (var module : candidateModules) {
            for (var loc : localization.localeAlternativesFor(locale)) {
                String res = localization.resolve(resource, loc);
                urlTmp = module.getClassLoader().resources(res).findAny().orElse(null);
                if (urlTmp != null) {
                    effectiveModuleTmp = module;
                    effeciveLocaleTmp = loc;
                    effectiveResourceTmp = res;
                    break;
                }
            }
            if (urlTmp != null) {
                break;
            }
        }

        if (urlTmp == null) {
            throw new IllegalArgumentException(String.format(
                "Cannot locate a localized resource for module://%s/%s[%s]",
                baseModule.getName(),
                resource,
                locale
            ));
        }

        this.effeciveLocale = Objects.requireNonNull(effeciveLocaleTmp);
        this.effectiveModule = Objects.requireNonNull(effectiveModuleTmp);
        this.effectiveResource = Objects.requireNonNull(effectiveResourceTmp);
        this.url = Objects.requireNonNull(urlTmp);

    }



    public Locale requestedLocale() {
        return requestedLocale;
    }


    public Locale effeciveLocale() {
        return effeciveLocale;
    }


    public String resource() {
        return resource;
    }


    public URL url() {
        return url;
    }


    public InputStream open() throws IOException {
        return url.openStream();
    }

    public Reader reader() throws IOException {
        return reader(StandardCharsets.UTF_8);
    }

    public Reader reader(Charset charset) throws IOException {
        return new InputStreamReader(open(), charset);
    }


    private static Module extensionModule(Module module, String prefix) {
        if (module.getLayer() == null) {
            return null;
        }
        return module.getLayer().findModule(module+prefix).orElse(null);
    }

}
