package nl.stijlaartit.generator.engine.model;

import java.util.Objects;

public sealed interface TypeDescriptor permits TypeDescriptor.ComplexType, TypeDescriptor.ListType, TypeDescriptor.MapType, TypeDescriptor.SimpleType {

    record SimpleType(String qualifiedName) implements TypeDescriptor {
        public SimpleType {
            Objects.requireNonNull(qualifiedName);
        }
    }

    record ComplexType(String modelName) implements TypeDescriptor {
        public ComplexType {
            Objects.requireNonNull(modelName);
        }
    }

    record ListType(TypeDescriptor elementType) implements TypeDescriptor {
        public ListType {
            Objects.requireNonNull(elementType);
        }
    }

    record MapType(TypeDescriptor valueType) implements TypeDescriptor {
        public MapType {
            Objects.requireNonNull(valueType);
        }
    }

    static SimpleType simple(String qualifiedName) {
        return new SimpleType(qualifiedName);
    }

    static ComplexType complex(String modelName) {
        return new ComplexType(modelName);
    }

    static ListType list(TypeDescriptor elementType) {
        return new ListType(elementType);
    }

    static MapType map(TypeDescriptor valueType) {
        return new MapType(valueType);
    }
}
