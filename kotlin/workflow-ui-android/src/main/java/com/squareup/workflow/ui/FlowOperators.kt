/*
 * Copyright 2019 Square Inc.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.workflow.ui

import io.reactivex.Flowable
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactive.openSubscription

/**
 * TODO kdoc
 *
 * TODO unit tests
 */
@FlowPreview
@UseExperimental(ObsoleteCoroutinesApi::class)
@Suppress("NOTHING_TO_INLINE")
internal inline fun <T> Flowable<out T>.asFlow(): Flow<T> = flow {
  openSubscription().consumeEach {
    emit(it)
  }
}
