package ru.beryukhov.client_lib

import kotlin.random.Random


@OptIn(ExperimentalStdlibApi::class)
fun generatePassword(
    length: Int = 8,
    isUpperCase: Boolean = true,
    isLowerCase: Boolean = true,
    isNumbersChars: Boolean = true,
    isSpecialChars: Boolean = true
): String {
    val passwordValue = StringBuilder()
    val alphabet = "abcdefghijklmnopqrstuvwxyz"
    val charsList = mutableListOf<Char>()
    // Add numbers to generation list
    if (isNumbersChars) {
        val numbersList = arrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0')
        charsList.addAll(numbersList)
    }
    // Add lowecase letters to generation list
    if (isLowerCase) {
        val arLowerChars = alphabet.toCharArray()
        charsList.addAll(arLowerChars.asIterable())
    }
    // Add uppercase letters to generation list
    if (isUpperCase) {
        val arUpperChars = alphabet.toUpperCase().toCharArray()
        charsList.addAll(arUpperChars.asIterable())
    }
    // Add special symbols to generation list
    if (isSpecialChars) {
        val specialChars = "~!@#$%^&*+-/.,\\{}[]();:"
        charsList.addAll(specialChars.asIterable())
    }
    if (charsList.size > 0) { // Generate password
        val random = Random.Default
        for (i in 0 until length) {
            val index = random.nextInt(charsList.size)
            passwordValue.append(charsList[index].toString())
        }
    } else {
        throw IllegalStateException("No characters for generation was chosen")
    }
    return passwordValue.toString()
}
