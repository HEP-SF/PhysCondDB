var condJSApp = angular.module('condJSApp', [ 'ngRoute', 'condJSControllers',
		'condJSServices','smart-table','ui.bootstrap.datetimepicker','highcharts-ng' ]);

condJSApp.config([ '$routeProvider', function($routeProvider) {
	$routeProvider.when('/globaltags', {
		templateUrl : 'partials/globaltag-list.html',
		controller : 'GlobalTagListCtrl'
	}).when('/globaltags/:gtagname/trace', {
		templateUrl : 'partials/tagtrace-list.html'
		//controller : 'TagTraceListCtrl'
	}).when('/tags', {
		templateUrl : 'partials/tag-list.html',
		controller : 'TagListCtrl'
	}).when('/tags/:tagname/trace', {
		templateUrl : 'partials/tagbacktrace-list.html',
		controller : 'TagBackTraceListCtrl'
	}).when('/globaltags/:globaltagname/maps', {
		templateUrl : 'partials/mappings.html',
		controller : 'MapCtrl'
	}).when('/home', {
		templateUrl : 'partials/home.html'
	}).when('/monitor', {
		templateUrl : 'partials/monitor.html'
//		controller : 'MonitorCtrl'
	}).otherwise({
		redirectTo : '/404.html'
	});
} ]);

