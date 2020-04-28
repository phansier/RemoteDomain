package ru.beryukhov.remote_domain.main

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import ru.beryukhov.common.model.Entity

interface MainView : MvpView {
    @AddToEndSingle
    fun updateEntityUI(entity: Entity)
}