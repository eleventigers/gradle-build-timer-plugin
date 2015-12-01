package net.jokubasdargis.buildtimer

import static net.jokubasdargis.buildtimer.BuildTimerPluginExtension.DEFAULT_REPORT_ABOVE
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

        assert result.output.contains("Task timings over ${DEFAULT_REPORT_ABOVE}ms:")
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

        assert result.output.contains("Task timings over ${DEFAULT_REPORT_ABOVE}ms:")
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

        assert result.output.contains("Task timings over ${customAbove}ms:")
        assert result.output.contains("longTask took")
        assert result.task(':longTask').outcome == SUCCESS
    }

    @Test
    public void skipReportTimingsBelowDefault() {
        buildFile << """
            plugins {
                id 'net.jokubasdargis.build-timer'
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

        assert !result.output.contains("Task timings over ${DEFAULT_REPORT_ABOVE}ms:")
        assert !result.output.contains("quickTask took")
        assert result.task(':quickTask').outcome == SUCCESS
    }
}