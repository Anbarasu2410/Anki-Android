// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki

import androidx.test.ext.junit.runners.AndroidJUnit4
import anki.backend.backendError
import anki.scheduler.CongratsInfoResponse
import com.ichi2.testutils.rules.BackendOverrideRule
import net.ankiweb.rsdroid.Backend
import net.ankiweb.rsdroid.exceptions.BackendInvalidInputException
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class DeckPickerContextMenuReadErrorTest : RobolectricTest() {
    @get:Rule
    val backendOverride = BackendOverrideRule()

    @Test
    fun `a failed read while opening the deck context menu does not crash`() {
        val failRead = AtomicBoolean(false)
        val readFailed = AtomicBoolean(false)
        backendOverride.replaceWith {
            object : Backend() {
                override fun congratsInfo(): CongratsInfoResponse {
                    if (failRead.getAndSet(false)) {
                        readFailed.set(true)
                        throw BackendInvalidInputException(backendError { message = "buried card read failed" })
                    }
                    return super.congratsInfo()
                }
            }
        }
        withDeckPicker(deckCount = 2) { picker ->
            advanceRobolectricLooperUntil { picker.viewModel.loadDeckCounts?.isCompleted == true }
            throwOnShowError = false
            failRead.set(true)
            picker.viewModel.requestContextMenu(1L)
            advanceRobolectricLooper()
            assertTrue(readFailed.get(), "The context menu did not read the deck's buried cards")
        }
    }

    @Test
    fun `a failed read while opening the right click deck context menu does not crash`() {
        val failRead = AtomicBoolean(false)
        val readFailed = AtomicBoolean(false)
        backendOverride.replaceWith {
            object : Backend() {
                override fun congratsInfo(): CongratsInfoResponse {
                    if (failRead.getAndSet(false)) {
                        readFailed.set(true)
                        throw BackendInvalidInputException(backendError { message = "buried card read failed" })
                    }
                    return super.congratsInfo()
                }
            }
        }
        withDeckPicker(deckCount = 2) { picker ->
            advanceRobolectricLooperUntil { picker.viewModel.loadDeckCounts?.isCompleted == true }
            throwOnShowError = false
            failRead.set(true)
            picker.viewModel.requestRightClickContextMenu(1L, 0f, 0f)
            advanceRobolectricLooper()
            assertTrue(readFailed.get(), "The right click context menu did not read the deck's buried cards")
        }
    }
}
