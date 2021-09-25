import me.emyar.Ipv4Storage
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class Ipv4StorageTests {

    @Test
    fun emptyIpsListTest() = abstractListTest(
        emptyArray(),
        emptyArray()
    )

    @Test
    fun uniqueIpsListTest() = abstractListTest(
        arrayOf(
            "192.168.0.1",
            "192.168.0.1",
            "192.168.0.2"
        ),
        arrayOf(
            listOf(192, 168, 0, 1),
            listOf(192, 168, 0, 2),
        )
    )

    @Test
    fun emptyIpsCountTest() {
        abstractCountTest(emptyArray(), 0)
    }

    @Test
    fun uniqueIpsCountTest() {
        abstractCountTest(
            arrayOf(
                "192.168.0.1",
                "192.168.0.1",
                "192.168.0.2"
            ),
            2
        )
    }

    private fun abstractListTest(testIps: Array<String>, expectedIps: Array<List<Int>>) {
        val storage = Ipv4Storage()
        testIps.forEach(storage::saveIp)
        val uniqueIps = storage.getAllUniqueIps().toList()
        assertEquals(expectedIps.size, uniqueIps.size, "The number of expected and generated unique IPs is not equal ")
        expectedIps.forEachIndexed { i, it ->
            assertContentEquals(it, uniqueIps[i])
        }
    }

    private fun abstractCountTest(testIps: Array<String>, expectedCount: Long) {
        val storage = Ipv4Storage()
        testIps.forEach(storage::saveIp)
        assertEquals(expectedCount, storage.uniqueCount)
    }
}