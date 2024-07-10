package it.polito.students.document_store.repositories

import it.polito.students.document_store.entities.Document
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DocumentRepository: JpaRepository<Document, Int>