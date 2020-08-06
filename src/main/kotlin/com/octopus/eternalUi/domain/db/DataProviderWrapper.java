package com.octopus.eternalUi.domain.db;

import com.vaadin.flow.data.provider.Query;
import org.springframework.beans.BeanUtils;

import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class DataProviderWrapper<T extends Identifiable> extends com.vaadin.flow.data.provider.AbstractDataProvider<T, String> {

    private DataProvider<T> dataProvider;

    public DataProviderWrapper(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public boolean isInMemory() {
        return false;
    }

    @Override
    public int size(Query<T, String> query) {
        return dataProvider.count(dataProvider.getFilters());
    }

    @Override
    public Stream<T> fetch(Query<T, String> query) {
        return dataProvider.page(new Page(query.getOffset() / query.getLimit(), query.getLimit()), dataProvider.getFilters()).stream();
    }

    @Override
    public void refreshItem(T v) {
        super.refreshItem(dataProvider.find(v));
    }

    public DataProvider<T> getDataProvider() {
        return dataProvider;
    }
}
