package com.anindra.messages.ui

import android.telephony.SmsManager
import com.anindra.messages.R
import com.anindra.messages.data.Message

internal fun smsFailureLabel(message: Message): Int {
    if (message.mediaType != "text" || message.transport != "sms") return R.string.chat_status_not_sent
    return when (message.smsError) {
        SmsManager.RESULT_ERROR_RADIO_OFF -> R.string.sms_radio_recovery
        SmsManager.RESULT_ERROR_NO_SERVICE, SmsManager.RESULT_RADIO_NOT_AVAILABLE,
        SmsManager.RESULT_RIL_RADIO_NOT_AVAILABLE, SmsManager.RESULT_RIL_NETWORK_NOT_READY -> R.string.sms_service_recovery
        else -> R.string.sms_unknown_recovery
    }
}
