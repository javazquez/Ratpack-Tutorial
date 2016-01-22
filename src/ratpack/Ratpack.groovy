import ratpack.groovy.template.MarkupTemplateModule
import static ratpack.groovy.Groovy.groovyMarkupTemplate
import static ratpack.groovy.Groovy.ratpack
import ratpack.handlebars.HandlebarsModule
import static ratpack.handlebars.Template.handlebarsTemplate;
import ratpack.form.Form
import ratpack.path.PathBinding
import ratpack.error.ClientErrorHandler
import com.javazquez.ApiErrorHandler

import ratpack.handling.Context
import ratpack.pac4j.RatpackPac4j
import ratpack.session.Session
import ratpack.session.SessionModule
import org.pac4j.oauth.client.GitHubClient
//More info at https://github.com/ratpack/ratpack-asset-pipeline and
// http://ratpack.io/manual/current/static-assets.html#asset_pipeline
import asset.pipeline.ratpack.AssetPipelineModule
import org.pac4j.oauth.profile.github.GitHubProfile

ratpack {
  bindings {
    /* The SessionModule will make sure every request is set up with a session.
     Also by default provide an in memory session data store.
     resources
     http://beckje01.com/talks/gr8us-2015-sec-ratpack.html#/6/1
     https://github.com/beckje01/ratpack-gr8us-sec/blob/basicAuth/src/ratpack/Ratpack.groovy
     */
    module SessionModule
    module MarkupTemplateModule
    module HandlebarsModule
    bind com.javazquez.SiteErrorHandler //this is global
    module(AssetPipelineModule) { config ->
      // only matters at development time, and path is relative to the build path
       config.sourcePath ="../../../src/assets"
    }
    // added above module and code ^^^^ based on this example in the below link
    // https://github.com/robfletcher/midcentury-ipsum/blob/master/src/main/kotlin/com/energizedwork/midcenturyipsum/Main.kt#L55
    //suggested per https://github.com/bertramdev/asset-pipeline/issues/55
  }

  handlers {
    get{
      //lets access request params
      if(!request.queryParams.isEmpty()){
        render "Here are the params you sent ${request.queryParams}"
      }else{
        //sending a static file
        render file("public/pages/index.html")
      }
    }

    get("register/"){
       render file("public/pages/registerForm.html")
    }
    get("contactus/"){
      render file("public/pages/contactus.html")
    }
    //take form data and only accept post for registration
    //curl -d "fname=twitterHandle" "http://localhost:5050/acceptRegistration/"
    post("acceptRegistration/"){
         parse(Form).then{ form ->
          //render a handlebars Template
           render handlebarsTemplate("acceptRegistration.html", [twitterHandle: form.get('twitterHandle')])
         }
    }
    //serverConfig is provided by ratpack
    //how to get the baseDir a.k.a ServerConfig.getBaseDir() but more groovy!
    get("baseDir/"){
      render "$serverConfig.baseDir"
    }
    get("userDetails/:userId"){
      def tokens =  get(PathBinding).allTokens
      render "show some stuff about user with id ${tokens.userId}"
    }
    //serve up a template file URL => http://localhost:5050/template/
    get('template/') {
      render groovyMarkupTemplate("index.gtpl", title: "My Ratpack App")
    }

    //lets create an api URL will start with http://localhost:5050/api/
    prefix("api"){
      //add a more specific api error handler rather than global SiteErrorHandler
      register { add(ApiErrorHandler, new ApiErrorHandler()) }
      post('sendGroupMessage/:user'){
        def tokens = get(PathBinding).allTokens
        parse(Form).then{ form ->
          render "user ${tokens.user} says '${form.get('message')}'"
        }
      }
      post('contentAware/'){
        byContent{
          //curl -X POST -H "Accept: application/json" http://localhost:5050/api/contentAware/
          json {
            render '"content: value goes here"'
          }
          //curl -X POST -H "Accept: text/plain" http://localhost:5050/api/contentAware/
          plainText {
            render "You requested plain text "
          }
          //curl -X POST -H "Accept: application/xml" http://localhost:5050/api/contentAware/
          xml {
            render "<ratpack><demo>demo xml</demo></ratpack>"
          }
          //curl -X POST -H "Accept: text/html" http://localhost:5050/api/contentAware/
          html {
            render '<doctype html><html><head><meta charset="UTF-8"></head><body><h1>This is an html page</h1></body></html>'
          }
          // we can create our own custom types!
          //curl -X POST -H "Accept: application/Johnny#5" http://localhost:5050/api/contentAware/
          type('application/Johnny#5'){
            render 'Need more Input'
          }
          //if nothing matches provide the following default
          //curl -d "MC=Hammer" -H "Accept: application/UcantTouchThis" http://localhost:5050/api/contentAware/
          noMatch {
            render "2 Legit to quit!"
          }
        }

      }
      // Lets bind a custom api error handler and make sure all requests let
      // the user know it is not supported
        all {
          clientError 404 //clientError this comes from Context
      }
     }
    /* Lets serve up static files.
     all files within the src/ratpack/public (or ${baseDir}/public) available
     to be served at the root URL unless another handler is previously matched in the chain
     ex ->   <link href="/styles/sticky-footer-navbar.css" rel="stylesheet">
       will serve the sticky-footer-navbar.css file
     */
    files { dir "public" }

    /* NOTE: set Authorization URL in Github admin screen to root of app
      for this example http://localhost:5050/ is the root
      lets add GitHubClient to this groovychain for downstream handlers
      details for client at the following URL
      http://www.pac4j.org/apidocs/pac4j/org/pac4j/oauth/client/GitHubClient.html
      */
    all(RatpackPac4j.authenticator(new GitHubClient('key', 'secret')))


    prefix("githubAuth"){
      //Require all requests past this point to have auth.
      //user admin ,pwd = admin
      // all(RatpackPac4j.requireAuth(GitHubClient))
      get{ Session session, Context ctx ->
        RatpackPac4j.userProfile(ctx, GitHubProfile).route({
          !it.isPresent()
        }, {
          ctx.response.send('text/html', 'No profile, click <a href="/githubAuth/login">here</a> to login')
        }).then{
          //ctx.get(Session).id
          render "An authenticated page. SessionId is $session.id"
        }
      }

      get('login') { ctx ->
        RatpackPac4j.login(ctx, GitHubClient).then {
          ctx.redirect('/githubAuth')
        }
      }
      get('testRoute'){Session session, Context ctx ->
        RatpackPac4j.userProfile(ctx, GitHubProfile).route({
          !it.isPresent()
        }, {
          ctx.response.send('text/html', 'No profile, click <a href="/githubAuth/login">here</a> to login')
        }).then{
          session.getId()
          render "An authenticated page. SessionId is  ${ctx.get(Session).id}"
        }
      }
      get('logout'){ Session session, Context ctx ->
        RatpackPac4j.logout(ctx)
        .flatMap{session.remove(ctx.get(Session).id).promise()}
        .flatMap {session.terminate().promise()}
        .then {
          ctx.redirect('/githubAuth')}
      }

    }
  }

}
