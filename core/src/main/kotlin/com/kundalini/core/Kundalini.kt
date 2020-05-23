package com.kundalini.core

import com.sun.javafx.collections.ObservableSetWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.*
import java.util.concurrent.TimeUnit
import javax.persistence.Persistence
import kotlin.coroutines.CoroutineContext

class Kundalini : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main
    private val factory = Persistence.createEntityManagerFactory("com.kundalini")
    private val repository = H2TestAccountRepository(factory.createEntityManager())
    private val apiService = OkHttpApiService()
    val isRefreshing = AtomicObservableValue(false)
    val statuses = ObservableSetWrapper<TestAccountStatus>(TreeSet())

    suspend fun getAccountCount() = repository.countAll()

    suspend fun monitorStatuses() {
        while (true) {
            isRefreshing.value = true
            repository.findAll().forEach {
                checkStatus(it)
            }
            isRefreshing.value = false
            delay(TimeUnit.HOURS.toMillis(1))
            if (!coroutineContext.isActive) break
        }
    }

    suspend fun refresh() {
        isRefreshing.value = true
        repository.findAll().forEach {
            checkStatus(it)
        }
        isRefreshing.value = false
    }

    suspend fun checkStatus(account: TestAccount) {
        statuses.replace(TestAccountStatus(account = account))
        val session = apiService.login(account.username, account.password, account.environment)
        if (session.isNullOrBlank()) {
            statuses.replace(TestAccountStatus(account, login = false, dataBlob = false, pastRequests = false))
            return
        } else {
            statuses.replace(TestAccountStatus(account, login = true))
        }
        val contentBoard = apiService.dataBlob(session, account.environment)
        if (contentBoard.isEmpty()) {
            statuses.replace(TestAccountStatus(account, login = true, dataBlob = false, pastRequests = false))
            return
        } else {
            statuses.replace(TestAccountStatus(account, login = true, dataBlob = true))
        }
        val pastRequests = mutableListOf<Boolean>()
        contentBoard.forEach {
            pastRequests.add(apiService.getInformation(session, it, account.environment))
        }
        statuses.replace(
            TestAccountStatus(
            account,
            login = true,
            dataBlob = true,
            pastRequests = pastRequests.any { it } // TODO: show partial status to note when some contentBoard are working and some aren't
        ))
    }

    suspend fun saveAccount(account: TestAccount) {
        val updated = if (account.id == null) repository.create(account)
        else repository.update(account)
        checkStatus(updated)
    }

    suspend fun deleteAccount(account: TestAccount) {
        repository.delete(account)
        statuses.removeIf { it.account == account  }
    }
}
