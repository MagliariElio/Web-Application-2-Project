package it.polito.students.crm.repositories

import it.polito.students.crm.entities.Address
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AddressRepository : JpaRepository<Address, Long> {
    fun findByAddressIgnoreCaseAndCityIgnoreCaseAndRegionIgnoreCaseAndStateIgnoreCase(
        address: String,
        city: String,
        state: String,
        case: String
    ): Address?
}