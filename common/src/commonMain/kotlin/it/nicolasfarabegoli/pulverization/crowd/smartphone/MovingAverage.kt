package it.nicolasfarabegoli.pulverization.crowd.smartphone

/**
 * TODO.
 */
class MovingAverage<in T>(private val period: Int) where T : Number, T : Comparable<T> {

    private val queue = ArrayDeque<T>(period)

    fun add(element: T) {
        if (queue.size > period) {
            queue.removeFirst()
        }
        queue.addLast(element)
    }

    fun mean(): Double = queue.sumOf { it.toDouble() } / period
}
