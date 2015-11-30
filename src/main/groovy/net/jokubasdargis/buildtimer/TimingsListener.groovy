package net.jokubasdargis.buildtimer

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState
import org.gradle.util.Clock;

class TimingsListener implements TaskExecutionListener, BuildListener {

    final long reportAbove

    private Clock clock
    private timings = []

    private boolean reportWhenFinished

    TimingsListener(long reportAbove) {
        this.reportAbove = reportAbove
    }

    @Override
    void beforeExecute(Task task) {
        clock = new Clock()
    }

    @Override
    void afterExecute(Task task, TaskState taskState) {
        def ms = clock.timeInMs
        timings.add([ms, task.path])
        if (shouldReport(ms)) {
            reportWhenFinished = true
            task.project.logger.warn "${task.path} took ${ms}ms"
        }
    }

    @Override
    void buildFinished(BuildResult result) {
        if (reportWhenFinished) {
            printf "Task timings over %sms:\n", reportAbove
            timings.each { timing ->
                if (shouldReport(timing[0])) {
                    printf "%7sms  %s\n", timing
                }
            }
        }
    }

    private boolean shouldReport(long timing) {
        return timing > reportAbove
    }

    @Override
    void buildStarted(Gradle gradle) {}

    @Override
    void projectsEvaluated(Gradle gradle) {}

    @Override
    void projectsLoaded(Gradle gradle) {}

    @Override
    void settingsEvaluated(Settings settings) {}
}