package me.emyar

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.concurrent.thread

private const val readedIpsArraysSize = 4096

fun main(args: Array<String>) = runBlocking {
    val inputFileName = args.getOrElse(0) { "${System.getProperty("user.home")}/IP-Addr-Counter/ip_addresses" }

    val storage = Ipv4Storage()

    val channel = Channel<Array<String?>>()

    thread {
        runBlocking {
            File(inputFileName).bufferedReader().use { reader ->
                var array = Array<String?>(readedIpsArraysSize) { null }
                var i = 0
                reader.lineSequence()
                    .forEach {
                        array[i++] = it
                        if (i == array.size) {
                            channel.send(array)
                            array = Array(readedIpsArraysSize) { null }
                            i = 0
                        }
                    }
            }
            channel.close()
        }
    }

    runBlocking {
        for (array in channel) {
            for (ip in array) {
                if (ip == null)
                    break
                storage.saveIp(ip)
            }
        }
    }

    println(storage.uniqueIpsCount)
}