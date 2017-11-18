package core.ui

import com.vaadin.icons.VaadinIcons
import com.vaadin.server.ExternalResource
import com.vaadin.server.VaadinRequest
import com.vaadin.shared.ui.ContentMode
import com.vaadin.spring.annotation.SpringUI
import com.vaadin.ui.*
import core.db.ReporterRepository
import core.entity.TestEntity
import core.entity.TestSuiteEntity
import core.showError
import core.status
import core.toReadableDate
import core.toReadableTime
import core.ui.components.*
import org.springframework.beans.factory.annotation.Autowired


@SpringUI
class ReporterUI : UI() {

    @Autowired lateinit var repository: ReporterRepository


    override fun init(request: VaadinRequest?) {
        val report = request?.parameterMap?.get("report")

        if (report != null && report.isNotEmpty()) {
            val testSuiteEntity = repository.getTestSuiteByName(report[0])
            if (testSuiteEntity == null) {
                showError("Прогон ${report[0]} не найден")
            } else {
                renderReport(testSuiteEntity)
            }
        } else {
            renderReportList()
        }
    }

    private fun renderReportList() {
        content = vertical {
            repository.getTestSuiteList().filter { it.testSuiteName.isNotBlank() }
                    .sortedByDescending { it.createdTime }
                    .forEach {
                        with(it) {
                            addComponent(horizontal {
                                addComponent(
                                        link {
                                            caption = testSuiteName
                                            resource = ExternalResource("/?report=$testSuiteName")
                                        }
                                )
                                addComponent(component<Label> { value = createdTime.toReadableDate() })
                                addComponent(component<Label> {
                                    contentMode = ContentMode.HTML
                                    value = if (passed) {
                                        "Ок".green() + " $comment"
                                    } else {
                                        "failed".red() + " $comment"
                                    }
                                })
                            })
                        }
                    }
        }
    }

    private val contentLayout = VerticalLayout()

    class ReportContext(
            private val testSuite: TestSuiteEntity,
            private val repository: ReporterRepository,
            private val contentLayout: VerticalLayout) {
        var testRunName = ""
        var passed = 0
        var failed = 0
        var unchecked = 0
        var renderContentFun: () -> VerticalLayout = { VerticalLayout() }

        var issues = listOf<TestEntity>()

        fun update() {
            testRunName = testSuite.testSuiteName
            issues = repository.getTestEntitiesBySuiteName(testSuite.testSuiteName).toList<TestEntity>()

            passed = issues.filter { it.passed }.count()
            failed = issues.filter { !it.passed }.count()
            unchecked = issues.filter { !it.passed && !it.checked }.count()

            with(contentLayout) {
                removeAllComponents()
                addComponent(renderContentFun())
            }
        }
    }

    private fun renderReport(testSuite: TestSuiteEntity) {
        val context = ReportContext(testSuite, repository, contentLayout)
        context.renderContentFun = { renderContent(testSuite, context) }
        context.update()
        content = contentLayout
    }

    private fun renderContent(testSuite: TestSuiteEntity, context: ReportContext) = vertical {
        addComponent(horizontal {
            defaultComponentAlignment = Alignment.MIDDLE_CENTER
            addComponent(
                    link {
                        icon = VaadinIcons.HOME
                        resource = ExternalResource("/")
                    }
            )
            addComponent(html(testSuite.testSuiteName.tag("h2")))
        })
        addComponent(horizontal {
            addComponent(vertical {
                addComponent(component<CheckBox> {
                    caption = "Выкладка разрешена"
                    value = testSuite.passed
                    addValueChangeListener {
                        if (value && context.unchecked > 0) {
                            value = false
                            showError("Не все упавшие тесты проверены")
                        } else if (value && !testSuite.isArmChecked) {
                            value = false
                            showError("Что там с армами?")
                        } else if (value && !testSuite.isLogsChecked) {
                            value = false
                            showError("Что там с логами, ок?")
                        } else {
                            testSuite.passed = value
                            repository.saveTestSuite(testSuite)
                        }
                    }
                })
                addComponent(horizontal {
                    addComponent(component<CheckBox> {
                        caption = "Армы проверены"
                        value = testSuite.isArmChecked
                        addValueChangeListener {
                            testSuite.isArmChecked = value
                            repository.saveTestSuite(testSuite)
                        }
                    })
                    addComponent(component<CheckBox> {
                        caption = "Логи проверены"
                        value = testSuite.isLogsChecked
                        addValueChangeListener {
                            testSuite.isLogsChecked = value
                            repository.saveTestSuite(testSuite)
                        }
                    })
                })
            })
            addComponent(vertical {
                addComponent(component<Label> {
                    value = "Прошло ${context.passed}".green()
                    contentMode = ContentMode.HTML
                })
                addComponent(component<Label> {
                    value = "Упало ${context.failed}".red()
                    contentMode = ContentMode.HTML
                })
                addComponent(component<Label> {
                    value = "Надо проверить ${context.unchecked}".red()
                    contentMode = ContentMode.HTML
                })
            })
        })
        addComponent(editableField("Комментарий к прогону") {
            read { testSuite.comment }
            save {
                testSuite.comment = it
                repository.saveTestSuite(testSuite)
            }
        })
        addComponent(component<Button> {
            caption = "Известные проблемы"
            addClickListener {
                UI.getCurrent().addWindow(KnownIssuesWindow(repository))
            }
        })

        addComponent(vertical {
            if (context.failed > 0) {
                addComponent(component<Label> { value = "Упавшие тесты:" })
                context.issues.filter { !it.passed }.forEach { testEntity ->
                    addComponent(horizontal {
                        addComponent(component<Label> {
                            with(testEntity) {
                                value = "$issue $testName " +
                                        (if (checked) "Проверен".green()
                                        else "Не проверен!".red())
                                contentMode = ContentMode.HTML
                            }
                        })
                        addComponent(button {
                            icon = VaadinIcons.EDIT
                            addClickListener {
                                UI.getCurrent().addWindow(
                                        componentWithParent(context) { context ->
                                            center()
                                            setHeight("600px")
                                            caption = "${testEntity.issue} ${testEntity.suiteName} ${testEntity.testName}"

                                            val commentField = editableField("Почему тест падал?") {
                                                read {
                                                    testEntity.comment
                                                }
                                                save {
                                                    testEntity.comment = it
                                                    repository.saveTestEntity(testEntity)
                                                    context.update()
                                                }
                                            }

                                            content = vertical {
                                                addComponent(horizontal {
                                                    addComponent(component<CheckBox> {
                                                        caption = "Проверено"
                                                        value = testEntity.checked
                                                        addValueChangeListener {
                                                            if (value && testEntity.comment.isEmpty()) {
                                                                value = false
                                                                showError("Заполни причину падения")
                                                            } else {
                                                                testEntity.checked = value
                                                                repository.saveTestEntity(testEntity)
                                                                context.update()
                                                            }
                                                        }
                                                    })
                                                    addComponent(button {
                                                        caption = "Прошлые результаты"
                                                        addClickListener {
                                                            UI.getCurrent().addWindow(
                                                                    component<Window> {
                                                                        center()
                                                                        setWidth("700px")
                                                                        caption = "История ${testEntity.issue}"
                                                                        content = vertical {
                                                                            repository.getTestEntitiesByIssue(testEntity.issue)
                                                                                    .sortedByDescending { it.runs.first().startTime }
                                                                                    .forEach { testEntity ->
                                                                                        addComponent(horizontal {
                                                                                            addComponent(component<Label> {
                                                                                                value = testEntity.passed.status()
                                                                                            })
                                                                                            addComponent(link {
                                                                                                caption = testEntity.testSuiteName
                                                                                                resource = ExternalResource("/?report=${testEntity.testSuiteName}")
                                                                                            })
                                                                                            addComponent(component<Label> { value = testEntity.comment })
                                                                                            addComponent(button {
                                                                                                icon = VaadinIcons.EDIT
                                                                                                addClickListener {
                                                                                                    UI.getCurrent().addWindow(
                                                                                                            component<Window> {
                                                                                                                caption = testEntity.testSuiteName
                                                                                                                center()
                                                                                                                content = vertical {
                                                                                                                    addComponent(Label(testEntity.passed.status()))
                                                                                                                    addComponent(editableField("Комментарий") {
                                                                                                                        read { testEntity.comment }
                                                                                                                        save {
                                                                                                                            testEntity.comment = it
                                                                                                                            repository.saveTestEntity(testEntity)
                                                                                                                        }
                                                                                                                    })
                                                                                                                }
                                                                                                            })
                                                                                                }
                                                                                            })
                                                                                        })
                                                                                    }
                                                                        }
                                                                    })
                                                        }
                                                    })
                                                })
                                                val knownIssues = repository.getKnownIssues(testEntity.issue)
                                                if (knownIssues.count() > 0) {
                                                    addComponent(vertical {
                                                        knownIssues.forEach { knownIssue ->
                                                            addComponent(horizontal {
                                                                addComponent(component<Button> {
                                                                    icon = VaadinIcons.EDIT
                                                                    addClickListener {
                                                                        UI.getCurrent().addWindow(
                                                                                KnownIssueEditWindow(knownIssue, repository, {context.update()})
                                                                        )
                                                                    }
                                                                })
                                                                addComponent(Label(knownIssue.name))
                                                                addComponent(component<Label> {
                                                                    value = knownIssue.keyWords.toString()
                                                                })
                                                            })
                                                        }
                                                    })
                                                }
                                                addComponent(commentField)
                                                addComponent(component<Accordion> {
                                                    testEntity.runs.forEach {
                                                        with(it) {
                                                            val title = "(${startTime.toReadableDate()} - " +
                                                                    "${endTime.toReadableTime()}): $passed"
                                                            addTab(vertical {
                                                                addComponent(editableField("Комментарий:") {
                                                                    read { comment }
                                                                    save {
                                                                        comment = it
                                                                        repository.saveTestEntity(testEntity)
                                                                        context.update()
                                                                    }
                                                                })
                                                            }, title)
                                                        }
                                                    }
                                                })
                                            }
                                        }
                                )
                            }
                        })
                        addComponent(component<Label> {
                            value = testEntity.comment
                        })
                    })
                }
            }
        })
    }
}