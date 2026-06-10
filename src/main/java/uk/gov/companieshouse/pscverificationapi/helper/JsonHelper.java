package uk.gov.companieshouse.pscverificationapi.helper;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import java.text.SimpleDateFormat;
import tools.jackson.databind.PropertyNamingStrategy;

/**
 * Helper class for JSON conversion and mapping.
 * <p>
 * Provides utility methods to convert objects using a configured {@link ObjectMapper}
 * with custom naming strategies and date formats.
 * </p>
 */
public final class JsonHelper {

    private JsonHelper() {
        // intentionally blank
    }

    private static ObjectMapper mapper = null;

    private static void initialiseMapper(PropertyNamingStrategy namingStrategy) {
        if (mapper == null) {
            mapper = JsonMapper.builder()
                .propertyNamingStrategy(namingStrategy)
                .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd"))
                .build();
        }
    }

    public static <T>T convertLinkedHashmap(final Object hashMap, final PropertyNamingStrategy namingStrategy,
                                            Class<T> clazz) {
        initialiseMapper(namingStrategy);

        return mapper.convertValue(hashMap, clazz);
    }

}