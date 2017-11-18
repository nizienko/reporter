import core.entity.TestResult
import core.entity.TestSuiteEntity
import org.apache.http.Consts
import org.apache.http.client.fluent.Form
import org.apache.http.client.fluent.Request
import org.junit.jupiter.api.Test
import org.litote.kmongo.KMongo
import org.litote.kmongo.distinct
import org.litote.kmongo.getCollection

class DataLoadTest {
    val url = "http://127.0.0.1:8080"


    private val db = KMongo.createClient(
            "127.0.0.1",
            27017
    ).getDatabase("reporter")!!

    @Test
    fun getIssues() {
        val res = db.getCollection<TestResult>().distinct<String>("issue","{testRunName: 'QA.int.skrat-1-1.0.0', passed: true}")
        res.forEach{
            println(it)
        }
    }


    @Test
    fun loadData2() {
        for (i in 131..230) {
            val testSuiteName = "QA.int.skrat-$i-1.0.0";
            val testSuiteEntity = TestSuiteEntity(
                    null,
                    testSuiteName = testSuiteName,
                    comment = "Тестовый прогон №$i",
                    passed = false,
                    createdTime = System.currentTimeMillis()
            )
            with(testSuiteEntity) {
                val res = Request.Post("$url/testSuite")
                        .addHeader("charset", "UTF-8")
                        .bodyForm(Form.form()
                                .add("testSuiteName", testSuiteName)
                                .add("comment", comment)
                                .add("passed", passed.toString())
                                .add("createdTime", createdTime.toString())
                                .build(), Consts.UTF_8
                        ).execute().returnContent().asString()
                println(res)
            }
            (1..300).map {
                TestResult(
                        issue = "TC-$it",
                        passed = it*i % 11 != 1,
                        checked = false,
                        testSuiteName = testSuiteName,
                        suiteName = "Сьют тестов ${it % 10}",
                        testName = "Тест номер $it",
                        startTime = System.currentTimeMillis() - 10000,
                        endTime = System.currentTimeMillis(),
                        comment = "Прогон $testSuiteName j=$it"
                )
            }.forEach {
                with(it) {
                    val res = Request.Post("$url/test")
                            .addHeader("charset", "UTF-8")
                            .bodyForm(Form.form()
                                    .add("issue", issue)
                                    .add("passed", passed.toString())
                                    .add("testSuiteName", testSuiteName)
                                    .add("suiteName", suiteName)
                                    .add("testName", testName)
                                    .add("startTime", startTime.toString())
                                    .add("endTime", endTime.toString())
                                    .add("comment", comment)
                                    .build(), Consts.UTF_8)
                            .execute().returnContent().asString()
                    println(res)
                }
            }

        }
    }

}