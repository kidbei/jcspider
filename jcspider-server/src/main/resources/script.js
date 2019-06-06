
this.start = function (self, url) {
    self.crawl(url, {headers: {"Content-Type": "application/json"}, method:"index_page"});
}

this.index_page = function (self, response) {
    var urls = [];
    var newsItems = response.doc("#projectNewsList .news-item");
    for (var i = 0; i < newsItems.length; i ++) {
        var item = newsItems[i];
        var url = item.select(".header a").attr("href");
        urls.push(url);
    }
    return {urls: urls};
}
