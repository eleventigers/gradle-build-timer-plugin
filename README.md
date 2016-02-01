Gradle Build Timer Plugin
=========================

A Gradle plugin which reports timings of a project build and individual project tasks. See what tasks take the longest to run:

```
Task timings over threshold:
    128ms  :app:mergeDebugAssets
   1099ms  :app:mergeDebugResources
    788ms  :app:processDebugResources
   5000ms  :app:compileDebugJava
  13757ms  :app:proguardDebug
  15965ms  :app:dexDebug
   3449ms  :app:packageDebug
    158ms  :app:zipalignDebug
```

Usage
----
Add the plugin to your `buildscript`'s `dependencies` section:

```groovy
classpath 'net.jokubasdargis.buildtimer:gradle-plugin:0.2.0'
```

Apply the `build-timer` plugin:

```groovy
apply plugin: 'net.jokubasdargis.build-timer'
```

Optionally configure the plugin:

```groovy
buildTimer {
    reportAbove = 100L // The lowest time threshold (in ms) this plugin should report above, default is 50L
    sort = 'asc' // Sort timings by ms, possible values: 'none', 'asc', 'desc'
}
```
