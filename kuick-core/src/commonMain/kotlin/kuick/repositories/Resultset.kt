package kuick.repositories


val PAGE_MAX_SIZE = 1000

open class Resultset<T>(val rows: List<T>, val next: Page = Page()) {
    fun <S> map(f: (T) -> S) = Resultset<S>(rows.map(f), next)
}

data class Page(val asc: Boolean = true, val limit: Int = PAGE_MAX_SIZE, val cursor: String? = null)

suspend fun <T:Any> toList(f: suspend (Page) -> Resultset<T>): List<T> {
    val out = mutableListOf<T>()
    var page: Page = Page()
    do {
        val rs = f(page)
        out.addAll(rs.rows)
        page = rs.next
    } while (page.cursor != null)

    return out
}