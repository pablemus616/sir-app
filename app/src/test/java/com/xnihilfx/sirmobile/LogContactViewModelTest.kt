package com.xnihilfx.sirmobile

import app.cash.turbine.test
import com.xnihilfx.sirmobile.data.remote.ApiEnvelope
import com.xnihilfx.sirmobile.data.remote.CandidateContactsApi
import com.xnihilfx.sirmobile.data.remote.ContactTypesApi
import com.xnihilfx.sirmobile.data.remote.Paginated
import com.xnihilfx.sirmobile.data.remote.dto.CandidateContactDto
import com.xnihilfx.sirmobile.data.remote.dto.ContactTypeDto
import com.xnihilfx.sirmobile.data.remote.dto.CreateCandidateContactRequest
import com.xnihilfx.sirmobile.data.repository.CandidateContactsRepository
import com.xnihilfx.sirmobile.data.repository.ContactTypesRepository
import com.xnihilfx.sirmobile.ui.logcontact.LogContactEvent
import com.xnihilfx.sirmobile.ui.logcontact.LogContactViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LogContactViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun submit_posts_contact_and_emits_saved() = runTest {
        val repo = FakeContactsRepo()
        val vm = LogContactViewModel(
            repo,
            FakeContactTypesRepo(listOf(ContactTypeDto(1, "call"))),
            json,
            candidateId = 5,
            opportunityId = 9,
        )
        vm.onTypeSelected(1)
        vm.onDirection("outbound")
        vm.onNotes("Llamada inicial")
        vm.submit()
        vm.events.test { assertEquals(LogContactEvent.Saved, awaitItem()) }
        assertEquals(5, repo.last!!.candidateId)
        assertEquals(9, repo.last!!.opportunityId)
        assertEquals(1, repo.last!!.contactType)
        assertNotNull(repo.last!!.contactTime) // ISO timestamp set by the VM
    }

    @Test
    fun submit_without_type_emits_error() = runTest {
        val repo = FakeContactsRepo()
        val vm = LogContactViewModel(
            repo,
            FakeContactTypesRepo(listOf(ContactTypeDto(1, "call"))),
            json,
            candidateId = 5,
            opportunityId = 9,
        )
        // No seleccionamos tipo → error de validación
        vm.submit()
        vm.events.test {
            val event = awaitItem()
            assertTrue(event is LogContactEvent.Error)
        }
        assertNull(repo.last)
    }
}

// ---------------------------------------------------------------------------
// Dobles de prueba (fakes) definidos en el archivo de test
// ---------------------------------------------------------------------------

private class FakeContactsRepo : CandidateContactsRepository(FakeContactsApi) {
    var last: CreateCandidateContactRequest? = null

    override suspend fun create(req: CreateCandidateContactRequest): CandidateContactDto {
        last = req
        return CandidateContactDto(
            id = 1,
            candidateId = req.candidateId,
            opportunityId = req.opportunityId,
            contactType = null,
            contactTime = req.contactTime,
            recruiterEmployeeId = 0,
        )
    }
}

private object FakeContactsApi : CandidateContactsApi {
    override suspend fun list(
        candidateId: Int?,
        opportunityId: Int?,
        page: Int,
        limit: Int,
    ): ApiEnvelope<Paginated<CandidateContactDto>> =
        ApiEnvelope(ok = true, data = Paginated())

    override suspend fun create(body: CreateCandidateContactRequest): ApiEnvelope<CandidateContactDto> =
        ApiEnvelope(ok = true, data = null)
}

private class FakeContactTypesRepo(
    private val types: List<ContactTypeDto>,
) : ContactTypesRepository(FakeContactTypesApi) {
    override suspend fun all(): List<ContactTypeDto> = types
}

private object FakeContactTypesApi : ContactTypesApi {
    override suspend fun list(limit: Int): ApiEnvelope<Paginated<ContactTypeDto>> =
        ApiEnvelope(ok = true, data = Paginated())
}
