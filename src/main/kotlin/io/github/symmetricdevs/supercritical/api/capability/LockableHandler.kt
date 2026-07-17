package io.github.symmetricdevs.supercritical.api.capability

interface LockableHandler<T> {

    var lockIntent: Boolean

    val stack: T
}
