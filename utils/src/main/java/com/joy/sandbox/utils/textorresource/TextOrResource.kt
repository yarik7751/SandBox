package com.joy.sandbox.utils.textorresource

import android.content.Context
import android.view.View
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment

@Immutable
sealed class TextOrResource {

    @Immutable
    class PluralsResource(@PluralsRes val id: Int, val count: Int, vararg val args: Any) : TextOrResource() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is PluralsResource) return false
            if (this.id != other.id) return false
            if (this.count != other.count) return false
            if (!this.args.contentEquals(other.args)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = id
            result = 31 * result + 17 * count + args.contentHashCode()
            return result
        }
    }

    @Immutable
    class Resource(@StringRes val id: Int, vararg val args: Any) : TextOrResource() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Resource) return false
            if (this.id != other.id) return false
            if (!this.args.contentEquals(other.args)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = id
            result = 31 * result + args.contentHashCode()
            return result
        }
    }

    @Immutable
    class ResourceParams(@StringRes val id: Int, vararg val args: TextOrResource) : TextOrResource() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ResourceParams) return false
            if (this.id != other.id) return false
            if (!this.args.contentEquals(other.args)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = id
            result = 31 * result + args.contentHashCode()
            return result
        }
    }

    @Immutable
    data class Text(val text: String) : TextOrResource()

    companion object {

        val empty = Text("")
    }
}

@Suppress("SpreadOperator")
fun TextOrResource.getString(context: Context): String {
    return when (this) {
        is TextOrResource.Text -> {
            text
        }

        is TextOrResource.Resource -> {
            context.getString(id, *args)
        }

        is TextOrResource.ResourceParams -> {
            val stringArgs = args.map { it.getString(context) }
            context.getString(id, *stringArgs.toTypedArray())
        }

        is TextOrResource.PluralsResource -> {
            context.resources.getQuantityString(id, count, *args)
        }
    }
}

fun Fragment.getString(textOrResource: TextOrResource): String = textOrResource.getString(requireContext())

fun View.getString(textOrResource: TextOrResource): String = textOrResource.getString(context)

@Composable
fun TextOrResource.getString(): String {
    return when (this) {
        is TextOrResource.Text -> {
            text
        }

        is TextOrResource.Resource -> {
            stringResource(id, *args)
        }

        is TextOrResource.ResourceParams -> {
            val stringArgs = args.map { it.getString() }
            stringResource(id, *stringArgs.toTypedArray())
        }

        is TextOrResource.PluralsResource -> {
            pluralStringResource(id, count, *args)
        }
    }
}
