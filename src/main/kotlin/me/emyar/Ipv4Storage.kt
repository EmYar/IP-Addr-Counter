package me.emyar

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class Ipv4Storage {

    private val secondBytes = Array(256) { IpBytesArray(1) }

    suspend fun saveIp(ipString: String) {
        val ipBytesStrings = ipString.splitIp()
        secondBytes[ipBytesStrings[0].toInt()].saveIp(ipBytesStrings)
    }

    fun getUniqueIpsCount(coroutineDispatcher: CoroutineContext = EmptyCoroutineContext): Long = runBlocking {
        val result = AtomicLong(0)
        val jobsList = Array<Job?>(256) { null }
        secondBytes.forEachIndexed { i, secondBytesArray ->
            jobsList[i] = launch(coroutineDispatcher) {
                var currentArraySum = 0L
                secondBytesArray.getCountingSequence()
                    .forEach { _ -> currentArraySum++ }
                if (currentArraySum > 0) // avoids unnecessary usages of atomic var
                    result.addAndGet(currentArraySum)
            }
        }
        jobsList.forEach { it?.join() }
        result.get()
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

private sealed class IpBytesArrayAbstract {

    companion object {
        fun createNew(byteNumber: Int) =
            when (byteNumber) {
                3 -> FourthIpBytesArray()
                else -> IpBytesArray(byteNumber)
            }
    }

    abstract suspend fun saveIp(ipBytesStrings: Array<String>)
    abstract fun getCountingSequence(): Sequence<Any>
}

private class IpBytesArray(private val ipByteNumber: Int) : IpBytesArrayAbstract() {

    private val nextIpBytesReferences = Array<IpBytesArrayAbstract?>(256) { null }
    private val mutex = Mutex()

    override suspend fun saveIp(ipBytesStrings: Array<String>) {
        val ipByte = ipBytesStrings[ipByteNumber].toInt()
        val nextIpBytesArray = mutex.withLock {
            nextIpBytesReferences[ipByte] ?: run {
                val newNextIpsBytesArray = createNew(ipByteNumber + 1)
                nextIpBytesReferences[ipByte] = newNextIpsBytesArray
                newNextIpsBytesArray
            }
        }
        nextIpBytesArray.saveIp(ipBytesStrings)
    }

    override fun getCountingSequence(): Sequence<Any> =
        nextIpBytesReferences.asSequence()
            .filterNotNull()
            .flatMap(IpBytesArrayAbstract::getCountingSequence)
}

private class FourthIpBytesArray : IpBytesArrayAbstract() {

    private val fourthIpBytesArray = BooleanArray(256) { false }

    override suspend fun saveIp(ipBytesStrings: Array<String>) {
        val ipByte = ipBytesStrings[3].toInt()
        fourthIpBytesArray[ipByte] = true
    }

    override fun getCountingSequence(): Sequence<Any> =
        fourthIpBytesArray.asSequence()
            .filter { it }
}