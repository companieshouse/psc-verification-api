package uk.gov.companieshouse.pscverificationapi.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.text.SimpleDateFormat;
public final class JsonHelper {

    private JsonHelper() {
        // intentionally blank
    }

    private static ObjectMapper mapper = null;

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