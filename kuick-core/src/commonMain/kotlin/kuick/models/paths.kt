package kuick.models

fun String.parentPath(): String? = if (this == "") null else substringBeforeLast('/')
fun pathToSectionNumber(path: String) = path.split("/").filter { it != "" }.map { it.toInt() }.joinToString(".")
fun sectionNumberToPath(sectionNumber: String) = when {
    sectionNumber == "" -> ""
    else -> sectionNumber.split(".").map { it.padStart(2, '0') }.joinToString("/", prefix = "/")
}
