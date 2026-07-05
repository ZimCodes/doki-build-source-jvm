package io.unthrottled.doki.build.jvm.tools

import com.google.gson.Gson
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
  val gson: Gson = GsonBuilder()
    .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
    .setPrettyPrinting().create()

  private fun <T : HasId> getAllProductDefinitions(
    productBuildSourceDirectory: Path,
    dokiProduct: DokiProduct,
    clazz: Class<T>
  ): Map<String, T> {
    return Files.walk(productBuildSourceDirectory)
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
  }

  fun <T : HasId> getAllDokiThemeDefinitions(
    dokiProduct: DokiProduct,
    productBuildSourceDirectory: Path,
    masterThemeDirectory: Path,
    clazz: Class<T>
  ): Stream<Triple<Path, MasterThemeDefinition, T>> {
    val allProductDefinitions = getAllProductDefinitions(productBuildSourceDirectory, dokiProduct, clazz)
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

  private fun getFileSuffix(dokiProductName: String, variantName: String, includeVariantType: Boolean): String = when {
    variantName.startsWith("custom") -> {
      val variantSplit = variantName.split("-".toPattern()) // Ex [custom,variant]
      val variantName = variantSplit[0] // Ex: custom
      val variantType = variantSplit[1] // Ex: variant
      if (includeVariantType) "$variantName.$variantType.${dokiProductName}.definition.json" else "$variantName.${dokiProductName}.definition.json"
    }
    else -> "$variantName.${dokiProductName}.definition.json"
  }

  private fun <T : HasId> getJetProductDefinitions(
    productBuildSourceDirectory: Path,
    clazz: Class<T>,
    suffix: String
  ): Map<String, T> {
    return Files.walk(productBuildSourceDirectory)
      .filter { !Files.isDirectory(it) }
      .filter { it.fileName.toString().endsWith(suffix) }
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
  }

  fun <T : HasId> getAllJetbrainsDefinitions(
    dokiProduct: DokiProduct,
    productBuildSourceDirectory: Path,
    masterThemeDirectory: Path,
    clazz: Class<T>,
    variantName: String
  ): Stream<Triple<Path, MasterThemeDefinition, T>> {
    val allVariantDefinitions = getJetProductDefinitions(productBuildSourceDirectory, clazz, getFileSuffix(dokiProduct.value,variantName,true))
    val masterThemeDefinitionPath = Paths.get(masterThemeDirectory.toString(), "definitions")
    return Files.walk(masterThemeDefinitionPath)
      .filter { !Files.isDirectory(it) }
      .filter { it.fileName.toString().endsWith(getFileSuffix("master",variantName, false)) }
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
        val key = masterThemeDefinition.id + if (variantName == "darcula" || variantName.startsWith("custom")) "" else variantName
        val variantDefinition = (allVariantDefinitions[key] ?: throw IllegalArgumentException(
          """
              doki-build-plugin/assets/themes,'${masterThemeDefinition.displayName}', is missing a $variantName variant definition file!
            """.trimIndent()
        ))

        Triple(productDefinitionDefinitionPath, masterThemeDefinition, variantDefinition)
      }
  }
}
