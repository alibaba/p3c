package com.alibaba.p3c.idea.util

import com.intellij.openapi.progress.ProcessCanceledException
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock

/**
 * @date 2020/06/14
 * @author caikang
 */

fun <T> Lock.withLockNotInline(action: () -> T?): T? {
    lock()
    try {
        return action()
    } finally {
        unlock()
    }
}

fun <T> Semaphore.withAcquire(action: () -> T?): T? {
    acquire()
    try {
        return action()
    } finally {
        release()
    }
}

fun <T> Lock.withTryLock(time: Long, timeUnit: TimeUnit, action: () -> T?): T? {
    if (!tryLock(time, timeUnit)) {
        throw ProcessCanceledException()
    }
    try {
        return action()
    } finally {
        unlock()
    }
}

fun <T> Semaphore.withTryAcquire(time: Long, timeUnit: TimeUnit, action: () -> T?): T? {
    if (!tryAcquire(time, timeUnit)) {
        throw ProcessCanceledException()
    }
    try {
        return action()
    } finally {
        release()
    }
}