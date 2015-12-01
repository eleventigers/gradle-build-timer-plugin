package net.jokubasdargis.buildtimer

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState
import org.gradle.util.Clock

class TimingsListener implements TaskExecutionListener, BuildListener {

    private Map<Task, Timing> timings = Collections.synchronizedMap(new LinkedHashMap())

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
            timings.values().each { Timing timing ->
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
            BuildTimerPluginExtension extension = task.project.extensions.findByType(BuildTimerPluginExtension)
            extension?.reportAbove ?: BuildTimerPluginExtension.DEFAULT_REPORT_ABOVE
        }

    }

}