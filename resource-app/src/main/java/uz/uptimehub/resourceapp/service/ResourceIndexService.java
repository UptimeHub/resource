package uz.uptimehub.resourceapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import uz.uptimehub.resourceapp.jpa.entity.resource.Resource;
import uz.uptimehub.resourceapp.jpa.repository.ResourceRepository;
import uz.uptimehub.resourceapp.jpa.repository.ResourceSearchRepository;
import uz.uptimehub.resourceapp.mapper.ResourceMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceIndexService {

    private final ResourceRepository repository;
    private final ResourceSearchRepository searchRepository;
    private final ResourceMapper mapper;

    public void indexById(UUID resourceId) {
        repository.findByIdWithResourceType(resourceId)
                .map(mapper::toDocument)
                .ifPresentOrElse(
                        searchRepository::save,
                        () -> {
                            log.warn("Resource not found during indexing. resourceId={}", resourceId);
                            searchRepository.deleteById(resourceId.toString());
                        }
                );

    }

    public void deleteById(UUID resourceId) {
        searchRepository.deleteByResourceId(resourceId);
    }

    public long reindexAll() {
        searchRepository.deleteAll();

        long indexedCount = 0;
        int pageNumber = 0;
        Page<Resource> page;

        do {
            page = repository.findAllBy(PageRequest.of(pageNumber, 500));
            searchRepository.saveAll(page.map(mapper::toDocument));
            indexedCount += page.getNumberOfElements();
            pageNumber++;
        } while (page.hasNext());

        log.info("Reindexed resources. indexedCount={}", indexedCount);
        return indexedCount;
    }
}
