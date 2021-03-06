# coding: utf-8

"""
AdminApi.py
Copyright 2015 SmartBear Software

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
"""

from __future__ import absolute_import

import sys
import os

# python 2 and python 3 compatibility library
from six import iteritems

from ..configuration import Configuration
from ..api_client import ApiClient


class AdminApi(object):
    """
    NOTE: This class is auto generated by the swagger code generator program.
    Do not edit the class manually.
    Ref: https://github.com/swagger-api/swagger-codegen
    """

    def __init__(self, api_client=None):
        config = Configuration()
        if api_client:
            self.api_client = api_client
        else:
            if not config.api_client:
                config.api_client = ApiClient()
            self.api_client = config.api_client

    def clone_global_tag(self, source, dest, **kwargs):
        """
        Clone a GlobalTag.
        Clone a source global tag into a destination global tag. It associates all tags from the source.

        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please define a `callback` function
        to be invoked when receiving the response.
        >>> def callback_function(response):
        >>>     pprint(response)
        >>>
        >>> thread = api.clone_global_tag(source, dest, callback=callback_function)

        :param callback function: The callback function
            for asynchronous request. (optional)
        :param str source: source: name of the globaltag to be cloned (required)
        :param str dest: dest: name of the destination global tag (required)
        :return: GlobalTag
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['source', 'dest']
        all_params.append('callback')

        params = locals()
        for key, val in iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method clone_global_tag" % key
                )
            params[key] = val
        del params['kwargs']

        # verify the required parameter 'source' is set
        if ('source' not in params) or (params['source'] is None):
            raise ValueError("Missing the required parameter `source` when calling `clone_global_tag`")
        # verify the required parameter 'dest' is set
        if ('dest' not in params) or (params['dest'] is None):
            raise ValueError("Missing the required parameter `dest` when calling `clone_global_tag`")

        resource_path = '/admin/globaltags/clone'.replace('{format}', 'json')
        method = 'POST'

        path_params = {}

        query_params = {}
        if 'source' in params:
            query_params['source'] = params['source']
        if 'dest' in params:
            query_params['dest'] = params['dest']

        header_params = {}

        form_params = {}
        files = {}

        body_params = None

        # HTTP header `Accept`
        header_params['Accept'] = self.api_client.\
            select_header_accept(['application/json', 'application/xml'])
        if not header_params['Accept']:
            del header_params['Accept']

        # HTTP header `Content-Type`
        header_params['Content-Type'] = self.api_client.\
            select_header_content_type([])

        # Authentication setting
        auth_settings = []

        response = self.api_client.call_api(resource_path, method,
                                            path_params,
                                            query_params,
                                            header_params,
                                            body=body_params,
                                            post_params=form_params,
                                            files=files,
                                            response_type='GlobalTag',
                                            auth_settings=auth_settings,
                                            callback=params.get('callback'))
        return response

    def clone_tag(self, source, dest, **kwargs):
        """
        Clone a Tag.
        Clone a source  tag into a destination  tag. It copies all iovs from the source. A time range selection can be applied.

        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please define a `callback` function
        to be invoked when receiving the response.
        >>> def callback_function(response):
        >>>     pprint(response)
        >>>
        >>> thread = api.clone_tag(source, dest, callback=callback_function)

        :param callback function: The callback function
            for asynchronous request. (optional)
        :param str source: source: name of the globaltag to be cloned (required)
        :param str dest: dest: name of the destination global tag (required)
        :param str _from: from: since time of the iov range to be copied
        :param str to: to: until time of the iov range to be copied
        :param str time: time: definition of time type {time|run|date|timemilli}
        :return: Tag
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['source', 'dest', '_from', 'to', 'time']
        all_params.append('callback')

        params = locals()
        for key, val in iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method clone_tag" % key
                )
            params[key] = val
        del params['kwargs']

        # verify the required parameter 'source' is set
        if ('source' not in params) or (params['source'] is None):
            raise ValueError("Missing the required parameter `source` when calling `clone_tag`")
        # verify the required parameter 'dest' is set
        if ('dest' not in params) or (params['dest'] is None):
            raise ValueError("Missing the required parameter `dest` when calling `clone_tag`")

        resource_path = '/admin/tags/clone'.replace('{format}', 'json')
        method = 'POST'

        path_params = {}

        query_params = {}
        if 'source' in params:
            query_params['source'] = params['source']
        if 'dest' in params:
            query_params['dest'] = params['dest']
        if '_from' in params:
            query_params['from'] = params['_from']
        if 'to' in params:
            query_params['to'] = params['to']
        if 'time' in params:
            query_params['time'] = params['time']

        header_params = {}

        form_params = {}
        files = {}

        body_params = None

        # HTTP header `Accept`
        header_params['Accept'] = self.api_client.\
            select_header_accept(['application/json', 'application/xml'])
        if not header_params['Accept']:
            del header_params['Accept']

        # HTTP header `Content-Type`
        header_params['Content-Type'] = self.api_client.\
            select_header_content_type([])

        # Authentication setting
        auth_settings = []

        response = self.api_client.call_api(resource_path, method,
                                            path_params,
                                            query_params,
                                            header_params,
                                            body=body_params,
                                            post_params=form_params,
                                            files=files,
                                            response_type='Tag',
                                            auth_settings=auth_settings,
                                            callback=params.get('callback'))
        return response
