package edu.kit.outwait.instituteDatabase.facade

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import edu.kit.outwait.instituteDatabase.rooms.DBAuxiliaryIdentifierDao
import edu.kit.outwait.instituteDatabase.rooms.DBLoginDataDao
import edu.kit.outwait.instituteDatabase.rooms.InstituteRoomDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class InstituteRoomDBFacadeTest {
    private lateinit var dataBase: InstituteRoomDatabase
    private lateinit var dbFacade: InstituteDBFacade
    private lateinit var dao: DBAuxiliaryIdentifierDao
    private lateinit var loginDao: DBLoginDataDao

    @Before
    fun setUp() {
        dataBase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            InstituteRoomDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = dataBase.getDBAuxiliaryIdentifierDao()
        loginDao = dataBase.getDBLoginDataDao()

        dbFacade = InstituteRoomDBFacade(dao, loginDao)
    }

    @After
    fun tearDown() {
        dataBase.close()
    }

    @Test
    fun insertThreeAuxiliaryIdentifiersCorrectly() = runBlocking {
        dbFacade.insertUpdateAux("slot1", "aux1")
        dbFacade.insertUpdateAux("slot2", "aux2")
        dbFacade.insertUpdateAux("slot3", "aux3")
        val storedIdentifiers = dbFacade.getAuxiliaryIdentifiers()

        assertEquals(storedIdentifiers.keys.size, 3)
        assertEquals("aux1", storedIdentifiers["slot1"])
        assertEquals("aux2", storedIdentifiers["slot2"])
        assertEquals("aux3", storedIdentifiers["slot3"])
    }

    @Test
    fun updateAlreadyInsertedAuxiliaryIdentifierCorrectly() = runBlocking {
        dbFacade.insertUpdateAux("slot1", "aux1forSlot1")
        dbFacade.insertUpdateAux("slot1", "aux2forSlot1")
        val storedIdentifiers = dbFacade.getAuxiliaryIdentifiers()

        assertEquals(storedIdentifiers.keys.size, 1)
        assertEquals("aux2forSlot1", storedIdentifiers["slot1"])
    }

    @Test
    fun deleteOneOfThreeAuxiliaryIdentifiersCorrectly() = runBlocking {
        dbFacade.insertUpdateAux("slot1", "aux1")
        dbFacade.insertUpdateAux("slot2", "aux2")
        dbFacade.insertUpdateAux("slot3", "aux3")

        assertEquals(dbFacade.getAuxiliaryIdentifiers().keys.size, 3)

        dbFacade.deleteAux("slot2")
        val storedIdentifiers = dbFacade.getAuxiliaryIdentifiers()

        assertEquals(storedIdentifiers.keys.size, 2)
        assertEquals("aux1", storedIdentifiers["slot1"])
        assertEquals(null, storedIdentifiers["slot2"])
        assertEquals("aux3", storedIdentifiers["slot3"])
    }

    @Test
    fun testClearingTable() = runBlocking {
        dbFacade.insertUpdateAux("slot1", "aux1")
        dbFacade.insertUpdateAux("slot2", "aux2")
        dbFacade.insertUpdateAux("slot3", "aux3")

        assertEquals(dbFacade.getAuxiliaryIdentifiers().keys.size, 3)

        dbFacade.deleteAll()
        val storedIdentifiers = dbFacade.getAuxiliaryIdentifiers()

        assertEquals(storedIdentifiers.keys.size, 0)
    }
    /*
    getAuxiliaryIdentifiers() is tested implicitly in all the other methods
     */
    /*
    Following: Tests related to login data saving
     */
    @Test
    fun savingLoginDataWorksCorrectly() = runBlocking {
        dbFacade.insertUpdateLoginData("institutionName", "institutionPassword")
        assertEquals("institutionPassword", dbFacade.getPassword())
        assertEquals("institutionName", dbFacade.getUserName())
    }

    @Test
    fun test_LoginDataSaved_expectingTrue() = runBlocking {
        dbFacade.insertUpdateLoginData("institutionName", "institutionPassword")
        assertTrue(dbFacade.loginDataSaved())
    }

    @Test
    fun test_LoginDataSaved_expectingFalse() = runBlocking {
        assertFalse(dbFacade.loginDataSaved())
    }

    @Test
    fun oldLoginDataIsOverwrittenCorrectly() = runBlocking {
        dbFacade.insertUpdateLoginData("institutionName", "institutionPassword")
        dbFacade.insertUpdateLoginData("institutionName2", "institutionPassword2")
        dbFacade.insertUpdateLoginData("institutionName3", "institutionPassword3")
        assertEquals("institutionPassword3", dbFacade.getPassword())
        assertEquals("institutionName3", dbFacade.getUserName())
    }

    @Test
    fun clearingTableDeletesLoginData() = runBlocking {
        dbFacade.insertUpdateLoginData("institutionName", "institutionPassword")
        dbFacade.deleteAll()
        assertFalse(dbFacade.loginDataSaved())
    }
}
