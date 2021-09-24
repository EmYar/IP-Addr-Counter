import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@ExperimentalUnsignedTypes
class IpPartTests {
    @Test
    fun emptyIpsTest() = abstractTest(
        emptyArray(),
        emptyArray()
    )

    @Test
    fun simpleTest() = abstractTest(
        arrayOf(
            listOf(192, 168, 0, 1),
            listOf(192, 168, 0, 1),
            listOf(192, 168, 0, 2)
        ),
        arrayOf(
            ubyteArrayOf(192u, 168u, 0u, 1u),
            ubyteArrayOf(192u, 168u, 0u, 2u)
        )
    )

    private fun abstractTest(testIps: Array<List<Int>>, expectedIps: Array<UByteArray>) {
        val head = IpsContainer.createNew()
        testIps.forEach(head::addIp)
        val uniqueIps = head.getUniqueIps().toList()
        assertEquals(expectedIps.size, uniqueIps.size, "The number of expected and generated unique IPs is not equal ")
        expectedIps.forEachIndexed { i, it ->
            assertContentEquals(it, uniqueIps[i])
        }
    }
}