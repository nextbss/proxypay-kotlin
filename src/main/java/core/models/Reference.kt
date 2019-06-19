package core.models

data class Reference(
        val number: String? = null,
        val amount: String? = null,
        val updated_at: String? = null,
        val custom_fields: CustomFields? = null,
        val expiry_date: String? = null,
        val created_at: String? = null,
        val id: String? = null,
        val entity_id: String? = null,
        val status: String? = null
) : GenericModel
