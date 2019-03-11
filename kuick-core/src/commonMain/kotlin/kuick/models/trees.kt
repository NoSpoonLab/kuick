package kuick.models

/**
 * Tree main structure, with a root node
 */
interface NumberedTree<T: Any> {
    fun rootNode(): T
    fun T.nodeNumber(): String
    fun T.nodeChildren(): Array<T>
}


fun <T: Any> NumberedTree<T>.findByNumber(_number: String, node: T = rootNode()): T? = when {
    _number == node.nodeNumber() -> node
    (node == rootNode()) || _number.startsWith(node.nodeNumber() + ".") -> {
        val subnode = node.nodeChildren().firstOrNull { findByNumber(_number, it) != null }
        if (subnode == null) null else findByNumber(_number, subnode)
    }
    else -> null
}


fun String.parentNumber() = substringBeforeLast('.')
fun <T: Any> NumberedTree<T>.isRoot(node: T) = node.nodeNumber() == ""
fun <T: Any> NumberedTree<T>.hasChildren(node: T) = node.nodeChildren().isNotEmpty()

fun <T: Any> NumberedTree<T>.flat(): List<T> = flat(rootNode())
fun <T: Any> NumberedTree<T>.flat(node: T): List<T> = listOf(node) + node.nodeChildren().flatMap { flat(it) }

fun <T: Any> NumberedTree<T>.findNextByNumber(_number: String): T? = findByNumberAndOffset(_number, 1)
fun <T: Any> NumberedTree<T>.findPreviousByNumber(_number: String): T? = findByNumberAndOffset(_number, -1)
fun <T: Any> NumberedTree<T>.findByNumberAndOffset(_number: String, offset: Int): T? {
    val flat = flat()
    val currentIdx = flat.indexOfFirst { it.nodeNumber() == _number }
    val next = (flat.size + currentIdx + offset) % flat.size
    return flat[next]
}