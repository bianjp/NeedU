# NeedU服务器端

支撑客户端，同时提供基于Web的管理功能，管理应用中的数据。

## 技术实现

采用基于Node.js的Express框架，使用HTTP协议在客户端与服务器端之间通信。

支撑客户端部分用到以下技术：

- Node.js: 服务器端的Javascript平台
- Express: Node.js下简洁而灵活的网络应用框架
- async: Node.js异步解决方案之一
- MongoDB: NoSQL数据库的一种
- Grunt: 项目构建工具

基于Web的管理部分用到以下技术：

- Jade: 优雅的模板引擎
- jQuery: 最流行的javascript框架
- Bootstrap: Twitter出品的前端UI框架
- Less: CSS扩展，完全兼容CSS

## 安装

1. 安装Node.js, NPM  
   下载：http://nodejs.org/download  
   Linux下参考https://github.com/joyent/node/wiki/Installing-Node.js-via-package-manager  
   通常Node.js安装包已经包含了NPM。
   
2. 安装MongoDB  
   参考http://docs.mongodb.org/manual
   
3. 安装grunt-cli  
   npm install -g grunt-cli

## 运行

### 运行数据库

Windows下：  
参考http://docs.mongodb.org/manual/tutorial/install-mongodb-on-windows
```
"C:\Program Files\MongoDB 2.6 Standard\bin\mongod" --dbpath YourDbPath
```
__注意:__
- 安装路径可能有所不同，请修改为你的安装路径
- 数据文件的默认存储路径为\data\db，如果MongoDB所在分区存在\data\db（如C:\data\db）目录，可不指定dbpath
- YourDbPath中不能出现非英文字符，否则MongoDB无法启动
- 建议将上述命令保存为一个.bat文件，或按照官方文档添加service，方便以后执行

Linux下：
```
mongod --dbpath YourDbPath
```


### 运行服务器
```
cd server
npm install #仅首次运行需要
grunt
```

## 文件结构

- collections: Collection的定义，仅用来记录、查询数据结构
- config: 应用的配置信息，如数据库的地址、端口等
- lib: 供服务器端其它部分使用的函数
- node_modules: npm包
- package.json: 管理npm依赖
- public: 静态资源，包括css, js, images
- routes: 路由定义及处理
- views: jade模板
- app.js: 应用的入口
- Gruntfile.js: Grunt配置

## 参考资料

* Express: http://expressjs.com/4x/api.html
* async: https://github.com/caolan/async
* MongoDB: http://docs.mongodb.org/manual
* MongoDB Node.js Driver: http://mongodb.github.io/node-mongodb-native/
* Grunt: http://gruntjs.com, http://gruntjs.cn
* Jade: http://jade-lang.com
* HTML to Jade: http://html2jade.org
* jQuery: http://api.jquery.com
* Bootstrap: http://v2.bootcss.com
* Less: http://lesscss.org, http://www.lesscss.net
