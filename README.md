**LibX for Minecraft 1.16 is no longer supported.**

# LibX

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
    compile fg.deobf("io.github.noeppi_noeppi.mods:LibX:${mc_version}-${libx-version}")
}
```

**Javadoc can be found here: https://git.io/libx**