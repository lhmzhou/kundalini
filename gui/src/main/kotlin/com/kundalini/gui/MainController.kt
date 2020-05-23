package com.kundalini.gui

import com.kundalini.core.*
import com.sun.javafx.collections.ObservableListWrapper
import javafx.collections.SetChangeListener
import tornadofx.Controller

class MainController: Controller() {
    private val kundalini: kundalini = kundalini()
    val selectedAccount = AtomicObservableValue<TestAccount?>()
    val statuses = ObservableListWrapper<TestAccountStatus>(mutableListOf())
    val isRefreshing = kundalini.isRefreshing

    init {
        kundalini.statuses.addListener(SetChangeListener {
            statuses.apply {
                clear()
                addAll(it.set)
            }
        })
    }

    suspend fun getAccountCount() = kundalini.getAccountCount()

    suspend fun monitorStatuses() = kundalini.monitorStatuses()

    suspend fun refresh() = kundalini.refresh()

    suspend fun saveAccount(id: Int?, username: String, password: String, environment: Environment) = kundalini.saveAccount(TestAccount(
        id,
        username,
        password,
        environment
    ))

    suspend fun deleteAccount(account: TestAccount) = kundalini.deleteAccount(account)
}