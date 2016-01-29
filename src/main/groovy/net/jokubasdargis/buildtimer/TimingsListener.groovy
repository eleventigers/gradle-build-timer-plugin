package net.jokubasdargis.buildtimer

import net.jokubasdargis.buildtimer.BuildTimerPluginExtension.SortOrder
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState
import org.gradle.util.Clock

class TimingsListener implements TaskExecutionListener, BuildListener {

    private BuildTimerPlugin plugin;
    private Map<Task, Timing> timings = Collections.synchronizedMap(new LinkedHashMap())

    TimingsListener(BuildTimerPlugin plugin) {
        this.plugin = plugin
    }

    @Override
    void beforeExecute(Task task) {
        timings.put(task, new Timing(task))
    }

    @Override
    void afterExecute(Task task, TaskState taskState) {
        Timing timing = timings.get(task)

        if (timing) {
            timing.complete()

            if (timing.report) {
                task.project.logger.warn "${timing.path} took ${timing.ms}ms"
            }
        }
    }

    @Override
    void buildFinished(BuildResult result) {
        boolean reportWhenFinished = timings.values().find { Timing timing -> timing.report }

        if (reportWhenFinished) {
            println "Task timings over threshold:\n"
            Comparator<Timing> comparator = Timing.orderByMsFor(plugin.sortOrder)
            timings.values().toSorted(comparator).each { Timing timing ->
                if (timing.report) {
                    printf "%7sms  %s\n", [timing.ms, timing.path]
                }
            }
        }
        timings.clear()
    }

    @Override
    void buildStarted(Gradle gradle) {}

    @Override
    void projectsEvaluated(Gradle gradle) {}

    @Override
    void projectsLoaded(Gradle gradle) {}

    @Override
    void settingsEvaluated(Settings settings) {}

    private static class Timing {

        private static final def ORDER_BY_MS_DESC = new OrderBy<Timing>([{ -it.ms }])
        private static final def ORDER_BY_MS_ASC = new OrderBy<Timing>([{ it.ms }])
        private static final def ORDER_BY_NONE = new OrderBy();

        private Task task
        private Clock clock
        private boolean report
        private long ms

        Timing(Task task) {
            this.task = task
            this.clock = new Clock()
        }

        String getPath() {
            task.path
        }

        void complete() {
            ms = clock.timeInMs
            report = (ms > getReportAboveForTask())
        }

        private long getReportAboveForTask() {
            BuildTimerPluginExtension extension = task.project.extensions.findByType(
                    BuildTimerPluginExtension)
            extension?.reportAbove ?: BuildTimerPluginExtension.DEFAULT_REPORT_ABOVE
        }

        static Comparator<Timing> orderByMsFor(SortOrder sortOrder) {
            switch (sortOrder) {
                case SortOrder.NONE:
                    return ORDER_BY_NONE;
                    break;
                case SortOrder.ASC:
                    return ORDER_BY_MS_ASC;
                    break;
                case SortOrder.DESC:
                    return ORDER_BY_MS_DESC;
                    break;
            }
        }
    }
}