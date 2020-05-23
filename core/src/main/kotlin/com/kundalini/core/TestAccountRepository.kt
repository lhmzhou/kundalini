package com.kundalini.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.persistence.EntityManager

interface TestAccountRepository {
    suspend fun countAll(): Long
    suspend fun findAll(): Collection<TestAccount>
    suspend fun findById(id: Int): TestAccount?
    suspend fun findByUsername(username: String): TestAccount?
    suspend fun findByEnvironment(environment: Environment): Collection<TestAccount>
    suspend fun create(testAccount: TestAccount): TestAccount
    suspend fun update(testAccount: TestAccount): TestAccount
    suspend fun delete(testAccount: TestAccount)
}

class H2TestAccountRepository(private val entityManager: EntityManager) : TestAccountRepository {

    override suspend fun countAll(): Long =
        entityManager.createQuery("SELECT COUNT(t) FROM TestAccount t").singleResult as? Long ?: 0

    override suspend fun findAll(): Collection<TestAccount> = withContext(Dispatchers.IO) {
        entityManager.createQuery("SELECT t FROM TestAccount t", TestAccount::class.java).resultList
    }

    override suspend fun findById(id: Int): TestAccount? = withContext(Dispatchers.IO) {
        entityManager.find(TestAccount::class.java, id)
    }

    override suspend fun findByUsername(username: String): TestAccount? = withContext(Dispatchers.IO) {
        try {
            entityManager.createQuery(
                "SELECT t FROM TestAccount t WHERE t.username = :username",
                TestAccount::class.java
            ).singleResult
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun findByEnvironment(environment: Environment): Collection<TestAccount> =
        withContext(Dispatchers.IO) {
            try {
                entityManager.createQuery(
                    "SELECT t FROM TestAccount t WHERE t.environment = :environment",
                    TestAccount::class.java
                ).resultList
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList<TestAccount>()
            }
        }

    override suspend fun create(testAccount: TestAccount): TestAccount = withContext(Dispatchers.IO) {
        entityManager.withTransaction {
            persist(testAccount)
        }
        testAccount
    }

    override suspend fun update(testAccount: TestAccount): TestAccount = withContext(Dispatchers.IO) {
        entityManager.withTransaction {
            merge(testAccount)
        }
        testAccount
    }

    override suspend fun delete(testAccount: TestAccount) = withContext(Dispatchers.IO) {
        try {
            entityManager.withTransaction {
                remove(testAccount)
            }
        } catch (ignored: Exception) {
        }
    }
}
