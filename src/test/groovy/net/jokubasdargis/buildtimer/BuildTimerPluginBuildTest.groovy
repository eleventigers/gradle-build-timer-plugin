package net.jokubasdargis.buildtimer

import java.util.regex.Pattern

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class BuildTimerPluginBuildTest {

    private static final String LABEL_OVER_THRESHOLD = "Task timings over threshold:"
    private static final Pattern TIMING_REGEX = Pattern.compile("^\\d*ms {2,}:")

    @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile

    @Before
    void setUp() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    private static List<File> getPluginClasspath() {
        System.getProperty("java.class.path").split(File.pathSeparator)
                .collect { new File(it) }
    }

    @Test
    void reportTimingsAboveDefault() {
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
    void reportMultipleTimingsAboveDefault() {
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
    void reportMultipleTimingsSortOrderDesc() {
        buildFile << """
            plugins {
                id 'net.jokubasdargis.build-timer'
            }

            buildTimer {
                reportAbove = 1L
                sort = 'desc'
            }

            task longTask1 {
                doLast {
                    Thread.sleep(1)
                }
            }

            task longTask2 {
                doLast {
                    Thread.sleep(50)
                }
            }

            task longTask3 {
                doLast {
                    Thread.sleep(100)
                }
            }
        """

        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('longTask1', 'longTask2', 'longTask3')
            .withPluginClasspath(pluginClasspath)
            .build()


        assert result.task(':longTask1').outcome == SUCCESS
        assert result.task(':longTask2').outcome == SUCCESS
        assert result.task(':longTask3').outcome == SUCCESS

        def output = result.output;

        def expected = ["longTask3", "longTask2", "longTask1"]
        def actual = output
            .substring(output.indexOf(LABEL_OVER_THRESHOLD) + LABEL_OVER_THRESHOLD.length() + 1)
            .tokenize("\n").collect{ it.trim().replaceFirst(TIMING_REGEX, "") }

        assert expected == actual
    }

    @Test
    void reportMultipleTimingsSortOrderAsc() {
        buildFile << """
            plugins {
                id 'net.jokubasdargis.build-timer'
            }

            buildTimer {
                reportAbove = 1L
                sort = 'asc'
            }

            task longTask1 {
                doLast {
                    Thread.sleep(100)
                }
            }

            task longTask2 {
                doLast {
                    Thread.sleep(10)
                }
            }

            task longTask3 {
                doLast {
                    Thread.sleep(50)
                }
            }
        """

        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('longTask1', 'longTask2', 'longTask3')
            .withPluginClasspath(pluginClasspath)
            .build()


        assert result.task(':longTask1').outcome == SUCCESS
        assert result.task(':longTask2').outcome == SUCCESS
        assert result.task(':longTask3').outcome == SUCCESS

        def output = result.output;

        def expected = ["longTask2", "longTask3", "longTask1"]
        def actual = output
            .substring(output.indexOf(LABEL_OVER_THRESHOLD) + LABEL_OVER_THRESHOLD.length() + 1)
            .tokenize("\n").collect{ it.trim().replaceFirst(TIMING_REGEX, "") }

        assert expected == actual
    }

    @Test
    void reportTimingsAboveCustom() {
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
    void skipReportTimingsBelowDefault() {
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
    void combinedReportWithSubprojectBuild() {
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