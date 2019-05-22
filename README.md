# ProxyPay 
A Kotlin/Java library that helps you easily interact with the ProxyPay API

[![](https://jitpack.io/v/nextbss/proxypay-kotlin.svg)](https://jitpack.io/#nextbss/proxypay-kotlin)



Usage
---------------

### Download
To get a Git project into your build:

Step 1. Add the JitPack repository to your build file

maven
```xml
<repositories>
	<repository>
		<id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>
```

gradle
```xml
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
}
```

Step 2. Add the dependency

maven 
```xml
<dependency>
	<groupId>com.github.nextbss</groupId>
	<artifactId>proxypay-kotlin</artifactId>
	<version>Tag</version>
</dependency>
```

gradle
```xml
dependencies {
	 implementation 'com.github.nextbss:proxypay-kotlin:v1.0'
}
```

### Configuring Authorization and environment
To interact with ProxyPay you will need to define the environment to interact with and
the api key for authentication.

You have at your disposal two environments: SANDBOX and PRODUCTION

Using the SANDBOX Environment
```java
    ProxyPayConfig.configure(Environment.SANDBOX, "YOUR_API_KEY")
```

Using the PRODUCTION Environment
```java
    ProxyPayConfig.configure(Environment.PRODUCTION, "YOUR_API_KEY")
```

### Get Payment References
Returns any Payment events stored on the server that were not yet Acknowledged
```java

    ProxyPayConfig.configure(Environment.SANDBOX, "YOUR_API_KEY")

    val proxyPay = ProxyPayReference.ProxyReferenceBuilder()
        .addProxyPayConfiguration(ProxyPayConfig.getInstance())
        .build()

    proxyPay.getPayments(object : TransactionCallback<List<ReferencesResponse>> {
        override fun onSuccess(response: List<ReferencesResponse>) {
            println(response)
        }

        override fun onFailure(error: String) {
            println(error)
        }
    })
```

### Get Payments
Returns any Payment events stored on the server that were not yet Acknowledged by the client application.
Optional argument: Specify the amount of references (between 1 and 100), defaults to 100.

```java
ProxyPayConfig.configure(Environment.SANDBOX, "YOUR_API_KEY")
    val proxyPay = ProxyPayReference.ProxyReferenceBuilder()
        .addProxyPayConfiguration(ProxyPayConfig.getInstance())
        .build()

    proxyPay.getPayments(object : TransactionCallback<List<ReferencesResponse>> {
        override fun onSuccess(response: List<ReferencesResponse>) {
            println(response)
        }

        override fun onFailure(error: String) {
            println(error)
        }
    }, 10)
```
### Create Reference Id
The ```generateReferenceId()``` method allows the generation of a Reference Id
```java

ProxyPayConfig.configure(Environment.SANDBOX, "YOUR_API_KEY")

val proxyPay = ProxyPayPayment.PaymentTransactionBuilder()
        .addProxyPayConfiguration(ProxyPayConfig.getInstance())
        .build()

proxyPayPayment?.generateReferenceId(object: TransactionCallback<String> {
        override fun onSuccess(response: String) {
            println(response)
        }

        override fun onFailure(error: String) { println(error) }
    })
```

### Create Payment Reference
The ```generateReference``` method creates or updates a payment reference with given Id
```java

ProxyPayConfig.configure(Environment.SANDBOX, "YOUR_API_KEY")

val customFields = CustomFields()
    customFields.app_description = "YOUR APP DESCRIPTION"
    customFields.invoice = "YOUR_INVOICE_NUMBER"
    customFields.order_number = "YOUR_ORDER_NUMBER"
    customFields.proposal_number = "YOUR_PROPOSAL_NUMBER"

    val request = PaymentReferenceRequest()
    request.amount = "3000.00"
    request.custom_fields = customFields
    request.expiry_date = "10-09-2019"

val proxyPay = ProxyPayPayment.PaymentTransactionBuilder()
        .addProxyPayConfiguration(ProxyPayConfig.getInstance())
        .addReferenceRequest(request)
        .build()

proxyPayPayment?.generateReference(object: TransactionCallback<String> {
        override fun onSuccess(response: String) { 
            println(response)
        }

        override fun onFailure(error: String) { 
            println(error)
        }
    }, referenceId)
```

### Delete a reference with a given id
```java

ProxyPayConfig.configure(Environment.SANDBOX, "YOUR_API_KEY")

val proxyPay = ProxyPayPayment.PaymentTransactionBuilder()
        .addProxyPayConfiguration(ProxyPayConfig.getInstance())
        .build()

proxyPayPayment?.deleteReference(object : TransactionCallback<String> {
        override fun onSuccess(response: String) {
            println(response)
        }

        override fun onFailure(error: String) {
            println(error)
        }
    }, referenceId)
```

### Acknowledge a payment
This method is used to ackonwledge that a payment was processed
```java

ProxyPayConfig.configure(Environment.SANDBOX, "YOUR_API_KEY")

val proxyPay = ProxyPayPayment.PaymentTransactionBuilder()
        .addProxyPayConfiguration(ProxyPayConfig.getInstance())
        .build()
        
    proxyPayPayment?.acknowledgePayment(object: TransactionCallback<String> {
            override fun onSuccess(response: String) {
                println(response)
            }
    
            override fun onFailure(error: String) {
                println(error)
            }
        }, referenceId)
```

License
----------------

The library is available as open source under the terms of the [MIT License](http://opensource.org/licenses/MIT).