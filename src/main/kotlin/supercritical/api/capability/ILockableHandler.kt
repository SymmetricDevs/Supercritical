package supercritical.api.capability

interface ILockableHandler<T> {
    fun setLock(isLocked: Boolean)

    val isLocked: Boolean

    val lockedObject: T?
}
