import java.nio.file.Paths

@ExperimentalUnsignedTypes
fun main(args: Array<String>) {
    val folderPath = Paths.get(System.getProperty("user.home"), args[0])

    val resultFile = folderPath.resolve(args[2]).toFile()
    assert(resultFile.createNewFile())

    resultFile.bufferedWriter().use { writer ->
        val ipsContainer = IpsContainer.createNew()

        folderPath.resolve(args[1]).toFile().forEachLine {
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