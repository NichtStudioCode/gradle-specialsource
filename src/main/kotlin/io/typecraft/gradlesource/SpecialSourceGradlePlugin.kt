package io.typecraft.gradlesource

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

/**
 * Plugin for remap mojang to spigot for distribution.
 *
 * This plugin will applies:
 * - Extensions:
 *    - `spigotRemap` [SpigotRemapExtension]
 * - Tasks:
 *    - `remapMojangToObf` [RemapTask]: remap your jar(mojang mapping) to obfuscated.
 *    - `remapObfToSpigot` [RemapTask]: remap the obfuscated jar to spigot(for distribution). Depends on `remapMojangToObf` and `assemble`.
 * - Plugins:
 *    - `java` - [JavaPlugin][org.gradle.api.plugins.JavaPlugin]
 * - Repositories:
 *    - `mavenLocal()`
 *
 * @since 1.0
 */
class SpecialSourceGradlePlugin : Plugin<Project> {
    override fun apply(p: Project) {
        p.pluginManager.apply(JavaPlugin::class)
        p.repositories.mavenLocal {
            metadataSources {
                mavenPom() // To resolve `maven-metadata-local.xml`
                artifact()
            }
        }

        val spigotRemapExt = p.extensions.create<SpigotRemapExtension>("spigotRemap")
        val remapMojangToObf = p.tasks.register<RemapTask>("remapMojangToObf")
        val remapObfToSpigot = p.tasks.register<RemapTask>("remapObfToSpigot")

        remapMojangToObf.configure {
            inJarFile.set(spigotRemapExt.sourceJarTask.flatMap { jarTask -> jarTask.archiveFile })
            outJarFile.set(spigotRemapExt.sourceJarTask.flatMap { jarTask ->
                jarTask.destinationDirectory.map { dir ->
                    val archiveName =
                        archiveNameFromTask(jarTask).copy(classifier = spigotRemapExt.obfJarClassifier.getOrElse("obf"))
                    dir.file(archiveName.toFileName())
                }
            })
            srgIn.set(p.layout.file(spigotRemapExt.spigotVersionExact.map { ver ->
                // NOTE(detachedConfiguration): https://github.com/spring-gradle-plugins/dependency-management-plugin/issues/222#issuecomment-411005109
                val config = p.configurations.detachedConfiguration(
                    p.dependencies.create("org.spigotmc:minecraft-server:${ver}:maps-mojang@txt")
                )
                config.singleFile
            }))
            reverse.set(true)
        }

        remapObfToSpigot.configure {
            inJarFile.set(remapMojangToObf.flatMap { it.outJarFile })
            outJarFile.set(spigotRemapExt.sourceJarTask.flatMap { jarTask ->
                jarTask.destinationDirectory.map { dir ->
                    val archiveName =
                        archiveNameFromTask(jarTask).copy(classifier = spigotRemapExt.spigotJarClassifier.getOrElse(""))
                    dir.file(archiveName.toFileName())
                }
            })
            srgIn.set(p.layout.file(spigotRemapExt.spigotVersionExact.map { ver ->
                val config = p.configurations.detachedConfiguration(
                    p.dependencies.create("org.spigotmc:minecraft-server:${ver}:maps-spigot@csrg")
                )
                config.singleFile
            }))
        }

        val assemble = p.tasks.getByName("assemble")
        assemble.dependsOn(remapObfToSpigot)
    }
}

private fun archiveNameFromTask(x: AbstractArchiveTask): ArchiveName =
    ArchiveName(
        x.archiveBaseName.orNull ?: "",
        x.archiveAppendix.orNull ?: "",
        x.archiveVersion.orNull ?: "",
        x.archiveClassifier.orNull ?: "",
        x.archiveExtension.orNull ?: ""
    )