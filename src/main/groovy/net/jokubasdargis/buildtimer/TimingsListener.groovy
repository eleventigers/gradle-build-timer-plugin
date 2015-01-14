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

    private static final long REPORT_ABOVE_MS = 50L;

    private Clock clock
    private timings = []

    @Override
    void beforeExecute(Task task) {
        clock = new Clock()
    }

    @Override
    void afterExecute(Task task, TaskState taskState) {
        def ms = clock.timeInMs
        timings.add([ms, task.path])
        task.project.logger.warn "${task.path} took ${ms}ms"
    }

    @Override
    void buildFinished(BuildResult result) {
        printf "Task timings over %sms:\n", REPORT_ABOVE_MS
        timings.each { timing ->
            if (timing[0] >= REPORT_ABOVE_MS) {
                printf "%7sms  %s\n", timing
            }
        }
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