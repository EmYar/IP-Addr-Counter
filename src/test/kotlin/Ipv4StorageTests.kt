import kotlinx.coroutines.runBlocking
import me.emyar.Ipv4Storage
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Ipv4StorageTests {

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

    private fun abstractCountTest(testIps: Array<String>, expectedCount: Long) = runBlocking {
        val storage = Ipv4Storage()
        testIps.forEach { storage.saveIp(it) }
        assertEquals(expectedCount, storage.getUniqueIpsCount())
    }
}