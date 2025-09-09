package tr.com.cetinkaya.common.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement

class EnumByCodeDeserializer<T>(
    private val fromCode: (Byte?) -> T
) : JsonDeserializer<T> {
    override fun deserialize(json: JsonElement, typeOfT: java.lang.reflect.Type, ctx: JsonDeserializationContext): T {
        val code = when {
            json.isJsonNull -> null
            json.asJsonPrimitive.isNumber -> json.asByte
            json.asJsonPrimitive.isString -> json.asString.toByteOrNull()
            else -> null
        }
        return fromCode(code)
    }
}