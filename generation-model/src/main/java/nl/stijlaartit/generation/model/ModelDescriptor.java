package nl.stijlaartit.generation.model;

import java.util.List;

public sealed interface ModelDescriptor permits RecordDescriptor, EnumDescriptor, OneOfDescriptor {

    String name();

    List<String> dependencies();

    static RecordDescriptor record(String name, List<FieldDescriptor> fields) {
        return new RecordDescriptor(name, fields, List.of());
    }

    static RecordDescriptor record(String name, List<FieldDescriptor> fields, List<String> implementsTypes) {
        return new RecordDescriptor(name, fields, implementsTypes);
    }

    static EnumDescriptor enumModel(String name, List<String> enumValues, EnumValueType enumValueType) {
        return new EnumDescriptor(name, enumValues, enumValueType, List.of());
    }

    static EnumDescriptor enumModel(String name, List<String> enumValues, EnumValueType enumValueType,
                                    List<String> implementsTypes) {
        return new EnumDescriptor(name, enumValues, enumValueType, implementsTypes);
    }

    static OneOfDescriptor oneOf(String name, List<String> variantModels, String discriminatorProperty) {
        return new OneOfDescriptor(name, variantModels, discriminatorProperty);
    }
}
