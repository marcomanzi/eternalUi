package com.soldo.backoffice2.ui.views

import com.octopus.eternalUi.domain.EmptyDomain
import com.octopus.eternalUi.domain.Label
import com.octopus.eternalUi.domain.PageController
import com.octopus.eternalUi.domain.PageDomain
import com.octopus.eternalUi.vaadinBridge.VaadinActuator
import com.soldo.backoffice2.ui.template.OnLeftMenuTemplate
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.annotation.UIScope
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Route("businessSearch")
@UIScope
@Component
class BusinessSearchView(@Autowired var billing: BusinessSearch): VaadinActuator<BusinessSearchDomain>(billing)

@Component
class BusinessSearch(@Autowired var billingController: BusinessSearchController): com.octopus.eternalUi.domain.Page<BusinessSearchDomain>(
        OnLeftMenuTemplate(
                Label("BusinessSearch")
        ),
        billingController,
        PageDomain(BusinessSearchDomain())
)

@Service
class BusinessSearchController: PageController<BusinessSearchDomain>()

class BusinessSearchDomain: EmptyDomain()
