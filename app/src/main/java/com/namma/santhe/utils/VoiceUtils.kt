package com.namma.santhe.utils

object VoiceUtils {

    /**
     * Normalizes a string containing numbers in various regional scripts to standard ASCII digits.
     * Also handles some common word representations if possible.
     */
    fun normalizePhoneNumber(input: String): String {
        var result = ""
        
        // Map of regional digits to standard digits
        val digitMap = mapOf(
            // Kannada
            '೦' to '0', '೧' to '1', '೨' to '2', '೩' to '3', '೪' to '4', 
            '೫' to '5', '೬' to '6', '೭' to '7', '೮' to '8', '೯' to '9',
            // Hindi / Devanagari
            '०' to '0', '१' to '1', '२' to '2', '३' to '3', '४' to '4', 
            '५' to '5', '६' to '6', '७' to '7', '८' to '8', '९' to '9',
            // Telugu
            '౦' to '0', '౧' to '1', '౨' to '2', '౩' to '3', '౪' to '4', 
            '౫' to '5', '౬' to '6', '౭' to '7', '౮' to '8', '౯' to '9',
            // Tamil (though modern Tamil often uses standard digits)
            '௦' to '0', '௧' to '1', '௨' to '2', '௩' to '3', '௪' to '4', 
            '௫' to '5', '௬' to '6', '௭' to '7', '௮' to '8', '௯' to '9'
        )

        for (char in input) {
            when {
                char.isDigit() -> {
                    if (char in '0'..'9') {
                        result += char
                    } else {
                        // Check regional map
                        result += digitMap[char] ?: ""
                    }
                }
                // Ignore spaces and common separators
                char.isWhitespace() || char == '-' || char == '(' || char == ')' -> {
                    continue
                }
            }
        }

        return result
    }

    /**
     * Replaces regional digits with standard ASCII digits in any string.
     */
    fun normalizeDigits(input: String): String {
        val digitMap = mapOf(
            '೦' to '0', '೧' to '1', '೨' to '2', '೩' to '3', '೪' to '4', 
            '೫' to '5', '೬' to '6', '೭' to '7', '೮' to '8', '೯' to '9',
            '०' to '0', '१' to '1', '२' to '2', '३' to '3', '४' to '4', 
            '५' to '5', '६' to '6', '७' to '7', '८' to '8', '९' to '9',
            '౦' to '0', '౧' to '1', '౨' to '2', '౩' to '3', '౪' to '4', 
            '౫' to '5', '౬' to '6', '౭' to '7', '౮' to '8', '౯' to '9',
            '௦' to '0', '௧' to '1', '௨' to '2', '௩' to '3', '௪' to '4', 
            '௫' to '5', '௬' to '6', '௭' to '7', '௮' to '8', '௯' to '9'
        )

        val sb = StringBuilder()
        for (char in input) {
            val normalized = digitMap[char]
            if (normalized != null) {
                sb.append(normalized)
            } else {
                sb.append(char)
            }
        }
        return sb.toString()
    }
}
