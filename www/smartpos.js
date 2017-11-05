/*global cordova, module*/

module.exports = {
    test: function (name, success, error) {
        cordova.exec(success, error, "SmartPos", "test", [name]);
    },
    print: function (name, success, error) {
        cordova.exec(success, error, "SmartPos", "print", [name]);
    }
};
