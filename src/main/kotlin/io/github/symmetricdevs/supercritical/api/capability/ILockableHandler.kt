package io.github.symmetricdevs.supercritical.api.capability

interface ILockableHandler<T> {

    var lockIntent: Boolean

    val stack: T
}
