package org.reekwest.http.lens


import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws

object BiDiLensContract {

    val spec = BiDiLensSpec("location", Get.Companion { _: String, str: String ->
        if (str.isBlank()) emptyList() else listOf(str)
    },
        Set.Companion { _: String, values: List<String>, str: String -> values.fold(str, { memo, next -> memo + next }) })

    fun <T> checkContract(spec: BiDiLensSpec<String, String, T>, valueAsString: String, tValue: T) {
        val optionalLens = spec.optional("hello")
        assertThat(optionalLens(valueAsString), equalTo(tValue))
        assertThat(optionalLens(""), absent())
        assertThat(spec.optional("hello", default = tValue)(""), equalTo(tValue))
        assertThat({ optionalLens("hello") }, throws(equalTo(LensFailure(optionalLens.invalid()))))
        assertThat(optionalLens(tValue, "original"), equalTo("original" + valueAsString))

        val optionalMultiLens = spec.multi.optional("hello")
        assertThat(optionalMultiLens(valueAsString), equalTo(listOf(tValue)))
        assertThat(optionalMultiLens(""), absent())
        assertThat(spec.multi.optional("hello", default = listOf(tValue))(""), equalTo(listOf(tValue)))
        assertThat({ optionalMultiLens("hello") }, throws(equalTo(LensFailure(optionalLens.invalid()))))
        assertThat(optionalMultiLens(listOf(tValue, tValue), "original"), equalTo("original" + valueAsString + valueAsString))

        val requiredLens = spec.required("hello")
        assertThat(requiredLens(valueAsString), equalTo(tValue))
        assertThat({ requiredLens("") }, throws(equalTo(LensFailure(requiredLens.missing()))))
        assertThat({ requiredLens("hello") }, throws(equalTo(LensFailure(requiredLens.invalid()))))
        assertThat(requiredLens(tValue, "original"), equalTo("original" + valueAsString))

        val requiredMultiLens = spec.multi.required("hello")
        assertThat(requiredMultiLens(valueAsString), equalTo(listOf(tValue)))
        assertThat({ requiredMultiLens("") }, throws(equalTo(LensFailure(requiredLens.missing()))))
        assertThat({ requiredMultiLens("hello") }, throws(equalTo(LensFailure(requiredLens.invalid()))))
        assertThat(requiredMultiLens(listOf(tValue, tValue), "original"), equalTo("original" + valueAsString + valueAsString))
    }
}

data class MyCustomBodyType(val value: String)