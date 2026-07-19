package io.unthrottled.doki.build.jvm.models

typealias StringDictionary<T> = Map<String, T>

data class SchemaDefinition(
  val required: List<String>?,
)

data class ThemeDefinitionSchema(
  val properties: StringDictionary<SchemaDefinition>
)

class Background(
  val name: String,
  val position: String,
  val opacity: Int? = null
)

class Backgrounds(
  val default: Background,
  val secondary: Background?
)

data class AssetTemplateDefinition(
  val type: String? = null,
  val extends: String? = null,
  val name: String,
  val ui: StringDictionary<Any>? = null,
  val icons: StringDictionary<Any>? = null,
  val colors: StringDictionary<String>? = null
)

data class BuildSticker(
  val name: String,
  val anchor: String,
  val opacity: Int
)

data class JetbrainsStickers(
  val default: String,
  val secondary: String?
)

data class BuildStickers(
  val default: BuildSticker,
  val secondary: BuildSticker?
)

data class EditorSchemeOverrides(
  override val colors: StringDictionary<String>
) : HasColors

data class Overrides(
  val editorScheme: EditorSchemeOverrides?
)

data class BackgroundDefinition(
  val name: String?,
  val position: String?,
  val opacity: Int?
)

data class BackgroundsDefinition(
  val default: BackgroundDefinition?,
  val secondary: BackgroundDefinition?
)

interface HasId {
  val id: String
}

interface HasColors {
  val colors: StringDictionary<String>
}

data class MasterThemeDefinition(
  val id: String,
  val name: String,
  val displayName: String,
  val dark: Boolean,
  val author: String,
  val group: String,
  val product: String?,
  val stickers: BuildStickers,
  val overrides: Overrides?,
  override val colors: StringDictionary<String>,
  val meta: StringDictionary<String>?
) : HasColors {
  val usableName: String
    get() = name.replace(' ', '_')
      .replace(":", "")
  val usableGroup: String
    get() = group.replace(' ', '_')
      .replace(":", "")
}