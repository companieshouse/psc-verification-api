package uk.gov.companieshouse.pscverificationapi.enumerations;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.util.Objects;

public class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource encodedResource) {
        final var encoded = Objects.requireNonNull(encodedResource);
        final var resource = Objects.requireNonNull(encoded.getResource());
        final var factory = new YamlPropertiesFactoryBean();

        factory.setResources(resource);

        final var properties = Objects.requireNonNull(factory.getObject());
        final var filename = Objects.requireNonNull(resource.getFilename());

        return new PropertiesPropertySource(filename, properties);
    }
}