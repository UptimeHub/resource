package uz.uptimehub.resourceapp.jpa.entity.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SpecificationDefinition {

    private String name;
    private String label;
    private Boolean required;
    private Boolean searchable;
    private Boolean filterable;
    private DataType dataType;

    public enum DataType {
        TEXT, NUMBER, BOOLEAN
    }
}
