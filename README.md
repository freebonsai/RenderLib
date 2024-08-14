# RenderLib

RenderLib is a rendering library that makes rendering various highlights, shapes,
and shaders simple by providing significant abstractions.

## Dependency

In your repository block, add:

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io/")
}
```

Add the dependency:
```kotlin
dependencies {
    implementation("com.github.bonsai:RenderLib:{version}")
}
```

## Usage

By using Kotlin DSL, you're able to create a command tree
by essentially _visualizing_ it.
