package com.soldo.backoffice2.ui.template

import com.octopus.eternalUi.domain.HorizontalContainer
import com.octopus.eternalUi.domain.InsideAppLink
import com.octopus.eternalUi.domain.UIComponent
import com.octopus.eternalUi.domain.VerticalContainer
import com.soldo.backoffice2.ui.views.BillingView
import com.soldo.backoffice2.ui.views.BusinessSearchView

class OnLeftMenuTemplate(vararg uiComponents: UIComponent): HorizontalContainer("onLeftMenuTemplate", Menu(), *uiComponents)

class Menu: VerticalContainer("menuContainer",
        InsideAppLink("billing", BillingView::class.java),
        InsideAppLink("business", BusinessSearchView::class.java))
