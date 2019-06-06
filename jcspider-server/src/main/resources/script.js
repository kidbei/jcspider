
this.start = function (self, url) {
    self.crawl(url, {headers: {"Content-Type": "application/json"}, method:"index_page"});
}

this.index_page = function (self, response) {
    var newsItems = response.doc("#projectNewsList .news-item");
    for (var i = 0; i < newsItems.length; i ++) {
        var item = newsItems[i];
        var url = item.select(".header a").attr("href");
        if (url) {
            self.crawl(url, {headers: {"Content-Type": "application/json"}, method:"detail_page", charset:"utf-8"});
        }
    }
}

this.detail_page = function (self, response) {
    var title = response.doc(".article-detail .header").text();
    var content = response.doc("#articleContent").text();
    return {title: title, content: content}
}
