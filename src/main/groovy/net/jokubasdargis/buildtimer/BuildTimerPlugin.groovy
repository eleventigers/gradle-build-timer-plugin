package net.jokubasdargis.buildtimer

import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildTimerPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.gradle.addListener(new TimingsListener())
    }
}