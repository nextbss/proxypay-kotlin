package core.models

data class ReferencesResponse(
        val terminal_period_id: Int? = null,
        val terminal_type: String? = null,
        val transaction_id: Int? = null,
        val amount: String? = null,
        val reference_id: Int? = null,
        val custom_fields: CustomFields? = null,
        val fee: Any? = null,
        val entity_id: Int? = null,
        val period_end_datetime: String? = null,
        val terminal_location: String? = null,
        val period_start_datetime: String? = null,
        val datetime: String? = null,
        val product_id: Int? = null,
        val id: Long? = null,
        val period_id: Int? = null,
        val terminal_transaction_id: Int? = null,
        val terminal_id: String? = null,
        val parameter_id: Any? = null
) : GenericModel
