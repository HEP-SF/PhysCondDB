var condJSServices = angular.module('condJSServices', [ 'ngResource' ]);

condJSServices.constant('baseurl', {
	'url' : 'http://localhost:8080/physconddb/conddbweb/'
}).constant('userurl', {
	'user' : 'rest/user/'
}).constant('experturl', {
	'expert' : 'rest/expert/'
}).constant('adminurl', {
	'admin' : 'rest/admin/'
}).factory('GlobalTag', [
 			function() {
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
		'GlobalTagGet',
		[
				'$resource',
				'baseurl',
				'userurl',
				function($resource, baseurl, userurl) {
					var url = baseurl.url + userurl.user
							+ 'globaltag/:globaltagname/:action';
					return $resource(url, {
						globaltagname : '@globaltagname'
					}, {
						query : {
							method : 'GET',
							params : {
								globaltagname : '%',
								action : 'like'
							},
							isArray : true
						},
						trace : {
							method : 'GET',
							params : {
								globaltagname : 'GTAG_02',
								action : 'trace'
							},
							isArray : true
						}
					});
				} ]).factory(
		'GlobalTagUpd',
		[
				'$resource',
				'baseurl',
				'experturl',
				function($resource, baseurl, experturl) {
					var url = baseurl.url + experturl.expert
							+ 'globaltag/:action';
					return $resource(url, {}, {
						update : {
							method : 'POST',
							params : {
								action : 'update'
							},
							isArray : false
						},
						add : {
							method : 'POST',
							params : {
								action : 'add'
							},
							isArray : false
						}
					});
				} ]).factory(
		'GlobalTagRem',
		[
				'$resource',
				'baseurl',
				'adminurl',
				function($resource, baseurl, adminurl) {
					var url = baseurl.url + adminurl.admin
							+ 'globaltag/:action';
					return $resource(url, {}, {
						clone : {
							method : 'POST',
							params : {
								action : 'clone'
							},
							isArray : false
						},
						remove : {
							method : 'DELETE',
							params : {
								action : 'delete'
							},
							isArray : false
						}
					});
				} ]).factory(
		'TagGet',
		[
				'$resource',
				'baseurl',
				'userurl',
				function($resource, baseurl, userurl) {
					var url = baseurl.url + userurl.user
							+ 'tag/:tagname/:action';
					console.log('Calling url ' + url);

					return $resource(url, {
						tagname : '@tagname'
					}, {
						query : {
							method : 'GET',
							params : {
								tagname : '%',
								action : 'like'
							},
							isArray : true
						},
						backtrace : {
							method : 'GET',
							params : {
								tagname : 'atag',
								action : 'backtrace'
							},
							isArray : true
						}
					});
				} ]).factory(
		'TagUpd',
		[ '$resource', 'baseurl', 'experturl',
				function($resource, baseurl, experturl) {
					var url = baseurl.url + experturl.expert + 'tag/:action';
					return $resource(url, {}, {
						update : {
							method : 'POST',
							params : {
								action : 'update'
							},
							isArray : false
						},
						add : {
							method : 'POST',
							params : {
								action : 'add'
							},
							isArray : false
						}
					});
				} ]).factory(
		'TagRem',
		[ '$resource', 'baseurl', 'adminurl',
				function($resource, baseurl, adminurl) {
					var url = baseurl.url + adminurl.admin + 'tag/:action';
					return $resource(url, {}, {
						remove : {
							method : 'DELETE',
							params : {
								action : 'delete'
							},
							isArray : false
						}
					});
				} ]).factory(
		'MapUpd',
		[ '$resource', 'baseurl', 'experturl',
				function($resource, baseurl, experturl) {
					var url = baseurl.url + experturl.expert + 'map/:action';
					return $resource(url, {}, {
						update : {
							method : 'POST',
							params : {
								action : 'update'
							},
							isArray : false
						},
						add : {
							method : 'POST',
							params : {
								action : 'add'
							},
							isArray : false
						},
						addtoglobaltag : {
							method : 'POST',
							params : {
								globaltagname : '@globaltagname',
								tagname : '@tagname',
								action : 'addtoglobaltag'
							},
							isArray : false
						}
					});
				} ]).factory(
		'MapRem',
		[ '$resource', 'baseurl', 'adminurl',
				function($resource, baseurl, adminurl) {
					var url = baseurl.url + adminurl.admin + 'map/:action';
					return $resource(url, {}, {
						remove : {
							method : 'DELETE',
							params : {
								globaltag : '@globaltag',
								tag : '@tag',
								action : 'delete'
							},
							isArray : false
						}
					});
				} ]);
