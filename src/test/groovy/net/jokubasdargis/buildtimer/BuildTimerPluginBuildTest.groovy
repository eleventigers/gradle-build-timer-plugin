package net.jokubasdargis.buildtimer

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class BuildTimerPluginBuildTest {

    @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile

    @Before
    public void setUp() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    private List<File> getPluginClasspath() {
        System.getProperty("java.class.path").split(File.pathSeparator)
                .collect { new File(it) }
    }

    @Test
    public void reportTimingsAboveDefault() {
        buildFile << """
            plugins {
                id 'net.jokubasdargis.build-timer'
            }

            task longTask {
                doLast {
                    Thread.sleep(60)
                }
            }
        """

        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('longTask')
                .withPluginClasspath(pluginClasspath)
                .build()

        assert result.output.contains("Task timings over threshold:")
        // cannot check exact timing string logged as it will always be slightly different
        assert result.output.contains("longTask took")
        assert result.task(':longTask').outcome == SUCCESS
    }

    @Test
    public void reportMultipleTimingsAboveDefault() {
        buildFile << """
            plugins {
                id 'net.jokubasdargis.build-timer'
            }

            task longTask1 {
                doLast {
                    Thread.sleep(60)
                }
            }

            task longTask2 {
                doLast {
                    Thread.sleep(100)
                }
            }
        """

        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('longTask1', 'longTask2')
                .withPluginClasspath(pluginClasspath)
                .build()

        assert result.output.contains("Task timings over threshold:")
        assert result.output.contains("longTask1 took")
        assert result.output.contains("longTask2 took")
        assert result.task(':longTask1').outcome == SUCCESS
        assert result.task(':longTask2').outcome == SUCCESS
    }

    @Test
    public void reportTimingsAboveCustom() {
        def customAbove = 100L

        buildFile << """
            plugins {
                id 'net.jokubasdargis.build-timer'
            }

            buildTimer {
                reportAbove = ${customAbove}
            }

            task longTask {
                doLast {
                    Thread.sleep(110)
                }
            }
        """

        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('longTask')
                .withPluginClasspath(pluginClasspath)
                .build()

        assert result.output.contains("Task timings over threshold:")
        assert result.output.contains("longTask took")
        assert result.task(':longTask').outcome == SUCCESS
    }

    @Test
    public void skipReportTimingsBelowDefault() {
        buildFile << """
            plugins {
                id 'net.jokubasdargis.build-timer'
            }

            buildTimer {
                reportAbove = 1000
            }

            task quickTask {
                doLast {
                    println 'hello quick task'
                }
            }
        """

        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('quickTask')
                .withPluginClasspath(pluginClasspath)
                .build()

        assert !result.output.contains("Task timings over threshold:")
        assert !result.output.contains("quickTask took")
        assert result.task(':quickTask').outcome == SUCCESS
    }

    @Test
    public void combinedReportWithSubprojectBuild() {
        buildFile << """
            plugins {
                id 'net.jokubasdargis.build-timer'
            }

            buildTimer {
                reportAbove = 250
            }

            task longTask {
                doLast {
                    Thread.sleep(260)
                }
            }
        """

        testProjectDir.newFile('settings.gradle') << "include 'submodule'"
        new File(testProjectDir.newFolder('submodule'), 'build.gradle') << """
            apply plugin: 'net.jokubasdargis.build-timer'

            buildTimer {
                reportAbove = 100
            }

            task submoduleLongTask {
                 doLast {
                    Thread.sleep(110)
                 }
            }
"""

        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('longTask', 'submoduleLongTask')
                .withPluginClasspath(pluginClasspath)
                .build()

        assert result.output.contains("Task timings over threshold:")
        assert result.output.contains("longTask took")
        assert result.output.contains("submoduleLongTask took")
        assert result.task(':longTask').outcome == SUCCESS
        assert result.task(':submodule:submoduleLongTask').outcome == SUCCESS
    }

}