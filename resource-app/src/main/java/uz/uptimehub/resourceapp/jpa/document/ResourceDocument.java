package uz.uptimehub.resourceapp.jpa.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "resources")
public class ResourceDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private UUID resourceId;

    @Field(type = FieldType.Keyword)
    private UUID organizationId;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "standard"),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String name;

    @Field(type = FieldType.Long)
    private Long resourceTypeId;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword)
    private String status;

    @Builder.Default
    @Field(type = FieldType.Object)
    private Map<String, String> customCharacteristics = new HashMap<>();

    @Builder.Default
    @Field(type = FieldType.Object)
    private Map<String, Object> specificationValues = new HashMap<>();

    @Field(type = FieldType.Text)
    private String searchableText;

    @Field(type = FieldType.Date)
    private LocalDateTime indexedAt;

}
