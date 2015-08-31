import spock.lang.Specification
import ratpack.test.http.TestHttpClient
import spock.lang.Shared
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
  // def "Need to have an error page"(){
  //
  // }

/*  def "test the baseDir Url"(){
    when: "a GET request is sent to baseDir url"
      testClient.get("baseDir/") // we don't have to assign the ReceivedResponse returned as TestHttpClient will keep track of this for us

    then: "a response is returned with body text of 'Hello Greach!'"
    testClient.response.body.text.contains("src/ratpack") // `testClient.response` is the ReceivedResponse from the last request sent

  }
*/

  def "test index page "(){
    when: "a GET request is sent with no path"
      testClient.get() // we don't have to assign the ReceivedResponse returned as TestHttpClient will keep track of this for us
      //println testClient.response.body.text
    then: "a response is returned with body text of 'Hello Greach!'"
    testClient.response.body.text == """<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title>Welcome to my conference</title>
  </head>
  <body>
    Thanks for your interest in my Conference
  </body>
</html>
""" // `testClient.response` is the ReceivedResponse from the last request sent

  }

  def "test registration page "(){
    when:"A GET request is sent"
        get("register/") // Using `@Delegate` on the testClient property means we don't have to keep doing `testClient.get()`
    then:"Expect a response from the page"
        response.body.text == 'enter your registration data here' // Taking advantage of `@Delegate` here too
  }

  def "test POSTing of registration form data"(){
    when: "posting form with fname as a param"
    requestSpec { requestSpec ->
			requestSpec.headers.set("Content-Type", APPLICATION_FORM)
       requestSpec.body.stream({ it << "fname=Juan" })
    }

    post("acceptRegistration/")

    then: "expecting a string to come backe indicating success"
      response.body.text == 'you have passed Juan'
  }

  def "test that our userDetails/:userId page is working"(){
    when:"passing userId in URL"
      get('userDetails/123')
    then:
      response.body.text == "show some stuff about user with id 123"
  }
  def "test defualt error page"(){
    when:"testing 404"
      get('I_DontExist/onThisServer')
    then:
      response.body.text == "This is not the page you are looking for"
  }

}
