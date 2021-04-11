import jext.l10n.*;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class TestLocalizedProperties {

    private static LocalizedProperties properties = Localization.properties(
        TestLocalizedProperties.class,
        "messages.properties"
    );

    private static Locale spanish = Localization.locale("es-ES");



    @Test
    public void existingPropertyCanBeRetrievedRaw() {
        assertThat(properties.property(spanish, "message.a"))
            .hasValue("Esto es un mensaje en español con número '{0}' {0}");
    }

    @Test
    public void existingPropertyCanBeRetrievedAsFormattedMessage() {
        assertThat(properties.message(spanish, "message.a", 78))
            .hasValue("Esto es un mensaje en español con número {0} 78");
    }

    @Test
    public void nonExistingPropertyReturnsEmptyOptional() {
        assertThat(properties.property(spanish, "message.b")).isEmpty();
    }

    @Test
    public void nonExistingLocaleUseFallbackLocale() {
        assertThat(properties.message(Localization.locale("fr"), "message.a", 55))
            .hasValue("This's a message in English with number {0} 55");
    }



}
