var condJSServices = angular.module('condJSServices', [ 'ngResource' ]);

condJSServices.constant('baseurl', {
	'url' : 'http://localhost:8080/physconddb/api/'
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
		lockstatus : 'UNLOCKED',
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
//			if (data.insertionTime != undefined) {
//				delete data.insertionTime;
//			}
			if (data.isSelected != undefined) {
				delete data.isSelected;
			}
		}
	};
} ]).factory('Tag', [function() {
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
}]).factory('System', [ function() {
	var data = {
			nodeFullpath : 'NODE_FULLPATH',
			nodeDescription : 'a fake node',
			tagNameRoot : 'NodeFullPath',
			groupSize : '1000',
			schemaName : 'A schema name'
	};
	var defaultdata = angular.copy(data);

	return {
			getData : function() {
				console.log('System: getData');
				return data;
			},
			getDefaultData : function() {
				console.log('System: getDefaultData');
				return defaultdata;
			},
			setData : function(newData) {
				data = newData;
//				if (data.insertionTime != undefined) {
//					delete data.insertionTime;
//				}
				if (data.isSelected != undefined) {
					delete data.isSelected;
				}
			}
	};
}]).factory(
		'CondRest',
		[
				'$http',
				'$q',
				'baseurl',
				'userurl',
				'experturl',
				function($http, $q, baseurl, userurl, experturl) {
					// Return public API.
					return ({
						request : request
					});

					function request(requrl, httpmethod, mode, reqheader,
							reqdata) {
						var req = {
							method : 'GET',
							url : baseurl.url + requrl,
						}
						if (mode === 'expert') {
							req.url = baseurl.url + experturl.expert + requrl;
						} else if (mode === 'admin') {
							req.url = baseurl.url + experturl.expert + requrl;
						} else if (mode === 'user') {
							req.url = baseurl.url + userurl.user + requrl;
						} else if (mode === 'full') {
							req.url = requrl;
						}
						if (httpmethod != null) {
							req.method = httpmethod;
						}
						if (reqheader != null) {
							req.headers = reqheader;
						}
						if (reqdata != null) {
							req.data = reqdata;
						}
						console.log('Call http method' + httpmethod + ' with '
								+ req.url);
						return $http(req);
					};

} ]).factory('GetHref', [ '$http', '$q', function($http, $q) {
	// Return public API.
	return ({
		link : link
	});

	function link(urlget) {
		console.log('Call http method with ' + urlget);
		return $http({
			method : 'GET',
			url : urlget
		});
	}
	;

} ]).factory(
		'CondGlobalTag',
		[ '$resource', 'baseurl', 'userurl',
				function($resource, baseurl, userurl) {
					var url = baseurl.url + userurl.user + 'globaltags/:id';
					console.log('Call CondGlobalTag using url ' + url);
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
				} ]).factory(
		'CondTag',
		[ '$resource', 'baseurl', 'userurl',
				function($resource, baseurl, userurl) {
					var url = baseurl.url + userurl.user + 'tags/:id';
					console.log('Call CondTag using url ' + url);
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
				} ])
.factory(
		'CondSystem',
		[ '$resource', 'baseurl', 'userurl',
				function($resource, baseurl, userurl) {
					var url = baseurl.url + userurl.user + 'systems/:id';
					console.log('Call CondSystem using url ' + url);
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
				} ])
.factory('GetChartOptions', [function() {
	var data = {
			options : {
				chart : {
					type: 'spline',
					zoomType : 'x'
				},
				// Enable for both axes
				tooltip : {
					crosshairs : [ true, true ],
					pointFormat : '[time] <b>{point.y:.2f} ms </b><br>{point.url}'
				},
				plotOptions: {
					series: {
					   	turboThreshold: 0
					}
			    }
			},
			xAxis : {
				type : 'datetime'
			},
			yAxis : {
				title : {
					text : 'Count'
				},
				plotLines : [ {
					value : 0,
					width : 1,
					color : '#808080'
				} ]
			},
			legend : {
				enabled : false
			},
			series : undefined,
			title : {
				text : 'Test HighChart'
			},
			credits : {
				enabled : true
			},
			loading : false,
			size : {}					
	};

	var defaultdata = angular.copy(data);

	return {
		getChart : function() {
			console.log('GetChartOptions: getChart');
			return data;
		}
	};
}])
;

// I transform the error response, unwrapping the application dta from
// the API response payload.
function handleError(response) {
	// The API response from the server should be returned in a
	// nomralized format. However, if the request was not handled by the
	// server (or what not handles properly - ex. server error), then we
	// may have to normalize it on our end, as best we can.
	if (!angular.isObject(response.data) || !response.data.message) {
		return ($q.reject("An unknown error occurred."));
	}
	// Otherwise, use expected error message.
	return ($q.reject(response.data.message));
}
// I transform the successful response, unwrapping the application data
// from the API response payload.
function handleSuccess(response) {
	console.log('Success in retrieval...' + response.data);
	return (response.data);
}
