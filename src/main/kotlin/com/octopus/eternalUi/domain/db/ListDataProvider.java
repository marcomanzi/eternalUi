package com.octopus.eternalUi.domain.db;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ListDataProvider<T extends Identifiable> extends AbstractDataProvider<T> {

    private List<T> elements;

    @SuppressWarnings("unchecked")
    public ListDataProvider(List<String> items) {
        elements = (List<T>) items.stream().map(i -> (Identifiable) new Message(i)).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public ListDataProvider(String... items) {
        elements = (List<T>) Arrays.stream(items).map(i -> (Identifiable) new Message(i)).collect(Collectors.toList());
    }

    public ListDataProvider(T... items) {
        elements = Arrays.stream(items).collect(Collectors.toList());
    }

    @Override
    public int count(Map<String, Object> filters) {
        return elements.size();
    }

    @Override
    public List<T> page(Page page, Map<String, Object> filters) {
        int fromIndex = page.page;
        int toIndex = fromIndex + page.size;
        if (toIndex > count(filters)) return elements;
        else return elements.subList(fromIndex, toIndex);
    }

    @Override
    public T find(String id) {
        return elements.stream().filter(e -> e.getUiId().equals(id)).findFirst().get();
    }

    @Override
    public void addFilter(String name, Object value) {
        super.addFilter(name, value);
    }

    public List<T> getElements() {
        return elements;
    }
}
