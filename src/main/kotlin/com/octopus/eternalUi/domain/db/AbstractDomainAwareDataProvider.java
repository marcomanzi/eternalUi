package com.octopus.eternalUi.domain.db;


public abstract class AbstractDomainAwareDataProvider extends AbstractDataProvider {

    private Object domain;

    public void setDomain(Object domain) {
        this.domain = domain;
    }

    public Object getDomain() {
        return domain;
    }
}
