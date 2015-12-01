package net.jokubasdargis.buildtimer

import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildTimerPlugin implements Plugin<Project> {

    private static TimingsListener timingsListener = new TimingsListener()

    @Override
    void apply(Project project) {
        project.extensions.create(BuildTimerPluginExtension.NAME, BuildTimerPluginExtension)
        project.gradle.addListener(timingsListener)
    }

}