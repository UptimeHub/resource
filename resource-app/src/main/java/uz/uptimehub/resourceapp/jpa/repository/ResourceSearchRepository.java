package uz.uptimehub.resourceapp.jpa.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import uz.uptimehub.resourceapp.jpa.document.ResourceDocument;

import java.util.UUID;

public interface ResourceSearchRepository extends ElasticsearchRepository<ResourceDocument, String> {

    void deleteByResourceId(UUID resourceId);
}
