package com.octopus.eternalUi.domain.db;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ListDataProvider<T extends Identifiable> extends AbstractDataProvider<T> {

    private List<T> elements;
    private BiFunction<T, Map<String, Object>, Boolean> listFilter = (v, filters) -> true;

    @SuppressWarnings("unchecked")
    public ListDataProvider(List<String> items) {
        elements = (List<T>) items.stream().map(i -> (Identifiable) new Message(i)).collect(Collectors.toList());
    }

    public ListDataProvider(List<String> items, BiFunction<T, Map<String, Object>, Boolean> listFilter) {
        elements = (List<T>) items.stream().map(i -> (Identifiable) new Message(i)).collect(Collectors.toList());
        this.listFilter = listFilter;
    }

    @SuppressWarnings("unchecked")
    public ListDataProvider(String... items) {
        elements = (List<T>) Arrays.stream(items).map(i -> (Identifiable) new Message(i)).collect(Collectors.toList());
    }

    public ListDataProvider(BiFunction<T, Map<String, Object>, Boolean> listFilter, String... items) {
        elements = (List<T>) Arrays.stream(items).map(i -> (Identifiable) new Message(i)).collect(Collectors.toList());
        this.listFilter = listFilter;
    }

    public ListDataProvider(T... items) {
        elements = Arrays.stream(items).collect(Collectors.toList());
    }

    public ListDataProvider(BiFunction<T, Map<String, Object>, Boolean> listFilter, T... items) {
        elements = Arrays.stream(items).collect(Collectors.toList());
        this.listFilter = listFilter;
    }

    @Override
    public int count(Map<String, Object> filters) {
         return ((Long) elements.stream().filter(e -> listFilter.apply(e, filters)).count()).intValue();
    }

    @Override
    public List<T> page(Page page, Map<String, Object> filters) {
        int fromIndex = page.page;
        int toIndex = fromIndex + page.size;
        List<T> filteredItems = elements.stream().filter(e -> listFilter.apply(e, filters)).collect(Collectors.toList());
        if (toIndex > filteredItems.size()) return filteredItems;
        else return filteredItems.subList(fromIndex, toIndex);
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

    public void addElement(T element) {
        elements.add(element);
    }
}
