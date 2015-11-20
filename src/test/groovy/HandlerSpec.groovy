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
    testClient.response.body.text == '''
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <meta name="description" content="">
    <meta name="author" content="">
    <link rel="icon" href="../../favicon.ico">

    <title>Register for my conf</title>
    <!-- Latest compiled and minified CSS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">

    <!-- Custom styles for this template -->
    <link href="/styles/sticky-footer-navbar.css" rel="stylesheet">



    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
  </head>

  <body>

    <!-- Fixed navbar -->
    <nav class="navbar navbar-default navbar-fixed-top">
      <div class="container">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand " href="#">My Conf</a>
        </div>
        <div id="navbar" class="collapse navbar-collapse">
          <ul class="nav navbar-nav">
            <li ><a href="/register/">Register</a></li>
            <li><a href="/contactus/">Contact</a></li>

          </ul>
        </div><!--/.nav-collapse -->
      </div>
    </nav>

    <!-- Begin page content -->
    <div class="container">
      <div class="page-header">
        <h1>Welcome to conference home page</h1>
      </div>
      <p class="lead">We are very excited that you are considering attending</p>


    </div>

    <footer class="footer">
      <div class="container">
        <p class="text-muted">Our conference is great!</p>
      </div>
    </footer>


    <!-- Bootstrap core JavaScript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>

<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>


  </body>
</html>
''' // `testClient.response` is the ReceivedResponse from the last request sent

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
      response.body.text == '''<!doctype html>
<!--[if lt IE 7]>      <html class="no-js lt-ie9 lt-ie8 lt-ie7"> <![endif]-->
<!--[if IE 7]>         <html class="no-js lt-ie9 lt-ie8"> <![endif]-->
<!--[if IE 8]>         <html class="no-js lt-ie9"> <![endif]-->
<!--[if gt IE 8]><!--> <html class="no-js"> <!--<![endif]-->
<head><meta charset='utf-8'/><title>Custom error page</title><meta name='apple-mobile-web-app-title' content='Ratpack'/><meta name='description' content='Ratpack apps are lightweight, fast, composable with other tools and libraries, easy to test and enjoyable to develop.'/><meta name='viewport' content='width=device-width, initial-scale=1'/></head><body><p class='http-status-code' style='display: none'>404</p><section><article class='content'><h3>The page you have requested does not exist. [SiteErrorHandler.groovy]</h3><p>Sorry about that :(</p></article></section></body></html>'''
  }

  def "test API Error Handler"(){
    when: "I ask for a bogus API"
      post('api/bogus')

    then: "to receive the custom APIErrorHandler"
      response.body.text.find(/APIErrorHandler.groovy/)

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
