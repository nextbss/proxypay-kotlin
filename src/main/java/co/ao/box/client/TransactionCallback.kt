/*
 *
 *  * Copyright (C) Next Solutions - All Rights Reserved
 *  * Unauthorized copying of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential and will be punished by law
 *  * Written by Alexandre Antonio Juca <alexandre.juca@nextbss.co.ao>
 *
 */

package co.ao.box.client

/*
 *
 */
interface TransactionCallback<T> {
    fun onSuccess(response: T)
    fun onFailure(error: String)
}