package com.kundalini.gui

import com.kundalini.core.Environment
import com.kundalini.core.TestAccount
import com.kundalini.core.TestAccountStatus
import javafx.geometry.Insets
import javafx.scene.Parent
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import tornadofx.*
import java.awt.GraphicsEnvironment
import kotlin.coroutines.CoroutineContext


class MainView : View(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main
    private val controller: MainController by inject()
    override val root = stackpane {
        title = "kundalini"
        val (width, height) = with(GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.displayMode) {
            Pair(width, height)
        }
        setPrefSize(width.toDouble(), height.toDouble())
        hbox {
            setPrefSize(width.toDouble(), height.toDouble())
            vbox {
                spacing = 5.0
                padding = Insets(5.0)
                button("Add Account") {
                    hgrow = Priority.ALWAYS
                    maxWidth = Double.POSITIVE_INFINITY
                    action {
                        openInternalWindow(EditAccountFragment(controller))
                    }
                }
                button("Edit Account") {
                    hgrow = Priority.ALWAYS
                    maxWidth = Double.POSITIVE_INFINITY
                    disableProperty().set(true)
                    controller.selectedAccount.addListener { _, _, new ->
                        disableProperty().set(new == null)
                    }
                    action {
                        openInternalWindow(EditAccountFragment(controller, controller.selectedAccount.value), owner = this@stackpane)
                    }
                }
                button("Remove Account") {
                    hgrow = Priority.ALWAYS
                    maxWidth = Double.POSITIVE_INFINITY
                    disableProperty().set(true)
                    controller.selectedAccount.addListener { _, _, new ->
                        disableProperty().set(new == null)
                    }
                    action {
                        openInternalWindow(RemoveAccountFragment(controller, controller.selectedAccount.value!!), owner = this@stackpane)
                    }
                }
                button("Refresh") {
                    hgrow = Priority.ALWAYS
                    maxWidth = Double.POSITIVE_INFINITY
                    controller.isRefreshing.addListener { _, _, isRefreshing ->
                        disableProperty().set(isRefreshing)
                    }
                    action {
                        launch {
                            controller.refresh()
                        }
                    }
                }
            }
            tableview(controller.statuses) {
                vgrow = Priority.ALWAYS
                hgrow = Priority.ALWAYS
                smartResize()
                onSelectionChange {
                    controller.selectedAccount.setValue(it?.account)
                }
                launch {
                    val placeholderText = if (controller.getAccountCount() > 0) {
                        "Loading account statuses…"
                    } else {
                        "Add accounts to monitor to see their statuses here."
                    }
                    placeholder = label(placeholderText)
                }
                readonlyColumn("Environment", TestAccountStatus::environment)
                    .weightedWidth(1.0)
                    .centeredCellFormat()
                readonlyColumn("Username", TestAccountStatus::username)
                    .weightedWidth(1.0)
                    .centeredCellFormat()
                readonlyColumn("Login", TestAccountStatus::login)
                    .weightedWidth(1.0)
                    .booleanCellFormat()
                readonlyColumn("Data Blob", TestAccountStatus::dataBlob)
                    .weightedWidth(1.0)
                    .booleanCellFormat()
                readonlyColumn("Past Requests", TestAccountStatus::pastRequests)
                    .weightedWidth(1.0)
                    .booleanCellFormat()
            }
        }
    }

    override fun onDock() {
        super.onDock()
        launch {
            controller.monitorStatuses()
        }
    }

    override fun onUndock() {
        coroutineContext.cancel()
        super.onUndock()
    }

}

class EditAccountFragment(
    private val controller: MainController,
    initialAccount: TestAccount? = TestAccount()
) : Fragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main
    override val root: Parent

    init {
        val account = initialAccount?: TestAccount()
        val id = account.id
        val usernameProperty = account.username.toProperty()
        val passwordProperty = account.password.toProperty()
        val environmentGroup = ToggleGroup()
        root = form {
            fieldset {
                field("Username") {
                    textfield().bind(usernameProperty)
                }
                field("Password") {
                    passwordfield().bind(passwordProperty)
                }
                field("Environment") {
                    radiobutton("E1", environmentGroup) {
                        userData = Environment.E1
                        isSelected = account.environment == userData
                    }
                    radiobutton("E2", environmentGroup) {
                        userData = Environment.E2
                        isSelected = account.environment == userData
                    }
                    radiobutton("E3", environmentGroup) {
                        userData = Environment.E3
                        isSelected = account.environment == userData
                    }
                }
                button("Add") {
                    action {
                        launch {
                            controller.saveAccount(
                                id,
                                usernameProperty.value,
                                passwordProperty.value,
                                environmentGroup.selectedToggle.userData as Environment
                            )
                            close()
                        }
                    }
                }
            }
        }
    }

    override fun onUndock() {
        coroutineContext.cancel()
        super.onUndock()
    }
}

class RemoveAccountFragment(
    private val controller: MainController,
    private val account: TestAccount
) : Fragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main
    override val root = vbox {
        padding = Insets(5.0)
        spacing = 5.0
        label("Are you sure you want to remove ${account.username}?")
        hbox {
            button("Cancel") {
                action {
                    close()
                }
            }
            region {
                hgrow = Priority.ALWAYS
            }
            button("Remove") {
                action {
                    launch {
                        controller.deleteAccount(account)
                        close()
                    }
                }
            }
        }
    }

    override fun onUndock() {
        coroutineContext.cancel()
        super.onUndock()
    }
}
