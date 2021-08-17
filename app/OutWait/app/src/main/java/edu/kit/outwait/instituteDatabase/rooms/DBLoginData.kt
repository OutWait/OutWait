package edu.kit.outwait.instituteDatabase.rooms

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * This class helps room (through its annotations) to create a table
 * where the login Data is stored.
 *
 * @property username the username of the institution
 * @property password the password of the institution
 */
@Entity(tableName = "login_data")
data class DBLoginData (
    @PrimaryKey
    val username: String,
    val password: String
)
