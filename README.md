# Cordova SmartPos Plugin

这是一个简单的安卓版pos机s1000 SDK调用的插件.

它包含了基本的调用功能，包括测试，打印走纸，安全键盘等等功能。

## 使用

创建一个新项目（已有cordova项目不用创建）

    $ cordova create demo com.example.demo demo
    
### 安装插件

    $ cd demo
    $ cordova plugin add https://github.com/Xonlystar/cordova-plugin-smartpos.git
    

### 调用插件
- isbind方法----判断是否绑定服务

```js
    smartpos.isbind("", function(message) {
        alert(message);
    }, function() {
        alert("插件调用出错");
    });
```
- bind方法----绑定服务

```js
    smartpos.bind("", function(message) {
        alert(message);
    }, function() {
        alert("插件调用出错");
    });
```
- unbind方法----解绑服务

```js
    smartpos.unbind("", function(message) {
        alert(message);
    }, function() {
        alert("插件调用出错");
    });
```
- searchCard方法----寻卡

```js
    smartpos.searchCard("", function(message) {
        alert(message);
    }, function() {
        alert("插件调用出错");
    });
```
- print方法----打印

```js
    smartpos.print("", function(message) {
        alert(message);
    }, function() {
        alert("插件调用出错");
    });
```
- pinpad方法----密码键盘

```js
    smartpos.pinpad("", function(message) {
        alert(message);
    }, function() {
        alert("插件调用出错");
    });
```
- systemInterface方法----系统接口

```js
    smartpos.systemInterface("", function(message) {
        alert(message);
    }, function() {
        alert("插件调用出错");
    });
```
- emv方法----EMV

```js
    smartpos.emv("", function(message) {
        alert(message);
    }, function() {
        alert("插件调用出错");
    });
```
- pin方法----PIN

```js
    smartpos.pin("", function(message) {
        alert(message);
    }, function() {
        alert("插件调用出错");
    });
```
- scan方法----扫码接口

```js
    smartpos.scan("", function(message) {
        alert(message);
    }, function() {
        alert("插件调用出错");
    });
```
- stopScan方法----停止扫码

```js
    smartpos.stopScan("", function(message) {
        alert(message);
    }, function() {
        alert("插件调用出错");
    });
```
- ICApdu方法----IC卡APDU

```js
    smartpos.ICApdu("", function(message) {
        alert(message);
    }, function() {
        alert("插件调用出错");
    });
```
- RFApdu方法----RF卡APDU

```js
    smartpos.RFApdu("", function(message) {
        alert(message);
    }, function() {
        alert("插件调用出错");
    });
```
- PsamApdu方法----PSAM卡APDU

```js
    smartpos.PsamApdu("", function(message) {
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
