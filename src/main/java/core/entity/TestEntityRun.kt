package core.entity

data class TestEntityRun(
        var startTime: Long = 0,
        var endTime: Long = 0,
        var comment: String = "",
        var passed: Boolean = false
)