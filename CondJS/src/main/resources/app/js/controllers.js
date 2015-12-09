var condJSControllers = angular.module('condJSControllers', []);

/**
 * The controller for schemas search.
 */
condJSControllers
		// This controller is used for global tag forms: creation or update of a
		// global tag
		.controller(
				'GlobalTagFormCtrl',
				[
						'$scope',
						'GlobalTag',
						'CondRest',
						function($scope, GlobalTag, CondRest) {
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
									var delurl = 'globaltags';
									var httpmethod = 'POST';
									CondRest
											.request(delurl, httpmethod,
													'expert', null,
													GlobalTag.getData())
											.then(
													function(response) {
														console
																.log('Received response '
																		+ response);
													},
													function(error) {
														console
																.log('Error occurred '
																		+ error);
													});
									// GlobalTagUpd.add(GlobalTag.getData());
								} else {
									console.log('sending update for: ')
									console.log(globaltag);
									GlobalTag.setData(globaltag);
									var delurl = 'globaltags/' + globaltag.name;
									var httpmethod = 'POST';
									CondRest
											.request(delurl, httpmethod,
													'expert', null,
													GlobalTag.getData())
											.then(
													function(response) {
														console
																.log('Received response '
																		+ response);
													},
													function(error) {
														console
																.log('Error occurred '
																		+ error);
													});
									// GlobalTagUpd.update(GlobalTag.getData());
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
							$scope.onTimeSet = function(newDate, oldDate) {
								console.log(newDate);
								console.log(oldDate);
							}
						} ])
		// Use this controller to retrieve global tag list
		.controller(
				'GlobalTagListCtrl',
				[
						'$rootScope',
						'$scope',
						'$filter',
						'GlobalTag',
						'CondGlobalTag',
						'CondRest',
						function($rootScope, $scope, $filter, GlobalTag,
								CondGlobalTag, CondRest) {
							$scope.globaltags = [];
							$scope.displayedCollection = [];
							loadglobaltags({
								id : '%',
								expand : 'true'
							});
							$scope.itemsByPage = 10;
							$scope.displayedPages = 10;

							function loadglobaltags(queryargs) {
								var promise = CondGlobalTag.query(queryargs);
								promise.$promise.then(function(data) {
									$scope.globaltags = data.items;
									$scope.displayedCollection = []
											.concat($scope.globaltags);
									console.log('Inside promise got data '
											+ data);
								});
							}
							;

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
								$scope.globaltagname = '%';
								loadglobaltags({
									id : $scope.globaltagname,
									expand : 'true'
								});
							}

							$scope.search = function() {
								var gtagsearch = '%' + $scope.globaltagname
										+ '%';
								loadglobaltags({
									id : gtagsearch,
									expand : 'true'
								});
							};
							$scope.remove = function(gtag) {
								var delurl = 'globaltags/' + gtag.name;
								var httpmethod = 'DELETE';
								CondRest
										.request(delurl, httpmethod, 'expert')
										.then(
												// success
												function(response) {
													console
															.log('Received response '
																	+ response);
													$scope.refresh();
												},
												// error
												function(error) {
													console
															.log('Error occurred in url call...');
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
		// Use this controller to perform global tag trace action and handle the
		// list of associated tags
		.controller(
				'TagTraceListCtrl',
				[
						'$rootScope',
						'$scope',
						'$routeParams',
						'$location',
						'CondGlobalTag',
						'GetHref',
						function($rootScope, $scope, $routeParams, $location,
								CondGlobalTag, GetHref) {

							console.log('Route is ' + $routeParams.gtagname);
							$scope.selectedglobaltagname = $routeParams.gtagname;
							$scope.selglobaltag = [];
							$scope.selectedtracetags = [];
							$scope.itemsByPage = 10;
							$scope.displayedPages = 10;
							$scope.displayedTagsCollection = [];
							// Load system tags
							loadglobaltagtrace({
								id : $scope.selectedglobaltagname,
								trace : 'on',
								expand : 'true'
							});

							$scope
									.$on(
											'tagTraceDataChanged',
											function() {
												console
														.log('data in model have changed: '
																+ JSON
																		.stringify($scope.selectedtracetags));
												// $scope.displayedTagsCollection
												// =
												// [].concat($scope.selectedtracetags);
											}, true);

							$scope.goToMaps = function(hash) {
								var url = '/globaltags/' + hash + '/maps';
								console.log('go to ' + url);
								$location.path(url);
							}

							function loadglobaltagtrace(queryargs) {
								// Activate the trace
								console
										.log('Loading global tag trace with args '
												+ JSON.stringify(queryargs));
								var promise = CondGlobalTag.query(queryargs);
								promise.$promise.then(function(data) {
									$scope.selglobaltag = data;
									globaltagMaps = data.globalTagMaps.items;
									var taglist = [];
									for (i = 0; i < globaltagMaps.length; i++) {
										atag = globaltagMaps[i].systemTag;
										console.log('Get system tag '
												+ JSON.stringify(atag));
										taglist.push(atag);
										// load the specific global tags object
									}
									console.log('Retrieved tag list '
											+ JSON.stringify(taglist));
									$scope.selectedtracetags = taglist;
									$scope.displayedTagsCollection = []
											.concat($scope.selectedtracetags);

									$rootScope.$broadcast(
											'tagTraceDataChanged',
											$scope.selectedtracetags);
									return taglist;
								});
							}
							;
						} ])
		// Use this controller to perform tag search action and handle the
		// list of associated global tags
		.controller(
				'TagListCtrl',
				[
						'$rootScope',
						'$scope',
						'$routeParams',
						'Tag',
						'CondTag',
						'GetHref',
						function($rootScope, $scope, $routeParams, Tag,
								CondTag, GetHref) {

							$scope.tags = [];
							$scope.displayedTagsCollection = [];
							loadtags({
								id : '%',
								expand : 'true'
							});
							$scope.itemsByPage = 10;
							$scope.displayedPages = 10;

							function loadtags(queryargs) {
								var promise = CondTag.query(queryargs);
								promise.$promise.then(function(data) {
									$scope.tags = data.items;
									$scope.displayedTagsCollection = []
											.concat($scope.tags);
									console.log('Inside promise got data '
											+ data);
								});
							}
							;

							// fired when table rows are selected
							$scope.$watch('displayedTagsCollection', function(
									row) {
								// get selected row
								row.filter(function(r) {
									if (r.isSelected) {
										console.log(r);
										tag = angular.copy(r);
										Tag.setData(tag);
										$scope.tag = Tag.getData();
										console.log('Selected tag is '
												+ JSON.stringify($scope.tag));
									}
								})
							}, true);

							$scope.refresh = function() {
								loadtags({
									id : '%',
									expand : 'true'
								});
							}

							$scope.search = function() {
								loadtags({
									id : '%' + $scope.tagname + '%',
									expand : 'true'
								});
							};

							$scope.remove = function(atag) {
								var delurl = 'tags/' + atag.name;
								var httpmethod = 'DELETE';
								CondRest
										.request(delurl, httpmethod, 'expert')
										.then(
												// success
												function(response) {
													console
															.log('Received response '
																	+ response);
													$scope.refresh();
												},
												// error
												function(error) {
													console
															.log('Error occurred in url call...');
												});

							};

							$scope.edit = function(atag) {
								console.log('Edit tag with name ' + atag.name);
								Tag.setData(atag);
								$rootScope.$broadcast('tagDataChanged', atag);
							};

						} ])
		// Tag backtrace controller...
		.controller(
				'TagBackTraceListCtrl',
				[
						'$rootScope',
						'$scope',
						'$routeParams',
						'CondTag',
						'GetHref',
						function($rootScope, $scope, $routeParams, CondTag,
								GetHref) {

							console.log('Route is ' + $routeParams.tagname);
							$scope.selectedtagname = $routeParams.tagname;
							$scope.seltag = [];
							$scope.selectedbacktracetags = [];
							$scope.itemsByPage = 5;
							$scope.displayedPages = 10;
							$scope.displayedCollection = [];

							loadbacktracetags({
								id : $scope.selectedtagname,
								trace : 'on',
								expand : 'true'
							});

							function loadbacktracetags(queryargs) {
								var promise = CondTag.query(queryargs);
								promise.$promise
										.then(function(data) {
											$scope.selglobaltag = data;
											globaltagMaps = data.globalTagMaps.items;
											var gtaglist = [];
											for (i = 0; i < globaltagMaps.length; i++) {
												gtag = globaltagMaps[i].globalTag;
												console.log('Get system tag '
														+ JSON.stringify(gtag));
												gtaglist.push(gtag);
											}
											console.log('Retrieved gtag list '
													+ JSON.stringify(gtaglist));
											$scope.selectedbacktracetags = gtaglist;
											$scope.displayedCollection = []
													.concat($scope.selectedbacktracetags);

											$rootScope
													.$broadcast(
															'tagBackTraceDataChanged',
															$scope.selectedbacktracetags);
											return gtaglist;
										});
							}
							;

						} ])
		// Tag form controller
		.controller(
				'TagFormCtrl',
				[
						'$scope',
						'Tag',
						'CondRest',
						function($scope, Tag, CondRest) {
							$scope.edit = true;
							$scope.tagdata = {};
							$scope.tagdata = Tag.getData();

							if ($scope.tag == undefined) {
								$scope.tag = $scope.tagdata;
							}

							$scope.$watch('tagdata', function(formdata) {
								console.log('formdata has changed: '
										+ formdata.name);
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
									var delurl = 'tags';
									var httpmethod = 'POST';
									CondRest
											.request(delurl, httpmethod,
													'expert', null,
													Tag.getData())
											.then(
													function(response) {
														console
																.log('Received response '
																		+ response);
													},
													function(error) {
														console
																.log('Error occurred '
																		+ error);
													});
								} else {
									console.log('sending update for: ')
									console.log(tag);
									Tag.setData(tag);
									var delurl = 'tags/' + tag.name;
									var httpmethod = 'POST';
									CondRest
											.request(delurl, httpmethod,
													'expert', null,
													Tag.getData())
											.then(
													function(response) {
														console
																.log('Received response '
																		+ response);
													},
													function(error) {
														console
																.log('Error occurred '
																		+ error);
													});
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
						'CondGlobalTag',
						'CondTag',
						function($scope, $routeParams, CondGlobalTag, CondTag) {
							$scope.tags = [];
							$scope.itemsByPage = 5;
							$scope.displayedPages = 10;
							$scope.displayedTagsCollection = [];
							$scope.tagname = '%';
							$scope.noRow = 0;
							$scope.selglobaltag = {};
							console.log('Using globaltagname : '
									+ $routeParams.globaltagname);
							$scope.globaltagname = $routeParams.globaltagname;
							$scope.globaltagmappings = [];
							var tagsfilled = false;
							$scope.assmap = [];

							// Load system tags associated to a given global tag
							loadglobaltagtrace({
								id : $scope.globaltagname,
								trace : 'on',
								expand : 'true'
							});
							// Load all system tags matching pattern tagname
							loadtags({
								id : '%' + $scope.tagname + '%',
								expand : 'true'
							});

							$scope
									.$on(
											'tagTraceDataChanged',
											function() {
												console
														.log('data in model have changed: '
																+ JSON
																		.stringify($scope.selectedtracetags));
												// $scope.displayedTagsCollection
												// =
												// [].concat($scope.selectedtracetags);
											}, true);

							$scope.initMappings = function() {
								var disptagcoll = [];
								$scope.assmap = [];

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
													+ $scope.tags.length);
									var selectedtags = [];
									var nonseltags = [];
									var isel = 0;
									var inosel = 0;
									for (var j = 0; j < $scope.tags.length; ++j) {
										console.log($scope.tags[j]);
										var atag = {};
										atag.name = $scope.tags[j];
										if ($scope.tags[j].name == $scope.globaltagmappings[i].systemTag.name) {
											atag.select = true;
											selectedtags.push(atag);
											$scope.noRow = $scope.noRow + 1;
											$scope.assmap.push({
												name : $scope.tags[j].name
											});
											console
													.log('Adding '
															+ $scope.tags[j].name
															+ ' to array of mapped tags ');
											console.log(disptagcoll[j]);
										} else {
											atag.select = false;
											nonseltags.push(atag);
										}
									}
									disptagcoll = [].concat(selectedtags);
									disptagcoll = disptagcoll
											.concat(nonseltags);
								}
								return disptagcoll;
							};
							//
							function loadtags(queryargs) {
								var promise = CondTag.query(queryargs);
								promise.$promise.then(function(data) {
									$scope.tags = data.items;
									$scope.displayedTagsCollection = []
											.concat($scope.tags);
									console.log('Inside promise got data '
											+ data);
								});
							}
							;

							function loadglobaltagtrace(queryargs) {
								// Activate the trace
								console
										.log('Loading global tag trace with args '
												+ JSON.stringify(queryargs));
								var promise = CondGlobalTag.query(queryargs);
								promise.$promise
										.then(function(data) {
											$scope.selglobaltag = data;
											$scope.globaltagmappings = data.globalTagMaps.items;
											// var taglist = [];
											// for (i=0; i<$scope.gtags.length;
											// i++) {
											// atag =
											// $scope.globaltagmappings[i].systemTag;
											// console.log('Get system tag
											// '+JSON.stringify(atag));
											// taglist.push(atag);
											// // load the specific global tags
											// object
											// }
											// console.log('Retrieved tag list
											// '+JSON.stringify(taglist));
											// $scope.selectedtracetags =
											// taglist;
											// $scope.displayedTagsCollection =
											// [].concat($scope.selectedtracetags);

											// $rootScope.$broadcast('tagTraceDataChanged',
											// $scope.selectedtracetags);
											// return taglist;
										});
							}
							;

							$scope.init = function() {
								$scope.noRow = 0;
								$scope.tags = [];
								$scope.displayedTagsCollection = [];
								$scope.assmap = [];
								$scope.globaltagmappings = [];
								loadglobaltagtrace({
									id : $routeParams.globaltagname,
									trace : 'on',
									expand : 'true'
								});
								// $scope.gtags = GlobalTagGet.trace({
								// globaltagname : $routeParams.globaltagname
								// });
								// console.log($scope.gtags);
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
											'tags.length+globaltagmappings.length',
											function() {
												console
														.log('Length of tags has changed');
												if ($scope.tags.length > 0
														&& $scope.globaltagmappings.length > 0) {
													$scope.displayedTagsCollection = $scope
															.initMappings();
												}
											}, true);

							$scope.search = function() {
								console
										.log('Retrieve data using search parameter '
												+ $scope.tagname);
								loadtags({
									id : '%' + $scope.tagname + '%',
									expand : 'true'
								});
								// $scope.tags = TagGet.query({
								// tagname : $scope.tagname
								// })
							};

							$scope.refresh = function() {
								$scope.init();
								loadtags({
									id : '%' + $scope.tagname + '%',
									expand : 'true'
								});
								// $scope.tags = TagGet.query({
								// tagname : $scope.tagname
								// });
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
										console.log('Add tag to mapping: '
												+ $scope.assmap[i].name
												+ ' will be associated to '
												+ $scope.globaltagname);
										// MapUpd
										// .addtoglobaltag({
										// globaltagname : $scope.globaltagname,
										// tagname : $scope.assmap[i].name
										// });
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
										// MapRem
										// .remove({
										// globaltag : $scope.globaltagname,
										// tag :
										// $scope.globaltagmappings[i].systemTag.name
										// });
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
						} ])
		// Use this controller to monitor requests
		.controller(
				'MonitorCtrl',
				[
						'$rootScope',
						'$scope',
						'$q',
						'CondRest',
						'GetChartOptions',
						function($rootScope, $scope, $q, CondRest,GetChartOptions) {
							// $scope.logrequests = [];
							

							function lookup(name, series) {
								for (var i = 0, len = series.length; i < len; i++) {
									if (series[i].name === name)
										return {
											got : true,
											el : series[i]
										};
								};
								return {
									got : false,
									el : undefined
								};
							};

							$scope.chartGetConfig = getConfig('GET');
							$scope.chartPostConfig = getConfig('POST');

							function getConfig(meth) {
								var chartseries = [];
								var qry = {
									id : meth,
									serie : chartseries
								};
								console.log("Calling loadrequests...." + qry);
								// return {};
								var charopt = angular.copy(GetChartOptions.getChart());
//								var promise = loadrequests(qry);
								var promise = asyncload(qry);
								promise.then(function(response) {
									console.log('getConfig Inside promise got successful answer ');
									charopt.series = response;
								}, function(error) {
									console.log("getConfig received error "+error);
								});
								return charopt;
							};
							
							function asyncload(queryargs) {
								var geturl = 'monitor/log/' + queryargs.id;
								var httpmethod = 'GET';
								var chartseries = queryargs.serie;
								
								return $q(function(resolve, reject) {
									CondRest.request(geturl, httpmethod, 'user', null, null).then(
										function(response) {
											console.log("Called query and got result "+response.data.items.length);
											var logrequests = [];
											console.log('Received response '+ response);
											logrequests = response.data.items;
											var highserie = {};
											highserie.name = '';
											highserie.data = [];
											for (var i = 0; i < logrequests.length; i++) {
												var el = logrequests[i];
												var xy = []
												var time = new Date(el.start);
												var requrl = el.httpMethod+ '-'+ el.requestUrl.split("/")[0];
												if (el.requestUrl.split("/")[0].indexOf("expert") > -1) {
													requrl = el.httpMethod+ '-'+ el.requestUrl.split("/")[1];
												}
												requrl = requrl.split("?")[0];
														
												var point = {
													x : time.getTime(),
													y : el.lengthMilli,
													url : el.requestUrl
												};
												if (highserie.name === '') {
													highserie.name = requrl;
												} else if (requrl != highserie.name) {
													highserie = {};
													var isthere = lookup(requrl,chartseries);
													if (isthere.got) {
														highserie = isthere.el;
														console.log('Retrieved serie from chartseries '+ highserie.name);
													} else {
														chartseries.push(highserie);
														highserie.name = requrl;
														highserie.data = [];
													}
												}
														// highserie.data.push(xy);
												console.log('Add point '+ JSON.stringify(point));
												highserie.data.push(point);
											}
											console.log('Add last serie to chartseries');
											chartseries.push(highserie);
											resolve(chartseries);
										},
										function(error) {
											console.log("Called query but got error");
											reject('Buggy query');
										}
									);
//								    setTimeout(function() {
//								      if (queryargs.id == "GET") {
//								        resolve('Hello, ' + queryargs.id + '!');
//								      } else {
//								        reject('Greeting ' + queryargs.id + ' is not allowed.');
//								      }
//								    }, 1000);
								});								
							}

							function loadrequests(queryargs) {
								var geturl = 'monitor/log/' + queryargs.id;
								var httpmethod = 'GET';
								var chartseries = queryargs.serie;
								return $q(function(){
									CondRest.request(geturl, httpmethod, 'user', null, null).then(
									function(response) {
										var logrequests = [];
										console.log('Received response '+ response);
										logrequests = response.data.items;
										var highserie = {};
										highserie.name = '';
										highserie.data = [];
										for (var i = 0; i < logrequests.length; i++) {
											var el = logrequests[i];
											var xy = []
											var time = new Date(el.start);
											var requrl = el.httpMethod+ '-'+ el.requestUrl.split("/")[0];
											requrl = requrl.split("?")[0];
													
											var point = {
												x : time.getTime(),
												y : el.lengthMilli,
												url : el.requestUrl
											};
											if (highserie.name === '') {
												highserie.name = requrl;
											} else if (requrl != highserie.name) {
												highserie = {};
												var isthere = lookup(requrl,chartseries);
												if (isthere.got) {
													highserie = isthere.el;
													console.log('Retrieved serie from chartseries '+ highserie.name);
												} else {
													chartseries.push(highserie);
													highserie.name = requrl;
													highserie.data = [];
												}
											}
													// highserie.data.push(xy);
//											console.log('Add point '+ JSON.stringify(point));
											highserie.data.push(point);
										}
										console.log('Add last serie to chartseries');
										chartseries.push(highserie);
									},
									function(error) {
										console.log('Error occurred '+ error);
									}
								);
								console.log("Return serie "+chartseries.length);
								return {
									serie : chartseries
								};
								});								
							};
} ])
;

condJSControllers.filter('myStrictFilter', function($filter) {
	return function(input, predicate) {
		return $filter('filter')(input, predicate, true);
	}
});

condJSControllers.filter('unique', function() {
	return function(arr, field) {
		var o = {}, i, l = arr.length, r = [];
		for (i = 0; i < l; i += 1) {
			o[arr[i][field]] = arr[i];
		}
		for (i in o) {
			r.push(o[i]);
		}
		return r;
	};
});