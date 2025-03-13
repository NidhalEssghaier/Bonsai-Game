package helper

/**
 * Pushes all the supplied elements onto the Stack. The element at index 0 of the List is pushed first.
 * @param elements The elements to push onto this Stack.
 */
fun <T> ArrayDeque<T>.pushAll(elements: Collection<T>) = elements.forEach { this.addFirst(it) }

/**
 * Pushes all the supplied elements onto the Stack. The element at index 0 of the List is pushed first.
 * @param elements The elements to push onto this Stack.
 */
fun <T> ArrayDeque<T>.pushAll(vararg elements: T) = elements.forEach { this.addFirst(it) }

/**
 * Pushes the supplied element onto the Stack.
 * @param element The element to push onto this Stack.
 */
fun <T> ArrayDeque<T>.push(element: T) = this.addFirst(element)

/**
 * Pops the topmost element in this Stack.
 * @throws NoSuchElementException If this Stack is empty.
 */
fun <T> ArrayDeque<T>.pop(): T = this.removeFirst()

/**
 * Pops the n topmost elements in this Stack, where n is specified by the parameter.
 * The topmost element in the stack gets the list's head i.e. index 0.
 * @param numToPop Specifies how many elements to pop.
 * @throws IllegalArgumentException If [numToPop] is negative or greater than the Stack's size.
 */
fun <T> ArrayDeque<T>.popAll(numToPop : Int = this.size): List<T> {
    require(numToPop in 0..this.size)
    val list = this.take(numToPop)
    repeat(numToPop) {this.removeFirst()}
    return list
}

/**
 * Returns the topmost element in this Stack but does not pop it.
 * @throws NoSuchElementException If the stack is empty.
 */
fun <T> ArrayDeque<T>.peek(): T = this.first()

/**
 * Returns the topmost n elements in the Stack but does not pop them.
 * The topmost element in the stack gets the list's head i.e. index 0.
 * @param numToPeek Specifies how many elements to peek.
 * @throw IllegalArgumentException If [numToPeek] is negative or greater than the Stack's size.
 */
fun <T> ArrayDeque<T>.peekAll(numToPeek : Int = this.size): List<T> {
    require(numToPeek in 0..this.size)
    return this.take(numToPeek)
}

/**
 * Pops the topmost element in this Stack.
 * @return The topmost element in this Stack or null if this Stack is empty.
 */
fun <T> ArrayDeque<T>.popOrNull() = this.removeFirstOrNull()

/**
 * Returns the topmost element in this Stack but does not pop it.
 * @return The topmost element in this Stack or null if this Stack is empty.
 */
fun <T> ArrayDeque<T>.peekOrNull() = this.firstOrNull()