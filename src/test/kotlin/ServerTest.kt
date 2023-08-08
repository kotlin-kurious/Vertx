import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServerTest {

    private val vertx: Vertx = Vertx.vertx()
    lateinit var client: WebClient
    lateinit var db: SqlClient

    @BeforeAll
    fun setup() {
        runBlocking {
            vertx.deployVerticle(ServerVerticle()).await()
            vertx.deployVerticle(DogsVerticle()).await()
            client = WebClient.create(
                vertx,
                WebClientOptions()
                    .setDefaultPort(8081)
                    .setDefaultHost("localhost")
            )
            db = Db.connect(vertx)
        }
    }

    @AfterAll
    fun tearDown() {
        // And you want to stop your server once
        vertx.close()
    }

    @Test
    fun `status should return 200`() {
        runBlocking {
            val response = client.get("/status").send().await()

            assertEquals(200, response.statusCode())
        }
    }

    @Nested
    inner class `With Dog` {
        private lateinit var dogRow: Row

        @BeforeEach
        fun createDogs() {
            runBlocking {
                val result = db.preparedQuery(
                    """INSERT INTO dogs (name, age)VALUES ($1, $2) RETURNING ID""".trimIndent()
                ).execute(Tuple.of("Binky", 7)).await()
                dogRow = result.first()
            }
        }

        @AfterEach
        fun deleteAll() {
            runBlocking {
                db.preparedQuery("DELETE FROM dogs")
                    .execute().await()
            }
        }

        @Test
        fun `delete deletes a dog by ID`() {
            runBlocking {
                val dogId = dogRow.getInteger(0)
                client.delete("/dogs/${dogId}").send().await()

                val result = db.preparedQuery("SELECT * FROM dogs WHERE id = $1")
                    .execute(Tuple.of(dogId)).await()

                assertEquals(0, result.size())
            }
        }

        @Test
        fun `put updates a dog by ID`() {
            runBlocking {
                val dogId = dogRow.getInteger(0)
                val requestBody = json {
                    obj("name" to "Meatloaf", "age" to 4)
                }
                client.put("/dogs/${dogId}")
                    .sendBuffer(Buffer.buffer(requestBody.toString()))
                    .await()

                val result = db.preparedQuery("SELECT * FROM dogs WHERE id = $1")
                    .execute(Tuple.of(dogId)).await()

                assertEquals("Meatloaf", result.first().getString("name"))
                assertEquals(4, result.first().getInteger("age"))
            }
        }
    }
}