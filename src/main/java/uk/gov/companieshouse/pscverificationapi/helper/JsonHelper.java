package uk.gov.companieshouse.pscverificationapi.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.text.SimpleDateFormat;
import java.util.Map;

public final class JsonHelper {

    private JsonHelper() {
        // intentionally blank
    }

    private static ObjectMapper mapper = null;

    /**
     * Convert an Object into a Key/Value property map.
     *
     * @param obj            the Object
     * @param namingStrategy the property naming strategy
     * @return a Map of property values
     */
    public static Map<String, Object> convertObject(final Object obj,
            final PropertyNamingStrategy namingStrategy) {
        initialiseMapper(namingStrategy);

        return mapper.convertValue(obj, new TypeReference<>() {
        });
    }

    private static void initialiseMapper(PropertyNamingStrategy namingStrategy) {
        if (mapper == null) {
            mapper = new ObjectMapper().registerModule(new JavaTimeModule())
                    .setPropertyNamingStrategy(namingStrategy)
                    .setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        }
    }

    public static <T>T convertLinkedHashmap(final Object hashMap, final PropertyNamingStrategy namingStrategy,
                                            Class<T> clazz) {
        initialiseMapper(namingStrategy);

        return mapper.convertValue(hashMap, clazz);
    }

}