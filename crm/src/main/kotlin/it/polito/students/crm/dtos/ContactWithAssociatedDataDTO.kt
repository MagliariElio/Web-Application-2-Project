package it.polito.students.crm.dtos

import it.polito.students.crm.entities.Contact

data class ContactWithAssociatedDataDTO(
    var contactDTO: ContactDTO,
    var emailDTOs: List<EmailDTO>,
    var telephoneDTOs: List<TelephoneDTO>,
    var addressDTOs: List<AddressDTO>
)

fun Contact.toDTOWithAssociatedData(): ContactWithAssociatedDataDTO = ContactWithAssociatedDataDTO(
    this.toDTO(),
    this.emails.map { it.toDTO() },
    this.telephones.map { it.toDTO() },
    this.addresses.map { it.toDTO() }
)