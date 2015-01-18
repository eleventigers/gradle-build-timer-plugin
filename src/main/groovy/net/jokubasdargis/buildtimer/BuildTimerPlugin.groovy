package net.jokubasdargis.buildtimer

import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildTimerPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create(BuildTimerPluginExtension.NAME, BuildTimerPluginExtension)
        project.afterEvaluate {
            addTimingsListener(project)
        }
    }

    static TimingsListener addTimingsListener(Project project) {
        final BuildTimerPluginExtension extension = project.extensions
                .findByType(BuildTimerPluginExtension);
        final long reportAbove = extension != null ?
                extension.reportAbove : BuildTimerPluginExtension.DEFAULT_REPORT_ABOVE;
        final TimingsListener listener = new TimingsListener(reportAbove);
        project.gradle.addListener(listener)

        return listener
    }
}