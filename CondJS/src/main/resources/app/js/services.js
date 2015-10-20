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
})
.factory(
		'GetHref',
		[ '$http', '$q', 
			function( $http, $q ) {
                // Return public API.
                return({
                    link: link
                });
                
                function link(urlget) {
                	console.log('Call http method with '+urlget);
					return $http({
						  method: 'GET',
						  url: urlget
						});
                };
                
				} ])
.factory(
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
				} ])				
;

// I transform the error response, unwrapping the application dta from
// the API response payload.
function handleError( response ) {
    // The API response from the server should be returned in a
    // nomralized format. However, if the request was not handled by the
    // server (or what not handles properly - ex. server error), then we
    // may have to normalize it on our end, as best we can.
    if (
        ! angular.isObject( response.data ) ||
        ! response.data.message
        ) {
        return( $q.reject( "An unknown error occurred." ) );
    }
    // Otherwise, use expected error message.
    return( $q.reject( response.data.message ) );
}
// I transform the successful response, unwrapping the application data
// from the API response payload.
function handleSuccess( response ) {
	console.log('Success in retrieval...'+response.data);
    return( response.data );
}
