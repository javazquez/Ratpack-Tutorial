import ratpack.groovy.template.MarkupTemplateModule
import static ratpack.groovy.Groovy.groovyMarkupTemplate
import static ratpack.groovy.Groovy.ratpack
import ratpack.handlebars.HandlebarsModule
import static ratpack.handlebars.Template.handlebarsTemplate;
import ratpack.form.Form
import ratpack.path.PathBinding
import ratpack.error.ClientErrorHandler
import com.javazquez.ApiErrorHandler


ratpack {
  bindings {
    module MarkupTemplateModule
    module HandlebarsModule
    bind com.javazquez.SiteErrorHandler //this is global
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
  }
}
