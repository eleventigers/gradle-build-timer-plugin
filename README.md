Gradle Build Timer Plugin
=========================

A Gradle plugin which reports timings of a project build and individual project tasks. See what tasks take the longest to run:

```
Task timings over 50ms:
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
classpath 'net.jokubasdargis.buildtimer:gradle-plugin:0.1.0'
```

Apply the `build-timer` plugin:

```groovy
apply plugin: 'net.jokubasdargis.build-timer'
```

Optionally configure the lowest time threshold (in ms) this plugin should report above:

```groovy
buildTimer {
    reportAbove: 100L // Default is 50L
}
```
