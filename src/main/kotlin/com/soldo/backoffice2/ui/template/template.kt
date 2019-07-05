package com.soldo.backoffice2.ui.template

import com.octopus.eternalUi.domain.HorizontalContainer
import com.octopus.eternalUi.domain.InsideAppLink
import com.octopus.eternalUi.domain.UIComponent
import com.octopus.eternalUi.domain.VerticalContainer
import com.soldo.backoffice2.ui.views.BillingView
import com.soldo.backoffice2.ui.views.BusinessSearchView
import com.vaadin.flow.component.orderedlayout.VerticalLayout

class OnLeftMenuTemplate(vararg uiComponents: UIComponent): HorizontalContainer("onLeftMenuTemplate", Menu(), VerticalContainer("pageGroup", *uiComponents)) {
    init {
        setStyle { vaadinActuator ->
            vaadinActuator.getComponentById("menuContainer").apply { (this as VerticalLayout).let { verticalLayout ->
                verticalLayout.width = "330px"
                verticalLayout.className = "SoldoMenu"
            }}

            vaadinActuator.getComponentById("pageGroup").apply { (this as VerticalLayout).let { verticalLayout ->
                verticalLayout.className = "SoldoContent"
            }}
        }
    }
}

class Menu: VerticalContainer("menuContainer",
        InsideAppLink("recurringBilling", BillingView::class.java),
        InsideAppLink("businessAccountManagement", BusinessSearchView::class.java))