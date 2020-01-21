package com.agm91.prontoapp.data

class ApiResponse<T> {
    var data: T? = null
    var error: Throwable? = null

    constructor(data: T?) {
        this.data = data
    }

    constructor(error: Throwable?) {
        this.error = error
    }
}