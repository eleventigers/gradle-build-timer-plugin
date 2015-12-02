package net.jokubasdargis.buildtimer;

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class BuildTimerPluginTest {

    @Test
    public void pluginApplies() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'net.jokubasdargis.build-timer'

        assert project.getPluginManager().hasPlugin('net.jokubasdargis.build-timer')
    }

    @Test
    public void setupDefaultTimingsListener() {
        Project project = ProjectBuilder.builder().build()
        TimingsListener listener = new TimingsListener()

        Task task = project.tasks.create("task")
        listener.beforeExecute(task)

        TimingsListener.Timing timing = listener.timings.values().first()
        timing.getReportAboveForTask() == BuildTimerPluginExtension.DEFAULT_REPORT_ABOVE;
    }

    @Test
    public void setupCustomTimingsListener() {
        Project project = ProjectBuilder.builder().build()
        TimingsListener listener = new TimingsListener()
        project.apply plugin: 'net.jokubasdargis.build-timer'

        def customTime = 60L
        project.buildTimer.reportAbove = customTime

        Task task = project.tasks.create("task")
        listener.beforeExecute(task)

        TimingsListener.Timing timing = listener.timings.values().first()
        timing.getReportAboveForTask() == customTime
    }

}