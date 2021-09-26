package me.emyar

import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.concurrent.Executors.newFixedThreadPool

private val defaultInputFilePath = "${System.getProperty("user.home")}/IP-Addr-Counter/ip_addresses"
private const val defaultThreadsCount = 5

private const val ipsBuffer = 4096
private const val arraysInBufferPerWorker = 10

fun main(args: Array<String>) = runBlocking {
    val inputFilePath = args.getOrElse(0) { defaultInputFilePath }
    val threadsCount = args.getOrNull(1)?.toInt() ?: defaultThreadsCount

    val storage = Ipv4Storage()

    val channel = Channel<Array<String?>>(threadsCount * arraysInBufferPerWorker)

    newFixedThreadPool(threadsCount).asCoroutineDispatcher().use { coroutineDispatcher ->
        launch(coroutineDispatcher) {
            File(inputFilePath).bufferedReader().use { reader ->
                var array = Array<String?>(ipsBuffer) { null }
                var i = 0
                reader.lineSequence()
                    .forEach {
                        array[i++] = it
                        if (i == array.size) {
                            channel.send(array)
                            array = Array(ipsBuffer) { null }
                            i = 0
                        }
                    }
            }
            channel.close()
        }

        val workersJobsList = mutableListOf<Job>()
        repeat(threadsCount - 1) { // one thread is for file reader
            workersJobsList += launch(coroutineDispatcher) {
                for (array in channel)
                    for (ip in array) {
                        if (ip == null)
                            break
                        storage.saveIp(ip)
                    }
            }
        }

        for (job in workersJobsList)
            job.join()

        println(storage.uniqueIpsCount)
    }
}