package me.emyar

import java.io.File

fun main(args: Array<String>) {
    val inputFileName = args.getOrElse(0) { "${System.getProperty("user.home")}/IP-Addr-Counter/ip_addresses" }

    val storage = Ipv4Storage()

    File(inputFileName)
        .forEachLine(action = storage::saveIp)

    println(storage.uniqueCount)
}