package core.ui.components

import com.vaadin.icons.VaadinIcons
import com.vaadin.ui.HorizontalLayout

class EditableField(private val title: String): HorizontalLayout() {
    fun read(block: ()-> String) {readText = block}

    fun save(block: (text: String) -> Unit) {saveText = block}

    fun isEmpty(): Boolean = textArea.isEmpty

    private val textArea = textArea {
        caption = title
        setWidth("500px")
        setHeight("100px")
    }

    private var readText: ()-> String = {""}

    private var saveText: (text: String) -> Unit = {}

    private val editButton = button {
        icon = VaadinIcons.EDIT
        addClickListener {
            setEditMode()
        }
    }

    private val cancelButton = button {
        icon = VaadinIcons.ARROW_BACKWARD
        addClickListener {
            setReadMode()
        }
    }

    private val saveButton = button {
        icon = VaadinIcons.CHECK
        addClickListener {
            saveText(textArea.value)
            setReadMode()
        }
    }


    fun setReadMode() {
        textArea.value = readText()
        removeAllComponents()
        textArea.isReadOnly = true
        addComponent(textArea)
        addComponent(vertical {
            addComponent(editButton)
        })
    }

    fun setEditMode() {
        textArea.value = readText()
        removeAllComponents()
        textArea.isReadOnly = false
        addComponent(textArea)
        addComponent(vertical {
            addComponent(cancelButton)
            addComponent(saveButton)
        })
    }
}