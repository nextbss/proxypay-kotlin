package co.ao.box.models

data class PaymentReferenceRequest(
        var amount: String? = null,
        var custom_fields: CustomFields? = null,
        var end_datetime: String? = null
)