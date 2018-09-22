package emil.beothy.utilFun

/**
 * removes all [substring] from the string
 * @param [substring] string that gets removed from this string
 */
fun String.removeAllSubstrings(substring: String): String{
    var substringsRemoved = 0
    var returnString = copy()
    
    forEachIndexed { index, c ->
        if(hasSubstringAt(index, substring)){
            returnString = returnString.removeRange(index - (substringsRemoved * substring.length),
                    index + substring.length - (substringsRemoved * substring.length))
            
            substringsRemoved ++
        }
    }
    
    return returnString
}

/**
 * returns true if the [substring] is starting at [pos]
 * @param1 [pos] position the comparison starts
 * @param2 [substring] string that is looked for
 */
fun String.hasSubstringAt(pos: Int, substring: String): Boolean{
    for((i, char) in substring.withIndex()){
        if (get(pos + i) != char) return false
    }
    return true
}

/**
 * @returns the number of times the [substring] is contained in [this] string
 * @param [substring] the string that is searched for and counted
 */
fun String.countSubstrings(substring: String): Int{
    var count = 0
    forEachIndexed { index, c ->
        if (hasSubstringAt(index, substring)) count++
    }
    return count
}

/**
 * @returns the stating positions of the [substring] in [this] string
 * @param [substring] the string that is searched for
 */
fun String.findPositionOfSubstring(substring: String): List<Int>{
    val returnList = mutableListOf<Int>()
    forEachIndexed { index, c ->
        if (hasSubstringAt(index, substring)) returnList.add(index)
    }
    return returnList.toList()
}

/**
 * removes all chars that are in [other] from this
 */
operator fun String.minus(other: String): String{
    val l1 = this.toMutableList()
    other.forEach { l1.remove(it) }
    return l1.joinToString(separator = "")
}

/**
 * returns a copy of the string
 * https://www.rosettacode.org/wiki/Copy_a_string#Kotlin
 */
fun String.copy() = "" + this