import spock.lang.Specification
import ratpack.test.http.TestHttpClient
import spock.lang.Shared
import spock.lang.Unroll
import ratpack.groovy.test.GroovyRatpackMainApplicationUnderTest
import ratpack.test.ApplicationUnderTest
import static ratpack.http.MediaType.APPLICATION_FORM

// NOTE: used https://github.com/ratpack/ratpack/blob/master/ratpack-core/src/test/groovy/ratpack/http/FormHandlingSpec.groovy
// as a reference for testing


class HandlerSpec extends Specification {
  // Start our application and make it available for testing. `@Shared` means the same app instance will be used for _all_ tests
  @Shared
  ApplicationUnderTest appUnderTest = new GroovyRatpackMainApplicationUnderTest()

  // ApplicationUnderTest includes a TestHttpClient that we can use for sending requests to our application.
  @Delegate
  TestHttpClient testClient = appUnderTest.httpClient

  def "contact us page"(){
    when:
    testClient.get()

    then:

    testClient.response.statusCode == 200
  }

  def "test index page "(){
    when: "a GET request is sent with no path"
      testClient.get() // we don't have to assign the ReceivedResponse returned as TestHttpClient will keep track of this for us
      //println testClient.response.body.text
    then: "a response is returned with body text of 'Hello Greach!'"
    testClient.response.statusCode == 200 // `testClient.response` is the ReceivedResponse from the last request sent

  }

  def "test registration page "(){
    when:"A GET request is sent"
        get("register/") // Using `@Delegate` on the testClient property means we don't have to keep doing `testClient.get()`
    then:"Expect a response from the page"
        response.body.text.find(/This is the best Conf ever!/) // Taking advantage of `@Delegate` here too
  }

  def "test POSTing of registration form data"(){
    when: "posting form with fname as a param"
    requestSpec { requestSpec ->
			requestSpec.headers.set("Content-Type", APPLICATION_FORM)
       requestSpec.body.stream({ it << "twitterHandle=Juan" })
    }

    post("acceptRegistration/")

    then: "expecting a string to come back indicating success"
      response.body.text == '''
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title>Accept registration</title>
  </head>
  <body>
<h1>Welcome @Juan!</h1>
  </body>
</html>
'''
  }

  def "test that our userDetails/:userId page is working"(){
    when:"passing userId in URL"
      get('userDetails/123')
    then:
      response.body.text == "show some stuff about user with id 123"
  }
  def "test default error page"(){
    when:"testing 404"
      get('I_DontExist/onThisServer')
    then:
    response.statusCode == 404
//       response.body.text == '''<!doctype html>
// <!--[if lt IE 7]>      <html class="no-js lt-ie9 lt-ie8 lt-ie7"> <![endif]-->
// <!--[if IE 7]>         <html class="no-js lt-ie9 lt-ie8"> <![endif]-->
// <!--[if IE 8]>         <html class="no-js lt-ie9"> <![endif]-->
// <!--[if gt IE 8]><!--> <html class="no-js"> <!--<![endif]-->
// <head><meta charset='utf-8'/><title>Custom error page</title><meta name='apple-mobile-web-app-title' content='Ratpack'/><meta name='description' content='Ratpack apps are lightweight, fast, composable with other tools and libraries, easy to test and enjoyable to develop.'/><meta name='viewport' content='width=device-width, initial-scale=1'/></head><body><p class='http-status-code' style='display: none'>404</p><section><article class='content'><h3>The page you have requested does not exist. [SiteErrorHandler.groovy]</h3><p>Sorry about that :(</p></article></section></body></html>'''
  }

  def "test API Error Handler"(){
    when: "I ask for a bogus API"
      post('api/bogus')

    then: "to receive the custom APIErrorHandler"
      response.body.text.find(/APIErrorHandler.groovy/)//get text from page and find string

  }
  def "test API is working"(){
    when:"I ask for a legit api that uses Path tokens"
    requestSpec { requestSpec ->
			requestSpec.headers.set("Content-Type", APPLICATION_FORM)
       requestSpec.body.stream({ it << "message=VIM is evil mode in Emacs" })
    }
          post('api/sendGroupMessage/Juan')

    then:"I should see a greeting message with the username pathtoken"
      response.body.text.find(/user Juan says 'VIM is evil mode in Emacs'/)
  }

  @Unroll
  def "test that our API byContent endpoint is working for '#header'"(header, resultText){
    expect: "I pass in the Accept Header for $header"
    requestSpec { requestSpec ->
			requestSpec.headers.set("Accept", header)
    }

    post('api/contentAware/')
    response.body.text == resultText
    where:
    header                       | resultText
    'application/json'           | '"content: value goes here"'
    'text/plain'                 | 'You requested plain text '
    'application/xml'            | '<ratpack><demo>demo xml</demo></ratpack>'
    'text/html'                  | '<doctype html><html><head><meta charset="UTF-8"></head><body><h1>This is an html page</h1></body></html>'
    'application/Johnny#5'       | 'Need more Input'
    'application/UcantTouchThis' | '2 Legit to quit!'
  }

}
