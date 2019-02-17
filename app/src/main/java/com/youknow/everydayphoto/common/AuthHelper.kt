package com.youknow.everydayphoto.common

object AuthHelper {
    var token: String? = null
    get() {
        return "Bearer $field"
    }

}