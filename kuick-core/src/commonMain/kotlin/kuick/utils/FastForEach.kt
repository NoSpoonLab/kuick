package kuick.utils

// Allocation-Free forEach (no iterator is created). Also *no* ConcurrentModification is thrown.
inline fun <T> List<T>.fastForEach(callback: (T) -> Unit) {
    var n = 0
    while (n < size) {
        callback(this[n])
        n++
    }
}
