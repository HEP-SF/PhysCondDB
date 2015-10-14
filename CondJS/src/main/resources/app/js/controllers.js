var condJSControllers = angular.module('condJSControllers', []);

/**
 * The controller for schemas search.
 */
condJSControllers
		// Use this controller to retrieve global tag list
		.controller(
				'GlobalTagListCtrl',
				[
						'$rootScope',
						'$scope',
						'GlobalTag',
						'CondGlobalTag',
						function($rootScope, $scope, GlobalTag, CondGlobalTag
								) {
							$scope.globaltags = [];
							var promise = CondGlobalTag.query({id : '%', expand : 'true'});
							promise.$promise.then(function (data) {
								$scope.globaltags = data.items;
								$scope.displayedCollection = []
								.concat($scope.globaltags);
								console.log('Inside promise got data '
										+ data);
							});
							$scope.itemsByPage = 20;
							$scope.displayedPages = 10;

							console.log('Loaded global tags '
									+ $scope.globaltags);

						} ]);
		