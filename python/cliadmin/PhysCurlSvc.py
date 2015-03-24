'''
Created on Feb 26, 2015

@author: formica
'''

import sys, os, pickle, getopt,re
import json
import pycurl
import cStringIO
import os.path
import urllib

from xml.dom import minidom
from clint.textui import colored
from datetime import datetime

try:
    # python 3
    from urllib.parse import urlencode
except ImportError:
    # python 2
    from urllib import urlencode



class PhysRestConnection:
    
    __baseurl = 'localhost:8080/'
    __debug = True
    __header = None
    __timeout = 10
    
    def getBaseUrl(self):
        return self.__baseurl
    def setBaseUrl(self,baseurl):
        self.__baseurl=baseurl
    
    def setSocks(self, host, port):
        self.__host = host
        self.__port = port
        self.__curl.setopt(pycurl.PROXY, self.__host)
        self.__curl.setopt(pycurl.PROXYPORT, self.__port)
        self.__curl.setopt(pycurl.PROXYTYPE, pycurl.PROXYTYPE_SOCKS5)
        
    def setUrl(self, url):
        if self.__debug:
            print "Setting url ", url
        self.__url = self.__baseurl + url
        self.__curl.setopt(pycurl.URL, self.__url)
        
    def getData(self):
        print "Get data using url ", self.__url
        buf = cStringIO.StringIO()
#        self.__curl.setopt(self.__curl.WRITEFUNCTION, buf.write)
# in python 3 this is the correct way
# buf is a ByteIO object...check documentation
#        self.__curl.setopt(self.__curl.WRITEDATA, buf)
        self.__curl.setopt(self.__curl.WRITEFUNCTION, buf.write)
        self.__curl.perform()
        # HTTP response code, e.g. 200.
        print('Status: %d' % self.__curl.getinfo(self.__curl.RESPONSE_CODE))
        # Elapsed time for the transfer.
        print('Status: %f' % self.__curl.getinfo(self.__curl.TOTAL_TIME))

        data = buf.getvalue()
#        print 'Retrieved data :' , data
        try:
            jsondata = json.loads(data)
            return jsondata
        except json, e:
            print e
            return {}
        
    def setTimeout(self,to):
        self.__timeout=to
        self.__curl.setopt(self.__curl.TIMEOUT, self.__timeout)

    def setHeader(self,header):
        self.__header = header
        self.__curl.setopt(self.__curl.HTTPHEADER, self.__header)        

# This function should set appropriate values for POST type
    def postData(self, jsonobj):
        print "Set data using url ", self.__url
        post_data = jsonobj
# Sets request method to POST,
# Content-Type header to application/x-www-form-urlencoded
# and data to send in request body.
        print post_data
        self.__curl.setopt(self.__curl.POST,1)
        self.__curl.setopt(self.__curl.POSTFIELDS,post_data)
        self.__curl.setopt(self.__curl.VERBOSE, True)
        try:
            self.__curl.perform()
            response = self.__curl.getinfo(self.__curl.RESPONSE_CODE)
        # HTTP response code, e.g. 200.
            print('Status: %d' % response)
        # Elapsed time for the transfer.
            print('Status: %f' % self.__curl.getinfo(self.__curl.TOTAL_TIME))
            return { 'response' : response }
        except pycurl.error, error:
             errno, errstr = error
             print 'An error occurred: ', errstr

# This function should set appropriate values for POST type
    def postPayload(self, params):
        print "Post file using url ", self.__url
        post_data = [("file", (self.__curl.FORM_FILE, params['file'])),
                     ("type", params['objectType']),
                    ("streamer", params['streamerInfo']),
                    ("version", params['version'])
                    ]
# Sets request method to POST,
# Content-Type header to application/x-www-form-urlencoded
# and data to send in request body.
        print post_data
        self.__curl.setopt(self.__curl.POST,1)
        self.__curl.setopt(self.__curl.HTTPPOST,post_data)
        self.__curl.setopt(self.__curl.VERBOSE, True)
        try:
            self.__curl.perform()
            response = self.__curl.getinfo(self.__curl.RESPONSE_CODE)
        # HTTP response code, e.g. 200.
            print('Status: %d' % response)
        # Elapsed time for the transfer.
            print('Status: %f' % self.__curl.getinfo(self.__curl.TOTAL_TIME))
            return { 'response' : response }
        except pycurl.error, error:
             errno, errstr = error
             print 'An error occurred: ', errstr
 
    
    def __init__(self):
        self.__curl = pycurl.Curl()
        self.__curl.setopt(self.__curl.TIMEOUT, self.__timeout)
        self.__curl.setopt(self.__curl.FAILONERROR, True)
        self.__curl.setopt(self.__curl.CONNECTTIMEOUT, 5)

   
class PhysCond(object):
    ''' classdoc '''
    _dictkeys = []
    _dictval = {}
    _dicttypes = []
    _example = ''' 
    Generic object...
    '''
    def __init__(self,jsonobj):
        self._dictval = jsonobj
    def getKeys(self):
        return self._dictkeys
    def getValues(self):
        return self._dictval
    def getTypes(self):
        return self._dicttypes
    def getParameter(self,field):
        return self._dictval[field]
    def toJson(self):
        return json.dumps(self._dictval, indent=4)
    def parseJson(self,jsonobj):
        self._dictval = json.loads(jsonobj)
    def help(self):
        return self._example
        
class GlobalTag(PhysCond):
    ''' classdoc '''
    _dictkeys = ['name','validity','description','release','lockstatus','snapshotTime']
    _dicttypes = ['String','BigDecimal','String','String','String','Timestamp']
    _example = '''
{ "name" : "MYTEST_01", "lockstatus" : "unlocked","validity" : 0, 
  "description" : "First test gtag", "release" : "1.0", 
  "snapshotTime" : "20141212010010:GMT"}
    '''
 
class Tag(PhysCond):
    ''' classdoc '''
    _dictkeys = ['name','timeType','objectType','synchronization','description','lastValidatedTime','endOfValidity']
    _dicttypes = ['String','String','String','String','String','BigDecimal','BigDecimal']
    _example = '''
{ "name" : "atag_02", "timeType" : "time", "objectType" : "test", 
  "synchronization" : "none", "description" : "Fake object for test tag", 
  "lastValidatedTime" : 10000,
  "endOfValidity" : -1 }    
    '''

class Iov(PhysCond):
    ''' classdoc '''
    _dictkeys = ['since','sinceString','payload','tag']
    _dicttypes = ['BigDecimal','String','PayloadHash','TagName']
    _example = '''
{ "since" : 5000, "sinceString" : "20141130100000:GMT", 
  "payload" : { "hash" : "f86eddd4f742ed61978e51c64e844004"},
  "tag" : { "name" : "atag_03" }}  
    '''
   
class Payload(PhysCond):
    ''' classdoc '''
    _dictkeys = ['hash','version','objectType','data','datasize','streamerInfo']
    _dicttypes = ['String','String','String','PayloadDataHash','Integer','String']

class PayloadData(PhysCond):
    ''' classdoc '''
    _dictkeys = ['hash','data']
    _dicttypes = ['String','BinaryBlob']

class PhysCurl(object):
    '''
    classdocs
    '''

    def addGlobalTag(self,params,servicebase="/conddbweb/gtagAdd"):

        print 'Add global tag using input '
        print params
        url = servicebase
        self.__curl.setUrl(url)
        self.__curl.setHeader(['Content-Type:application/json', 'Accept:application/json'])
#      globalTag = GlobalTag(params)
        print 'Try to serialize in json '
        jsonobj = json.dumps(params)
        print jsonobj
        return self.__curl.postData(jsonobj)

    def addTag(self,params,servicebase="/conddbweb/tagAdd"):

        print 'Add tag using input '
        print params
        url = servicebase
        self.__curl.setUrl(url)
        self.__curl.setHeader(['Content-Type:application/json', 'Accept:application/json'])
        print 'Try to serialize in json '
        jsonobj = json.dumps(params)
        print jsonobj
        return self.__curl.postData(jsonobj)

    def addIov(self,params,servicebase="/conddbweb/iovAdd"):

        print 'Add iov using input '
        print params
        url = servicebase
        self.__curl.setUrl(url)
        self.__curl.setHeader(['Content-Type:application/json', 'Accept:application/json'])
        print 'Try to serialize in json '
        jsonobj = json.dumps(params)
        print jsonobj
        return self.__curl.postData(jsonobj)

    def addPayload(self,params,servicebase="/conddbweb/uploadPayload"):

        print 'Add payload using input '
        print params
        url = servicebase
        self.__curl.setUrl(url)
        self.__curl.setHeader(['Content-Type:multipart/form'])
        return self.__curl.postPayload(params)

    def getGlobalTag(self,params,servicebase="/conddbweb/globaltag"):

        print 'Search global tag using parameter '
        print params
        url = servicebase
        params = [('name',params)]
        pairs = urllib.urlencode(params)
        self.__curl.setUrl(url+'?'+pairs)
        return self.__curl.getData()

    def getPayload(self,params,servicebase="/conddbweb/payload"):

        print 'Search payload using parameter '
        print params
        url = servicebase
        params = [('hash',params)]
#        pairs = urllib.urlencode(params)
        self.__curl.setUrl(url+'/'+params)
        return self.__curl.getData()

    def find(self,params,servicebase="/conddbweb"):

        print 'Search ',params['type'],' using parameter ',params['id']
        url = servicebase+'/'+params['type']+'/'+params['id']+params['opt']
        urlquoted = urllib.quote_plus(url,safe=':/')
        if params['type'] == 'globaltag' and params['opt'] == '/trace':
            print 'Verify url syntax ',url, ' ',urlquoted
            if url != urlquoted:
                return "Cannot access globaltag trace information for list of global tags"
        self.__curl.setUrl(urlquoted)
        return self.__curl.getData()

    def traceGlobalTags(self,params,servicebase="/conddbweb/gtagliketrace"):

        url = servicebase
        params = [('namepattern',params)]
        pairs = urllib.urlencode(params)
        self.__curl.setUrl(url+'?'+pairs)
        return self.__curl.getData()

    def mapTag2GlobalTag(self,tagname,gtagname,servicebase="/conddbweb/mapTagToGtag"):
        url = servicebase
        params = [('globaltagname', gtagname),('tagname' , tagname)]
        pairs = urllib.urlencode(params)
        self.__curl.setUrl(url)
        self.__curl.setHeader(['Content-Type:application/x-www-form-urlencoded'])
        return self.__curl.postData(pairs)
        

    def __init__(self, servicepath, usesocks=False):
        '''
        Constructor
        '''
        print 'Access to PhysCondDB via REST services'
        self.__servicepath = servicepath
        self.__curl = PhysRestConnection()
        self.__curl.setBaseUrl(self.__servicepath)
        if usesocks == True:
            self.__curl.setSocks('localhost', 3129)
        print 'Init Business Delegate with servicepath ', self.__servicepath