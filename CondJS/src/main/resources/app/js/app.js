var condJSApp = angular.module('condJSApp', [ 'ngRoute', 'condJSControllers',
		'condJSServices','smart-table','ui.bootstrap.datetimepicker' ]);

condJSApp.config([ '$routeProvider', function($routeProvider) {
	$routeProvider.when('/globaltags', {
		templateUrl : 'partials/globaltag-list.html',
		controller : 'GlobalTagListCtrl'
	}).otherwise({
		redirectTo : '/404.html'
	});
} ]);