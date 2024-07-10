package it.polito.students.crm.utils

import it.polito.students.crm.dtos.CreateAddressDTO
import java.util.regex.Pattern

/**
 *  Convert in a CategoryOptions and check whether category is a valid String
 *
 *  @param categoryIn
 *  @return contact
 *  @throws IllegalArgumentException if priority cannot be converted in PriorityOptions
 */
@Throws
public fun checkCategoryIsValid(categoryIn: String): CategoryOptions {
    try {
        val category = CategoryOptions.valueOf(categoryIn.uppercase())
        return category
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("Illegal category type!")
    }
}

public fun isValidEmail(email: String): Boolean {
    val mailRegex = getEmailRegex()
    val m: Pattern = Pattern.compile(mailRegex)
    val mail = m.matcher(email).matches()
    return mail
}

public fun isValidPhone(phone: String): Boolean {
    val phoneRegex = getPhoneRegex()
    val p: Pattern = Pattern.compile(phoneRegex)
    val tel = p.matcher(phone).matches()
    return tel
}

public fun isValidAddress(a: CreateAddressDTO): Boolean {
    return !(a.city.isNullOrBlank() || a.address.isNullOrBlank() || a.region.isNullOrBlank() || a.state.isNullOrBlank())
}