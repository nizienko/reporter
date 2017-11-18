package core.control

import core.db.ReporterRepository
import core.entity.TestResult
import core.entity.TestSuiteEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
class TestResultsControl {

    @Autowired lateinit var repository: ReporterRepository


    @RequestMapping(value = "/testSuite", method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun testSuite(@ModelAttribute testSuiteEntity: TestSuiteEntity): Answer = answer {
        repository.saveTestSuite(testSuiteEntity)
    }

    @RequestMapping(value = "/test", method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun test(@ModelAttribute test: TestResult): Answer = answer {
        repository.saveTestResult(test)
    }


    data class Answer(val result: String, val message: String)

    private fun answer(block: ()-> Unit): Answer = try {
        block()
        Answer("ok", "success")
    }
    catch (exception: Exception) {
        Answer("error", exception.message.toString())
    }
}