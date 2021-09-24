package me.emyar

import java.nio.file.Paths

@ExperimentalUnsignedTypes
fun main(args: Array<String>) {
    val folderPathString = args.getOrElse(0) { "${System.getProperty("user.home")}/ip_addresses" }
    val inputFileName = args.getOrElse(1) { "ip_addresses" }
    val outputFileName = args.getOrElse(2) { "ip_addresses_unique" }

    val folderPath = Paths.get(folderPathString)

    val resultFile = folderPath.resolve(outputFileName).toFile()
    assert(resultFile.createNewFile())

    resultFile.bufferedWriter().use { writer ->
        val ipsContainer = IpsContainer.createNew()

        folderPath.resolve(inputFileName).toFile().forEachLine {
            ipsContainer.addIp(
                it.split('.')
                    .map(String::toInt)
            )
        }

        ipsContainer.getUniqueIps()
            .forEach {
                writer.write(it.joinToString("."))
                writer.newLine()
            }
    }
}