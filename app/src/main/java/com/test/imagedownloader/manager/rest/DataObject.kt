package com.test.imagedownloader.manager.rest

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.io.Serializable
import java.lang.reflect.Type

class DataObject : Serializable {

    companion object {
        val TAG_ID = "id"
        val TAG_AVATAR = "avatar_url"
        val TAG_LOGIN = "login"
    }

    var id: String? = null
    var login: String? = null
    var avatarUrl: String? = null
}

class DataDeserializer : JsonDeserializer<DataObject> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): DataObject? {
        val dataObject = DataObject()
        val jsonObject = json as JsonObject

        dataObject.id = jsonObject.get(DataObject.TAG_ID).asString
        dataObject.login = jsonObject.get(DataObject.TAG_LOGIN).asString
        dataObject.avatarUrl = jsonObject.get(DataObject.TAG_AVATAR).asString

        return dataObject
    }
}