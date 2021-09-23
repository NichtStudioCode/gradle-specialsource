# gradle-specialsource

A Gradle plugin for remap name definitions in java bytecode using
md-5's [SpecialSource](https://github.com/md-5/SpecialSource).

Initial use-case was, using `Mojang mapping` for Spigot 1.17+ development, then remap back to `Spigot mapping` for distribution. 

Here [md-5's post](https://www.spigotmc.org/threads/spigot-bungeecord-1-17.510208/#post-4184317) to see what changed
in 1.17+.

## Usage

```kotlin
TODO()
```

## Developer notes

### [Incremental build](https://docs.gradle.org/current/userguide/java_plugin.html#sec:incremental_compile)

This is a performance improvement to avoid redoing already completed job, skip gradle tasks if it's input not changed.

To remap java `.class` files, it should be a task input.

But currently, `RemapTask` just checking a `.jar` file not `.class` files, therefore it remaps all classes even just a single class modified.

The reason why we can't check classes instead of jar is, this plugin depends on md5's `SpecialSource` which makes hard to doing that. 

This limitation is same with the `SpecialSourceMP` which for Maven, and it doesn't support any incremental! 

To improve this, we should refactor SpecialSource or just create a new one including only pure functions.

## References

What changed in 1.17: https://www.spigotmc.org/threads/spigot-bungeecord-1-17.510208/#post-4184317

SpecialSource: https://github.com/md-5/SpecialSource

SpecialSource Maven plugin: https://github.com/agaricusb/SpecialSourceMP
