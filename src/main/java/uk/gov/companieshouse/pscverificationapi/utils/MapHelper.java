package uk.gov.companieshouse.pscverificationapi.utils;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import java.text.SimpleDateFormat;
import java.util.Map;
import tools.jackson.databind.PropertyNamingStrategy;

/**
 * Helper class for converting objects to property maps.
 */
public final class MapHelper {

    private MapHelper() {
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
    public static Map<String, Object> convertObject(final Object obj, final PropertyNamingStrategy namingStrategy) {
        if (mapper == null) {
            mapper = JsonMapper.builder()
                .findAndAddModules()
                .propertyNamingStrategy(namingStrategy)
                .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd"))
                .build();
        }

        return mapper.convertValue(obj, new TypeReference<>() {
        });
    }

}