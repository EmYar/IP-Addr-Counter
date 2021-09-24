@ExperimentalUnsignedTypes
interface IpsContainer {

    companion object {
        fun createNew(): IpsContainer =
            IpPartAbstract.createNewPart(UByte.MIN_VALUE)
    }

    fun addIp(ipAsArr: List<Int>)
    fun getUniqueIps(): Sequence<UByteArray>
}

@ExperimentalUnsignedTypes
private abstract class IpPartAbstract : IpsContainer {

    companion object {
        fun createNewPart(partNumber: UByte): IpPartAbstract =
            if (partNumber <= 2u)
                IpPartImpl(partNumber)
            else
                LastIpPart()
    }

    override fun getUniqueIps(): Sequence<UByteArray> =
        getExistsIpsInternal(ubyteArrayOf(UByte.MIN_VALUE, UByte.MIN_VALUE, UByte.MIN_VALUE, UByte.MIN_VALUE))

    abstract fun getExistsIpsInternal(ipAsArr: UByteArray): Sequence<UByteArray>
}

@ExperimentalUnsignedTypes
private class IpPartImpl(private val partNumber: UByte) : IpPartAbstract() {

    private val existsIpParts = Array<IpPartAbstract?>(256) { null }

    override fun addIp(ipAsArr: List<Int>) {
        val part = ipAsArr[partNumber.toInt()]
        val nextPart = existsIpParts[part] ?: run {
            val newPart = createNewPart(partNumber.inc())
            existsIpParts[part] = newPart
            newPart
        }
        nextPart.addIp(ipAsArr)
    }

    override fun getExistsIpsInternal(ipAsArr: UByteArray): Sequence<UByteArray> =
        existsIpParts.asSequence()
            .flatMapIndexed { ipByte, nextPart ->
                if (nextPart != null) {
                    val newIpAsArr = ipAsArr.copyOf()
                        .apply { this[partNumber.toInt()] = ipByte.toUByte() }
                    nextPart.getExistsIpsInternal(newIpAsArr)
                } else
                    emptySequence()
            }
}

@ExperimentalUnsignedTypes
private class LastIpPart : IpPartAbstract() {

    private val ipParts = BooleanArray(256) { false }

    override fun addIp(ipAsArr: List<Int>) {
        ipParts[ipAsArr[3]] = true
    }

    override fun getExistsIpsInternal(ipAsArr: UByteArray): Sequence<UByteArray> =
        ipParts.asSequence()
            .mapIndexedNotNull { ipByte, isExists ->
                if (isExists)
                    ipAsArr.copyOf()
                        .apply { this[3] = ipByte.toUByte() }
                else
                    null
            }
}