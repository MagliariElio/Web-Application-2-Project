package it.polito.students.document_store.repositories

import it.polito.students.document_store.entities.Metadata
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MetadataRepository: JpaRepository<Metadata, Int> {
    @Query("SELECT m FROM Metadata m WHERE m.name = :name")
    fun findAllByName(name: String): List<Metadata>
}