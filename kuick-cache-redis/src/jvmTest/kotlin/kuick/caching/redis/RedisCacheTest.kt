package kuick.caching.redis

import io.lettuce.core.*
import kotlinx.coroutines.*
import kuick.caching.*
import kuick.util.*
import org.arquillian.cube.containerobject.*
import org.arquillian.cube.docker.impl.client.containerobject.dsl.*
import org.jboss.arquillian.junit.*
import org.junit.runner.*
import kotlin.test.*

@RunWith(Arquillian::class)
class RedisCacheTest {
    @DockerContainer
    var container = Container.withContainerName("kuick-redis-test")
            .fromImage("redis:5.0.4")
            .withPortBinding(6379)
            .withEnvironment("discovery.type", "single-node")
            .withConnectionMode(ConnectionMode.START_AND_STOP_AROUND_CLASS)
            .withAwaitStrategy(AwaitBuilder.logAwait("Ready to accept connections"))
            .build()

    data class MyClass(val v: Int)

    @Test
    fun test() = runBlocking {
        val cache = RedisCache("mycache", RedisURI.create("redis://${container.ipAddress}")).typeWithJson<MyClass>()
        val instance = MyClass(10)
        val result1 = cache.get("test") { instance }
        val result2 = cache.get("test") { instance }
        assertEquals(10, result1.v)
        assertEquals(10, result2.v)
        //assertSame(result1, instance, "First call should return the same object")
        assertNotSame(result1, instance, "First call does not return the same object since it uses typeWithJson")
        assertNotSame(result1, result2, "If deserialized, should be a different instance")
    }

    @Test
    fun testWithRedisInvalidation() = runBlocking {
        InvalidationRedisClient().use { cacheClient ->
            //InmemoryCache<String, Int>().let { cache ->
            InmemoryCache<String, Int>().withInvalidation("mycache2", cacheClient).use { cache ->
                val log = arrayListOf<Int>()
                log += cache.get("test") { 10 }
                log += cache.get("test") { 20 }
                cache.invalidate("test")
                delay(1000L)
                log += cache.get("test") { 30 }
                assertEquals("10:10:30", log.joinToString(":"))
            }
        }
    }
}