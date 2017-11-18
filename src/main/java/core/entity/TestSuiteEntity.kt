package core.entity

import org.bson.types.ObjectId

data class TestSuiteEntity(
        val _id: ObjectId? = null,
        var testSuiteName: String = "",
        var comment: String = "",
        var passed: Boolean = false,
        var isArmChecked: Boolean = false,
        var isLogsChecked: Boolean = false,
        var createdTime: Long = 0L
)