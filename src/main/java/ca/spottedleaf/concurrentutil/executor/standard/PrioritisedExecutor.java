package ca.spottedleaf.concurrentutil.executor.standard;
 
// TODO
// import ca.spottedleaf.concurrentutil.executor.BaseExecutor;

public interface PrioritisedExecutor
{
	// TODO
// extends BaseExecutor {

    default public PrioritisedTask queueRunnable(Runnable task) {
        return this.queueRunnable(task, Priority.NORMAL);
    }

    public PrioritisedTask queueRunnable(Runnable var1, Priority var2);

    default public PrioritisedTask createTask(Runnable task) {
        return this.createTask(task, Priority.NORMAL);
    }

    public PrioritisedTask createTask(Runnable var1, Priority var2);

    public static enum Priority {
        COMPLETING(-1),
        BLOCKING,
        HIGHEST,
        HIGHER,
        HIGH,
        NORMAL,
        LOW,
        LOWER,
        LOWEST,
        IDLE;

        static final Priority[] PRIORITIES;
        public static final int TOTAL_PRIORITIES;
        public static final int TOTAL_SCHEDULABLE_PRIORITIES;
        private static int priorityCounter;
        public final int priority;

        public static boolean isValidPriority(Priority priority) {
            return priority != null && priority != COMPLETING;
        }

        public static Priority max(Priority p1, Priority p2) {
            return p1.isHigherOrEqualPriority(p2) ? p1 : p2;
        }

        public static Priority min(Priority p1, Priority p2) {
            return p1.isLowerOrEqualPriority(p2) ? p1 : p2;
        }

        public boolean isHigherOrEqualPriority(Priority than) {
            return this.priority <= than.priority;
        }

        public boolean isHigherPriority(Priority than) {
            return this.priority < than.priority;
        }

        public boolean isLowerOrEqualPriority(Priority than) {
            return this.priority >= than.priority;
        }

        public boolean isLowerPriority(Priority than) {
            return this.priority > than.priority;
        }

        public boolean isHigherOrEqualPriority(int than) {
            return this.priority <= than;
        }

        public boolean isHigherPriority(int than) {
            return this.priority < than;
        }

        public boolean isLowerOrEqualPriority(int than) {
            return this.priority >= than;
        }

        public boolean isLowerPriority(int than) {
            return this.priority > than;
        }

        public static boolean isHigherOrEqualPriority(int priority, int than) {
            return priority <= than;
        }

        public static boolean isHigherPriority(int priority, int than) {
            return priority < than;
        }

        public static boolean isLowerOrEqualPriority(int priority, int than) {
            return priority >= than;
        }

        public static boolean isLowerPriority(int priority, int than) {
            return priority > than;
        }

        public static Priority getPriority(int priority) {
            return PRIORITIES[priority + 1];
        }

        private static int nextCounter() {
            return priorityCounter++;
        }

        private Priority() {
            this(Priority.nextCounter());
        }

        private Priority(int priority) {
            this.priority = priority;
        }

        static {
            PRIORITIES = Priority.values();
            TOTAL_PRIORITIES = PRIORITIES.length;
            TOTAL_SCHEDULABLE_PRIORITIES = TOTAL_PRIORITIES - 1;
        }
    }

    public static interface PrioritisedTask
    {
   // TODO extends BaseExecutor.BaseTask {
        public Priority getPriority();

        public boolean setPriority(Priority var1);

        public boolean raisePriority(Priority var1);

        public boolean lowerPriority(Priority var1);
    }
}

