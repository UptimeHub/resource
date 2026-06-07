package uz.uptimehub.resourceapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;
import uz.uptimehub.resourceapp.exception.ElasticsearchUnavailableException;
import uz.uptimehub.resourceapp.jpa.document.ResourceDocument;
import uz.uptimehub.resourceapp.jpa.entity.resource.Resource;
import uz.uptimehub.resourceapp.jpa.repository.ResourceRepository;
import uz.uptimehub.resourceapp.mapper.ResourceMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceIndexService {

    private final ResourceRepository repository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ResourceMapper mapper;

    public void indexById(UUID resourceId) {
        try {
            repository.findByIdWithResourceType(resourceId)
                    .map(mapper::toDocument)
                    .ifPresentOrElse(
                            elasticsearchOperations::save,
                            () -> {
                                log.warn("Resource not found during indexing. resourceId={}", resourceId);
                                elasticsearchOperations.delete(resourceId.toString(), ResourceDocument.class);
                            }
                    );
        } catch (DataAccessException ex) {
            log.warn("Failed to index resource in Elasticsearch. resourceId={}", resourceId, ex);
        }

    }

    public void deleteById(UUID resourceId) {
        try {
            elasticsearchOperations.delete(resourceId.toString(), ResourceDocument.class);
        } catch (DataAccessException ex) {
            log.warn("Failed to delete resource from Elasticsearch. resourceId={}", resourceId, ex);
        }
    }

    public long reindexAll() {
        try {
            recreateIndex();

            long indexedCount = 0;
            int pageNumber = 0;
            Page<Resource> page;

            do {
                page = repository.findAllBy(PageRequest.of(pageNumber, 500));
                elasticsearchOperations.save(page.map(mapper::toDocument));
                indexedCount += page.getNumberOfElements();
                pageNumber++;
            } while (page.hasNext());

            log.info("Reindexed resources. indexedCount={}", indexedCount);
            return indexedCount;
        } catch (DataAccessException ex) {
            throw new ElasticsearchUnavailableException("Elasticsearch is unavailable. Reindex could not be completed.", ex);
        }
    }

    private void recreateIndex() {
        IndexOperations indexOperations = elasticsearchOperations.indexOps(ResourceDocument.class);

        if (indexOperations.exists()) {
            indexOperations.delete();
        }

        indexOperations.create();
        indexOperations.putMapping(indexOperations.createMapping(ResourceDocument.class));
    }
}
