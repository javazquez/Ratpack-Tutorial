yieldUnescaped '''<!doctype html>
<!--[if lt IE 7]>      <html class="no-js lt-ie9 lt-ie8 lt-ie7"> <![endif]-->
<!--[if IE 7]>         <html class="no-js lt-ie9 lt-ie8"> <![endif]-->
<!--[if IE 8]>         <html class="no-js lt-ie9"> <![endif]-->
<!--[if gt IE 8]><!--> <html class="no-js"> <!--<![endif]-->
'''
//look at https://raw.githubusercontent.com/ratpack/ratpack/master/ratpack-site/src/ratpack/templates/layout.gtpl as an example
head {
  meta(charset:'utf-8')
  title('Ratpack: Simple, lean & powerful HTTP apps')

  meta(name: 'apple-mobile-web-app-title', content: 'Ratpack')
  meta(name: 'description', content: 'Ratpack apps are lightweight, fast, composable with other tools and libraries, easy to test and enjoyable to develop.')
  meta(name: 'viewport', content: 'width=device-width, initial-scale=1')

}
body {
  bodyContents()
}

yieldUnescaped '</html>'
