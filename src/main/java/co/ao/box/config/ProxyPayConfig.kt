/*
 *
 *  * Copyright (C) Next Solutions - All Rights Reserved
 *  * Unauthorized copying of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential and will be punished by law
 *  * Written by Alexandre Antonio Juca <alexandre.juca@nextbss.co.ao>
 *
 */

package co.ao.box.config

object ProxyPayConfig {
    private lateinit var environment: Environment
    private lateinit var apiKey: String

    /**
     * This method is used to define the environment the client will interact with
     * as well the apiKey for that environment.
     * @param Environment: The environment the client will interact with
     * @param apiKey: Token required to authenticate to ProxyPay
     * @returns: An Instance of ProxyPayConfig
     */
    fun configure(environment: Environment, apiKey: String): ProxyPayConfig {
        if (environment == null) throw IllegalStateException("Environment cannot be null")

        if (apiKey == null || apiKey == "") throw java.lang.IllegalStateException("ApiKey cannot be null.")

        ProxyPayConfig.environment = environment
        ProxyPayConfig.apiKey = apiKey
        return this
    }

    /**
     * @return: Returns an instance of ProxyPayConfig
     */
    fun getInstance(): ProxyPayConfig {
        if (!this::apiKey.isInitialized && !this::environment.isInitialized) throw java.lang.IllegalStateException("Cannot get an instance of an unconfigured ProxyPayConfig.")
        return this
    }

    /**
     * @return:  The Token used to authenticate to ProxyPay
     */
    fun getApiKey() = apiKey

    /**
     * @return: The Environment used to interact with ProxyPay
     */
    fun getEnvironment() = environment

}