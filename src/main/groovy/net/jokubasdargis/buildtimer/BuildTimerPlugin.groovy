package net.jokubasdargis.buildtimer

import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildTimerPlugin implements Plugin<Project> {

    public static final String TAG = 'taggedByTimerPlugin'

    @Override
    void apply(Project project) {
        project.extensions.create(BuildTimerPluginExtension.NAME, BuildTimerPluginExtension)

        if (project.rootProject.hasProperty(TAG) == false) {
            project.gradle.addListener(new TimingsListener())
            project.rootProject.ext[TAG] = true
        }
    }

}