package me.emyar

class Ipv4Storage {

    private val storedIps = Array<Array<Array<BooleanArray?>?>?>(256) { null }

    fun saveIp(ipString: String) {
        val ipBytesStrings = ipString.split(".")
        storedIps.saveByte(ipBytesStrings[0])
            .saveByte(ipBytesStrings[1])
            .saveByte(ipBytesStrings[2])
            .saveByte(ipBytesStrings[3])
    }

    fun getAllUniqueIps(): Sequence<List<Int>> =
        storedIps.asSequence()
            .flatMapIndexed { firstByte, secondByteArray ->
                secondByteArray?.asSequence()
                    ?.flatMapIndexed { secondByte, thirdByteArray ->
                        thirdByteArray?.asSequence()
                            ?.flatMapIndexed { thirdByte, fourthByteArray ->
                                fourthByteArray?.asSequence()
                                    ?.mapIndexedNotNull { fourthByte, isExists ->
                                        if (isExists)
                                            listOf(firstByte, secondByte, thirdByte, fourthByte)
                                        else
                                            null
                                    }
                                    ?: emptySequence()
                            }
                            ?: emptySequence()
                    }
                    ?: emptySequence()
            }

    private fun Array<Array<Array<BooleanArray?>?>?>.saveByte(firstByteString: String): Array<Array<BooleanArray?>?> {
        val firstByte = firstByteString.toInt()
        return this[firstByte] ?: run {
            val newSecondByteArray = Array<Array<BooleanArray?>?>(256) { null }
            this[firstByte] = newSecondByteArray
            newSecondByteArray
        }
    }

    private fun Array<Array<BooleanArray?>?>.saveByte(secondByteString: String): Array<BooleanArray?> {
        val secondByte = secondByteString.toInt()
        return this[secondByte] ?: run {
            val newThirdByteArray = Array<BooleanArray?>(256) { null }
            this[secondByte] = newThirdByteArray
            newThirdByteArray
        }
    }

    private fun Array<BooleanArray?>.saveByte(thirdByteString: String): BooleanArray {
        val thirdByte = thirdByteString.toInt()
        return this[thirdByte] ?: run {
            val newFourthByteArray = BooleanArray(256) { false }
            this[thirdByte] = newFourthByteArray
            newFourthByteArray
        }
    }

    private fun BooleanArray.saveByte(fourthByteString: String) {
        this[fourthByteString.toInt()] = true
    }
}