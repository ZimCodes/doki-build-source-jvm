package io.unthrottled.doki.build.jvm.tools

import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import io.unthrottled.doki.build.jvm.models.HasId
import io.unthrottled.doki.build.jvm.models.MasterThemeDefinition
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
import java.util.stream.Stream

object CommonConstructionFunctions {
  private val gson = GsonBuilder()
    .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
    .setPrettyPrinting().create()

  fun <T : HasId> getAllDokiThemeDefinitions(
    dokiProduct: DokiProduct,
    productBuildSourceDirectory: Path,
    masterThemeDirectory: Path,
    clazz: Class<T>
  ): Stream<Triple<Path, MasterThemeDefinition, T>> {
    val allProductDefinitions =
      Files.walk(productBuildSourceDirectory)
        .filter { !Files.isDirectory(it) }
        .filter { it.fileName.toString().endsWith("${dokiProduct.value}.definition.json") }
        .map { Files.newInputStream(it) }
        .map {
          gson.fromJson(
            InputStreamReader(it, StandardCharsets.UTF_8),
            clazz
          )
        }.collect(
          Collectors.toMap(
            { it.id },
            { it }
          )
        )

    val masterThemeDefinitionPath = Paths.get(masterThemeDirectory.toString(), "definitions")
    return Files.walk(masterThemeDefinitionPath)
      .filter { !Files.isDirectory(it) }
      .filter { it.fileName.toString().endsWith("master.definition.json") }
      .map { it to Files.newInputStream(it) }
      .map {
        val masterThemePath = it.first.toString()
        val masterFileDefinition = masterThemePath.substringAfter("$masterThemeDefinitionPath")
        val productDefinitionDefinitionPath =
          Paths.get(productBuildSourceDirectory.toString(), masterFileDefinition)
        val masterThemeDefinition = gson.fromJson(
          InputStreamReader(it.second, StandardCharsets.UTF_8),
          MasterThemeDefinition::class.java
        )
        val productDefinition =
          allProductDefinitions[masterThemeDefinition.id] ?: throw IllegalArgumentException(
            """
            Master Theme ${masterThemeDefinition.displayName} is missing the ${dokiProduct.prettyName} definition file!
            """.trimIndent()
          )
        Triple(productDefinitionDefinitionPath, masterThemeDefinition, productDefinition)
      }
  }
}
