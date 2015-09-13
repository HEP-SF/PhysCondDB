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
						'GlobalTagGet',
						'GlobalTagRem',
						function($rootScope, $scope, GlobalTag, GlobalTagGet,
								GlobalTagRem) {
							$scope.globaltags = [];
							$scope.globaltags = GlobalTagGet.query();
							$scope.itemsByPage = 5;
							$scope.displayedPages = 10;
							$scope.displayedCollection = []
									.concat($scope.globaltags);

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
								$scope.globaltags = GlobalTagGet.query();
							};
							$scope.search = function() {
								$scope.globaltag = GlobalTag.getData();
								$scope.globaltags = GlobalTagGet.query({
									globaltagname : $scope.globaltag.name
								});
								$scope.displayedCollection = []
										.concat($scope.globaltags);
							};
							$scope.remove = function(gtag) {
								console.log('Removing global tag with name '
										+ gtag.name);
								result = GlobalTagRem.remove({
									'sourcegtag' : gtag.name
								});
							};
							$scope.edit = function(gtag) {
								console.log('Edit global tag with name '
										+ gtag.name);
								GlobalTag.setData(gtag);
								$rootScope.$broadcast('globaltagDataChanged',
										gtag);
							};

						} ])
		.controller(
				'TagBackTraceListCtrl',
				[
						'$scope',
						'$routeParams',
						'TagGet',
						function($scope, $routeParams, TagGet) {

							console.log('Route is ' + $routeParams.tagname);
							$scope.selectedtagname = $routeParams.tagname;
							$scope.seltag = [];
							$scope.selectedbacktracetags = [];
							$scope.itemsByPage = 5;
							$scope.displayedPages = 10;
							$scope.displayedCollection = [];

							$scope.seltag = TagGet
									.backtrace(
											{
												tagname : $routeParams.tagname
											},
											function(seltag) {
												var length = 0;
												if (seltag[0].globalTagMaps != undefined) {
													length = seltag[0].globalTagMaps.length;
												}
												console
														.log('Length of retrieved list '
																+ seltag.length);
												console.log('Retrieved tag '
														+ seltag[0].name);
												console
														.log('  - mapping global tags : '
																+ length);
												$scope.seltag = seltag;
												$scope.selectedbacktracetags = seltag[0].globalTagMaps;
												$scope.displayedCollection = []
														.concat($scope.selectedbacktracetags);
											});
						} ])
		.controller(
				'GlobalTagFormCtrl',
				[
						'$scope',
						'GlobalTag',
						'GlobalTagUpd',
						function($scope, GlobalTag, GlobalTagUpd) {
							$scope.edit = true;
							$scope.globaltagdata = {};
							$scope.globaltagdata = GlobalTag.getData();

							if ($scope.globaltag == undefined) {
								$scope.globaltag = $scope.globaltagdata;
							}

							$scope.$watch('globaltagdata', function(formdata) {
								console.log('formdata has changed: '
										+ formdata.name);
								$scope.globaltag = $scope.globaltagdata;
							}, true);

							$scope.$on('globaltagDataChanged', function() {
								console.log('data in model have changed: ');
								$scope.globaltagdata = GlobalTag.getData();
								$scope.edit = false;
							}, true);

							$scope.save = function(globaltag) {
								if ($scope.edit == true) {
									console.log(globaltag);
									GlobalTag.setData(globaltag);
									GlobalTagUpd.add(GlobalTag.getData());
								} else {
									console.log('sending update for: ')
									console.log(globaltag);
									GlobalTag.setData(globaltag);
									GlobalTagUpd.update(GlobalTag.getData());
								}
							};

							$scope.add = function(globaltag) {
								if (globaltag == 'new') {
									console.log('Addition mode');
									$scope.edit = true;
									$scope.reset();
								} else {
									console.log('Edition mode');
									$scope.edit = false;
								}
							};

							$scope.reset = function() {
								console.log('reset form data ')
								$scope.globaltagdata = GlobalTag
										.getDefaultData();
								$scope.globaltag = angular
										.copy($scope.globaltagdata);
							};
							$scope.clean = function() {
								console.log('clean form data ')
								$scope.globaltagdata = {};
								$scope.globaltag = angular
										.copy($scope.globaltagdata);
							};
							$scope.edit = function() {
								$scope.edit = false;
								$scope.globaltagdata = GlobalTag.getData();
								$scope.globaltag = angular
										.copy($scope.globaltagdata);
							};
							$scope.onTimeSet = function (newDate, oldDate) {
							    console.log(newDate);
							    console.log(oldDate);
							}
						} ])
		// Use this controller to perform global tag trace action and handle the
		// list of associated tags
		.controller(
				'TagTraceListCtrl',
				[
						'$scope',
						'$routeParams',
						'GlobalTagGet',
						function($scope, $routeParams, GlobalTagGet) {

							console.log('Route is ' + $routeParams.gtagname);
							$scope.selectedglobaltagname = $routeParams.gtagname;
							$scope.selglobaltag = [];
							$scope.selectedtracetags = [];
							$scope.itemsByPage = 5;
							$scope.displayedPages = 10;
							$scope.displayedTagsCollection = [];

							$scope.selglobaltag = GlobalTagGet
									.trace(
											{
												globaltagname : $routeParams.gtagname
											},
											function(selglobaltag) {
												var length = 0;
												if (selglobaltag[0].globalTagMaps != undefined) {
													length = selglobaltag[0].globalTagMaps.length;
												}
												console
														.log('Length of retrieved list '
																+ selglobaltag.length);
												console
														.log('Retrieved global tag '
																+ selglobaltag[0].name);
												console
														.log('  - mapping tags : '
																+ length);
												$scope.selglobaltag = selglobaltag;
												$scope.selectedtracetags = selglobaltag[0].globalTagMaps;
												if ($scope.selectedtracetags == undefined) {
													$scope.selectedtracetags = [];
												}
												$scope.displayedTagsCollection = []
														.concat($scope.selectedtracetags);
											});
						} ])
		// Use this controller to list tags
		.controller(
				'TagListCtrl',
				[
						'$rootScope',
						'$scope',
						'$routeParams',
						'Tag',
						'TagGet',
						'TagRem',
						function($rootScope, $scope, $routeParams, Tag,
								TagGet, TagRem) {
							$scope.tagname = '%';
							$scope.tags = [];
							$scope.itemsByPage = 5;
							$scope.displayedPages = 10;
							$scope.displayedTagsCollection = [];

							// fired when table rows are selected
							$scope.$watch('displayedTagsCollection', function(
									row) {
								// get selected row
								row.filter(function(r) {
									if (r.isSelected) {
										console.log(r);
										atag = angular.copy(r);
										Tag.setData(atag);
										$scope.tag = Tag.getData();
									}
								})
							}, true);

							$scope.refresh = function() {
								$scope.tags = TagGet.query({
									tagname : $scope.tagname
								});
								$scope.displayedTagsCollection = []
										.concat($scope.tags);
							};
							$scope.search = function() {
								$scope.tag = Tag.getData();
								$scope.tags = TagGet.query({
									tagname : $scope.tagname
								});
								$scope.displayedTagsCollection = []
										.concat($scope.tags);
							};
							$scope.remove = function(atag) {
								console.log('Removing tag with name '
										+ atag.name);
								result = TagRem.remove({
									'sourcetag' : atag.name
								});
							};
							$scope.edit = function(atag) {
								console.log('Edit tag with name '
										+ atag.name);
								Tag.setData(atag);
								$rootScope.$broadcast('tagDataChanged',
										atag);
							};
						} ])
		// Tag form controller
		.controller('TagFormCtrl',
				[ '$scope', 'Tag', 'TagUpd', function($scope, Tag, TagUpd) {
					$scope.edit = true;
					$scope.tagdata = {};
					$scope.tagdata = Tag.getData();

					if ($scope.tag == undefined) {
						$scope.tag = $scope.tagdata;
					}

					$scope.$watch('tagdata', function(formdata) {
						console.log('formdata has changed: ' + formdata.name);
						$scope.tag = $scope.tagdata;
					}, true);

					$scope.$on('tagDataChanged', function() {
						console.log('data in model have changed: ');
						$scope.tagdata = Tag.getData();
						$scope.edit = false;
					}, true);
					
					$scope.save = function(tag) {
						if ($scope.edit == true) {
							console.log('adding tag :')
							console.log(tag);
							Tag.setData(tag);
							TagUpd.add(Tag.getData());
						} else {
							console.log('sending update for: ')
							console.log(tag);
							Tag.setData(tag);
							TagUpd.update(Tag.getData());
						}
					};

					$scope.add = function(tag) {
						if (tag == 'new') {
							console.log('Addition mode');
							$scope.edit = true;
							$scope.reset();
						} else {
							console.log('Edition mode');
							$scope.edit = false;
						}
					};

					$scope.reset = function() {
						$scope.tagdata = Tag.getDefaultData();
						$scope.tag = angular.copy($scope.tagdata);
					};
					$scope.clean = function() {
						$scope.tagdata = {};
						$scope.tag = angular.copy($scope.tagdata);
					};
					$scope.edit = function() {
						$scope.edit = false;
						$scope.tagdata = Tag.getData();
						$scope.tag = angular.copy($scope.tagdata);
					};
				} ])
		.controller(
				'MapCtrl',
				[
						'$scope',
						'$routeParams',
						'GlobalTagGet',
						'TagGet',
						'MapUpd',
						'MapRem',
						function($scope, $routeParams, GlobalTagGet, TagGet,
								MapUpd, MapRem) {
							$scope.tags = [];
							$scope.itemsByPage = 5;
							$scope.displayedPages = 10;
							$scope.displayedTagsCollection = [];
							$scope.tagname = '%';
							$scope.noRow = 0;
							$scope.gtags = [];
							$scope.globaltagmappings = [];
							console.log('Using globaltagname : '
									+ $routeParams.globaltagname);
							$scope.globaltagname = $routeParams.globaltagname;
							$scope.globaltagmappings = [];
							var tagsfilled = false;
							$scope.assmap = [];

							$scope.initMappings = function() {
								var disptagcoll = [].concat($scope.tags);
								$scope.assmap = [];
								$scope.globaltagmappings = [];
								if ($scope.gtags[0] != undefined) {
									$scope.globaltagmappings = $scope.gtags[0].globalTagMaps;
									if ($scope.globaltagmappings == undefined) {
										$scope.globaltagmappings = [];
									}
								}

								console.log('Loop over globaltagmaps...'
										+ $scope.globaltagmappings.length);
								for (var i = 0; i < $scope.globaltagmappings.length; ++i) {
									console
											.log('i = '
													+ i
													+ ' '
													+ $scope.globaltagmappings[i].systemTag.name);
									console
											.log('Length of displayedTagsCollection is '
													+ disptagcoll.length);
									for (var j = 0; j < disptagcoll.length; ++j) {
										console.log(disptagcoll[j]);
										if (disptagcoll[j].name == $scope.globaltagmappings[i].systemTag.name) {
											disptagcoll[j].select = true;
											$scope.noRow = $scope.noRow + 1;
											$scope.assmap.push({
												name : disptagcoll[j].name
											});
											console
													.log('Adding '
															+ disptagcoll[j].name
															+ ' to array of mapped tags ');
											console.log(disptagcoll[j]);
										}
									}
								}
								return disptagcoll;
							};

							$scope.init = function() {
								$scope.noRow = 0;
								$scope.tags = [];
								$scope.displayedTagsCollection = [];
								$scope.assmap = [];
								$scope.globaltagmappings = [];
								$scope.gtags = GlobalTagGet.trace({
									globaltagname : $routeParams.globaltagname
								});
								console.log($scope.gtags);
							}

							$scope.init();

							// fired when table rows are selected
							$scope.$watch('displayedTagsCollection', function(
									row) {
								// get selected row
								row.filter(function(r) {
									if (r.isSelected) {
										console.log(r);
										$scope.seltag = angular.copy(r);
									}
								})
							}, true);

							// fired when table rows are selected
							$scope
									.$watch(
											'tags.length+gtags.length',
											function() {
												console
														.log('Length of tags has changed');
												if ($scope.tags.length > 0
														&& $scope.gtags.length > 0) {
													$scope.displayedTagsCollection = $scope
															.initMappings();
												}
											}, true);

							$scope.search = function() {
								console
										.log('Retrieve data using search parameter '
												+ $scope.tagname);
								$scope.tags = TagGet.query({
									tagname : $scope.tagname
								})
							};

							$scope.refresh = function() {
								$scope.init();
								$scope.tags = TagGet.query({
									tagname : $scope.tagname
								});
								// $scope.displayedTagsCollection = []
								// .concat($scope.tags);
							};

							$scope.mapit = function() {

								for (var i = 0; i < $scope.assmap.length; ++i) {
									var mapped = {};
									var addtag = true;
									console.log('Mapping tag : '
											+ $scope.assmap[i].name);
									mapped = $scope.globaltagmappings
											.filter(function(row) {
												if (row.systemTag.name == $scope.assmap[i].name) {
													return row;
												}
											})[0];
									if (mapped != undefined) {
										console.log('Found mapped : ');
										console.log(mapped);
										if (mapped.systemTag != undefined) {
											console
													.log('Tag is already mapped '
															+ mapped.systemTag.name);
											addtag = false;
										}
									}
									if (addtag) {
										MapUpd
												.addtoglobaltag({
													globaltagname : $scope.globaltagname,
													tagname : $scope.assmap[i].name
												});
									}
								}
								for (var i = 0; i < $scope.globaltagmappings.length; ++i) {
									var mapped = {};
									var rmtag = false;
									console
											.log('UnMapping tag ?  '
													+ $scope.globaltagmappings[i].systemTag.name);
									mapped = $scope.assmap
											.filter(function(row) {
												if (row.name == $scope.globaltagmappings[i].systemTag.name) {
													return row;
												}
											})[0];
									console.log('Found mapped : ');
									console.log(mapped);
									if (mapped == undefined) {
										rmtag = true;
									}
									if (rmtag) {
										console
												.log('Tag is requested to be un-mapped '
														+ $scope.globaltagmappings[i].systemTag.name);
										MapRem
												.remove({
													globaltag : $scope.globaltagname,
													tag : $scope.globaltagmappings[i].systemTag.name
												});
									}
								}
							};

							$scope.manageSelected = function(select, tagname,
									row) {
								console.log('manageSelected using args '
										+ select + ' ' + tagname);
								row.select = select;
								console.log(row);
								if (select) {
									$scope.noRow = $scope.noRow + 1;
									$scope.assmap.push({
										name : tagname
									});
									console.log('Adding ' + tagname
											+ ' to array ');
								} else {
									$scope.noRow = $scope.noRow - 1;
									var removedobj = $scope.assmap
											.filter(function(obj) {
												return obj.name === tagname;
											})[0];

									if (tagname === removedobj.name) {
										console.log('Key exists in array '
												+ tagname);
										for (var i = 0; i < $scope.assmap.length; ++i) {
											if ($scope.assmap[i].name == tagname) {
												$scope.assmap.splice(i, 1);
											}
										}
									} else {
										console
												.log('Key does not exists in array '
														+ tagname);
									}
								}
								console.log($scope.assmap);
							};
						} ]);
