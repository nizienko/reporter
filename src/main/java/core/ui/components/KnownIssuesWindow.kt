package core.ui.components

import com.vaadin.icons.VaadinIcons
import com.vaadin.ui.*
import core.db.ReporterRepository
import core.entity.KnownIssue

class KnownIssuesWindow(private val repository: ReporterRepository) : Window() {
    init {
        center()
        caption = "Известные проблемы"
        update()
    }

    val thisWindow = this

    fun update() {
        content = generateContent()
    }


    private fun generateContent(): VerticalLayout {
        return vertical {
            addComponent(button {
                icon = VaadinIcons.PLUS
                addClickListener {
                    UI.getCurrent().addWindow(
                            KnownIssueEditWindow(KnownIssue(
                                    name = "новая проблема",
                                    keyWords = mutableListOf(),
                                    comment = "Что за проблема?"
                            ), repository, {thisWindow.update()}))
                }
            })
            repository.getKnownIssues().forEach { knownIssue ->
                addComponent(horizontal {
                    addComponent(component<Button> {
                        icon = VaadinIcons.EDIT
                        addClickListener {
                            UI.getCurrent().addWindow(
                                    KnownIssueEditWindow(knownIssue, repository, {thisWindow.update()})
                            )
                        }
                    })
                    addComponent(Label(knownIssue.name))
                    addComponent(component<Label> {
                        value = knownIssue.keyWords.toString()
                    })
                })

            }
        }
    }
}