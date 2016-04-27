//code from https://github.com/ratpack/example-books
layout 'layout.gtpl',
title: title,
msg: msg,
bodyContents: contents {
    if (username) {
        p(class: "navbar-text navbar-right") {
            span(class: "glyphicon glyphicon-user") {}
            yield 'Signed in as, ' strong(username)
        }
    }

    h1('Metrics Dashboard')

    div(id: "noData", class: "alert alert-info", style: "margin-top: 50px;", 'Waiting for data.....')

    def columns = [class: 'col-md-4']

    div(class: "row") {
        div(columns) {
            h2('Request Count')
            div(id: "requestCountChart") {}
        }
        div(columns) {
            h2('Heap Used (%)')
            div(id: "heapChart") {}
        }
        div(columns) {
            h2('Thread Count')
            div(id: "threadsChart") {}
        }
    }

    h2('Request Timers')
    div(id: "timerCharts") {}

    script(type: "text/javascript", src :  "http://www.google.com/jsapi", charset: "utf-8") {}
    script(type: "text/javascript") {
        yieldUnescaped '''
        google.load("visualization", "1", { packages: ["piechart", "corechart", "gauge"]});
        '''
    }
    script(type: 'text/javascript', src: "/metrics.js") {}
}
