package com.rosterforge.wh40k.presentation.create

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds in-progress roster creation drafts across the multi-step Create Roster
 * flow. The draft id is passed as a navigation argument; the actual data lives
 * here so each screen can read and update its own slice without serialising
 * everything through the back stack.
 *
 * Drafts are cleared automatically once the roster is persisted.
 */
@Singleton
class RosterDraftHolder @Inject constructor() {

    data class Draft(
        val id: String,
        val name: String = "",
        val pointsLimit: Int = 2000,
        val factionId: String? = null,
        val detachmentId: String? = null,
    )

    private val drafts = ConcurrentHashMap<String, Draft>()

    fun newDraft(): Draft {
        val draft = Draft(id = UUID.randomUUID().toString())
        drafts[draft.id] = draft
        return draft
    }

    fun get(id: String): Draft? = drafts[id]

    fun update(id: String, transform: (Draft) -> Draft): Draft? =
        drafts[id]?.let { existing ->
            val updated = transform(existing)
            drafts[id] = updated
            updated
        }

    fun clear(id: String) {
        drafts.remove(id)
    }
}
