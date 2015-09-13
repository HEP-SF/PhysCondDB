var condJSApp = angular.module('condJSApp', [ 'ngRoute', 'condJSControllers',
		'condJSServices','smart-table','ui.bootstrap.datetimepicker' ]);

condJSApp.config([ '$routeProvider', function($routeProvider) {
	$routeProvider.when('/globaltags', {
		templateUrl : 'partials/globaltag-list.html',
		controller : 'GlobalTagListCtrl'
	}).when('/tracetags/:gtagname', {
		templateUrl : 'partials/tagtrace-list.html',
		controller : 'TagTraceListCtrl'
	}).when('/backtracetags/:tagname', {
		templateUrl : 'partials/tagbacktrace-list.html',
		controller : 'TagBackTraceListCtrl'
	}).when('/tags', {
		templateUrl : 'partials/tag-list.html',
		controller : 'TagListCtrl'
	}).when('/maps/:globaltagname', {
		templateUrl : 'partials/mappings.html',
		controller : 'MapCtrl'
	}).otherwise({
		redirectTo : '/404.html'
	});
} ]);