/*
 *
 *  * Copyright (C) Next Solutions - All Rights Reserved
 *  * Unauthorized copying of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential and will be punished by law
 *  * Written by Alexandre Antonio Juca <alexandre.juca@nextbss.co.ao>
 *
 */

package core.client

interface ListTransactionCallback<T> {
    fun onSuccess(response: List<T>)
    fun onFailure(error: String)
}