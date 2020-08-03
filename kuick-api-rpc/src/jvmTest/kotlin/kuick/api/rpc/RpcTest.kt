package kuick.api.rpc

import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import kotlin.test.assertTrue

class RpcTest {

    companion object {
        lateinit var rpcServer: RpcServer
        lateinit var sampleSrv: SampleService
        val sampleUser = SampleUser("Test", 1, 1.0)

        @BeforeClass @JvmStatic fun setup() {
            rpcServer = RpcServer("SampleRPC", 8080, listOf(SampleServiceIml()))
            rpcServer.start(false)
            sampleSrv = RemoteSampleService("http://localhost:8080", RpcClientJvm())
        }

        @AfterClass @JvmStatic fun teardown() {
            rpcServer.stop()
        }

    }

    @Test
    fun testRpcWithoutParams() = runBlocking {
        assertTrue(sampleSrv.getTimestamp() > 0L)
    }


    @Test
    fun testRpcWithSeveralSimpleParams() = runBlocking {
        assertEquals(sampleUser, sampleSrv.getSampleUser(sampleUser.name, sampleUser.age, sampleUser.height))
    }


    @Test
    fun testRpcWithComplexParams() = runBlocking {
        assertEquals(sampleUser, sampleSrv.getSampleUserCopy(sampleUser))
    }

}
