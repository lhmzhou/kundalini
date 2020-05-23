# kundalini

A full end-to-end test environment is very costly. You may find yourself catching defects late in the deployment lifecycle that could have been found earlier if you had a more integrated environment. `kundalini` monitors the roll out of a test use case from development, testing, and production, taking consideration of the complexities around data protection and management. 

## Core Structure

```
kundalini
  │
  ├── buildSrc
  │   └── src
  │       └── ...
  │           └── Config.kt
  │
  ├── core
  │   └── src
  │       └── main
  │            ├── kotlin
  │            │    └── ...
  │            │        ├── ApiService.kt
  │            │        ├── AtomicObservableValue.kt
  │            │        ├── Extensions.kt
  │            │        ├── Kundalini.kt
  │            │        └── TestAccountRepository.kt
  │            │ 
  │            └── resources
  │                 └── META-INF
  │                      └── persistence.xml  
  │
  ├── gui
  │    └── ...
  │         ├── Extensions.kt
  │         ├── JavaFxDsl.kt 
  │         ├── KundaliniApp.kt 
  │         ├── MainController.kt
  │         └── MainView.kt
  │
  ├── ...
  ├── build.gradle.kts
  └── README.md
```

## Build

Apply the Kotlin JVM plugin using the Gradle plugins DSL:

```
plugins {
    kotlin("jvm") version "1.3.72"
}
```

Note: The alias `kotlin-dsl` is added to the customized `build.gradle` found in `buildSrc`.

`kundalini` is built with Gradle, so you can easily run using:

`./gradlew jfxNative`


## See Also

[klaxon](https://github.com/cbeust/klaxon)
</br>
[TornadoFX Docs](https://tornadofx.io/)
</br>
[okhttp recipes](https://square.github.io/okhttp/recipes/)
</br>
[kotlin coroutines overview](https://kotlinlang.org/docs/reference/coroutines-overview.html)