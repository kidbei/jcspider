
this.start = function (self, url) {
    self.crawl(url, {headers: {"Content-Type": "application/json"}, method:"index_page"});
}

this.index_page = function (self, response) {
    var list_elements = response.doc().select("a[href^=/article/");
    for (var i = 0; i < list_elements.length; i ++) {
        var url = list_elements[i].attr("href");
        if (url) {
            url = "http://www.mofcom.gov.cn" + url;
            self.crawl(url, {headers: {"Content-Type": "application/json"}, method:"detail_page"});
        }
    }
    for (var i = 2; i <= 20; i ++) {
        url = "http://www.mofcom.gov.cn/article/b/c/?" + i;
        self.crawl(url, {headers: {"Content-Type": "application/json"}, method:"index_page"});
    }
}

this.detail_page = function (self, response) {
    var title = response.doc("#artitle").text();
    var content = response.doc("#zoom").text();
    return {title: title, content: content}
}
