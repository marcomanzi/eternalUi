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

@Route("billing")
@UIScope
@Component
class BillingView(@Autowired var billing: Billing): VaadinActuator<BillingDomain>(billing)

@Component
class Billing(@Autowired var billingController: BillingController): com.octopus.eternalUi.domain.Page<BillingDomain>(
        OnLeftMenuTemplate(
                Label("Billing Control")
        ),
        billingController,
        PageDomain(BillingDomain())
)

@Service
class BillingController: PageController<BillingDomain>()

class BillingDomain: EmptyDomain()
