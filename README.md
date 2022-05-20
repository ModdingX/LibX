# LibX

[![CurseForge](http://cf.way2muchnoise.eu/full_412525_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/libx)
[![Issues](https://img.shields.io/github/issues/noeppi-noeppi/LibX)](https://github.com/noeppi-noeppi/LibX/issues)

LibX is a library mod for minecraft by [MelanX](https://www.curseforge.com/members/melanx/projects) and [noeppi_noeppi](https://www.curseforge.com/members/noeppinoeppi/projects).


### How to use LibX in a dev environment

```groovy
repositories {
    maven {
        name = 'MelanX Maven'
        url = 'https://maven.melanx.de/'
    }
}

dependencies {
    annotationProcessor fg.deobf("io.github.noeppi_noeppi.mods:LibX:${mc_version}-${libx-version}")
    implementation fg.deobf("io.github.noeppi_noeppi.mods:LibX:${mc_version}-${libx-version}")
}
```

**Javadoc can be found at https://git.io/libx**

**The wiki can be found at https://git.io/wikix**

**Join the dev discord: https://discord.gg/NfwG6txUN6**

### The future branch

The `future` branch is used to implement breaking changes that will be merged in the next major minecraft release.
