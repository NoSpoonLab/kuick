package kuick.api.rpc

data class SampleUser(
    val name: String,
    val age: Int,
    val height: Double
)

interface SampleService {

    suspend fun getTimestamp(): Long

    suspend fun getUUID(): String

    suspend fun getSampleUser(name: String, age: Int, height: Double): SampleUser

    suspend fun getSampleUserCopy(sampleUser: SampleUser): SampleUser
}

class RemoteSampleService(
    serviceBaseUrl: String,
    rpcClient: RpcClient
): AbstractRemoteApi(serviceBaseUrl, SampleService::class, rpcClient), SampleService {

    override suspend fun getTimestamp(): Long = call("getTimestamp")

    override suspend fun getUUID(): String = call("getUUID")

    override suspend fun getSampleUser(name: String, age: Int, height: Double): SampleUser =
        call("getSampleUser", name, age, height)

    override suspend fun getSampleUserCopy(sampleUser: SampleUser): SampleUser  =
        call("getSampleUserCopy", sampleUser)

}
