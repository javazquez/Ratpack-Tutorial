//code from https://github.com/ratpack/example-books
yieldUnescaped '<!DOCTYPE html>'
html(lang:'en') {
    head {
        meta(charset:'utf-8')
        title(title ?: 'Conference App')
        meta('http-equiv': "Content-Type", content:"text/html; charset=utf-8")
        meta(name: 'viewport', content: 'width=device-width, initial-scale=1.0')
        script(src: '/jquery.min.js') {}
        script(src: 'https://maxcdn.bootstrapcdn.com/bootstrap/3.0.2/js/bootstrap.min.js') {}
        link(href: 'https://maxcdn.bootstrapcdn.com/bootstrap/3.0.2/css/bootstrap.min.css', rel: 'stylesheet')
        link(href: 'https://maxcdn.bootstrapcdn.com/bootstrap/3.0.2/css/bootstrap-theme.min.css', rel: 'stylesheet')
        link(href: '/application.css', rel: 'stylesheet')

        link(href: '/img/favicon.ico', rel: 'shortcut icon')
    }
    body {
        div(class:'container') {
            if (msg) {
                div(class: 'alert alert-info alert-dismissable') {
                    button(type: 'button', class: 'close', 'data-dismiss': 'alert', 'aria-hidden':'true', '&times;')
                    yield msg
                }
            }
            bodyContents()
        }
    }
}
