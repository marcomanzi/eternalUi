package com.octopus.eternalUi.domain.db;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface DataProvider {

    int count(Map<String, Object> filters);

    List<Object> page(Page page, Map<String, Object> filters);

    Object find(Object id);

    void addFilter(String name, Object value);

    Map<String, Object> getFilters();

    void removeFilter(String name);

    Collection<Object> list();
}
