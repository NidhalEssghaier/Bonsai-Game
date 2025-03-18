import helper.*
import kotlin.test.*

class StackTest {

    @Test
    fun `push should add an element to the top of the stack`() {
        val stack = ArrayDeque<Int>()
        stack.push(42)
        assertEquals(42, stack.first())
        assertEquals(1, stack.size)
    }

    @Test
    fun `test collection pushAll`() {
        val stack = ArrayDeque<Int>()
        stack.pushAll(listOf(1, 2, 3))
        assertEquals(listOf(3, 2, 1), stack.toList())
    }

    @Test
    fun `test varrag pushAll`() {
        val stack = ArrayDeque<Int>()
        stack.pushAll(1, 2, 3)
        assertEquals(listOf(3, 2, 1), stack.toList())
    }

    @Test
    fun `test pop`() {
        var stack = ArrayDeque(listOf(1, 2, 3))
        val popped = stack.pop()
        assertEquals(1, popped)
        assertEquals(listOf(2, 3), stack.toList())
        stack = ArrayDeque<Int>()
        assertFailsWith<NoSuchElementException> { stack.pop() }
    }


    @Test
    fun `test popAll`() {
        var stack = ArrayDeque(listOf(1, 2, 3))
        var popped = stack.popAll()
        assertEquals(listOf(1, 2, 3), popped)
        assertTrue(stack.isEmpty())
        stack = ArrayDeque(listOf(1, 2, 3))
        popped = stack.popAll(2)
        assertEquals(listOf(1,2), popped)
        assertEquals(listOf(3), stack)
        assertFailsWith<IllegalArgumentException> { stack.popAll(5) }
    }

    @Test
    fun `test peek`() {
        var stack = ArrayDeque(listOf(1, 2, 3))
        assertEquals(1, stack.peek())
        assertEquals(3, stack.size)
        stack = ArrayDeque<Int>()
        assertFailsWith<NoSuchElementException> { stack.peek() }
    }


    @Test
    fun `test peekAll`() {
        val stack = ArrayDeque(listOf(1, 2, 3))
        assertEquals(listOf(1, 2), stack.peekAll(2))
        assertEquals(3, stack.size)
        assertEquals(listOf(1, 2,3), stack.peekAll())
        assertFailsWith<IllegalArgumentException> { stack.peekAll(5) }
    }

    @Test
    fun `popOrNull should return the top element or null`() {
        val stack = ArrayDeque(listOf(1, 2))
        assertEquals(1, stack.popOrNull())
        assertEquals(2, stack.popOrNull())
        assertNull(stack.popOrNull())
    }

    @Test
    fun `peekOrNull should return the top element or null`() {
        val stack = ArrayDeque(listOf(1))
        assertEquals(1, stack.peekOrNull())
        stack.pop()
        assertNull(stack.peekOrNull())
    }
}
