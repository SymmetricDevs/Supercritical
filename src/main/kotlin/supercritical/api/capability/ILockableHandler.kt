package supercritical.api.capability

interface ILockableHandler<T> {

    val locked: Boolean

    val lockedObject: T
}
