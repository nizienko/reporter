package core.entity

import org.bson.types.ObjectId

data class TestResult(
        val _id: ObjectId? = null,
        var issue: String = "",
        var passed: Boolean = false,
        var checked: Boolean = false,
        var testSuiteName: String = "",
        var suiteName: String = "",
        var testName: String = "",
        var comment: String = "",
        var startTime: Long = 0,
        var endTime: Long = 0)