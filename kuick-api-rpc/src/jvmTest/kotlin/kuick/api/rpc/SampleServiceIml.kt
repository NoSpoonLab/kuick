package kuick.api.rpc

import kuick.utils.randomUUID

class SampleServiceIml: SampleService {
    override suspend fun getTimestamp(): Long = System.currentTimeMillis()

    override suspend fun getUUID(): String  = "A random ID is ${randomUUID()}"

    override suspend fun getSampleUser(name: String, age: Int, height: Double): SampleUser =
        SampleUser(name, age, height)

    override suspend fun getSampleUserCopy(sampleUser: SampleUser): SampleUser = sampleUser.copy()

}
