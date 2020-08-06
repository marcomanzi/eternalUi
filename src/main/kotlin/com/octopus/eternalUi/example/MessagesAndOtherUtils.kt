package com.octopus.eternalUi.example

import com.octopus.eternalUi.domain.EmptyDomain
import com.octopus.eternalUi.domain.Page
import com.octopus.eternalUi.domain.PageDomain
import com.octopus.eternalUi.domain.VerticalContainer
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class MessagesAndOtherUtils: Page<EmptyDomain>(
        VerticalContainer(),
        pageDomain = PageDomain(EmptyDomain())
) {
}