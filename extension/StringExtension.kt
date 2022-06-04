import java.util.*


// https://stackoverflow.com/a/60010299/18418162

val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
val snakeRegex = "_[a-zA-Z]".toRegex()

fun String.camelToSnakeCase(): String = camelRegex.replace(this) { "_${it.value}" }.lowercase()

fun String.snakeToCamelCase(): String = snakeRegex.replace(this) { it.value.replace("_", "").uppercase() }

fun String.snakeToPascalCase(): String = this.snakeToCamelCase().toCapitalize()

fun String.toCapitalize(): String = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }