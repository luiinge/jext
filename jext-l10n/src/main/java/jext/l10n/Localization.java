package jext.l10n;

import java.util.*;

public class Localization {

    public static Locale locale(String tag) {
        return Locale.forLanguageTag(tag.replace('_','-'));
    }

    public interface Resolver {
        String resolve(String requestedResource, Locale appliedLocale);
    }

    public static final Resolver defaultResolver = ((resource, locale) -> {
        String localeSegment = "_" + locale.toLanguageTag();
        int extensionIndex = resource.lastIndexOf('.');
        return  extensionIndex == -1 ?
           resource + localeSegment :
           resource.substring(0,extensionIndex) + localeSegment + resource.substring(extensionIndex)
        ;
    });


    public static final Localization DEFAULT = new Localization(
        defaultResolver, Locale.ENGLISH, false
    );



    public static LocalizedProperties properties(Class<?> clazz, String resource) {
        return new LocalizedProperties(clazz.getModule(), resource, DEFAULT);
    }

    public static LocalizedProperties properties(Module module, String resource) {
        return new LocalizedProperties(module, resource, DEFAULT);
    }



    private final Locale fallbackLocale;
    private final Resolver resolver;
    private final boolean strict;


    public Localization(Resolver resolver, Locale fallbackLocale, boolean strict) {
        this.resolver = resolver;
        this.fallbackLocale = fallbackLocale;
        this.strict = strict;
    }


    public Optional<Locale> fallbackLocale() {
        return Optional.ofNullable(fallbackLocale);
    }


    public Resolver resolver() {
        return resolver;
    }


    public boolean strict() {
        return strict;
    }


    public List<Locale> localeAlternativesFor(Locale requestedLocale) {
        if (strict()) {
            return List.of(requestedLocale);
        }
        List<Locale> alternatives = new ArrayList<>();
        alternatives.add(requestedLocale);
        if (isVariant(requestedLocale)) {
            alternatives.add(withoutVariant(requestedLocale));
            alternatives.add(withoutCountry(requestedLocale));
        } else if (isRegional(requestedLocale)) {
            alternatives.add(withoutCountry(requestedLocale));
        }
        if (fallbackLocale != null) {
            alternatives.add(fallbackLocale);
        }
        return Collections.unmodifiableList(alternatives);
    }


    public String resolve(String requestedResource, Locale appliedLocale) {
        return resolver.resolve(requestedResource, appliedLocale);
    }


    private static boolean isVariant(Locale locale) {
        return !locale.getDisplayVariant().isEmpty();
    }

    private static boolean isRegional(Locale locale) {
        return !locale.getDisplayCountry().isEmpty();
    }


    private static Locale withoutVariant(Locale locale) {
        return new Locale.Builder().setLocale(locale).setVariant(null).build();
    }


    private static Locale withoutCountry(Locale locale) {
        return new Locale.Builder().setLocale(locale).setVariant(null).setRegion(null).build();
    }




}
