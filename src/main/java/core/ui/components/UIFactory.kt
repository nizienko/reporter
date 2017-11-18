package core.ui.components

import com.vaadin.shared.ui.ContentMode
import com.vaadin.ui.*

fun button(text: String, block: Button.() -> Unit): Button {
    val button = Button(text)
    button.block()
    return button
}

fun button(block: Button.() -> Unit): Button {
    val button = Button()
    button.block()
    return button
}

fun link(block: Link.()->Unit): Link {
    val link = Link()
    link.block()
    return link
}

fun vertical(block: VerticalLayout.() -> Unit): VerticalLayout {
    val verticalLayout = VerticalLayout()
    verticalLayout.block()
    return verticalLayout
}

fun horizontal(block: HorizontalLayout.() -> Unit): HorizontalLayout {
    val horizontalLayout = HorizontalLayout()
    horizontalLayout.block()
    return horizontalLayout
}


inline fun <reified C> component(block: C.()->Unit): C {
    val component = C::class.java.newInstance()
    component.block()
    return component
}

inline fun <reified C> componentWithCallBack(noinline callback: ()-> Unit, block: C.(()-> Unit)->Unit): C {
    val component = C::class.java.newInstance()
    component.block(callback)
    return component
}

inline fun <reified C, P> componentWithParent(parent: P, block: C.(parent: P)->Unit): C {
    val component = C::class.java.newInstance()
    component.block(parent)
    return component
}

fun textArea(block: TextArea.() -> Unit) : TextArea {
    val textArea = TextArea()
    textArea.block()
    return textArea
}


fun html(text: String) : Label = Label(text, ContentMode.HTML)

fun editableField(title: String, block: EditableField.()-> Unit): EditableField {
    val field = EditableField(title)
    field.block()
    field.setReadMode()
    return field
}

fun String.tag(tag: String) = "<$tag>$this</$tag>"
fun String.green() = "<font color=green>$this</font>"
fun String.red() = "<font color=red>$this</font>"