package nl.stijlaartit.generation.model;

import java.util.List;

public sealed interface ModelDescriptor permits RecordDescriptor, EnumDescriptor {

    String name();

    List<String> dependencies();

    static RecordDescriptor record(String name, List<FieldDescriptor> fields) {
        return new RecordDescriptor(name, fields);
    }

    static EnumDescriptor enumModel(String name, List<String> enumValues, EnumValueType enumValueType) {
        return new EnumDescriptor(name, enumValues, enumValueType);
    }
}
