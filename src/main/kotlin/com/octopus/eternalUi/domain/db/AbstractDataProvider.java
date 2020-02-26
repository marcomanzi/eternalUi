package com.octopus.eternalUi.domain.db;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractDataProvider<T extends Identifiable> implements DataProvider<T> {

    protected Map<String, Object> filters = new HashMap<>();

    @Override
    public void addFilter(String name, Object value) {
        filters.put(name, value);
    }

    @Override
    public Map<String, Object> getFilters() {
        return filters;
    }

    @Override
    public void removeFilter(String name) {
        filters.remove(name);
    }

    @Override
    public Collection<T> list() {
        return page(new Page(0, 100000), filters);
    }
}
