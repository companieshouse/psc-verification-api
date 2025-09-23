package uk.gov.companieshouse.pscverificationapi.enumerations;

import java.util.Objects;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.lang.NonNull;

/**
 * PropertySourceFactory for loading YAML files as property sources.
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public @NonNull PropertySource<?> createPropertySource(String name,@NonNull EncodedResource encodedResource) {
        final var encoded = Objects.requireNonNull(encodedResource);
        final var resource = Objects.requireNonNull(encoded.getResource());
        final var factory = new YamlPropertiesFactoryBean();

        factory.setResources(resource);

        final var properties = Objects.requireNonNull(factory.getObject());
        final var filename = Objects.requireNonNull(resource.getFilename());

        return new PropertiesPropertySource(filename, properties);
    }
}
