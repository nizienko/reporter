package core.ui.components

import com.vaadin.icons.VaadinIcons
import com.vaadin.ui.*
import core.db.ReporterRepository
import core.entity.KnownIssue


class KnownIssueEditWindow(
        private val knownIssue: KnownIssue,
        private val repository: ReporterRepository,
        private val callback: ()->Unit): Window() {
    init {

        center()
        caption = knownIssue.name
        addCloseListener {
            callback()
        }

        val wordList: ()-> HorizontalLayout = {
            horizontal {
                knownIssue.keyWords.forEach { word ->
                    addComponent(component<Button> {
                        caption = word
                        addClickListener {
                            knownIssue.keyWords.remove(word)
                            isVisible = false
                        }
                    })
                }
            }
        }
        val wordListLayout = VerticalLayout()

        content = vertical {
            addComponent(horizontal {
                val newWord = TextField()
                addComponent(button {
                    icon = VaadinIcons.PLUS
                    caption = "issue"
                    addClickListener {
                        if (newWord.value.isNotEmpty() && !knownIssue.keyWords.contains(newWord.value)) {
                            knownIssue.keyWords.add(newWord.value)
                            newWord.value = ""
                            wordListLayout.removeAllComponents()
                            wordListLayout.addComponent(wordList())
                        }
                    }
                })
                addComponent(newWord)
            })
            addComponent(wordListLayout)
            wordListLayout.addComponent(wordList())
            
            val name = component<TextField> {
                caption = "Имя"
                value = knownIssue.name
            }
            addComponent(name)
            val comment = component<TextArea> {
                caption = "Описание"
                value = knownIssue.comment
            }
            addComponent(name)
            addComponent(comment)
            addComponent(horizontal {
                addComponent(button {
                    caption ="Сохранить"
                    addClickListener {
                        knownIssue.comment = comment.value
                        knownIssue.name = name.value
                        repository.updateKnownIssue(knownIssue)
                        close()
                    }
                })
                addComponent(button {
                    caption ="Удалить"
                    addClickListener {
                        repository.deleteKnownIssue(knownIssue)
                        close()
                    }
                })
            })
        }
    }
}