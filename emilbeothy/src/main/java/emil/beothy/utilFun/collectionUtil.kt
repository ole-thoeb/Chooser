package emil.beothy.utilFun

/**
 * @param copy (a lambda) that returns a copy of the object at given position
 * @return deep copy of the list (one level deep)
 */
inline fun <T> List<T>.deepCopy(crossinline copy: (T) -> T) = List(size){ copy(get(it)) }

/**
 * @param copy (a lambda) that returns a copy of the object at given position
 * @return deep copy of the array (one level deep)
 */
inline fun <reified T> Array<T>.deepCopy(crossinline copy: (T) -> T) = Array(size){ copy(get(it)) }

/**
 * @return shallow copy of the list
 */
fun <T> List<T>.copyOf() = List(size){ get(it) }

/**
 * @param elements: collection of elements the collection should be checked for
 * @return true if all elements are in this collection, in high enough quantity otherwise false
 */
fun <T> Collection<T>.containsAllAlsoMultiple(elements: Collection<T>): Boolean{
    val copyList = toList().copyOf().toMutableList()
    elements.forEach{
        if (it !in copyList) return false
        copyList.remove(it)
    }
    return true
}

/**
 * @param1 newSize: size the List should be reduced to
 * @param2 predicate: condition the element has to satisfy to be dropped
 * @return list without the elements that match the predicate till either
 *      1. no more elements match the predicate
 *      2. the newSize is reached
 */
inline fun <T> MutableList<T>.dropToNewSize(newSize: Int, crossinline predicate: (T) -> Boolean): MutableList<T>{
    val copy = copyOf().toMutableList()
    var noMatchingElements = false
    while (copy.size >= newSize && !noMatchingElements){
        val element = copy.find { predicate(it) }
        
        if (element != null) copy.remove(element)
        else noMatchingElements = true
    }
    return copy
}