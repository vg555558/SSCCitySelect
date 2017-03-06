var exec = require('cordova/exec');

var SSCCitySelect = {
	start: function(parameters, success, error){
		exec(success, error, "SSCCitySelect","startSelectCity",[parameters]);
	}
};
module.exports = SSCCitySelect;

