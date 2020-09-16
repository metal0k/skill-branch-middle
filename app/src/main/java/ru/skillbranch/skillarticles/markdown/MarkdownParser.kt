package ru.skillbranch.skillarticles.markdown

import java.util.regex.Pattern

object MarkdownParser {

    private val LINE_SEPARATOR = "\n"

    //group regex
    private const val UNORDERED_LIST_ITEM_GROUP_SUB = "^[*+-] "
    private const val UNORDERED_LIST_ITEM_GROUP = "($UNORDERED_LIST_ITEM_GROUP_SUB.+$)"
    private const val HEADER_GROUP_SUB = "^#{1,6} "
    private const val HEADER_GROUP = "($HEADER_GROUP_SUB.+?$)"
    private const val QUOTE_GROUP_SUB = "^> "
    private const val QUOTE_GROUP = "($QUOTE_GROUP_SUB.+?$)"
    private const val ITALIC_GROUP_SUB =
        "(?<!\\*)\\*([^*].*?[^*]?)\\*(?!\\*)|(?<!_)_([^_].*?[^_]?)_(?!_)"
    private const val ITALIC_GROUP = "((?<!\\*)\\*[^*].*?[^*]?\\*(?!\\*)|(?<!_)_[^_].*?[^_]?_(?!_))"
    private const val BOLD_GROUP_SUB =
        "(?<!\\*)\\*{2}([^*].*?[^*]?)\\*{2}(?!\\*)|(?<!_)_{2}([^_].*?[^_]?)_{2}(?!_)"
    private const val BOLD_GROUP =
        "((?<!\\*)\\*{2}[^*].*?[^*]?\\*{2}(?!\\*)|(?<!_)_{2}[^_].*?[^_]?_{2}(?!_))"
    private const val STRIKE_GROUP_SUB = "(?<!~)~{2}([^~].*?[^~]?)\\~{2}(?!\\~)"
    private const val STRIKE_GROUP = "((?<!~)~{2}[^~].*?[^~]?\\~{2}(?!\\~))"
    private const val RULE_GROUP = "(^[-_*]{3}$)"
    private const val INLINE_GROUP_SUB = "(?<!`)`([^`\\s].*?[^`\\s]?)`(?!`)"
    private const val INLINE_GROUP = "((?<!`)`[^`\\s].*?[^`\\s]?`(?!`))"
    private const val LINK_GROUP_SUB = "\\[([^\\[\\]]*?)]\\(.+?\\)|^\\[*?]\\(.*?\\)"
    private const val LINK_GROUP = "(\\[[^\\[\\]]*?]\\(.+?\\)|^\\[*?]\\(.*?\\))"
    private const val BLOCK_CODE_GROUP_SUB = "^(?s)`{3}(.+?)`{3}$(?-s)"
    private const val BLOCK_CODE_GROUP = "(^(?s)`{3}.+?`{3}$(?-s))"
    private const val ORDER_LIST_GROUP_SUB = "^\\d+\\. "
    private const val ORDER_LIST_GROUP = "($ORDER_LIST_GROUP_SUB.+$)"

    //replace to empty string regex
    private const val MARKDOWN_REPLACE_TO_EMPTY =
        "($UNORDERED_LIST_ITEM_GROUP_SUB)|($HEADER_GROUP_SUB)" +
                "|($QUOTE_GROUP_SUB)|($ORDER_LIST_GROUP_SUB)"

    //replace to subgroup regex
    private val MARKDOWN_REPLACE_TO_SG = listOf<String>(
        ITALIC_GROUP_SUB, BOLD_GROUP_SUB,
        STRIKE_GROUP_SUB, INLINE_GROUP_SUB, LINK_GROUP_SUB
    )

    //result regex
    private const val MARKDOWN_GROUPS = "$UNORDERED_LIST_ITEM_GROUP|$HEADER_GROUP|$QUOTE_GROUP" +
            "|$ITALIC_GROUP|$BOLD_GROUP|$STRIKE_GROUP|$RULE_GROUP|$INLINE_GROUP|$LINK_GROUP" +
            "|$BLOCK_CODE_GROUP|$ORDER_LIST_GROUP"

    private val elementsPattern by lazy { Pattern.compile(MARKDOWN_GROUPS, Pattern.MULTILINE) }

    /**
     * parse markdown text to elements
     */
    fun parse(string: String): MarkdownText {
        val elements = mutableListOf<Element>()
        elements.addAll(findElements(string))
        return MarkdownText(elements)
    }

    /**
     * clear markdown text to string without markdown characters
     */
    fun clear(string: String?): String? {
        string ?: return null
        var result = MARKDOWN_REPLACE_TO_EMPTY.toRegex(RegexOption.MULTILINE).replace(string, "");
        result = RULE_GROUP.toRegex(RegexOption.MULTILINE).replace(result, " ")
        for (regStr in MARKDOWN_REPLACE_TO_SG) {
            val reg = Regex(regStr, RegexOption.MULTILINE)
            result = reg.replace(result) {
                it.groupValues.drop(1).first { it.isNotBlank() }
            }
        }
        result = BLOCK_CODE_GROUP_SUB
            .toRegex(setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE))
            .replace(result, "$1")
        return result
    }

    /**
     * find markdown elements in markdown text
     */
    private fun findElements(string: CharSequence): List<Element> {
        val parents = mutableListOf<Element>()
        val matcher = elementsPattern.matcher(string)
        var lastStartIndex = 0

        loop@ while (matcher.find(lastStartIndex)) {
            val startIndex = matcher.start()
            val endIndex = matcher.end()

            //if something is found then everything before - TEXT
            if (lastStartIndex < startIndex) {
                parents.add(Element.Text(string.subSequence(lastStartIndex, startIndex)))
            }

            //found text
            var text: CharSequence

            //groups range for iterate by groups
            val groups = 1..11
            var group = -1
            for (gr in groups) {
                if (matcher.group(gr) != null) {
                    group = gr
                    break
                }
            }

            when (group) {
                //NOT FOUND -> BREAK
                -1 -> break@loop

                //UNORDERED LIST
                1 -> {
                    //text without "*. "
                    text = string.subSequence(startIndex.plus(2), endIndex)

                    //find inner elements
                    val subs = findElements(text)
                    val element = Element.UnorderedListItem(text, subs)
                    parents.add(element)

                    //next find start from position "endIndex" (last regex character)
                    lastStartIndex = endIndex
                }

                //HEADER
                2 -> {
                    val reg = "^#{1,6}".toRegex().find(string.subSequence(startIndex, endIndex))
                    val level = reg!!.value.length

                    //text without "{#} "
                    text = string.subSequence(startIndex.plus(level.inc()), endIndex)

                    val element = Element.Header(level, text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //QUOTE
                3 -> {
                    //text without "> "
                    text = string.subSequence(startIndex.plus(2), endIndex)
                    val subelements = findElements(text)
                    val element = Element.Quote(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //ITALIC
                4 -> {
                    //text without "*{}*"
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val subelements = findElements(text)
                    val element = Element.Italic(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //BOLD
                5 -> {
                    //text without "**{}**"
                    text = string.subSequence(startIndex.plus(2), endIndex.plus(-2))
                    val subelements = findElements(text)
                    val element = Element.Bold(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //STRIKE
                6 -> {
                    //text without "~~{}~~"
                    text = string.subSequence(startIndex.plus(2), endIndex.plus(-2))
                    val subelements = findElements(text)
                    val element = Element.Strike(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //RULE
                7 -> {
                    //text without "***" insert empty character
                    val element = Element.Rule()
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //INLINE CODE
                8 -> {
                    //text without "`{}`"
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val element = Element.InlineCode(text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //LINK
                9 -> {
                    //full text for regex
                    text = string.subSequence(startIndex, endIndex)
                    val (title: String, link: String) = "\\[(.*)]\\((.*)\\)".toRegex()
                        .find(text)!!.destructured
                    val element = Element.Link(link, title)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //BLOCKCODE
                10 -> {
                    //text without "```{}```"
                    text = string.subSequence(startIndex.plus(3), endIndex.minus(3))
                    val lines = text.split('\n')
                    if (lines.size == 1) {
                        val subelements = findElements(text)
                        val element =
                            Element.BlockCode(Element.BlockCode.Type.SINGLE, text, subelements)
                        parents.add(element)
                    } else
                        lines.forEachIndexed { idx, line ->
                            val type = when (idx) {
                                0 -> Element.BlockCode.Type.START
                                lines.size - 1 -> Element.BlockCode.Type.END
                                else -> Element.BlockCode.Type.MIDDLE
                            }
                            val subelements = findElements(line)
                            val element = Element.BlockCode(
                                type,
                                line + if (idx < lines.size.dec()) '\n' else "",
                                subelements)
                            parents.add(element)
                            parents.add(Element.Text("\n"))

                        }
                    lastStartIndex = endIndex
                }

                //ORDERED LIST
                11 -> {

                    val reg = "^(\\d+\\.) ".toRegex().find(string.subSequence(startIndex, endIndex))
                    val order = reg!!.groups[1]!!.value

                    //text without "\\d+\\. "
                    text = string.subSequence(startIndex.plus(reg!!.value.length), endIndex)

                    //find inner elements
                    val subs = findElements(text)
                    val element = Element.OrderedListItem(order, text, subs)
                    parents.add(element)

                    //next find start from position "endIndex" (last regex character)
                    lastStartIndex = endIndex
                }
            }

        }

        if (lastStartIndex < string.length) {
            val text = string.subSequence(lastStartIndex, string.length)
            parents.add(Element.Text(text))
        }

        return parents
    }
}

data class MarkdownText(val elements: List<Element>)

sealed class Element() {
    abstract val text: CharSequence
    abstract val elements: List<Element>

    data class Text(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class UnorderedListItem(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Header(
        val level: Int = 1,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Quote(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Italic(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Bold(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Strike(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Rule(
        override val text: CharSequence = " ", //for insert span
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class InlineCode(
        override val text: CharSequence, //for insert span
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Link(
        val link: String,
        override val text: CharSequence, //for insert span
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class OrderedListItem(
        val order: String,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class BlockCode(
        val type: Type = Type.MIDDLE,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element() {
        enum class Type { START, END, MIDDLE, SINGLE }
    }
}