package com.kundalini.core

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.net.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.persistence.*

interface ApiService {
    /**
     * @return the value of the session cookie on success, or null on failure
     */
    suspend fun login(username: String, password: String, environment: Environment): String?

    /**
     * @return a list of [dataBlob] objects for each card on success or an empty list on failure
     */
    suspend fun dataBlob(session: String, environment: Environment): List<dataBlob>

    /**
     * @return true if any data is returned for the given account, false otherwise
     */
    suspend fun getInformation(
        session: String,
        dataBlob: dataBlob,
        environment: Environment
    ): Boolean
}

class OkHttpApiService : ApiService {
    private val client = OkHttpClient.Builder()
        .cookieJar(object : CookieJar {
            private val savedCookies: MutableMap<String, List<Cookie>> = mutableMapOf()
            override fun loadForRequest(url: HttpUrl): List<Cookie> = savedCookies.get(url.host) ?: emptyList()

            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                savedCookies[url.host] = cookies
            }
        })
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .proxySelector(object : ProxySelector() {
            val proxies = mutableListOf(
                Proxy(Proxy.Type.HTTP, InetSocketAddress("http://localhost:8080", 8080)),
                Proxy(Proxy.Type.HTTP, InetSocketAddress("http://localhost:8585", 8585)),
                Proxy.NO_PROXY
            )

            override fun select(uri: URI?): MutableList<Proxy> {
                return proxies
            }

            override fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?) {
                // TODO: figure out what to do here. Ignore? Re-order list of proxies?
                System.err.println("Connection to $uri failed with proxy $sa")
                proxies.rotate()
            }

        })
        .build()

    override suspend fun login(username: String, password: String, environment: Environment): String? =
        withContext(Dispatchers.IO) {
            val authString = Base64.getEncoder().encode("$username:$password".toByteArray()) ?: return@withContext null
            val response = sendRequest(
                mapOf("Authorization" to "Basic ${String(authString)}"),
                LOGIN_BODY,
                "mobile/signin",
                environment
            ) ?: return@withContext null
            val json =
                Parser.default().parse(StringBuilder(response.body?.string())) as? JsonObject ?: return@withContext null
            return@withContext if (json["status"] == "SUCCESS") {
                response.header("session", null)
            } else {
                null
            }
        }

    override suspend fun dataBlob(session: String, environment: Environment): List<dataBlob> =
        withContext(Dispatchers.IO) {
            val contentBoard = ArrayList<dataBlob>()
            val response = sendRequest(
                mapOf("securitydata" to session),
                COMMON_BODY,
                "mobile/v1/dataBlob",
                environment
            ) ?: return@withContext contentBoard
            val body = response.body?.string()
            if (body.isNullOrBlank()) {
                return@withContext contentBoard
            }

            val json = Parser.default().parse(StringBuilder(body)) as JsonObject
            val dataBlob = json.resolvePath("SVCResponse.responseData.responseBody.dataBlob") as? JsonArray<*>
                ?: return@withContext contentBoard
            dataBlob.forEach {
                val data = it as? JsonObject ?: return@forEach
                val key = data["key"] as? String ?: return@forEach
                val accountToken = data["accountToken"] as? String ?: return@forEach
                contentBoard.add(dataBlob(key, accountToken))
            }
            return@withContext contentBoard
        }

    override suspend fun getInformation(session: String, dataBlob: dataBlob, environment: Environment): Boolean =
        withContext(Dispatchers.IO) {
            val dateFormat = SimpleDateFormat("YYYY-mm-dd")
            val calendar = GregorianCalendar(TimeZone.getTimeZone("UTC"))
            val toDate = dateFormat.format(calendar.time)
            calendar.add(Calendar.YEAR, -5)
            val fromDate = dateFormat.format(calendar.time)
            val response = sendRequest(
                mapOf("Authorization" to """type="session", value="$session""""),
                CONTENT_BODY.format(dataBlob.key, dataBlob.accountToken, fromDate, toDate),
                "mobile/v3/getInformation",
                environment
            ) ?: return@withContext false
            val body = response.body?.string()
            if (body.isNullOrBlank()) {
                return@withContext false
            }

            val json = Parser.default().parse(StringBuilder(body)) as JsonObject
            if (json["responseMessage"] != "SUCCESS") {
                return@withContext false
            }
            val dataBlobDetails = json.resolvePath("responseData.contentBoard.0.dataBlob") as? JsonArray<*>
                ?: return@withContext false

            dataBlobDetails.forEach {
                val data = it as? JsonObject ?: return@forEach
                val pastRequests = data.resolvePath("transactionData.pastRequests") as? JsonArray<*> ?: return@forEach
                if (pastRequests.size > 0) return@withContext true
            }
            return@withContext false
        }

    private suspend fun sendRequest(
        headers: Map<String, String>,
        body: String,
        path: String,
        environment: Environment
    ): Response? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .addHeader("Content-Type", "application/json")
            .url(environment.url(path))
            .post(body.toRequestBody("application/json".toMediaType()))
        headers.forEach { key, value ->
            request.addHeader(key, value)
        }

        try {
            client.newCall(request.build()).execute()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

enum class Environment(val subdomain: String) {
    E1("e1qonline"),
    E2("e2qonline"),
    E3("online");

    fun url(path: String) = "https://$subdomain.mock1.com/$path"
}

@Entity
@Table(name = "test_account")
data class TestAccount(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int? = null,
    @Column(unique = true)
    var username: String = "",
    var password: String = "abcd1234",
    var environment: Environment = Environment.E1
)

data class TestAccountStatus(
    val account: TestAccount,
    val login: Boolean? = null,
    val dataBlob: Boolean? = null,
    val pastRequests: Boolean? = null
) : Comparable<TestAccountStatus> {
    val username = account.username
    val environment = account.environment

    override fun compareTo(other: TestAccountStatus): Int =
        Comparator.comparing(TestAccountStatus::environment)
            .thenComparing(TestAccountStatus::username)
            .thenComparing { o1, o2 -> o1.login.compareTo(o2.login) }
            .thenComparing { o1, o2 -> o1.dataBlob.compareTo(o2.dataBlob) }
            .thenComparing { o1, o2 -> o1.pastRequests.compareTo(o2.pastRequests) }
            .compare(this, other)

    override fun equals(other: Any?): Boolean {
        return other is TestAccountStatus && account == other.account
    }

    override fun hashCode(): Int {
        return account.hashCode()
    }
}

data class dataBlob(val key: String, val accountToken: String)

private val COMMON_BODY = """
{
  "common": {
    "appVersion": "8.3",
    "channelIndicator": "iOS/Andriod/Desktop",
    "clientAppId": "OpenMobileApp",
    "deviceInfo": {
      "deviceModel": "iPhone Simulator",
      "hardwareId": "1E8DBD15-37BD-4B06-BC8D-76BA2ja0084WW",
      "platformOS": "iPhone OS",
      "platformOSVersion": "6.1",
      "timeStamp": "1375211314202",
      "timeZoneOffset": "4269767296"
    },
    "locale": {
      "country": "USA",
      "language": "english"
    }
  }
}
""".trimIndent()

private val LOGIN_BODY = """
{
  "clientid": "RMNativeApp",
  "common": {
    "channelIndicator": "Mobile",
    "clientAppId": "RMNativeApp",
    "deviceInfo": {
      "deviceModel": "iPhone",
      "hardwareId": "956028013f3742ifhesdjkfhdsjkfhkds0d",
      "osBuild": "iPhone OS5.1.1",
      "timeStamp": "1347387999006",
      "timeZoneOffset": "49845767296"
    },
    "locale": {
      "country": "USA",
      "language": "english"
    },
    "requestType": "upload"
  },
  "face": "en_US",
  "rememberme": "yes",
  "remembermeenabled": "false"
}
""".trimIndent()

/**
 * Used to send the request for getInformation request. Requires 4 format parameters, in this order:
 * 1. key: String
 * 2. accountToken: String
 * 3. fromDate: String in YYYY-mm-dd format
 * 4. toDate: String in YYYY-mm-dd format
 */
private val CONTENT_BODY = """
{
    "requestData": {
        "qbStatusReqd": true,
        "demogReqd": true,
        "consumercontentBoardReqd": false,
        "corpcontentBoardReqd": true,
        "opencontentBoardReqd": true,
        "contentContext": [
            {
            "key": "%s",
            "accountToken": "%s"
            }
        ],
        "request": {
            "qbDetailReqd": true,
            "timePeriodsReqd": true,
            "includeAllTags": true,
            "infoRequest": {
                "realTime": true,
                "fragments": {
                    "deal": true
                },
                "tpIndex": [
                    -99
                ],
                "transactionRequest": {
                    "pagination": {
                        "startRecord": 1,
                        "recordsPerPage": 250
                    },
                    "sortCriteria": {
                        "sortBy": 1,
                        "sortOrder": "DESC"
                    },
                    "filterCriteria": {
                        "fromDate": "%s",
                        "toDate": "%s"
                    },
                    "includeAdditionalInfo": {
                        "category": true,
                        "tags": true,
                        "otherAttributes": false,
                        "etd": false,
                        "split": false
                    }
                }
            }
        }
    },
    "common": {
        "deviceInfo": {
            "deviceModel": "SM-GEJR0U1",
            "hardwareId": "a214UOFbc5251e",
            "timeStamp": "1550599513",
            "timeZoneOffset": "19800000",
            "platformOSVersion": "Android OS 8.0.0",
            "platformOS": "AndroidOS"
        },
        "channelIndicator": "Mobile",
        "clientAppId": "RMNativeApp",
        "locale": {
            "language": "english",
            "country": "USA"
        },
        "requestType": null
        }
}
""".trimIndent()