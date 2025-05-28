package com.kiryusha.myapplication.exceptions

class ApiException(code: Int, message: String) : Exception("API-запрос не удался")