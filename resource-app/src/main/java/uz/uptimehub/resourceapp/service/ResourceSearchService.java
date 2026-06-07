package uz.uptimehub.resourceapp.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
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

@Service
@RequiredArgsConstructor
public class ResourceSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ResourceMapper resourceMapper;

    public Page<ResourceDto> search(ResourceFilter filter, Pageable pageable) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(buildQuery(filter))
                .withPageable(normalizePageable(pageable))
                .build();

        try {
            SearchHits<ResourceDocument> hits = elasticsearchOperations.search(query, ResourceDocument.class);
            List<ResourceDto> content = hits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .map(resourceMapper::toDto)
                    .toList();

            return new PageImpl<>(content, pageable, hits.getTotalHits());
        } catch (DataAccessException ex) {
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
        if (pageable == null || pageable.isUnpaged() || pageable.getSort().isUnsorted()) {
            return pageable;
        }

        List<Sort.Order> orders = pageable.getSort().stream()
                .map(order -> new Sort.Order(order.getDirection(), normalizeSortProperty(order.getProperty())))
                .toList();

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
    }

    private String normalizeSortProperty(String property) {
        if (property == null) {
            return property;
        }
        if (property.startsWith("r.")) {
            property = property.substring(2);
        }
        if ("typeId".equals(property)) {
            return "resourceTypeId";
        }
        if ("name".equals(property)) {
            return "name.keyword";
        }
        return property;
    }
}
