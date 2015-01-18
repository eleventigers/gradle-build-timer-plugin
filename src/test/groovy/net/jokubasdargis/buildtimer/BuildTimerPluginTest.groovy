package net.jokubasdargis.buildtimer;

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class BuildTimerPluginTest {

    @Test
    public void pluginApplies() {
        Project project = ProjectBuilder.builder().build();
        project.apply plugin: 'net.jokubasdargis.build-timer'
    }

    @Test
    public void setupDefaultTimingsListener() {
        Project project = ProjectBuilder.builder().build();
        TimingsListener listener = BuildTimerPlugin.addTimingsListener(project)
        assert listener.reportAbove == BuildTimerPluginExtension.DEFAULT_REPORT_ABOVE;
    }

    @Test
    public void setupCustomTimingsListener() {
        Project project = ProjectBuilder.builder().build();
        project.apply plugin: 'net.jokubasdargis.build-timer'

        def customTime = 60L
        project.buildTimer.reportAbove = customTime

        TimingsListener listener = BuildTimerPlugin.addTimingsListener(project)
        assert listener.reportAbove == customTime
    }
}