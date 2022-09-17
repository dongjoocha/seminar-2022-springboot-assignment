package com.wafflestudio.seminar.survey.domain.database

import com.wafflestudio.seminar.survey.domain.OperatingSystem
import org.springframework.stereotype.Component

@Component
class OsRepositoryImpl(private val memoryDB: MemoryDB) : OsRepository {
    override fun findAll(): List<OperatingSystem> {
        return memoryDB.getOperatingSystems()
    }

    override fun findById(id: Long): OperatingSystem {
        return memoryDB.getOperatingSystems().find { it.id == id } ?: throw NoSuchElementException(id.toString())
    }

    override fun findByName(osName: String): List<OperatingSystem> {
        return memoryDB.getOperatingSystems().filter { it.osName == osName }
    }
}