# coding: utf-8

"""
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

    Ref: https://github.com/swagger-api/swagger-codegen
"""

from pprint import pformat
from six import iteritems


class FormDataContentDisposition(object):
    """
    NOTE: This class is auto generated by the swagger code generator program.
    Do not edit the class manually.
    """
    def __init__(self):
        """
        FormDataContentDisposition - a model defined in Swagger

        :param dict swaggerTypes: The key is attribute name
                                  and the value is attribute type.
        :param dict attributeMap: The key is attribute name
                                  and the value is json key in definition.
        """
        self.swagger_types = {
            'type': 'str',
            'parameters': 'dict(str, str)',
            'file_name': 'str',
            'creation_date': 'datetime',
            'modification_date': 'datetime',
            'read_date': 'datetime',
            'size': 'int',
            'name': 'str'
        }

        self.attribute_map = {
            'type': 'type',
            'parameters': 'parameters',
            'file_name': 'fileName',
            'creation_date': 'creationDate',
            'modification_date': 'modificationDate',
            'read_date': 'readDate',
            'size': 'size',
            'name': 'name'
        }

        self._type = None
        self._parameters = None
        self._file_name = None
        self._creation_date = None
        self._modification_date = None
        self._read_date = None
        self._size = None
        self._name = None

    @property
    def type(self):
        """
        Gets the type of this FormDataContentDisposition.


        :return: The type of this FormDataContentDisposition.
        :rtype: str
        """
        return self._type

    @type.setter
    def type(self, type):
        """
        Sets the type of this FormDataContentDisposition.


        :param type: The type of this FormDataContentDisposition.
        :type: str
        """
        self._type = type

    @property
    def parameters(self):
        """
        Gets the parameters of this FormDataContentDisposition.


        :return: The parameters of this FormDataContentDisposition.
        :rtype: dict(str, str)
        """
        return self._parameters

    @parameters.setter
    def parameters(self, parameters):
        """
        Sets the parameters of this FormDataContentDisposition.


        :param parameters: The parameters of this FormDataContentDisposition.
        :type: dict(str, str)
        """
        self._parameters = parameters

    @property
    def file_name(self):
        """
        Gets the file_name of this FormDataContentDisposition.


        :return: The file_name of this FormDataContentDisposition.
        :rtype: str
        """
        return self._file_name

    @file_name.setter
    def file_name(self, file_name):
        """
        Sets the file_name of this FormDataContentDisposition.


        :param file_name: The file_name of this FormDataContentDisposition.
        :type: str
        """
        self._file_name = file_name

    @property
    def creation_date(self):
        """
        Gets the creation_date of this FormDataContentDisposition.


        :return: The creation_date of this FormDataContentDisposition.
        :rtype: datetime
        """
        return self._creation_date

    @creation_date.setter
    def creation_date(self, creation_date):
        """
        Sets the creation_date of this FormDataContentDisposition.


        :param creation_date: The creation_date of this FormDataContentDisposition.
        :type: datetime
        """
        self._creation_date = creation_date

    @property
    def modification_date(self):
        """
        Gets the modification_date of this FormDataContentDisposition.


        :return: The modification_date of this FormDataContentDisposition.
        :rtype: datetime
        """
        return self._modification_date

    @modification_date.setter
    def modification_date(self, modification_date):
        """
        Sets the modification_date of this FormDataContentDisposition.


        :param modification_date: The modification_date of this FormDataContentDisposition.
        :type: datetime
        """
        self._modification_date = modification_date

    @property
    def read_date(self):
        """
        Gets the read_date of this FormDataContentDisposition.


        :return: The read_date of this FormDataContentDisposition.
        :rtype: datetime
        """
        return self._read_date

    @read_date.setter
    def read_date(self, read_date):
        """
        Sets the read_date of this FormDataContentDisposition.


        :param read_date: The read_date of this FormDataContentDisposition.
        :type: datetime
        """
        self._read_date = read_date

    @property
    def size(self):
        """
        Gets the size of this FormDataContentDisposition.


        :return: The size of this FormDataContentDisposition.
        :rtype: int
        """
        return self._size

    @size.setter
    def size(self, size):
        """
        Sets the size of this FormDataContentDisposition.


        :param size: The size of this FormDataContentDisposition.
        :type: int
        """
        self._size = size

    @property
    def name(self):
        """
        Gets the name of this FormDataContentDisposition.


        :return: The name of this FormDataContentDisposition.
        :rtype: str
        """
        return self._name

    @name.setter
    def name(self, name):
        """
        Sets the name of this FormDataContentDisposition.


        :param name: The name of this FormDataContentDisposition.
        :type: str
        """
        self._name = name

    def to_dict(self):
        """
        Returns the model properties as a dict
        """
        result = {}

        for attr, _ in iteritems(self.swagger_types):
            value = getattr(self, attr)
            if isinstance(value, list):
                result[attr] = list(map(
                    lambda x: x.to_dict() if hasattr(x, "to_dict") else x,
                    value
                ))
            elif hasattr(value, "to_dict"):
                result[attr] = value.to_dict()
            else:
                result[attr] = value

        return result

    def to_str(self):
        """
        Returns the string representation of the model
        """
        return pformat(self.to_dict())

    def __repr__(self):
        """
        For `print` and `pprint`
        """
        return self.to_str()
