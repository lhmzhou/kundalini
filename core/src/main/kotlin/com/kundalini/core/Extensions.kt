package com.kundalini.core

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonBase
import com.beust.klaxon.JsonObject
import com.sun.javafx.collections.ObservableSetWrapper
import javax.persistence.EntityManager

fun EntityManager.withTransaction(doAction: EntityManager.() -> Unit) {
    transaction.begin()
    doAction()
    transaction.commit()
}

fun JsonBase.resolvePath(path: String): JsonBase? {
    val element: Any = with(path.substringBefore(".")) {
        if (this.toIntOrNull() != null) {
            this.toInt()
        } else {
            this
        }
    }
    val currentNode = when (this) {
        is JsonObject -> this[element as String].toJsonBase() ?: return null
        is JsonArray<*> -> this[element as Int].toJsonBase() ?: return null
        else -> return null
    }
    val remainingNodes = path.substringAfter(".")
    if (remainingNodes == path) return currentNode as? JsonBase
    return (currentNode.resolvePath(remainingNodes))
}

fun Any?.toJsonBase(): JsonBase? {
    return when (this) {
        is JsonObject -> this
        is JsonArray<*> -> this
        else -> null
    }
}

fun ObservableSetWrapper<TestAccountStatus>.replace(item: TestAccountStatus) {
    removeIf { it.username == item.username }
    add(item)
}

fun Boolean?.compareTo(other: Boolean?): Int = (this ?: false).compareTo(other ?: false)

fun <E> MutableList<E>.rotate() {
    add(removeAt(0))
}
