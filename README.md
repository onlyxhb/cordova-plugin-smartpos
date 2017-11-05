# Cordova SmartPos Plugin

这是一个简单的安卓版pos机s1000 SDK调用的插件.

它包含了基本的调用功能，包括测试，打印走纸，安全键盘等等功能。

## Using

创建一个新项目（已有cordova项目不用创建）

    $ cordova create demo com.example.demo demo
    
安装插件

    $ cd demo
    $ cordova plugin add https://github.com/Xonlystar/cordova-plugin-smartpos.git
    

编辑 `www/js/index.js` 并添加下面的代码 `onDeviceReady`

```js
    hello.test("World", function(message) {
        alert(message);
    }, function() {
        alert("插件调用出错");
    });
```

安装安卓平台

    cordova platform add android
    
打包apk

    cordova build android

## 更多信息

有关设置Cordova的更多信息，请参阅 [文档](http://cordova.apache.org/docs/en/latest/guide/cli/index.html)

有关插件的更多信息，请参阅 [cordova插件开发指南](http://cordova.apache.org/docs/en/latest/guide/hybrid/plugins/index.html)
