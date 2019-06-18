package com.octopus.eternalUi.domain.db;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface DataProvider<T extends Identifiable> {

    int count(Map<String, String> filters);

    List<T> page(Page page, Map<String, String> filters);

    T find(String id);

    void addFilter(String name, String value);

    Map<String, String> getFilters();

    void removeFilter(String name);

    Collection<T> list();
}
