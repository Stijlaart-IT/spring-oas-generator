package nl.stijlaartit.generator.engine.model;

import java.util.List;

public sealed interface ModelDescriptor permits RecordDescriptor, EnumDescriptor, OneOfDescriptor {

    String name();

    static RecordDescriptor record(String name, List<FieldDescriptor> fields) {
        return new RecordDescriptor(name, fields, List.of());
    }

    static RecordDescriptor record(String name, List<FieldDescriptor> fields, List<String> implementsTypes) {
        return new RecordDescriptor(name, fields, implementsTypes);
    }
}
