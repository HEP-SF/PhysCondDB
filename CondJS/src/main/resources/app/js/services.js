var condJSServices = angular.module('condJSServices', [ 'ngResource' ]);

condJSServices.constant('baseurl', {
	'url' : 'http://localhost:8080/physconddb/conddbweb/'
}).constant('userurl', {
	'user' : 'rest/'
}).constant('experturl', {
	'expert' : 'rest/expert/'
}).constant('adminurl', {
	'admin' : 'rest/admin/'
}).factory('GlobalTag', [ function() {
	var data = {
		name : 'NEW_GTAG',
		description : 'a fake global tag',
		release : 'none',
		lockstatus : 'unlocked',
		validity : '-1',
		snapshotTime : '2014-01-01T02:00:00+02:00'
	};
	var defaultdata = angular.copy(data);

	return {
		getData : function() {
			console.log('GlobalTag: getData');
			return data;
		},
		getDefaultData : function() {
			console.log('GlobalTag: getDefaultData');
			return defaultdata;
		},
		setData : function(newData) {
			data = newData;
			if (data.insertionTime != undefined) {
				delete data.insertionTime;
			}
			if (data.isSelected != undefined) {
				delete data.isSelected;
			}
		}
	};
} ]).factory('Tag', function() {
	var data = {
		name : 'NEW_TAG',
		description : 'a fake tag',
		timeType : 'time',
		objectType : 'anobject',
		synchronization : 'none',
		lastValidatedTime : '-1',
		endOfValidity : '-1'
	};

	var defaultdata = angular.copy(data);

	return {
		getData : function() {
			console.log('Tag: getData');
			return data;
		},
		getDefaultData : function() {
			console.log('Tag: getDefaultData');
			return defaultdata;
		},
		setData : function(newData) {
			data = newData;
			if (data.insertionTime != undefined) {
				delete data.insertionTime;
			}
			if (data.isSelected != undefined) {
				delete data.isSelected;
			}
		}
	};
}).factory(
		'CondGlobalTag',
		[ '$resource', 'baseurl', 'userurl',
				function($resource, baseurl, userurl) {
					var url = baseurl.url + userurl.user + 'globaltags/:id';
					return $resource(url, {
						id : '@id'
					}, {
						query : {
							method : 'GET',
							params : {
								id : '%',
								expand : 'true'
							},
							isArray : false
						}
					});
				} ]);
