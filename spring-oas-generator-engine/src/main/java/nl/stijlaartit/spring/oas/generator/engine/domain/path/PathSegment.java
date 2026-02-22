package nl.stijlaartit.spring.oas.generator.engine.domain.path;

public sealed interface PathSegment permits PathSegment.AdditionalProperties, PathSegment.Items, PathSegment.Property, PathSegment.SingletonVariant, PathSegment.Variant {

    static PathSegment variant(String type, int index) {
        return new Variant(type, index);
    }
   static PathSegment singletonVariant(String type) {
        return new SingletonVariant(type);
    }

    static PathSegment property(String key) {
        return new Property(key);
    }

    static PathSegment items() {
        return new Items();
    }

    static PathSegment additionalProperties() {
        return new AdditionalProperties();
    }

    String displayName();

    record Property(String value) implements PathSegment {
        @Override
        public String displayName() {
            return value;
        }
    }

    record Variant(String type, int index) implements PathSegment {
        @Override
        public String displayName() {
            return type + index;
        }
    }

    record SingletonVariant(String type) implements PathSegment {
        @Override
        public String displayName() {
            return "";
        }
    }

    record Items() implements PathSegment {
        @Override
        public String displayName() {
             return "Item";
        }
    }

    record AdditionalProperties() implements PathSegment {
        @Override
        public String displayName() {
            return "AdditionalProperties";
        }
    }
}
