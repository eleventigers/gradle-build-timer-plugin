package net.jokubasdargis.buildtimer

import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildTimerPlugin implements Plugin<Project> {

    public static final String TAG = 'taggedByTimerPlugin'

    private BuildTimerPluginExtension extension

    @Override
    void apply(Project project) {
        extension = project.extensions.create(
            BuildTimerPluginExtension.NAME, BuildTimerPluginExtension)

        if (project.rootProject.hasProperty(TAG) == false) {
            project.gradle.addListener(new TimingsListener(this))
            project.rootProject.ext[TAG] = true
        }
    }

    BuildTimerPluginExtension.SortOrder getSortOrder() {
        extension.sort
    }
}