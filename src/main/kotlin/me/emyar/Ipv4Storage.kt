package me.emyar

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import java.util.concurrent.atomic.AtomicLong

class Ipv4Storage {

    private val counter: () -> Unit = { _uniqueIpsCount.getAndIncrement() }

    private val secondBytes = Array(256) { IpBytesArray(1, counter) }

    private val _uniqueIpsCount = AtomicLong(0)
    val uniqueIpsCount: Long get() = _uniqueIpsCount.get()

    suspend fun saveIp(ipString: String) {
        val ipBytesStrings = ipString.splitIp()
        secondBytes[ipBytesStrings[0].toInt()].saveIp(ipBytesStrings)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun String.splitIp(): Array<String> {
        val result = Array(4) { "" }
        var currentArrayElementIndex = 0
        var byteStartPosition = 0

        var i = 1 // every byte in IPv4 is not empty
        do {
            if (get(i) == '.') {
                result[currentArrayElementIndex++] = substring(byteStartPosition, i)
                byteStartPosition = i + 1
                if (currentArrayElementIndex == 3)
                    break // there is no need to check string after third '.'
                i += 2 // skip first digit after '.'
            } else
                i++
        } while (true) // the cycle will be completed after finding the 3rd '.'
        result[currentArrayElementIndex] = substring(byteStartPosition, length)

        return result
    }
}

private sealed class IpBytesArrayAbstract(protected val counter: () -> Unit) {

    companion object {
        fun createNew(byteNumber: Int, counter: () -> Unit) =
            when (byteNumber) {
                3 -> FourthIpBytesArray(counter)
                else -> IpBytesArray(byteNumber, counter)
            }
    }

    protected val mutex = Mutex()

    abstract suspend fun saveIp(ipBytesStrings: Array<String>)
}

private class IpBytesArray(private val ipByteNumber: Int, counter: () -> Unit) : IpBytesArrayAbstract(counter) {

    private val nextIpBytesReferences = Array<IpBytesArrayAbstract?>(256) { null }

    override suspend fun saveIp(ipBytesStrings: Array<String>) {
        val ipByte = ipBytesStrings[ipByteNumber].toInt()
        val nextIpBytesArray = nextIpBytesReferences[ipByte] ?: mutex.withLock {
            nextIpBytesReferences[ipByte] ?: run {
                val newNextIpsBytesArray = createNew(ipByteNumber + 1, counter)
                nextIpBytesReferences[ipByte] = newNextIpsBytesArray
                newNextIpsBytesArray
            }
        }
        nextIpBytesArray.saveIp(ipBytesStrings)
    }
}

private class FourthIpBytesArray(counter: () -> Unit) : IpBytesArrayAbstract(counter) {

    private val fourthIpBytesStorage = BitSet(256)

    override suspend fun saveIp(ipBytesStrings: Array<String>) {
        val ipByte = ipBytesStrings[3].toInt()
        if (!fourthIpBytesStorage[ipByte])
            mutex.withLock {
                if (!fourthIpBytesStorage[ipByte]) {
                    fourthIpBytesStorage[ipByte] = true
                    counter()
                }
            }
    }
}