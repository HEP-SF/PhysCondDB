# coding: utf-8

"""
PayloadApi.py
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


class PayloadApi(object):
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

    def get_blob(self, hash, **kwargs):
        """
        Finds payload data by hash; the payload object contains the real BLOB.
        Select one payload at the time, no regexp searches allowed here

        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please define a `callback` function
        to be invoked when receiving the response.
        >>> def callback_function(response):
        >>>     pprint(response)
        >>>
        >>> thread = api.get_blob(hash, callback=callback_function)

        :param callback function: The callback function
            for asynchronous request. (optional)
        :param str hash: hash of the payload (required)
        :return: str
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['hash']
        all_params.append('callback')

        params = locals()
        for key, val in iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method get_blob" % key
                )
            params[key] = val
        del params['kwargs']

        # verify the required parameter 'hash' is set
        if ('hash' not in params) or (params['hash'] is None):
            raise ValueError("Missing the required parameter `hash` when calling `get_blob`")

        resource_path = '/payload/data/{hash}'.replace('{format}', 'json')
        method = 'GET'

        path_params = {}
        if 'hash' in params:
            path_params['hash'] = params['hash']

        query_params = {}

        header_params = {}

        form_params = {}
        files = {}

        body_params = None

        # HTTP header `Accept`
        header_params['Accept'] = self.api_client.\
            select_header_accept(['application/octet-stream'])
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
                                            response_type='str',
                                            auth_settings=auth_settings,
                                            callback=params.get('callback'))
        return response

    def get_payload_filtered_list(self, param, _if, value, **kwargs):
        """
        Select a payload filtering on metadata...Not well implemented.
        Select one payload at the time, no regexp searches allowed here.  This method is for the moment not well implemented.

        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please define a `callback` function
        to be invoked when receiving the response.
        >>> def callback_function(response):
        >>>     pprint(response)
        >>>
        >>> thread = api.get_payload_filtered_list(param, _if, value, callback=callback_function)

        :param callback function: The callback function
            for asynchronous request. (optional)
        :param str param: Parameter name: {datasize|objectType|version} (required)
        :param str _if: condition: {eq|gt|..} (required)
        :param str value: Parameter value: the value of the selected parameter (required)
        :param int page: page: page number for the query, defaults to 0
        :param int size: size: page size, defaults to 25
        :return: SwaggerPayloadCollection
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['param', '_if', 'value', 'page', 'size']
        all_params.append('callback')

        params = locals()
        for key, val in iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method get_payload_filtered_list" % key
                )
            params[key] = val
        del params['kwargs']

        # verify the required parameter 'param' is set
        if ('param' not in params) or (params['param'] is None):
            raise ValueError("Missing the required parameter `param` when calling `get_payload_filtered_list`")
        # verify the required parameter '_if' is set
        if ('_if' not in params) or (params['_if'] is None):
            raise ValueError("Missing the required parameter `_if` when calling `get_payload_filtered_list`")
        # verify the required parameter 'value' is set
        if ('value' not in params) or (params['value'] is None):
            raise ValueError("Missing the required parameter `value` when calling `get_payload_filtered_list`")

        resource_path = '/payload/filter'.replace('{format}', 'json')
        method = 'GET'

        path_params = {}

        query_params = {}
        if 'param' in params:
            query_params['param'] = params['param']
        if '_if' in params:
            query_params['if'] = params['_if']
        if 'value' in params:
            query_params['value'] = params['value']
        if 'page' in params:
            query_params['page'] = params['page']
        if 'size' in params:
            query_params['size'] = params['size']

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
                                            response_type='SwaggerPayloadCollection',
                                            auth_settings=auth_settings,
                                            callback=params.get('callback'))
        return response

    def get_blob_hash(self, **kwargs):
        """
        Take an input file and get its hash generated by the server.
        Used for checking hash generation

        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please define a `callback` function
        to be invoked when receiving the response.
        >>> def callback_function(response):
        >>>     pprint(response)
        >>>
        >>> thread = api.get_blob_hash(callback=callback_function)

        :param callback function: The callback function
            for asynchronous request. (optional)
        :param file file: 
        :return: dict(str, str)
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['file']
        all_params.append('callback')

        params = locals()
        for key, val in iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method get_blob_hash" % key
                )
            params[key] = val
        del params['kwargs']


        resource_path = '/payload/hash'.replace('{format}', 'json')
        method = 'POST'

        path_params = {}

        query_params = {}

        header_params = {}

        form_params = {}
        files = {}
        if 'file' in params:
            files['file'] = params['file']

        body_params = None

        # HTTP header `Accept`
        header_params['Accept'] = self.api_client.\
            select_header_accept(['application/json'])
        if not header_params['Accept']:
            del header_params['Accept']

        # HTTP header `Content-Type`
        header_params['Content-Type'] = self.api_client.\
            select_header_content_type(['multipart/form-data'])

        # Authentication setting
        auth_settings = []

        response = self.api_client.call_api(resource_path, method,
                                            path_params,
                                            query_params,
                                            header_params,
                                            body=body_params,
                                            post_params=form_params,
                                            files=files,
                                            response_type='dict(str, str)',
                                            auth_settings=auth_settings,
                                            callback=params.get('callback'))
        return response

    def get_payload(self, hash, **kwargs):
        """
        Finds payload by hash; the payload object contains only metadata on the payload itself.
        Select one payload at the time, no regexp searches allowed here

        This method makes a synchronous HTTP request by default. To make an
        asynchronous HTTP request, please define a `callback` function
        to be invoked when receiving the response.
        >>> def callback_function(response):
        >>>     pprint(response)
        >>>
        >>> thread = api.get_payload(hash, callback=callback_function)

        :param callback function: The callback function
            for asynchronous request. (optional)
        :param str hash: hash of the payload (required)
        :param bool expand: expand {true|false} is for parameter expansion
        :return: Payload
                 If the method is called asynchronously,
                 returns the request thread.
        """

        all_params = ['hash', 'expand']
        all_params.append('callback')

        params = locals()
        for key, val in iteritems(params['kwargs']):
            if key not in all_params:
                raise TypeError(
                    "Got an unexpected keyword argument '%s'"
                    " to method get_payload" % key
                )
            params[key] = val
        del params['kwargs']

        # verify the required parameter 'hash' is set
        if ('hash' not in params) or (params['hash'] is None):
            raise ValueError("Missing the required parameter `hash` when calling `get_payload`")

        resource_path = '/payload/{hash}'.replace('{format}', 'json')
        method = 'GET'

        path_params = {}
        if 'hash' in params:
            path_params['hash'] = params['hash']

        query_params = {}
        if 'expand' in params:
            query_params['expand'] = params['expand']

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
                                            response_type='Payload',
                                            auth_settings=auth_settings,
                                            callback=params.get('callback'))
        return response