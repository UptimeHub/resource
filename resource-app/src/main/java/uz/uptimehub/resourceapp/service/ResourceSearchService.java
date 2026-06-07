package uz.uptimehub.resourceapp.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import uz.uptimehub.resource.dto.resource.ResourceDto;
import uz.uptimehub.resource.dto.resource.ResourceFilter;
import uz.uptimehub.resourceapp.exception.ElasticsearchUnavailableException;
import uz.uptimehub.resourceapp.jpa.document.ResourceDocument;
import uz.uptimehub.resourceapp.mapper.ResourceMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ResourceMapper resourceMapper;

    public Page<ResourceDto> search(ResourceFilter filter, Pageable pageable) {
        ensureIndexExists();

        Pageable elasticsearchPageable = normalizePageable(pageable);
        NativeQuery query = NativeQuery.builder()
                .withQuery(buildQuery(filter))
                .withPageable(elasticsearchPageable)
                .build();

        try {
            SearchHits<ResourceDocument> hits = elasticsearchOperations.search(query, ResourceDocument.class);
            List<ResourceDto> content = hits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .map(resourceMapper::toDto)
                    .toList();

            return new PageImpl<>(content, pageable, hits.getTotalHits());
        } catch (DataAccessException ex) {
            log.warn("Elasticsearch resource search failed. rootCause={}", rootCauseMessage(ex), ex);
            throw new ElasticsearchUnavailableException("Elasticsearch is unavailable. Resource search could not be completed.", ex);
        }
    }

    public boolean supports(ResourceFilter filter) {
        return filter != null && filter.getSearch() != null && !filter.getSearch().isBlank();
    }

    private Query buildQuery(ResourceFilter filter) {
        List<Query> filters = new ArrayList<>();

        if (filter.getId() != null) {
            filters.add(termQuery("resourceId", filter.getId().toString()));
        }
        if (filter.getOrganizationId() != null) {
            filters.add(termQuery("organizationId", filter.getOrganizationId().toString()));
        }
        if (filter.getResourceTypeId() != null) {
            filters.add(Query.of(q -> q.term(t -> t.field("resourceTypeId").value(filter.getResourceTypeId()))));
        }
        if (filter.getStatus() != null) {
            filters.add(termQuery("status", filter.getStatus().name()));
        }
        if (filter.getName() != null && !filter.getName().isBlank()) {
            filters.add(Query.of(q -> q.match(m -> m.field("name").query(filter.getName()))));
        }

        Query searchQuery = Query.of(q -> q.multiMatch(m -> m
                .query(filter.getSearch())
                .fields("name^3", "description^2", "searchableText")
        ));

        return Query.of(q -> q.bool(b -> b.must(searchQuery).filter(filters)));
    }

    private Query termQuery(String field, String value) {
        return Query.of(q -> q.term(t -> t.field(field).value(value)));
    }

    private Pageable normalizePageable(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return pageable;
        }

        List<Sort.Order> orders = pageable.getSort().stream()
                .map(order -> normalizeSortProperty(order.getProperty())
                        .map(property -> new Sort.Order(order.getDirection(), property))
                )
                .flatMap(Optional::stream)
                .toList();

        Sort sort = orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private Optional<String> normalizeSortProperty(String property) {
        if (property == null) {
            return Optional.empty();
        }
        if (property.startsWith("r.")) {
            property = property.substring(2);
        }
        if ("id".equals(property)) {
            return Optional.of("resourceId");
        }
        if ("organizationId".equals(property)) {
            return Optional.of("organizationId");
        }
        if ("typeId".equals(property)) {
            return Optional.of("resourceTypeId");
        }
        if ("resourceTypeId".equals(property)) {
            return Optional.of("resourceTypeId");
        }
        if ("status".equals(property)) {
            return Optional.of("status");
        }
        if ("name".equals(property)) {
            return Optional.of("name.keyword");
        }
        if ("indexedAt".equals(property)) {
            return Optional.of("indexedAt");
        }

        log.debug("Ignoring unsupported Elasticsearch sort property: {}", property);
        return Optional.empty();
    }

    private void ensureIndexExists() {
        try {
            IndexOperations indexOperations = elasticsearchOperations.indexOps(ResourceDocument.class);
            if (!indexOperations.exists()) {
                indexOperations.create();
                indexOperations.putMapping(indexOperations.createMapping(ResourceDocument.class));
            }
        } catch (DataAccessException ex) {
            throw new ElasticsearchUnavailableException("Elasticsearch is unavailable. Resource search could not be completed.", ex);
        }
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getMessage();
    }
}
