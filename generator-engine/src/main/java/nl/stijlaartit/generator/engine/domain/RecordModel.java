package nl.stijlaartit.generator.engine.domain;

import java.util.List;
import java.util.Objects;

public class RecordModel implements ModelFile {
    private final String name;
    private final List<FieldModel> fields;

    public RecordModel(String name, List<FieldModel> fields) {
        this.name = Objects.requireNonNull(name);
        this.fields = List.copyOf(fields);
    }

    @Override
    public String getName() {
        return name;
    }


    public List<FieldModel> getFields() {
        return fields;
    }
}
