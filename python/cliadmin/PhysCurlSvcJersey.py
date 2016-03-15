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
#from clint.textui import colored
from datetime import datetime

try:
    # python 3
    from urllib.parse import urlencode
except ImportError:
    # python 2
    from urllib import urlencode



class PhysRestConnection:
    
    __baseurl = 'localhost:8080/'
    __debug = False
    __header = None
    __timeout = 100
    __username = ''
    __password = ''
    
    def setdebug(self,value):
        self.__debug = value
    
    def getBaseUrl(self):
        return self.__baseurl
    
    def setBaseUrl(self,baseurl):
        self.__baseurl=baseurl
    
    def setUserPassword(self,user,password):
        self.__username = user
        self.__password = password
        
    def setSocks(self, host, port):
        self.__host = host
        self.__port = port
        self.__curl.setopt(pycurl.PROXY, self.__host)
        self.__curl.setopt(pycurl.PROXYPORT, self.__port)
        self.__curl.setopt(pycurl.PROXYTYPE, pycurl.PROXYTYPE_SOCKS5)
        
    def setUrl(self, url):
        if self.__debug:
            print "Setting url ", url
        if 'http://' in str(url):
            self.__url = url
        else:
            self.__url = self.__baseurl + url
        if self.__debug:
            print "URL after checking for http string: ", url
        
        self.__curl.setopt(pycurl.URL, self.__url)
        
    def downloadData(self, filename):
        if self.__debug:
            print "Download data using url ", self.__url
        fp = open(filename, "wb")
        # buf is a ByteIO object...check documentation
        #        self.__curl.setopt(self.__curl.WRITEDATA, buf)
        try:
            self.setDefaultOptions()
            self.__curl.setopt(self.__curl.HTTPGET, 1)
            self.__curl.setopt(self.__curl.FAILONERROR, True)
            self.__curl.setopt(self.__curl.WRITEDATA, fp)
            self.__curl.perform()
            # HTTP response code, e.g. 200.
            if self.__debug:
                print('Status: %d' % self.__curl.getinfo(self.__curl.RESPONSE_CODE))
            # Elapsed time for the transfer.
            if self.__debug:
                print('Status: %f' % self.__curl.getinfo(self.__curl.TOTAL_TIME))

            return fp
        except pycurl.error, error:
            response = self.__curl.getinfo(self.__curl.RESPONSE_CODE)
            if response is not 200:
                #print colored.red('Problem in executing GET : status returned is %d' % response)
                print ('Problem in executing GET : status returned is %d' % response)
            errno, errstr = error
            #print colored.red('An error occurred in GET method: %s ' % errstr)
            print ('An error occurred in GET method: %s ' % errstr)
            return None

    def getData(self):
        if self.__debug:
            print "Get data using url ", self.__url
        buf = cStringIO.StringIO()
#        self.__curl.setopt(self.__curl.WRITEFUNCTION, buf.write)
# in python 3 this is the correct way
# buf is a ByteIO object...check documentation
#        self.__curl.setopt(self.__curl.WRITEDATA, buf)
        try:
            self.setDefaultOptions()
            self.__curl.setopt(self.__curl.HTTPGET, 1)
            self.__curl.setopt(self.__curl.FAILONERROR, True)
            self.__curl.setopt(self.__curl.WRITEFUNCTION, buf.write)
            self.__curl.perform()
        # HTTP response code, e.g. 200.
            response = self.__curl.getinfo(self.__curl.RESPONSE_CODE)

            if self.__debug:
                print('Status: %d' % response)
        # Elapsed time for the transfer.
            if self.__debug:
                print('Status: %f' % self.__curl.getinfo(self.__curl.TOTAL_TIME))
                  
            data = None
            data = buf.getvalue()
            buf.close()
            if self.__debug:
                print 'Retrieved data :' , data
            if data == '':
                jsondata = {}
                jsondata['code'] = response
                return jsondata

            if self.__debug:
                print 'Parse data to get json...'
            jsondata = json.loads(data)
            if self.__debug:
                print 'Return json data ',jsondata,' and code ',response
            return jsondata, response
        except pycurl.error, error:
             errno, errstr = error
             #print colored.red('An error occurred: %s ' % errstr)
             print ('An error occurred: %s ' % errstr)
        
    def deleteData(self):
        if self.__debug:
            print "Delete data using url ", self.__url
        buf = cStringIO.StringIO()
        self.setDefaultOptions()
        self.__curl.setopt(self.__curl.CUSTOMREQUEST,'DELETE')
        self.__curl.setopt(self.__curl.USERNAME, self.__username)
        self.__curl.setopt(self.__curl.PASSWORD, self.__password)
        self.__curl.setopt(self.__curl.WRITEFUNCTION, buf.write)
        try:
            self.__curl.perform()
            response = self.__curl.getinfo(self.__curl.RESPONSE_CODE)
        # HTTP response code, e.g. 200.
            if self.__debug:
                print('Status: %d' % response)
        # Elapsed time for the transfer.
            if self.__debug:
                print('Status: %f' % self.__curl.getinfo(self.__curl.TOTAL_TIME))
            return { 'response' : response }
        except pycurl.error, error:
             errno, errstr = error
             #print colored.red('An error occurred: %s ' % errstr)
             print ('An error occurred: %s ' % errstr)

        
    def setTimeout(self,to):
        self.__timeout=to
        self.__curl.setopt(self.__curl.TIMEOUT, self.__timeout)

    def setHeader(self,header):
        self.__header = header
        self.__curl.setopt(self.__curl.HTTPHEADER, self.__header)        

# This function should set appropriate values for POST type
    def postData(self, jsonobj):
        if self.__debug:
            print "post data using url ", self.__url
        post_data = jsonobj
# Sets request method to POST,
# Content-Type header to application/x-www-form-urlencoded
# and data to send in request body.
        self.setDefaultOptions()
        buf = cStringIO.StringIO()
        self.__curl.setopt(self.__curl.HTTPGET, 0)
        self.__curl.setopt(self.__curl.POST,1)
        self.__curl.setopt(self.__curl.POSTFIELDS,post_data)
        self.__curl.setopt(self.__curl.FAILONERROR, True)
        self.__curl.setopt(self.__curl.WRITEFUNCTION, buf.write)
        try:
            self.__curl.perform()
            response = self.__curl.getinfo(self.__curl.RESPONSE_CODE)
        # HTTP response code, e.g. 200.
            if self.__debug:
                print('Status: %d' % response)
        # Elapsed time for the transfer.
                print('Status: %f' % self.__curl.getinfo(self.__curl.TOTAL_TIME))
            #            data = None
            data = buf.getvalue()
            if data is '':
                return None
            jsondata = json.loads(data)
            buf.close()
            return jsondata
        except pycurl.error, error:
             errno, errstr = error
             #print colored.red('An error occurred in POST: %s ' % errstr)
             print ('An error occurred in POST: %s ' % errstr)
             
# This function should set appropriate values for POST type
    def postAction(self, actionparams):
        if self.__debug:
            print "post data action using url ", self.__url
            print actionparams
        buf = cStringIO.StringIO()
        self.__curl.setopt(self.__curl.POSTFIELDS,actionparams)
        self.__curl.setopt(self.__curl.TIMEOUT, 30)
        self.__curl.setopt(self.__curl.WRITEFUNCTION, buf.write)
        try:
            self.__curl.perform()
            response = self.__curl.getinfo(self.__curl.RESPONSE_CODE)
            if self.__debug:
        # HTTP response code, e.g. 200.
                print('Status: %d' % response)
        # Elapsed time for the transfer.
                print('Status: %f' % self.__curl.getinfo(self.__curl.TOTAL_TIME))
            data = buf.getvalue()
            if data is '':
                return None
            jsondata = json.loads(data)
            jsondata['code'] = response
            buf.close()
            return jsondata
        except pycurl.error, error:
             errno, errstr = error
             #print colored.red('An error occurred in POST Action: %s ' % errstr)
             print ('An error occurred in POST Action: %s ' % errstr)


# This function should set appropriate values for POST type
    def postForm(self, post_data):
        if self.__debug:
            print "Post form using url ", self.__url
            # Sets request method to POST,
            # Content-Type header to application/x-www-form-urlencoded
            # and data to send in request body.
#         post_data = [("file", (self.__curl.FORM_FILE, params['file'])),
#                      ("path", str(params['path'])),
#                      ("package", str(params['package']))]
        if self.__debug:
            print post_data
        self.setDefaultOptions()
        self.__curl.setopt(self.__curl.POST,1)
        self.__curl.setopt(self.__curl.HTTPPOST,post_data)
        self.__curl.setopt(self.__curl.WRITEFUNCTION, self.write_out) # now passing own method
        try:
            self.__curl.perform()
            response = self.__curl.getinfo(self.__curl.RESPONSE_CODE)
            # HTTP response code, e.g. 200.
            if self.__debug:
                print('Status: %d' % response)
                # Elapsed time for the transfer.
                print('Status: %f' % self.__curl.getinfo(self.__curl.TOTAL_TIME))
            #            data = { 'response' : response, 'data' : self.dataresponse }
            data = self.dataresponse
            jsondata = json.loads(data)
            jsondata['code'] = response
            return jsondata
        except pycurl.error, error:
            errno, errstr = error
            print 'An error occurred: ', errstr


# This function should set appropriate values for POST type
    def postPayload(self, params):
        if self.__debug:
            print "Post payload file using url ", self.__url
        post_data = [("file", (self.__curl.FORM_FILE, params['file'])),
                     ("objectType", params['objectType']),
                     ("streamerInfo", params['streamerInfo']),
                     ("backendInfo", params['backendInfo']),
                     ("version", params['version']),
                     ("since", str(params['since'])),
                     ("sinceString", params['sinceString']),
                     ("tag", str(params['tag']))
                    ]
# Sets request method to POST,
# Content-Type header to application/x-www-form-urlencoded
# and data to send in request body.
        if self.__debug:
            print post_data
        self.setDefaultOptions()
        self.__curl.setopt(self.__curl.POST,1)
        self.__curl.setopt(self.__curl.HTTPPOST,post_data)
        self.__curl.setopt(self.__curl.WRITEFUNCTION, self.write_out) # now passing own method
        try:
            self.__curl.perform()
            response = self.__curl.getinfo(self.__curl.RESPONSE_CODE)
        # HTTP response code, e.g. 200.
            if self.__debug:
                print('Status: %d' % response)
        # Elapsed time for the transfer.
                print('Status: %f' % self.__curl.getinfo(self.__curl.TOTAL_TIME))
#            data = { 'response' : response, 'data' : self.dataresponse }
            data = self.dataresponse
            jsondata = json.loads(data)
            return jsondata
        except pycurl.error, error:
             errno, errstr = error
             print 'An error occurred: ', errstr

# This function should set appropriate values for POST type
    def postFile(self, params):
        print "Post file using url ", self.__url
        filename = params['file']
        post_data = [("file", (self.__curl.FORM_FILE, params['file']))]
        # Sets request method to POST,
        # Content-Type header to application/x-www-form-urlencoded
        # and data to send in request body.
        if self.__debug:
            print post_data
        self.__curl.setopt(self.__curl.POST,1)
        self.__curl.setopt(self.__curl.HTTPPOST,post_data)
        # Set size of file to be uploaded.
        filesize = os.path.getsize(filename)
        #self.__curl.setopt(self.__curl.INFILESIZE, filesize)
        if self.__debug:
            print 'File has size ',filesize
        try:
            self.__curl.perform()
            print 'URL action performed'
            response = self.__curl.getinfo(self.__curl.RESPONSE_CODE)
            # HTTP response code, e.g. 200.
            if self.__debug:
                print('Status: %d' % response)
            # Elapsed time for the transfer.
                print('Status: %f' % self.__curl.getinfo(self.__curl.TOTAL_TIME))
            return { 'response' : response }
        except pycurl.error, error:
            errno, errstr = error
            print 'An error occurred: ', errstr

    
    def write_out(self,data):
        if self.__debug:
            print 'Data len', len(data)
            print 'Data content : ', data
        self.dataresponse = data
        return len(data)
    
    def setDefaultOptions(self):
        if self.__debug:
            print 'Set default options'
        self.__curl.setopt(self.__curl.VERBOSE, False)

        #self.__curl.unsetopt(self.__curl.POST)
        #self.__curl.unsetopt(self.__curl.VERBOSE)

    def close(self):
        self.__curl.close()

    def getFormFile(self):
        return self.__curl.FORM_FILE;

    def __init__(self):
        self.__curl = pycurl.Curl()
        self.__curl.setopt(self.__curl.TIMEOUT, self.__timeout)
        self.__curl.setopt(self.__curl.FAILONERROR, True)
        self.__curl.setopt(self.__curl.VERBOSE, True)
        self.__curl.setopt(self.__curl.CONNECTTIMEOUT, 5)
        self.dataresponse = "none"

   
class PhysCond(object):
    ''' classdoc '''
    _dictkeys = []
    _dictval = {}
    _dicttypes = []
    _example = '''
    Generic object...
    '''
    __debug = False

    def __init__(self,jsonobj):
        self._dictval = jsonobj
    def getKeys(self):
        return self._dictkeys
    def getValues(self):
        return self._dictval
    def getTypes(self):
        return self._dicttypes
    def getParameter(self,field):
        #print 'Search for field ',field
        if field not in self._dictval:
            #print 'Not found...'
            return None
        return self._dictval[field]
    def setParameter(self,field,value):
        self._dictval[field] = value
    def toJson(self):
        return json.dumps(self._dictval, indent=4)
    def parseJson(self,jsonobj):
        self._dictval = json.loads(jsonobj)
    def help(self):
        return self._example
        
class GlobalTag(PhysCond):
    ''' classdoc '''
    _dictkeys = ['name','validity','description','release','lockstatus','snapshotTime','globalTagMaps']
    _dicttypes = ['String','BigDecimal','String','String','String','Timestamp','[]']
    _example = '''
The format of the time is fixed, and chosen to be the ISO 8601, as described in https://en.wikipedia.org/wiki/ISO_8601
{ "name" : "MYTEST_01", 
  "lockstatus" : "unlocked",
  "validity" : 0, 
  "description" : "First test gtag", 
  "release" : "1.0", 
  "snapshotTime" : "2014-12-12T01:00:10+02:00" }
'''
 
class Tag(PhysCond):
    ''' classdoc '''
    _dictkeys = ['name','timeType','objectType','synchronization','description','lastValidatedTime','endOfValidity','iovs']
    _dicttypes = ['String','String','String','String','String','BigDecimal','BigDecimal','[]']
    _example = '''
The timeType can be only time or run for the moment.
The lastValidatedTime is in the same unit (BigDecimal at db level).
{ "name" : "atag_02", 
  "timeType" : "time", 
  "objectType" : "test", 
  "synchronization" : "none", 
  "description" : "Fake object for test tag", 
  "lastValidatedTime" : 10000,
  "endOfValidity" : -1 }    
'''

class GtagMap(PhysCond):
    ''' classdoc '''
    _dictkeys = ['globaltagname','tagname','record','label']
    _dicttypes = ['String','String','String','String']
    _example = '''
        { "globaltagname" : "AGLOBAL_TAG", "tagname" : "atag", "record" : "some record",
        "label" : "some label"}
        '''


class Iov(PhysCond):
    ''' classdoc '''
    _dictkeys = ['since','sinceString','payload','tag']
    _dicttypes = ['BigDecimal','String','PayloadHash','TagName']
    _example = '''
        The since field corresponds to:
            - for time based tags: number of nano-seconds from Epoch
            - for run-lumi based tags: run-number << 32 & lumi-block
        The format of the sinceString depends on the time type
            - for time based tags: ISO 8601, as described in https://en.wikipedia.org/wiki/ISO_8601
            - for run-lumi based tags: <run>-<lumiblock>
        The hash is generated by the server, you cannot store an IOV without a payload
{ "since" : 5000, "sinceString" : "2014-12-12T01:00:10+02:00",
  "payload" : { "hash" : "f86eddd4f742ed61978e51c64e844004"},
  "tag" : { "name" : "atag_03" }}  
    '''
class SystemDesc(PhysCond):
    ''' classdoc '''
    _dictkeys = ['nodeFullpath','schemaName','tagNameRoot','nodeDescription','groupSize']
    _dicttypes = ['String','String','String','String','BigDecimal']
    _example = '''
The nodeFullpath is unique identifier, as well as tagNameRoot.
SchemaName is essentially for backward compatibility with COOL information.
groupSize should be used for paging.
{ "nodeFullpath" : "MY_NEW_SYSTEM", 
  "schemaName" : "aschema",
  "tagNameRoot" : "MyNewSystem-TagName", 
  "nodeDescription" : "system description"
  "groupSize" : "1000000" }
'''

class Payload(PhysCond):
    ''' classdoc '''
    _dictkeys = ['hash','version','objectType','data','datasize','streamerInfo','backendInfo']
    _dicttypes = ['String','String','String','PayloadDataHash','Integer','String','String']
    _example = '''
The payload metadata contains informations for the serialization and deserialization.
{ "version" : "a version", 
  "objectType" : "an object",
  "streamerInfo" : "streamer clob", 
  "backendInfo" : "the backend"}
'''

class PayloadData(PhysCond):
    ''' classdoc '''
    _dictkeys = ['hash','data']
    _dicttypes = ['String','BinaryBlob']

class PhysCurl(object):
    '''
    classdocs
    '''
    baseurl='/conddbweb/rest/expert'
    userbaseurl='/conddbweb/rest'
    adminbaseurl='/conddbweb/rest/admin'
    __debug = False


    ### TO BE DONE AT SERVER LEVEL
    def addPayload(self,params,servicebase="/payload"):
        if self.__debug:
            print 'Add payload using input '
            print params
        url = (self.baseurl + servicebase)
        self.__curl.setUrl(url)
        self.__curl.setHeader(['Content-Type:multipart/form-data'])
        return self.__curl.postPayload(params)

    def commitCalibration(self,params,servicebase="/calibration"):
        if self.__debug:
            print 'Add calibration file using input '
            print params
        url = (self.baseurl + servicebase)
        self.__curl.setUrl(url)
        self.__curl.setHeader(['Content-Type:multipart/form-data'])
        post_data = [("file", (self.__curl.getFormFile(), str(params['file']))),
                     ("package", str(params['package'])),
                     ("path", str(params['path'])),
                     ("since",str(params['since'])),
                     ("description",str(params['description']))]

        return self.__curl.postForm(post_data)

    def addWithPairs(self,data,params,servicebase="/globaltags"):
        if self.__debug:
            print 'Add object using parameter '
            print params
        url = (self.baseurl + servicebase )
        self.__curl.setUrl(url)
        self.__curl.setHeader(['Content-Type:application/json', 'Accept:application/json'])
        urlquoted = urllib.quote_plus(url,safe=':/')
        pairs = urllib.urlencode(params)
        self.__curl.setUrl(urlquoted+'?'+pairs)
        if self.__debug:
            print 'Try to serialize in json '
        jsonobj = json.dumps(data)
        return self.__curl.postData(jsonobj)

    def addPairs(self,params,servicebase="/globaltags"):
        if self.__debug:
            print 'Add object using parameter '
            print params
        url = (self.baseurl + servicebase )
        self.__curl.setUrl(url)
        self.__curl.setHeader(['Content-Type:application/json', 'Accept:application/json'])
        urlquoted = urllib.quote_plus(url,safe=':/')
        pairs = urllib.urlencode(params)
        self.__curl.setUrl(urlquoted+'?'+pairs)
        if self.__debug:
            print 'Try to serialize in json '
        return self.__curl.postAction(pairs)


    def addJsonEntity(self,params,servicebase="/globaltags"):
        if self.__debug:
            print 'Add object using parameter '
            print params
        url = (self.baseurl + servicebase )
        self.__curl.setUrl(url)
        self.__curl.setHeader(['Content-Type:application/json', 'Accept:application/json'])
        if self.__debug:
            print 'Try to serialize in json '
        jsonobj = json.dumps(params)
        return self.__curl.postData(jsonobj)

    def deleteEntity(self,id,servicebase="/globaltags"):
        if self.__debug:
            print 'Update object using parameter '
            print id
        url = (self.baseurl + servicebase + "/" + id)
        self.__curl.setUrl(url)
        self.__curl.setHeader(['Content-Type:application/json', 'Accept:application/json'])
        return self.__curl.deleteData()


### Update statement is based on an entry map, which represents in Json the fields we want to update
## params: should contain the json map for the modification of the object
## id    : the id of the object to be modified (for the moment we use the name in the url; like globaltagname, tagname, ... .
###    - /globaltags : { 'description', 'lockstatus', 'release', 'validity', snapshotTime' }
###                    { 'name' } ==> this can be updated only if no tags are linked to this global tag
###    - /maps :       { 'label', 'record' }
###    - /tags :       { 'description', 'synchronization','objectType','timeType','lastValidatedTime','endOfValidity' }

    def update(self,params,id,servicebase="/globaltags"):
        if self.__debug:
            print 'Update object using parameter '
            print params
            print id
        url = (self.baseurl + servicebase + "/" + id)
        self.__curl.setUrl(url)
        self.__curl.setHeader(['Content-Type:application/json', 'Accept:application/json'])
        if self.__debug:
            print 'Try to serialize in json '
        jsonobj = json.dumps(params)
        return self.__curl.postData(jsonobj)

### Get an object from the conditions DB, depending on the servicebase path
###   - /globaltags : <name> [optional: trace=on/off]
###   - /tags       : <name> [optional: trace=on/off]
    def get(self,params,servicebase="/globaltags"):
        if self.__debug:
            print 'get: search object using parameter '
            print params
        id = params['name']
        url = (self.userbaseurl + servicebase )
        if id is not None:
            url = (self.userbaseurl + servicebase + '/' + id)
        urlquoted = urllib.quote_plus(url,safe=':/')
        if params['trace'] is not None and params['trace'] == 'on':
            args = { 'trace' : 'on' }
            if '%' in id:
                msg = ('You are using pattern to select with trace=on: expect a failure from server !')
                #print colored.cyan(msg)
                print msg

            pairs = urllib.urlencode(args)
            self.__curl.setUrl(urlquoted+'?'+pairs)
        elif params['expand'] is not None and params['expand'] == 'true':
            args = { 'expand' : 'true' }
            pairs = urllib.urlencode(args)
            self.__curl.setUrl(urlquoted+'?'+pairs)
        else:
            self.__curl.setUrl(urlquoted)
        
        return self.__curl.getData()

    def getfile(self,params,servicebase="/globaltags"):
        if self.__debug:
            print 'getfile: search object using parameter '
            print params
        id = params['name']
        url = (self.userbaseurl + servicebase )
        if id is not None:
            url = (self.userbaseurl + servicebase + '/' + id)
        urlquoted = urllib.quote_plus(url,safe=':/')
        self.__curl.setUrl(urlquoted)

        return self.__curl.downloadData(id+'-temp.tar')


    def getiovs(self,params,servicebase="/iovs/find"):
        if self.__debug:
            print 'Search IOV object using parameter '
            print params
        id = params['tag']
        url = (self.userbaseurl + servicebase )
        urlquoted = urllib.quote_plus(url,safe=':/')
        pairs = urllib.urlencode(params)
        self.__curl.setUrl(urlquoted+'?'+pairs)
        return self.__curl.getData()

    def getmaps(self,params,servicebase="/maps/find"):
        if self.__debug:
            print 'Search Map object using parameter '
            print params
        gtag = params['globaltag']
        tag = params['tag']
        expand = params['expand']
        url = (self.userbaseurl + servicebase )
        urlquoted = urllib.quote_plus(url,safe=':/')
        pairs = urllib.urlencode(params)
        self.__curl.setUrl(urlquoted+'?'+pairs)
        return self.__curl.getData()

    def getsystems(self,params,servicebase="/systems/find"):
        if self.__debug:
            print 'Search systems using parameters ',params
        url = (self.userbaseurl + servicebase )
        urlquoted = urllib.quote_plus(url,safe=':/')
        pairs = urllib.urlencode(params)
        self.__curl.setUrl(urlquoted+'?'+pairs)
        return self.__curl.getData()

    def getlink(self,params,servicebase=None):
        if self.__debug:
            print 'follow href link ',params['href']
            print params
        url = (params['href'])
        urlquoted = urllib.quote_plus(url,safe='=&?:/')
        self.__curl.setUrl(urlquoted)
        return self.__curl.getData()

    def deletelink(self,link,servicebase=None):
        if self.__debug:
            print 'follow href link ',link
            print link
        modurl = link.split("/")
        urllength = len(modurl)
        modurl.insert(urllength-2,'expert')
        url = ('/'.join(modurl))
        self.__curl.setUrl(url)
        self.__curl.setHeader(['Content-Type:application/json', 'Accept:application/json'])
        return self.__curl.deleteData()


    def setUserPassword(self,user,passwd):
        self.__curl.setUserPassword(user, passwd)
    
    def close(self):
        self.__curl.close()

    def setdebug(self,value):
        self.__debug = value
        self.__curl.setdebug(value)

    def getFormFile(self):
        return self.__curl.getFormFile()
    
    def __init__(self, servicepath, usesocks=False):
        '''
        Constructor
        '''
        if self.__debug:
            print 'Access to PhysCondDB via REST services'
        self.__servicepath = servicepath
        self.__curl = PhysRestConnection()
        self.__curl.setBaseUrl(self.__servicepath)
        if usesocks == True:
            self.__curl.setSocks('localhost', 3129)
#print 'Init Business Delegate with servicepath ', self.__servicepath

class PhysUtils(object):
    '''
    classdocs
    '''
    __debug = False
    __restserver = {}
    
    def __init__(self, restserver):
        '''
        Constructor
        '''
        if self.__debug:
            print 'Function utils for command lines'
        self.__restserver = restserver
       
    def loadItems(self, data):
    # Assume that data is a list of items
        for anobj in data:
            print anobj
            href = anobj['href']
            url = {}
            url['href'] = href
            print 'Use url link ',url
            obj = self.__restserver.getlink(url)
            print obj

    def createObj(self, type, data):
        if type == 'globaltags':
            return GlobalTag(data)
        elif type == 'tags':
            return Tag(data)
        elif type == 'iovs':
            return Iov(data)
        elif type == 'maps':
            return GtagMap(data)
        elif type == 'systems':
            return SystemDesc(data)
        elif type == 'payload':
            return Payload(data)
        return None


    def lockit(self, params):
        globaltagname=params[0]
        lockstatus=params[1]
        data={}
        # Search globaltagname in global tags
        print 'Search for global tag name ',globaltagname
        data['lockstatus']=lockstatus
        gtag = self.__restserver.addJsonEntity(data,'/globaltags/'+globaltagname)
        print 'Updated status of global tag ', gtag


    def gettagiovs(self, data):
        objList = []
        print 'Select iovs using arguments ',data
        objList = self.__restserver.getiovs(data,'/iovs/find')
        return objList

    def getgtagtags(self, data):
        obj = {}
        print 'Select mappings using arguments ',data
        obj = self.__restserver.get(data,'/globaltags')
        mpobj = self.createObj('globaltags',obj)
        maplist=[]
        if mpobj.getValues()['globalTagMaps'] is not None:
            maplist = mpobj.getValues()['globalTagMaps']
            for amap in maplist:
                atag = Tag(amap['systemTag'])
                gtag = GlobalTag(amap['globalTag'])
                print atag.toJson()
        return maplist
    



