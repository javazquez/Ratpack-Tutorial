/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.javazquez

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import ratpack.error.internal.ErrorHandler
import ratpack.handling.Context

import static ratpack.groovy.Groovy.groovyMarkupTemplate


/*
  NOTE 1: ErrorHandler extends ClientErrorHandler, ServerErrorHandler
   look at the following interfaces to see which @Override corresponds to which interface
    https://github.com/ratpack/ratpack/blob/master/ratpack-core/src/main/java/ratpack/error/ServerErrorHandler.java
    https://github.com/ratpack/ratpack/blob/master/ratpack-core/src/main/java/ratpack/error/ClientErrorHandler.java

*/
@Slf4j
@CompileStatic
class ApiErrorHandler implements ErrorHandler {

// ClientErrorHandler
 @Override
 void error(Context context, int statusCode) {
   println "in api error "
   context.response.status(statusCode)
   message(context, statusCode == 404 ? "The API that you have requested does not exist. [APIErrorHandler.groovy]" : "The request is invalid (HTTP $statusCode).[APIErrorHandler.groovy]")
   if (statusCode == 404) {
     log.error "404 for $context.request.path"
   }
 }

// ServerErrorHandler
 @Override
 void error(Context context, Throwable throwable) throws Exception {
   println "in error"
   context.with {
     response.status(500)
     log.error "", throwable
     message(context, throwable.message ?: "<no message>")
   }
 }
 static void message(Context context, CharSequence message) {
    context.render(groovyMarkupTemplate("error.gtpl", message: message.toString(), statusCode: context.response.status.code))
  }
}
