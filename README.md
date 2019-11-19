# jcspider
## 功能介绍
> 基于java8实现的类似pyspider的爬虫系统，目标在于实现规则实时更新、异常报警、分布式调度、多种语法兼容(javascript/python/groovy/lua等等)的垂直爬虫系统。目前已经支持JavaScript语法，并有了前端管理界面。可以在线调试、开启/停止爬虫任务。该爬虫系统是为了自己做一些抓取供研究使用，严禁用于非法内容抓取。
  
## 使用截图
### 登录界面
 ![logo](https://github.com/kidbei/jcspider/blob/master/imgs/jcspider-logo.jpg "登录界面")

### 爬虫管理界面
 ![list](https://github.com/kidbei/jcspider/blob/master/imgs/jcspider-list.jpg "爬虫管理界面")
 
### 爬虫开发页面
 ![dev](https://github.com/kidbei/jcspider/blob/master/imgs/jcspider-detail.jpg "爬虫开发")

### 爬虫debug
 ![debug](https://github.com/kidbei/jcspider/blob/master/imgs/jcspider-debug.jpg "调试页面")
 
### 日志
 ![log](https://github.com/kidbei/jcspider/blob/master/imgs/jcspider-log.jpg "日志页面")
 

## 实现方案
> 使用nashorn来实现多语言支持，提供一些内置函数，尽量让爬虫开发者使用少量语法来实现规则配置。
   
## TODOLIST
* 分布式实现（通过定制dispatcher和process模块）
* 完善报警机制
* 支持多语言(python/groovy)

## 语法介绍
> todo









 
