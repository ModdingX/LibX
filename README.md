# LibX

[![Modrinth](https://badges.moddingx.org/modrinth/downloads/qEH6GYul)](https://modrinth.com/mod/libx)
[![CurseForge](https://badges.moddingx.org/curseforge/downloads/412525)](https://www.curseforge.com/minecraft/mc-mods/libx)
[![Issues](https://img.shields.io/github/issues/ModdingX/LibX)](https://github.com/ModdingX/LibX/issues)

LibX is the library mod of [ModdingX](https://moddingx.org/).

### How to use LibX in a dev environment

```groovy
repositories {
    maven {
        name = 'ModdingX Maven'
        url = 'https://maven.moddingx.org/release'
    }
}

dependencies {
    annotationProcessor "org.moddingx:LibX:${mc_version}-${libx-version}"
    implementation "org.moddingx:LibX:${mc_version}-${libx-version}"
}
```

**Javadoc can be found at https://moddingx.org/libx**

**The wiki can be found at https://moddingx.org/wiki/libx**

**Join the dev discord: https://moddingx.org/discord**
