var exec = require("cordova/exec");
module.exports = {
    isbind: function(arg0, success, error) {
        exec(success, error, 'SmartPos', "isbind", [arg0]);
    },
    bind: function(arg0, success, error) {
        exec(success, error, 'SmartPos', "bind", [arg0]);
    },
    unbind: function(arg0, success, error) {
        cordova.exec(success, error, "SmartPos", "unbind", [arg0]);
    },
    searchCard: function(arg0, success, error) {
        exec(success, error, 'SmartPos', "searchCard", [arg0]);
    },
    print: function(arg0, success, error) {
        exec(success, error, 'SmartPos', "print", [arg0]);
    },
    pinpad: function(arg0, success, error) {
        exec(success, error, 'SmartPos', "pinpad", [arg0]);
    },
    systemInterface: function(arg0, success, error) {
        exec(success, error, 'SmartPos', "systemInterface", [arg0]);
    },
    emv: function(arg0, success, error) {
        exec(success, error, 'SmartPos', "emv", [arg0]);
    },
    pin: function(arg0, success, error) {
        exec(success, error, 'SmartPos', "pin", [arg0]);
    },
    scan: function(arg0, success, error) {
        exec(success, error, 'SmartPos', "scan", [arg0]);
    },
    stopScan: function(arg0, success, error) {
        exec(success, error, 'SmartPos', "stopScan", [arg0]);
    },
    ICApdu: function(arg0, success, error) {
        exec(success, error, 'SmartPos', "ICApdu", [arg0]);
    },
    RFApdu: function(arg0, success, error) {
        exec(success, error, 'SmartPos', "RFApdu", [arg0]);
    },
    PsamApdu: function(arg0, success, error) {
        exec(success, error, 'SmartPos', "PsamApdu", [arg0]);
    }
}