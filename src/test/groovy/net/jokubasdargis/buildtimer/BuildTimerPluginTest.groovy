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
}