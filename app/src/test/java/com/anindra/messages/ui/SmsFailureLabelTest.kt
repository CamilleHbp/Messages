package com.anindra.messages.ui

import android.telephony.SmsManager
import com.anindra.messages.R
import com.anindra.messages.data.Message
import org.junit.Assert.assertEquals
import org.junit.Test

class SmsFailureLabelTest {
    private val sms = Message(1, 1, "Draft", 0, true, "failed")

    @Test fun distinguishesRadioPowerFromMissingService() {
        assertEquals(R.string.sms_radio_recovery, smsFailureLabel(sms.copy(smsError = SmsManager.RESULT_ERROR_RADIO_OFF)))
        for (code in listOf(SmsManager.RESULT_ERROR_NO_SERVICE, SmsManager.RESULT_RIL_NETWORK_NOT_READY,
            SmsManager.RESULT_RADIO_NOT_AVAILABLE, SmsManager.RESULT_RIL_RADIO_NOT_AVAILABLE)) {
            assertEquals(R.string.sms_service_recovery, smsFailureLabel(sms.copy(smsError = code)))
        }
    }

    @Test fun unclassifiedAndLegacySmsGiveAdviceWithoutGuessingTheCause() {
        assertEquals(R.string.sms_unknown_recovery, smsFailureLabel(sms))
        assertEquals(R.string.sms_unknown_recovery, smsFailureLabel(sms.copy(smsError = 999)))
    }

    @Test fun mmsUsesItsOwnFailureStatus() {
        assertEquals(R.string.chat_status_not_sent, smsFailureLabel(sms.copy(transport = "mms")))
        assertEquals(R.string.chat_status_not_sent, smsFailureLabel(sms.copy(mediaType = "image")))
    }
}
