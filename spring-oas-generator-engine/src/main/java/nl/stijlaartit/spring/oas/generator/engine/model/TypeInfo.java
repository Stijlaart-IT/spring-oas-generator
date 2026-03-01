package nl.stijlaartit.spring.oas.generator.engine.model;

import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
import nl.stijlaartit.spring.oas.generator.domain.file.TypeDescriptor;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.ObjectProperty;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public sealed interface TypeInfo permits TypeInfo.BasicTypeInfo, TypeInfo.CompositeTypeInfo, TypeInfo.UnionTypeInfo {

    TypeDescriptor typeDescriptor();

    record BasicTypeInfo(TypeDescriptor typeDescriptor) implements TypeInfo {
        public BasicTypeInfo {
            Objects.requireNonNull(typeDescriptor);
        }
    }

    record CompositeTypeInfo(TypeDescriptor typeDescriptor, Optional<CompositeProperties> properties) implements TypeInfo {
        public CompositeTypeInfo {
            Objects.requireNonNull(typeDescriptor);
            Objects.requireNonNull(properties);
        }
    }

    record UnionTypeInfo(TypeDescriptor typeDescriptor,
                         @Nullable String discriminatorProperty,
                         List<UnionVariantInfo> variants) implements TypeInfo {
        public UnionTypeInfo {
            Objects.requireNonNull(typeDescriptor);
            variants = List.copyOf(variants);
        }
    }

    record CompositeProperties(Map<String, ObjectProperty> properties, Set<String> requiredProperties) {
        public CompositeProperties {
            Objects.requireNonNull(properties);
            Objects.requireNonNull(requiredProperties);
            properties = new LinkedHashMap<>(properties);
            requiredProperties = new LinkedHashSet<>(requiredProperties);
        }
    }

    record UnionVariantInfo(JavaTypeName modelName,
                            @Nullable String discriminatorValue) {
        public UnionVariantInfo {
            Objects.requireNonNull(modelName);
        }
    }
}
