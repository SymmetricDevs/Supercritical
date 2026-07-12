package io.github.symmetricdevs.supercritical.api.capability

interface ILockableHandler<T> {

    var locked: Boolean

    val lockedObject: T
}
