import core.Environment
import core.ProxyPayPayment
import core.client.TransactionCallback
import core.config.ProxyPayConfig
import core.models.CustomFields
import core.models.MockPaymentRequest
import core.models.MockPaymentResponse
import core.models.PaymentReferenceRequest

fun main() {

    ProxyPayConfig.configure(Environment.SANDBOX, "YOUR_API_KEY")

    val customFields = CustomFields()
    customFields.app_description = "YOUR APP DESCRIPTION"
    customFields.invoice = "YOUR_INVOICE_NUMBER"
    customFields.order_number = "YOUR_ORDER_NUMBER"
    customFields.proposal_number = "YOUR_PROPOSAL_NUMBER"

    val request = PaymentReferenceRequest()
    request.amount = "3000.00"
    request.custom_fields = customFields
    request.end_datetime = "2019-10-12"

    var proxyPay = ProxyPayPayment.PaymentTransactionBuilder()
            .addProxyPayConfiguration(ProxyPayConfig.getInstance())
            .addReferenceRequest(request)
            .build()

    proxyPay?.generateReference(object: TransactionCallback<String> {
        override fun onSuccess(response: String) {
            println(response)
        }

        override fun onFailure(error: String) {
            println(error)
        }
    }, "841520000")

    val mockPaymentRequest = MockPaymentRequest("2000.00", "841520000")

    proxyPay = ProxyPayPayment.PaymentTransactionBuilder()
            .addProxyPayConfiguration(ProxyPayConfig.getInstance())
            .addMockPaymentRequest(mockPaymentRequest)
            .build()

    proxyPay.mockPayment(object: TransactionCallback<MockPaymentResponse> {
        override fun onSuccess(response: MockPaymentResponse) {
            println(response.toString())
        }

        override fun onFailure(error: String) {
            println("Failure occurred: $error")
        }
    })
}