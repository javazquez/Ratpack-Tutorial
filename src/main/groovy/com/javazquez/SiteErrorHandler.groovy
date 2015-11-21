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

//got this from https://github.com/ratpack/ratpack/blob/master/ratpack-site/src/main/groovy/ratpack/site/SiteErrorHandler.groovy
@Slf4j
@CompileStatic
class SiteErrorHandler implements ErrorHandler {

  @Override
  void error(Context context, int statusCode) {
    context.response.status(statusCode)
    String msg =''
    if(statusCode == 404){
      msg = "The page you have requested does not exist. [SiteErrorHandler.groovy]"
    }else if(statusCode == 401){
      msg = "You are not Authorized to view the page you have requested. [SiteErrorHandler.groovy]"
    }else{
      msg ="The request is invalid (HTTP $statusCode).[SiteErrorHandler.groovy]"
    }
    message(context, msg)
    if (statusCode) {
      log.error "$statusCode for $context.request.path"
    }
  }

  @Override
  void error(Context context, Throwable throwable) throws Exception {
    context.with {
      response.status(500)
      log.error "", throwable
      message(context, throwable.message ?: "<no message>")
    }
  }


  static void message(Context context, CharSequence message) {
//    context.render("This is not the page you are looking for")
      context.render(groovyMarkupTemplate("error.gtpl", message: message.toString(), statusCode: context.response.status.code))
  }

}
