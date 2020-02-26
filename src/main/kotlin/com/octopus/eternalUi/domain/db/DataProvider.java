package com.octopus.eternalUi.domain.db;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface DataProvider<T extends Identifiable> {

    int count(Map<String, Object> filters);

    List<T> page(Page page, Map<String, Object> filters);

    T find(String id);

    void addFilter(String name, Object value);

    Map<String, Object> getFilters();

    void removeFilter(String name);

    Collection<T> list();
}
