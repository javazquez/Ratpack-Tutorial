import ratpack.groovy.template.MarkupTemplateModule

import static ratpack.groovy.Groovy.groovyMarkupTemplate
import static ratpack.groovy.Groovy.ratpack
import ratpack.form.Form
import ratpack.path.PathBinding
ratpack {
  bindings {
    module MarkupTemplateModule
    bind com.javazquez.SiteErrorHandler //this is global
  }


  handlers {
    //sending a static file
    get{
      render file("public/pages/index.html")
    }

    get("register/"){
      render "enter your registration data here"
    }

    //take form data and only accept post for registration
    //curl -d "fname=Juan" "http://localhost:5050/acceptRegistration/"
    post("acceptRegistration/"){
         parse(Form).then{ form ->
           render "you have passed ${form.get('fname')}"
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

    //serve up a template file
    //http://localhost:5050/template/
    get('template/') {
      render groovyMarkupTemplate("index.gtpl", title: "My Ratpack App")
    }

    //Lets serve up static files
    files { dir "public" }
  }
}
