package kuick.caching.redis

import io.lettuce.core.*
import kotlinx.coroutines.*
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
        val cache = RedisCache<MyClass>("mycache", RedisURI.create("redis://${container.ipAddress}"))
        val instance = MyClass(10)
        val result1 = cache.get("test") { instance }
        val result2 = cache.get("test") { instance }
        assertEquals(10, result1.v)
        assertEquals(10, result2.v)
        assertSame(result1, instance, "First call should return the same object")
        assertNotSame(result1, result2, "If deserialized, should be a different instance")
    }
}