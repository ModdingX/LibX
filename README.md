# LibX

[![CurseForge](https://cf.way2muchnoise.eu/full_412525_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/libx)
[![Modrinth](https://img.shields.io/modrinth/dt/qEH6GYul?color=00AF5C&logo=modrinth)](https://modrinth.com/mod/libx)
[![Issues](https://img.shields.io/github/issues/noeppi-noeppi/LibX)](https://github.com/noeppi-noeppi/LibX/issues)

LibX is the library mod of [ModdingX](https://moddingx.org/).

### How to use LibX in a dev environment

```groovy
repositories {
    maven {
        name = 'ModdingX Maven'
        url = 'https://maven.moddingx.org/'
    }
}

dependencies {
    annotationProcessor fg.deobf("org.moddingx:LibX:${mc_version}-${libx-version}")
    implementation fg.deobf("org.moddingx:LibX:${mc_version}-${libx-version}")
}
```

**Javadoc can be found at https://moddingx.org/libx**

**The wiki can be found at https://moddingx.org/wiki/libx**

**Join the dev discord: https://moddingx.org/discord**
