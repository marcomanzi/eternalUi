package com.octopus.eternalUi.domain.db;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ListDataProvider extends AbstractDataProvider {

    private List elements;
    private BiFunction<Object, Map<String, Object>, Boolean> listFilter = (v, filters) -> true;

    @SuppressWarnings("unchecked")
    public ListDataProvider(List<String> items) {
        elements = (List) items.stream().map(i -> new Message(i)).collect(Collectors.toList());
    }

    public ListDataProvider(List<String> items, BiFunction<Object, Map<String, Object>, Boolean> listFilter) {
        elements = (List) items.stream().map(i -> new Message(i)).collect(Collectors.toList());
        this.listFilter = listFilter;
    }

    @SuppressWarnings("unchecked")
    public ListDataProvider(String... items) {
        elements = (List) Arrays.stream(items).map(i -> new Message(i)).collect(Collectors.toList());
    }

    public ListDataProvider(BiFunction<Object, Map<String, Object>, Boolean> listFilter, String... items) {
        elements = (List) Arrays.stream(items).map(i -> new Message(i)).collect(Collectors.toList());
        this.listFilter = listFilter;
    }

    public ListDataProvider(Object... items) {
        elements = Arrays.stream(items).collect(Collectors.toList());
    }

    public ListDataProvider(BiFunction<Object, Map<String, Object>, Boolean> listFilter, Object... items) {
        elements = Arrays.stream(items).collect(Collectors.toList());
        this.listFilter = listFilter;
    }

    @Override
    public int count(Map<String, Object> filters) {
         return ((Long) elements.stream().filter(e -> listFilter.apply(e, filters)).count()).intValue();
    }

    @Override
    public List page(Page page, Map<String, Object> filters) {
        int fromIndex = page.page;
        int toIndex = fromIndex + page.size;
        List filteredItems = (List) elements.stream().filter(e -> listFilter.apply(e, filters)).collect(Collectors.toList());
        if (toIndex > filteredItems.size()) return filteredItems;
        else return filteredItems.subList(fromIndex, toIndex);
    }

    @Override
    public Object find(Object id) {
        return elements.stream().filter(e -> e.equals(id)).findFirst().get();
    }

    @Override
    public void addFilter(String name, Object value) {
        super.addFilter(name, value);
    }

    public List getElements() {
        return elements;
    }

    public void addElement(Object element) {
        elements.add(element);
    }

    public void removeElement(Object element) {
        elements.remove(element);
    }
}
