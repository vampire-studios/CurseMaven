# ModrinthMaven
Gradle plugin to allow easy access to modrinth files for your project.

It is based on [CurseMaven](https://github.com/Wyn-Price/CurseMaven) plugin.

# Applying the plugin
To see latest version, look [here](https://jitpack.io/#vampire-studios/ModrinthMaven)

First, add jitpack.io to your plugin repositories:
```gradle
maven {
    name = 'Jitpack'
    url = 'https://jitpack.io'
}
```

Then add a resolution strategy to your plugin management:
```gradle
resolutionStrategy {
    eachPlugin {
        if (requested.id.id == "io.github.vampirestudios.modrinthmaven" && requested.version?.endsWith("-SNAPSHOT") != true) {
            useModule("com.github.vampire-studios.ModrinthMaven:ModrinthMaven:${requested.version}")
        }
    }
}
```

Finally add this to your plugins:
```gradle
plugins {
  id "io.github.vampirestudios.modrinthmaven" version "1.0.1"
}
```
# Usage
Using the plugin is very simple. The dependency format is as follows:  
`modrinth.maven:<modpageid>:<fileid>`
 - `modrinth.maven` -> Required. Marks the dependency to be resolved by the modrinth maven plugin.
 - `<modpageid>` -> the id of the mod on modrinth. e.g. : `tyCNRhS8` which is the mod id of VampireLib on modrinth, you can find it in the mod page url `https://modrinth.com/mod/tyCNRhS8/`.
 - `<fileid>` -> the file id of the file you want to add as a dependency. Same you can also find it in the file page's url `https://modrinth.com/mod/tyCNRhS8/version/UjMEn7wu`.
```gradle
dependencies {
  compile "modrinth.maven:tyCNRhS8:UjMEn7wu"
}
```
resolves the file [here](https://modrinth.com/mod/tyCNRhS8/version/UjMEn7wu), with the scope `compile`

```gradle
dependencies {
  include "modrinth.maven:aXf2OSFU:L7TmfftT"
}
```
resolves the file [here](https://modrinth.com/mod/aXf2OSFU/version/L7TmfftT), with the scope `include`
# Special Thanks to 
 - [Wyn-Price](https://github.com/Wyn-Price) for write the original plugin for curseforge
 - [Tamaized](https://github.com/Tamaized) for working with Wyn-Price to figure out the cloudflare/403 issues.
