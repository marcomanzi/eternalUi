package com.octopus.eternalUi.domain.db;


public abstract class AbstractDomainAwareDataProvider<T extends Identifiable> extends AbstractDataProvider<T> {

    private Object domain;

    public void setDomain(Object domain) {
        this.domain = domain;
    }

    public Object getDomain() {
        return domain;
    }
}
