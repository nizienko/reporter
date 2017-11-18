package core.db

import com.mongodb.client.FindIterable
import core.entity.*
import org.litote.kmongo.*
import org.springframework.stereotype.Repository


@Repository
open class ReporterRepository {
    private val db = KMongo.createClient(
            "127.0.0.1",
            27017
    ).getDatabase("reporter")!!

    fun saveTestResult(testResult: TestResult) = with(testResult) {
        val testEntity =
                db.getCollection<TestEntity>().findOne("{testSuiteName: '$testSuiteName', issue: '$issue'}")
        val testEntityRun = TestEntityRun(
                startTime,
                endTime,
                comment,
                passed)

        if (testEntity != null) {
            testEntity.runs.add(testEntityRun)
            if (testResult.passed) { testEntity.passed = true }
            db.getCollection<TestEntity>().updateOneById(testEntity._id!!, testEntity)
        } else {
            db.getCollection<TestEntity>().insertOne(TestEntity(
                    null,
                    issue,
                    passed,
                    checked,
                    testSuiteName,
                    suiteName,
                    testName,
                    "",
                    mutableListOf(testEntityRun)
            ))
        }
    }

    fun saveTestEntity(testEntity: TestEntity) = db.getCollection<TestEntity>().updateOneById(testEntity._id!!, testEntity)

    fun getTestSuiteByName(name: String) = db.getCollection<TestSuiteEntity>().findOne("{testSuiteName: '$name'}")
    fun getTestSuiteList(): FindIterable<TestSuiteEntity> = db.getCollection<TestSuiteEntity>().find()

    fun getTestEntitiesBySuiteName(name: String): FindIterable<TestEntity> = db.getCollection<TestEntity>().find("{testSuiteName: '$name'}")
    fun getTestEntitiesByIssue(issue: String): FindIterable<TestEntity> = db.getCollection<TestEntity>().find("{issue: '$issue'}")

    fun saveTestSuite(testSuiteEntity: TestSuiteEntity) = with(testSuiteEntity) {
        if (testSuiteName.isBlank()) throw IllegalArgumentException("Missed testRunName parameter")

        if (getTestSuiteByName(testSuiteName) != null) {
            db.getCollection<TestSuiteEntity>().updateOne("{testSuiteName: '$testSuiteName'}", this)
        } else {
            db.getCollection<TestSuiteEntity>().insertOne(this)
        }
    }

    fun getKnownIssues(): FindIterable<KnownIssue> =
            db.getCollection<KnownIssue>().find()
    fun getKnownIssues(issue: String): FindIterable<KnownIssue> =
            db.getCollection<KnownIssue>().find("{keyWords: {${MongoOperator.all}:['$issue']}}")

    fun updateKnownIssue(knownIssue: KnownIssue) =
            with(db.getCollection<KnownIssue>()) {
                if (knownIssue._id == null) {
                    insertOne(knownIssue)
                } else {
                    updateOneById(knownIssue._id!!, knownIssue)
                }
            }
    fun deleteKnownIssue(knownIssue: KnownIssue) =
            db.getCollection<KnownIssue>().deleteOneById(knownIssue._id!!)
}