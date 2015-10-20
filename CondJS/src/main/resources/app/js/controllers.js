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
							$scope.displayedCollection = [];
							loadglobaltags({id : '%', expand : 'true'});
							$scope.itemsByPage = 20;
							$scope.displayedPages = 10;
							
							function loadglobaltags(queryargs) {
								var promise = CondGlobalTag.query(queryargs);
								promise.$promise.then(function (data) {
									$scope.globaltags = data.items;
									$scope.displayedCollection = []
									.concat($scope.globaltags);
									console.log('Inside promise got data '
											+ data);
								});
							};
							
							// fired when table rows are selected
							$scope.$watch('displayedCollection', function(row) {
								// get selected row
								row.filter(function(r) {
									if (r.isSelected) {
										console.log(r);
										gtag = angular.copy(r);
										GlobalTag.setData(gtag);
										$scope.globaltag = GlobalTag.getData();
									}
								})
							}, true);

							$scope.refresh = function() {
								loadglobaltags({id : '%', expand : 'true'});
							}

							$scope.search = function() {
								loadglobaltags({id : $scope.globaltagname, expand : 'true'});
							};

						} ])
// Use this controller to perform global tag trace action and handle the
		// list of associated tags
		.controller(
				'TagTraceListCtrl',
				[
				 		'$rootScope',
						'$scope',
						'$routeParams',
						'CondGlobalTag',
						'GetHref',
						function($rootScope,$scope, $routeParams, CondGlobalTag,GetHref) {

							console.log('Route is ' + $routeParams.gtagname);
							$scope.selectedglobaltagname = $routeParams.gtagname;
							$scope.selglobaltag = [];
							$scope.selectedtracetags = [];
							$scope.itemsByPage = 5;
							$scope.displayedPages = 10;
							$scope.displayedTagsCollection = [];
							loadglobaltagtrace({id : $scope.selectedglobaltagname, trace : 'on'});
							
							$scope.$on('tagTraceDataChanged', function() {
								console.log('data in model have changed: '+$scope.selectedtracetags);
								$scope.displayedTagsCollection = [].concat($scope.selectedtracetags);
							}, true);
							
							function loadglobaltagtrace(queryargs) {
								//Activate the trace
								console.log('Loading global tag trace with args '+queryargs);
								var promise = CondGlobalTag.query(queryargs);
								promise.$promise.then(function (data) {
									$scope.selglobaltag = data;
									globaltagMaps = data.globalTagMaps.items;
									var taglist = [];
									for (i=0; i<globaltagMaps.length; i++) {
										// load the specific global tags object
										ataglink = globaltagMaps[i].systemTag.href;
										console.log('Get link '+ataglink);
										console.log('Updating selectedtracetags '+$scope.selectedtracetags);
										taglist.push(GetHref.link(ataglink).then(function (response) {
											console.log('Retrieved data '+response.data.name);
											return response.data;
										}));
										console.log('Filling tag list '+taglist.length);
									}
									console.log('Retrieved tag list '+taglist.length);
									$scope.selectedtracetags = taglist;
									$rootScope.$broadcast('tagTraceDataChanged',
											$scope.selectedtracetags);
									return taglist;
								});
							};
						} ])
;
		