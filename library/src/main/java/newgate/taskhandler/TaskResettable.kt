package newgate.taskhandler

/**
 * An interface extending [TaskObservable] with the ability to reset the task state back to Idle (`null`).
 *
 * @param P type of the progress value.
 * @param C type of the completed value.
 */
public interface TaskResettable<out P, out C> : TaskObservable<P, C> {

    /**
     * Resets the task state back to Idle (`null`).
     *
     * @return the reset task state or `null` if already Idle.
     */
    public fun reset(): Task<P, C>?
}