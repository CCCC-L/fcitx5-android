/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2021-2023 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.view.View
import androidx.annotation.Keep
import androidx.core.view.allViews
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.core.InputMethodEntry
import org.fcitx.fcitx5.android.core.KeyState
import org.fcitx.fcitx5.android.core.KeyStates
import org.fcitx.fcitx5.android.data.prefs.AppPrefs
import org.fcitx.fcitx5.android.data.prefs.ManagedPreference
import org.fcitx.fcitx5.android.data.theme.Theme
import org.fcitx.fcitx5.android.input.popup.PopupAction
import org.fcitx.fcitx5.android.input.picker.PickerWindow
import org.fcitx.fcitx5.android.input.keyboard.NumberKeyboard
import splitties.views.imageResource

@SuppressLint("ViewConstructor")
class TextKeyboard(
    context: Context,
    theme: Theme
) : BaseKeyboard(context, theme, getLayoutForOrientation(context)) {

    enum class CapsState { None, Once, Lock }

    companion object {
        const val Name = "Text"

        private fun getLayoutForOrientation(context: Context): List<List<KeyDef>> {
            return if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                LandscapeLayout
            } else {
                PortraitLayout
            }
        }

        val PortraitLayout: List<List<KeyDef>> = listOf(
            listOf(
                MultiSwipeAlphabetKey("Q", "1", "1", null, null, null),
                MultiSwipeAlphabetKey("W", "2", "2", null, null, null),
                MultiSwipeAlphabetKey("E", "3", "3", null, null, null),
                MultiSwipeAlphabetKey("R", "4", "4", null, null, null),
                MultiSwipeAlphabetKey("T", "5", "5", null, null, null),
                MultiSwipeAlphabetKey("Y", "6", "6", null, null, null),
                MultiSwipeAlphabetKey("U", "7", "7", null, null, null),
                MultiSwipeAlphabetKey("I", "8", "8", null, null, null),
                MultiSwipeAlphabetKey("O", "9", "9", null, null, null),
                MultiSwipeAlphabetKey("P", "0", "0", null, null, null)
            ),
            listOf(
                MultiSwipeAlphabetKey("A", "\\", "\\", null, null, null),
                MultiSwipeAlphabetKey("S", "*  /", "//", null, "*", "/"),
                MultiSwipeAlphabetKey("D", "!  =", "!=", null, "!", "="),
                MultiSwipeAlphabetKey("F", "&  |", "&&", "||", "&", "|"),
                MultiSwipeAlphabetKey("G", "^  ~", null, null, "^", "~"),
                MultiSwipeAlphabetKey("H", "<  >", "<>", null, "<", ">"),
                MultiSwipeAlphabetKey("J", "[  ]", "[]", null, "[", "]"),
                MultiSwipeAlphabetKey("K", "{  }", "{}", null, "{", "}"),
                MultiSwipeAlphabetKey("L", "?", "?", null, null, null)
            ),
            listOf(
                CapsKey(),
                MultiSwipeAlphabetKey("Z", "+  -", KeyAction.SelectAllAction, null, "+", "-"),
                MultiSwipeAlphabetKey("X", "@  #", KeyAction.CutAction, null, "@", "#"),
                MultiSwipeAlphabetKey("C", "$  %", KeyAction.CopyAction, null, "$", "%"),
                MultiSwipeAlphabetKey("V", ":  `", KeyAction.PasteAction, null, ":", "`"),
                MultiSwipeAlphabetKey("B", ";", ";", null, null, null),
                MultiSwipeAlphabetKey("N", "(  )", "()", null, "(", ")"),
                MultiSwipeAlphabetKey("M", "\"  '", "\"\"", "''", "\"", "'"),
                BackspaceKey()
            ),
            listOf(
                LayoutSwitchKey("!?#", PickerWindow.Key.Symbol.name),
                LayoutSwitchKey("123", NumberKeyboard.Name),
                LanguageKey(),
                CommaKey(0.1f, KeyDef.Appearance.Variant.Alternative),
                SpaceKey(),
                SymbolKey(".", 0.1f, KeyDef.Appearance.Variant.Alternative),
                ReturnKey(0.2f)
            )
        )

        val LandscapeLayout: List<List<KeyDef>> = listOf(
            listOf(
                MultiSwipeAlphabetKey("Q", "1", "1", null, null, null),
                MultiSwipeAlphabetKey("W", "2", "2", null, null, null),
                MultiSwipeAlphabetKey("E", "3", "3", null, null, null),
                MultiSwipeAlphabetKey("R", "4", "4", null, null, null),
                MultiSwipeAlphabetKey("T", "5", "5", null, null, null),
                SymbolKey("1", 0.1f, KeyDef.Appearance.Variant.Alternative),
                SymbolKey("2", 0.1f, KeyDef.Appearance.Variant.Alternative),
                SymbolKey("3", 0.1f, KeyDef.Appearance.Variant.Alternative),
                MultiSwipeAlphabetKey("Y", "6", "6", null, null, null),
                MultiSwipeAlphabetKey("U", "7", "7", null, null, null),
                MultiSwipeAlphabetKey("I", "8", "8", null, null, null),
                MultiSwipeAlphabetKey("O", "9", "9", null, null, null),
                MultiSwipeAlphabetKey("P", "0", "0", null, null, null)
            ),
            listOf(
                MultiSwipeAlphabetKey("A", "\\", "\\", null, null, null),
                MultiSwipeAlphabetKey("S", "*  /", "//", null, "*", "/"),
                MultiSwipeAlphabetKey("D", "!  =", "!=", null, "!", "="),
                MultiSwipeAlphabetKey("F", "&  |", "&&", "||", "&", "|"),
                MultiSwipeAlphabetKey("G", "^  ~", null, null, "^", "~"),
                SymbolKey("4", 0.1f, KeyDef.Appearance.Variant.Alternative),
                SymbolKey("5", 0.1f, KeyDef.Appearance.Variant.Alternative),
                SymbolKey("6", 0.1f, KeyDef.Appearance.Variant.Alternative),
                MultiSwipeAlphabetKey("H", "<  >", "<>", null, "<", ">"),
                MultiSwipeAlphabetKey("J", "[  ]", "[]", null, "[", "]"),
                MultiSwipeAlphabetKey("K", "{  }", "{}", null, "{", "}"),
                MultiSwipeAlphabetKey("L", "?", "?", null, null, null)
            ),
            listOf(
                CapsKey(),
                MultiSwipeAlphabetKey("Z", "+  -", KeyAction.SelectAllAction, null, "+", "-"),
                MultiSwipeAlphabetKey("X", "@  #", KeyAction.CutAction, null, "@", "#"),
                MultiSwipeAlphabetKey("C", "$  %", KeyAction.CopyAction, null, "$", "%"),
                MultiSwipeAlphabetKey("V", ":  `", KeyAction.PasteAction, null, ":", "`"),
                MultiSwipeAlphabetKey("B", ";", ";", null, null, null),
                SymbolKey("7", 0.1f, KeyDef.Appearance.Variant.Alternative),
                SymbolKey("8", 0.1f, KeyDef.Appearance.Variant.Alternative),
                SymbolKey("9", 0.1f, KeyDef.Appearance.Variant.Alternative),
                MultiSwipeAlphabetKey("N", "(  )", "()", null, "(", ")"),
                MultiSwipeAlphabetKey("M", "\"  '", "\"\"", "''", "\"", "'"),
                BackspaceKey()
            ),
            listOf(
                SpaceKey(),
                LayoutSwitchKey("!?#", PickerWindow.Key.Symbol.name),
                LanguageKey(),
                CommaKey(0.1f, KeyDef.Appearance.Variant.Alternative),
                SymbolKey("0", 0.1f, KeyDef.Appearance.Variant.Alternative),
                SymbolKey(".", 0.1f, KeyDef.Appearance.Variant.Alternative),
                SpaceKey(),
                ReturnKey(0.2f)
            )
        )
    }

    val caps: ImageKeyView by lazy { findViewById(R.id.button_caps) }
    val backspace: ImageKeyView by lazy { findViewById(R.id.button_backspace) }
    val quickphrase: ImageKeyView by lazy { findViewById(R.id.button_quickphrase) }
    val lang: ImageKeyView by lazy { findViewById(R.id.button_lang) }
    val space: TextKeyView by lazy { findViewById(R.id.button_space) }
    val `return`: ImageKeyView by lazy { findViewById(R.id.button_return) }

    private val showLangSwitchKey = AppPrefs.getInstance().keyboard.showLangSwitchKey

    @Keep
    private val showLangSwitchKeyListener = ManagedPreference.OnChangeListener<Boolean> { _, v ->
        updateLangSwitchKey(v)
    }

    private val keepLettersUppercase by AppPrefs.getInstance().keyboard.keepLettersUppercase

    init {
        updateLangSwitchKey(showLangSwitchKey.getValue())
        showLangSwitchKey.registerOnChangeListener(showLangSwitchKeyListener)
    }

    private val textKeys: List<TextKeyView> by lazy {
        allViews.filterIsInstance(TextKeyView::class.java).toList()
    }

    private var capsState: CapsState = CapsState.None

    private fun transformAlphabet(c: String): String {
        return when (capsState) {
            CapsState.None -> c.lowercase()
            else -> c.uppercase()
        }
    }

    private var punctuationMapping: Map<String, String> = mapOf()
    private fun transformPunctuation(p: String) = punctuationMapping.getOrDefault(p, p)

    override fun onAction(action: KeyAction, source: KeyActionListener.Source) {
        var transformed = action
        when (action) {
            is KeyAction.FcitxKeyAction -> when (source) {
                KeyActionListener.Source.Keyboard -> {
                    when (capsState) {
                        CapsState.None -> {
                            transformed = action.copy(act = action.act.lowercase())
                        }
                        CapsState.Once -> {
                            transformed = action.copy(
                                act = action.act.uppercase(),
                                states = KeyStates(KeyState.Virtual, KeyState.Shift)
                            )
                            switchCapsState()
                        }
                        CapsState.Lock -> {
                            transformed = action.copy(
                                act = action.act.uppercase(),
                                states = KeyStates(KeyState.Virtual, KeyState.CapsLock)
                            )
                        }
                    }
                }
                KeyActionListener.Source.Popup -> {
                    if (capsState == CapsState.Once) {
                        switchCapsState()
                    }
                }
            }
            is KeyAction.CapsAction -> switchCapsState(action.lock)
            else -> {}
        }
        super.onAction(transformed, source)
    }

    override fun onAttach() {
        capsState = CapsState.None
        updateCapsButtonIcon()
        updateAlphabetKeys()
    }

    override fun onReturnDrawableUpdate(returnDrawable: Int) {
        `return`.img.imageResource = returnDrawable
    }

    override fun onPunctuationUpdate(mapping: Map<String, String>) {
        punctuationMapping = mapping
        updatePunctuationKeys()
    }

    override fun onInputMethodUpdate(ime: InputMethodEntry) {
        space.mainText.text = buildString {
            append(ime.subMode.run { name.ifEmpty { label.ifEmpty { null } } } ?: ime.displayName)
        }
        if (capsState != CapsState.None) {
            switchCapsState()
        }
    }

    private fun transformPopupPreview(c: String): String {
        if (c.length != 1) return c
        if (c[0].isLetter()) return transformAlphabet(c)
        return transformPunctuation(c)
    }

    override fun onPopupAction(action: PopupAction) {
        val newAction = when (action) {
            is PopupAction.PreviewAction -> action.copy(content = transformPopupPreview(action.content))
            is PopupAction.PreviewUpdateAction -> action.copy(content = transformPopupPreview(action.content))
            is PopupAction.ShowKeyboardAction -> {
                val label = action.keyboard.label
                if (label.length == 1 && label[0].isLetter())
                    action.copy(keyboard = KeyDef.Popup.Keyboard(transformAlphabet(label), action.keyboard.symbols))
                else action
            }
            else -> action
        }
        super.onPopupAction(newAction)
    }

    private fun switchCapsState(lock: Boolean = false) {
        capsState =
            if (lock) {
                when (capsState) {
                    CapsState.Lock -> CapsState.None
                    else -> CapsState.Lock
                }
            } else {
                when (capsState) {
                    CapsState.None -> CapsState.Once
                    else -> CapsState.None
                }
            }
        updateCapsButtonIcon()
        updateAlphabetKeys()
    }

    private fun updateCapsButtonIcon() {
        caps.img.apply {
            imageResource = when (capsState) {
                CapsState.None -> R.drawable.ic_capslock_none
                CapsState.Once -> R.drawable.ic_capslock_once
                CapsState.Lock -> R.drawable.ic_capslock_lock
            }
        }
    }

    private fun updateLangSwitchKey(visible: Boolean) {
        lang.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun updateAlphabetKeys() {
        textKeys.forEach {
            if (it.def !is KeyDef.Appearance.AltText) return
            it.mainText.text = it.def.displayText.let { str ->
                if (str.length != 1 || !str[0].isLetter()) return@forEach
                if (keepLettersUppercase) str.uppercase() else transformAlphabet(str)
            }
        }
    }

    private fun updatePunctuationKeys() {
        textKeys.forEach {
            if (it is AltTextKeyView) {
                it.def as KeyDef.Appearance.AltText
                it.altText.text = transformPunctuation(it.def.altText)
            } else {
                it.def as KeyDef.Appearance.Text
                it.mainText.text = it.def.displayText.let { str ->
                    if (str[0].run { isLetter() || isWhitespace() }) return@forEach
                    transformPunctuation(str)
                }
            }
        }
    }

}