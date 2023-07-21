package com.uksusoff.rock63.data.entities

import com.j256.ormlite.field.DatabaseField

class Place {
    @DatabaseField(id = true)
    var id = 0
    @DatabaseField
    var name: String? = null
    @DatabaseField
    var address: String? = null
    @DatabaseField
    var url: String? = null
    @DatabaseField
    var phone: String? = null
    @DatabaseField
    var vkUrl: String? = null
}