package me.emyar

import java.nio.file.Paths

fun main(args: Array<String>) {
    val folderPathString = args.getOrElse(0) { "${System.getProperty("user.home")}/ip_addresses" }
    val inputFileName = args.getOrElse(1) { "ip_addresses" }
    val outputFileName = args.getOrElse(2) { "ip_addresses_unique" }

    val folderPath = Paths.get(folderPathString)

    val resultFile = folderPath.resolve(outputFileName).toFile()
    assert(resultFile.createNewFile())

    resultFile.bufferedWriter().use { writer ->
        val storage = Ipv4Storage()

        folderPath.resolve(inputFileName).toFile()
            .forEachLine(action = storage::saveIp)

        storage.getAllUniqueIps()
            .forEach {
                writer.write(it.joinToString("."))
                writer.newLine()
            }
    }
}