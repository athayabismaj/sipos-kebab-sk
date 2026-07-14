package com.sipos.kebabsk.common.validation

fun safeMultiply(left: Long, right: Int): Long? {
    return runCatching {
        Math.multiplyExact(left, right.toLong())
    }.getOrNull()
}

fun safeAdd(left: Long, right: Long): Long? {
    return runCatching {
        Math.addExact(left, right)
    }.getOrNull()
}

fun safeSubtract(left: Long, right: Long): Long? {
    return runCatching {
        Math.subtractExact(left, right)
    }.getOrNull()
}
