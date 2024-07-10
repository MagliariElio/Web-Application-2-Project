package it.polito.students.crm.utils

enum class WhatContactOptions {
    ADDRESS,
    EMAIL,
    TELEPHONE
}

fun getPhoneRegex(): String {
    return "^(\\+\\d{1,2}\\s?)?1?-?\\.?\\s?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}\$"
}

fun getEmailRegex(): String {
    return "^[a-zA-Z0-9_!#$%&'*.+/=?`{|}~^-]+( ?:.[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+)*@[a-zA-Z0-9-]+(?:.[a-zA-Z0-9-]+)*$"
}