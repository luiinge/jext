import jext.l10n.Localization;
import org.junit.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class TestLocalization {

    @Test
    public void resolveResourceNameWithFileExtension() {
        assertThat(
            Localization.defaultResolver.resolve("messages.xml", Localization.locale("es"))
        ).isEqualTo("messages_es.xml");
        assertThat(
            Localization.defaultResolver.resolve("messages.xml", Localization.locale("es_ES"))
        ).isEqualTo("messages_es-ES.xml");
    }


    @Test
    public void resolveResourceNameWithoutFileExtension() {
        assertThat(
            Localization.defaultResolver.resolve("assets", Localization.locale("es"))
        ).isEqualTo("assets_es");
        assertThat(
            Localization.defaultResolver.resolve("assets", Localization.locale("es_ES"))
        ).isEqualTo("assets_es-ES");
    }

    @Test
    public void alternativeResourcesIncludeNonRegionalAndFallbackLocales() {
        assertThat(
            Localization.DEFAULT.localeAlternativesFor(Localization.locale("th-TH"))
        ).containsExactly(
            Localization.locale("th-TH"),
            Localization.locale("th"),
            Locale.ENGLISH
        );
    }
}
