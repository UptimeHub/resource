package uz.uptimehub.resourceapp.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import uz.uptimehub.core.exception.InvalidSortRule;
import uz.uptimehub.core.pagination.Filter;
import uz.uptimehub.core.pagination.FilteredSortedPaginatedRequest;

public abstract class CommonService<R, E, F extends Filter> {

    @Value( "${custom-header-names.auth.permissions}")
    String permissionsHeader;

    @Value( "${custom-header-names.auth.roles}")
    String roleHeader;

    public abstract E create(R request);

    @Transactional
    public abstract void update(E dto);

    public abstract Page<E> findAll(FilteredSortedPaginatedRequest<F, InvalidSortRule> filter);
}
