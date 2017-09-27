package net.jokubasdargis.buildtimer

import net.jokubasdargis.buildtimer.BuildTimerPluginExtension.SortOrder
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState
import org.gradle.internal.time.Timer
import org.gradle.internal.time.Time

import java.util.concurrent.ConcurrentHashMap

final class TimingsListener implements TaskExecutionListener, BuildListener {

    private final BuildTimerPlugin plugin
    final Map<Task, Timing> timings = new ConcurrentHashMap<>()

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

    static final class Timing {

        private static final ORDER_BY_MS_DESC = new OrderBy<Timing>([{ -it.ms }])
        private static final ORDER_BY_MS_ASC = new OrderBy<Timing>([{ it.ms }])
        private static final ORDER_BY_NONE = new OrderBy()

        private final Task task
        private final Timer timer
        private boolean report
        private long ms

        Timing(Task task) {
            this.task = task
            this.timer = Time.startTimer()
        }

        String getPath() {
            task.path
        }

        void complete() {
            ms = timer.elapsedMillis
            report = (ms > getReportAboveForTask())
        }

        long getReportAboveForTask() {
            BuildTimerPluginExtension extension = task.project.extensions.findByType(
                    BuildTimerPluginExtension)
            extension?.reportAbove ?: BuildTimerPluginExtension.DEFAULT_REPORT_ABOVE
        }

        static Comparator<Timing> orderByMsFor(SortOrder sortOrder) {
            switch (sortOrder) {
                case SortOrder.ASC:
                    return ORDER_BY_MS_ASC;
                    break
                case SortOrder.DESC:
                    return ORDER_BY_MS_DESC;
                    break
                case SortOrder.NONE:
                default:
                    return ORDER_BY_NONE;
                    break
            }
        }
    }
}