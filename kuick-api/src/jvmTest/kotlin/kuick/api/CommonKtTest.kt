package kuick.api

import org.junit.Test
import kotlin.test.assertEquals

class CommonKtTest {
    @Test
    fun `should create one level tree`() {
        val actual = listOf("a", "b", "c", "d").toTree()
        val expected = Node(null,
                listOf(
                        Node("a", listOf(Node("", emptyList()))),
                        Node("b", listOf(Node("", emptyList()))),
                        Node("c", listOf(Node("", emptyList()))),
                        Node("d", listOf(Node("", emptyList())))
                )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `should create multi-level tree`() {
        val actual = listOf("a.a", "a.b", "b.a", "b.b", "b.a.a", "c", "d").toTree()
        val expected = Node(null,
                listOf(
                        Node("a", listOf(
                                Node("a", listOf(Node("", emptyList()))),
                                Node("b", listOf(Node("", emptyList())))
                        )),
                        Node("b", listOf(
                                Node("a", listOf(
                                        Node("", emptyList()),
                                        Node("a", listOf(Node("", emptyList())))
                                )),
                                Node("b", listOf(Node("", emptyList())))
                        )),
                        Node("c", listOf(Node("", emptyList()))),
                        Node("d", listOf(Node("", emptyList())))
                )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `should create multi-level tree with element itself`() {
        val actual = listOf("a", "a.a", "a.b", "b.a", "b.b", "b.a.a", "c", "d").toTree()
        val expected = Node(null,
                listOf(
                        Node("a", listOf(
                                Node("", emptyList()),
                                Node("a", listOf(Node("", emptyList()))),
                                Node("b", listOf(Node("", emptyList())))
                        )),
                        Node("b", listOf(
                                Node("a", listOf(
                                        Node("", emptyList()),
                                        Node("a", listOf(Node("", emptyList())))
                                )),
                                Node("b", listOf(Node("", emptyList())))
                        )),
                        Node("c", listOf(Node("", emptyList()))),
                        Node("d", listOf(Node("", emptyList())))
                )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `should create multi-level tree with duplicated elements without distinct flag`() {
        val actual = listOf("a", "a", "a.a", "a.a", "a.b", "b.a", "b.b", "b.a.a", "c", "d").toTree()
        val expected = Node(null,
                listOf(
                        Node("a", listOf(
                                Node("", emptyList()),
                                Node("", emptyList()),
                                Node("a", listOf(
                                        Node("", emptyList()),
                                        Node("", emptyList())
                                )),
                                Node("b", listOf(Node("", emptyList())))
                        )),
                        Node("b", listOf(
                                Node("a", listOf(
                                        Node("", emptyList()),
                                        Node("a", listOf(Node("", emptyList())))
                                )),
                                Node("b", listOf(Node("", emptyList())))
                        )),
                        Node("c", listOf(Node("", emptyList()))),
                        Node("d", listOf(Node("", emptyList())))
                )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `should create multi-level tree with duplicated elements with distinct flag`() {
        val actual = listOf("a", "a", "a.a", "a.a", "a.b", "b.a", "b.b", "b.a.a", "c", "d").toTree(distinct = true)
        val expected = Node(null,
                listOf(
                        Node("a", listOf(
                                Node("", emptyList()),
                                Node("a", listOf(Node("", emptyList()))),
                                Node("b", listOf(Node("", emptyList())))
                        )),
                        Node("b", listOf(
                                Node("a", listOf(
                                        Node("", emptyList()),
                                        Node("a", listOf(Node("", emptyList())))
                                )),
                                Node("b", listOf(Node("", emptyList())))
                        )),
                        Node("c", listOf(Node("", emptyList()))),
                        Node("d", listOf(Node("", emptyList())))
                )
        )
        assertEquals(expected, actual)
    }
}