'''
Created on Feb 26, 2015

@author: formica
'''

import sys, os, pickle, getopt,re
import json
import urllib2
import cStringIO
import os.path
import urllib
from cStringIO import StringIO
from urllib2 import Request, urlopen, URLError, HTTPError

import socks
import socksipyhandler
#from multipartform import MultiPartForm
from encode import multipart_encode
from streaminghttp import register_openers

from xml.dom import minidom
#from clint.textui import colored
from datetime import datetime

try:
    # python 3
    from urllib.parse import urlencode
except ImportError:
    # python 2
    from urllib import urlencode


class RequestWithMethod(urllib2.Request):
  def __init__(self, method, *args, **kwargs):
    print 'Calling constructor of RequestWithMethod ',method,' ',args
    self._method = method
    urllib2.Request.__init__(*args, **kwargs)

  def get_method(self):
    return self._method


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
        if self.__debug:
            print 'DEBUG: Setting base url ',baseurl
        self.__baseurl=baseurl
    
    def setUserPassword(self,user,password):
        self.__username = user
        self.__password = password
        
    def setSocks(self, host, port):
        self.__host = host
        self.__port = port
        self.__opener = urllib2.build_opener(socksipyhandler.SocksiPyHandler(socks.PROXY_TYPE_SOCKS5, self.__host, self.__port))
        
    def setUrl(self, url):
        if self.__debug:
            print "DEBUG: Setting url ", url
        if 'http://' in str(url):
            #            print "DEBUG: Setting url overriding base URL: ", url
            self.__url = str(url)
        else:
            #            print "DEBUG: Setting url using baseurl ", self.__baseurl
            self.__url = self.__baseurl + url
        if self.__debug:
            print "DEBUG: URL after checking for http string: ", url
        
#        self.__curl.setopt(pycurl.URL, self.__url)
        
    def downloadData(self, filename):
        if self.__debug:
            print "DEBUG: Download data using url ", self.__url
        fp = open(filename, "wb")
        req = Request(self.__url)
        try:
            response = self.__opener.open(req)
        except HTTPError as e:
            print '====== HTTPError occurred     ======='
            print 'The server couldn\'t fulfill the request.'
            print 'Error code: ', e.code
            print 'Reason ',e.reason
            details = e.read()
            print 'Details: ',details
            jsondata = json.loads(details)
            print 'Extracted message: ',jsondata['internalMessage']
            print '====== End of HTTPError report ======='
            raise e
        except URLError as e:
            print '====== URLError occurred      ======='
            print 'We failed to reach a server.'
            print 'Reason: ', e.reason
            print '====== End of URLError report ======='
            raise e
        else:
        # everything is fine
            fp.write(response.read())
            fp.close()
            if self.__debug:
                print 'DEBUG: Received data into output file ',filename

            return      


    def getData(self):
        if self.__debug:
            print "DEBUG: Get data using url ", self.__url
            
        req = Request(self.__url)
        req.add_header("Accept",'application/json')
        try:
            #            print 'Sending request ',req
            response = self.__opener.open(req)
        #            print 'Received response '
        except HTTPError as e:
            print '====== HTTPError occurred     ======='
            print 'The server couldn\'t fulfill the request.'
            print 'Error code: ', e.code
            print 'Reason ',e.reason
            details = e.read()
            print 'Details: ',details
            jsondata = json.loads(details)
            print 'Extracted message: ',jsondata['internalMessage']
            print '====== End of HTTPError report ======='
            raise e
        except URLError as e:
            print '====== URLError occurred      ======='
            print 'We failed to reach a server.'
            print 'Reason: ', e.reason
            print '====== End of URLError report ======='
            raise e
        else:
        # everything is fine
        #            print '...read response...'
            data = response.read()
            #            print '...data...',data
            if self.__debug:
                print 'DEBUG: Received data', data
            if data is '':
                jsondata = {}
                jsondata['code'] = response.getcode()
                return jsondata

            jsondata = json.loads(data)
            if self.__debug:
                print 'DEBUG: JSON data ', jsondata, ' and return Code: ',response.getcode()
#            jsondata['code'] = response.getcode()
            return jsondata, response.getcode()            
        
    def deleteData(self):
        if self.__debug:
            print "DEBUG: Delete data using url ", self.__url

        req = Request(self.__url)
        #####req = RequestWithMethod('DELETE',self.__url)
        req.get_method = lambda: 'DELETE'
        
        try:
            response = self.__opener.open(req)
        except HTTPError as e:
            print '====== HTTPError occurred     ======='
            print 'The server couldn\'t fulfill the request.'
            print 'Error code: ', e.code
            print 'Reason ',e.reason
            details = e.read()
            print 'Details: ',details
            jsondata = json.loads(details)
            print 'Extracted message: ',jsondata['internalMessage']
            print '====== End of HTTPError report ======='
            raise e
        except URLError as e:
            print '====== URLError occurred      ======='
            print 'We failed to reach a server.'
            print 'Reason: ', e.reason
            print '====== End of URLError report ======='
            raise e
        else:
            # everything is fine        
            if self.__debug:
                print 'DEBUG: Retrieved data from DELETE:' , response, ' Code: ',response.getcode()
        
    def setTimeout(self,to):
        self.__timeout=to
        print('Not implemented')
#        self.__curl.setopt(self.__curl.TIMEOUT, self.__timeout)

    def setHeader(self,header):
        self.__header = header
        print('WARNING: Not implemented')

# This function should set appropriate values for POST type
    def postData(self, jsonobj):
        if self.__debug:
            print "DEBUG: post data using url ", self.__url
        post_data = jsonobj
        
        req = Request(self.__url,post_data)
        req.add_header("Content-Type",'application/json')
        req.add_header("Accept",'application/json')
        try:
            response = self.__opener.open(req)
            ### was response = urlopen(req)
        except HTTPError as e:
            print '====== HTTPError occurred     ======='
            print 'The server couldn\'t fulfill the request.'
            print 'Error code: ', e.code
            print 'Reason ',e.reason
            details = e.read()
            print 'Details: ',details
            jsondata = json.loads(details)
            print 'Extracted message: ',jsondata['internalMessage']
            print '====== End of HTTPError report ======='
            raise e
			
        except URLError as e:
            print '====== URLError occurred      ======='
            print 'We failed to reach a server.'
            print 'Reason: ', e.reason
            print '====== End of URLError report ======='
            raise e
        else:
        # everything is fine
            data = response.read()
            if self.__debug:
                print 'DEBUG: Received data', data
            if data is '':
                return None
            jsondata = json.loads(data)
            if self.__debug:
                print 'DEBUG: JSON data ', jsondata
            jsondata['code'] = response.getcode()
            return jsondata    
             
# This function should set appropriate values for POST type
    def postAction(self, actionparams):
        if self.__debug:
            print "DEBUG: post data action using url ", self.__url
            #print actionparams

        req = Request(self.__url,actionparams)
        req.add_header("Content-Type",'application/json')
        req.add_header("Accept",'application/json')
        try:
            response = urlopen(req)
        except HTTPError as e:
            print '====== HTTPError occurred     ======='
            print 'The server couldn\'t fulfill the request.'
            print 'Error code: ', e.code
            print 'Reason ',e.reason
            details = e.read()
            print 'Details: ',details
            jsondata = json.loads(details)
            print 'Extracted message: ',jsondata['internalMessage']
            print '====== End of HTTPError report ======='
            raise e
        except URLError as e:
            print '====== URLError occurred      ======='
            print 'We failed to reach a server.'
            print 'Reason: ', e.reason
            print '====== End of URLError report ======='
            raise e
        else:
        # everything is fine
            data = response.read()
            if self.__debug:
                print 'DEBUG: Received data', data
            if data is '':
                return None
            jsondata = json.loads(data)
            if self.__debug:
                print 'DEBUG: JSON data ', jsondata
            jsondata['code'] = response.getcode()
            return jsondata    

# This function should set appropriate values for POST type
    def postForm(self, post_data):
        if self.__debug:
            print "DEBUG: Post form using url ", self.__url
        if self.__debug:
            print post_data
            
# Start the multipart/form-data encoding of the file "DSC0001.jpg"
# "image1" is the name of the parameter, which is normally set
# via the "name" parameter of the HTML <input> tag.

# headers contains the necessary Content-Type and Content-Length
# datagen is a generator object that yields the encoded parameters
# 
#         formdata = {
#                     'file': open(post_data['file'], 'rb'),
#                     'package' : str(post_data['package']),
#                     'path': str(post_data['path']),
#                     'since': str(post_data['since']),
#                     'description': str(post_data['description'])
#                     }
        datagen, headers = multipart_encode(post_data)

# Create the Request object
        req = Request(self.__url, datagen, headers)
        req.add_header("Accept",'application/json')
        if self.__debug:
            print 'DEBUG: OUTGOING DATA ...'
            print req.get_data()

        try:
            response = urlopen(req)
#            print response
        except HTTPError as e:
            print '====== HTTPError occurred     ======='
            print 'The server couldn\'t fulfill the request.'
            print 'Error code: ', e.code
            print 'Reason ',e.reason
            details = e.read()
            print 'Details: ',details
            jsondata = json.loads(details)
            print 'Extracted message: ',jsondata['internalMessage']
            print '====== End of HTTPError report ======='
            raise e
        except URLError as e:
            print '====== URLError occurred      ======='
            print 'We failed to reach a server.'
            print 'Reason: ', e.reason
            print '====== End of URLError report ======='
            raise e
        else:
        # everything is fine
            data = response.read()
            if self.__debug:
                print 'DEBUG: Received data', data
            if data is '':
                return None
            jsondata = json.loads(data)
            if self.__debug:
                print 'DEBUG: JSON data ', jsondata
            jsondata['code'] = response.getcode()
            return jsondata    
    
    def setDefaultOptions(self):
        if self.__debug:
            print 'Set default options...none'

    def close(self):
        self.__opener.close()

    def getFormFile(self):
        return
        #return self.__curl.FORM_FILE;

    def __init__(self):
        # Register the streaming http handlers with urllib2
        #self.__opener = urllib2.build_opener()
        self.__opener = register_openers()
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
The format of the global tag name is ^([A-Z]+[A-Za-z0-9]+)-([A-Z0-9]+)-([0-9])++$
{ "name" : "MyTest-T01-01", 
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
The format of the tag name is ^([A-Z]+[a-zA-Z0-9-_]+)_([A-Za-z0-9-]+)_([0-9])++$
{ "name" : "Atag_Tag01_01", 
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

class PayloadData(PhysCond):
    ''' classdoc '''
    _dictkeys = ['hash','data']
    _dicttypes = ['String','BinaryBlob']

class PhysCurl(object):
    '''
    classdocs
    '''
    baseurl='/api/rest/expert'
    userbaseurl='/api/rest'
    adminbaseurl='/api/rest/admin'
    __debug = False


    ### TO BE DONE AT SERVER LEVEL
    def addPayload(self,params,servicebase="/payload"):
        if self.__debug:
            print 'DEBUG: Add payload using input '
            print params
        url = (self.baseurl + servicebase)
        self.__curl.setUrl(url)
        formdata = {
            'file': open(params['file'], 'rb'),
            'objectType' : str(params['objectType']),
            'streamerInfo': str(params['streamerInfo']),
            'backendInfo': str(params['backendInfo']),
            'version': str(params['version']),
            'since': str(params['since']),
            'sinceString': str(params['sinceString']),
            'tag': str(params['tag'])
        }
        return self.__curl.postForm(formdata)

    def commitCalibration(self,params,servicebase="/calibration"):
        if self.__debug:
            print 'DEBUG: Add calibration file using input '
            print params
        url = (self.baseurl + servicebase)
        self.__curl.setUrl(url)
        formdata = {
            'file': open(params['file'], 'rb'),
            'package' : str(params['package']),
            'path': str(params['path']),
            'since': str(params['since']),
            'description': str(params['description'])
        }
        return self.__curl.postForm(formdata)

    def addWithPairs(self,data,params,servicebase="/globaltags"):
        if self.__debug:
            print 'DEBUG: Post method using pair parameters '
            print params
        url = (self.baseurl + servicebase )
        self.__curl.setUrl(url)
        self.__curl.setHeader(['Content-Type:application/json', 'Accept:application/json'])
        urlquoted = urllib.quote_plus(url,safe=':/')
        pairs = urllib.urlencode(params)
        self.__curl.setUrl(urlquoted+'?'+pairs)
        if self.__debug:
            print 'DEBUG: Try to serialize in json '
        jsonobj = json.dumps(data)
        return self.__curl.postData(jsonobj)

    def addPairs(self,params,servicebase="/globaltags"):
        if self.__debug:
            print 'DEBUG: Post action using pair parameters '
            print params
        url = (self.baseurl + servicebase )
        self.__curl.setUrl(url)
#        self.__curl.setHeader(['Content-Type:application/json', 'Accept:application/json'])
        urlquoted = urllib.quote_plus(url,safe=':/')
        pairs = urllib.urlencode(params)
#        self.__curl.setUrl(urlquoted)
        self.__curl.setUrl(urlquoted+'?'+pairs)
        if self.__debug:
            print 'Try to serialize in json '
        return self.__curl.postAction(pairs)


    def addJsonEntity(self,params,servicebase="/globaltags"):
        if self.__debug:
            print 'DEBUG: Add json object using parameter '
            print params
        url = (self.baseurl + servicebase )
        self.__curl.setUrl(url)
#        self.__curl.setHeader(['Content-Type:application/json', 'Accept:application/json'])
        if self.__debug:
            print 'DEBUG: Try to serialize in json '
        jsonobj = json.dumps(params)
        return self.__curl.postData(jsonobj)

    def deleteEntity(self,id,servicebase="/globaltags"):
        if self.__debug:
            print 'DEBUG: Update object using parameter ',id

        url = (self.baseurl + servicebase + "/" + id)
        self.__curl.setUrl(url)
##        self.__curl.setHeader(['Content-Type:application/json', 'Accept:application/json'])
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
            print 'DEBUG: Update object using parameters '
            print params
            print id
        url = (self.baseurl + servicebase + "/" + id)
        self.__curl.setUrl(url)
        self.__curl.setHeader(['Content-Type:application/json', 'Accept:application/json'])
        if self.__debug:
            print 'DEBUG: Try to serialize in json '
        jsonobj = json.dumps(params)
        return self.__curl.postData(jsonobj)

### Get an object from the conditions DB, depending on the servicebase path
###   - /globaltags : <name> [optional: trace=on/off]
###   - /tags       : <name> [optional: trace=on/off]
    def get(self,params,servicebase="/globaltags"):
        if self.__debug:
            print 'DEBUG: Get object using parameters '
            print params
        
        id=None
        if 'name' in params:
            id = params['name']
        #Ignore by when usind ID
            params['by'] = 'none'
            params['page'] = 'none'
            params['size'] = 'none'

        if 'package' not in params:
            params['package'] = 'none'
            
        if 'trace' not in params:
            params['trace'] = 'off'
            
        if 'expand' not in params:
            params['expand'] = 'false'
        
        url = (self.userbaseurl + servicebase )
        if id is not None:
            url = (self.userbaseurl + servicebase + '/' + id)
        else:
            # If name is not defined, this is a global search: use by=field:something to create specifications
            url = (self.userbaseurl + servicebase)
       
        print 'Use url: ',url,' and params ',params

        args = {}
        urlquoted = urllib.quote_plus(url,safe=':/')
        
        if 'trace' in params and params['trace'] is not None:
            args['trace'] = params['trace']
            if id is not None and '%' in id:
                msg = ('You are using pattern to select with trace=on: expect a failure from server !')
                #print colored.cyan(msg)
                print msg
        if params['expand'] is not None:
            args['expand'] = params['expand']

        if 'package' in params and params['package'] is not None and params['package'] != 'none':
            args['package'] = params['package']

        if params['by'] is not None and params['by'] != 'none':
            args['by'] = params['by']
        if params['page'] is not None and params['page'] != 'none':
            args['page'] = params['page']
        if params['size'] is not None and params['size'] != 'none':
            args['size'] = params['size']

        print 'Arguments for query parameters: ',args
        if len(args) == 0 :
            self.__curl.setUrl(urlquoted)
        else:
            pairs = urllib.urlencode(args)
            self.__curl.setUrl(urlquoted+'?'+pairs)

        return self.__curl.getData()

    def getpayload(self,params,servicebase="/payload/data"):
        if self.__debug:
            print 'DEBUG: Get payload object using parameters '
            print params
        id = params['name']            
        url = (self.userbaseurl + servicebase )
        if id is not None:
            url = (self.userbaseurl + servicebase + '/' + id)
            print 'Contact server using url ',url
            urlquoted = urllib.quote_plus(url,safe=':/')
            print 'Transform url in ',urlquoted
            self.__curl.setUrl(urlquoted)
        outf = params['filename']            
        return self.__curl.downloadData(outf)

    def getfile(self,params,servicebase="/expert/calibration/tar"):
        if self.__debug:
            print 'DEBUG: Get file using parameter '
            print params
        id = params['name']
        url = (self.userbaseurl + servicebase )
        if id is not None:
            url = (self.userbaseurl + servicebase + '/' + id)
        urlquoted = urllib.quote_plus(url,safe=':/')
        if params['package'] is not None and params['package'] != 'none':
            args = { 'package' : params['package'] }
            pairs = urllib.urlencode(args)
            self.__curl.setUrl(urlquoted+'?'+pairs)
        else:      
            self.__curl.setUrl(urlquoted)

        return self.__curl.downloadData(id+'-temp.tar')
#        return 'Not implemented'

    def getiovs(self,params,servicebase="/iovs/find"):
        if self.__debug:
            print 'DEBUG: Search IOV object using parameter '
            print params
        id = params['tag']
        url = (self.userbaseurl + servicebase )
        urlquoted = urllib.quote_plus(url,safe=':/')
        pairs = urllib.urlencode(params)
        self.__curl.setUrl(urlquoted+'?'+pairs)
        return self.__curl.getData()

    def getmaps(self,params,servicebase="/maps/find"):
        if self.__debug:
            print 'DEBUG: Search Map object using parameter '
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
            print 'DEBUG: Search systems using parameters ',params
        url = (self.userbaseurl + servicebase )
        urlquoted = urllib.quote_plus(url,safe=':/')
        pairs = urllib.urlencode(params)
        self.__curl.setUrl(urlquoted+'?'+pairs)
##        escurl = urllib.quote_plus(urlquoted+'?'+pairs,safe='=&?:/')
        if self.__debug:
            print 'DEBUG: Search systems using url ',urlquoted        
##        self.__curl.setUrl(escurl)
        return self.__curl.getData()

    def getlink(self,params,servicebase=None):
        if self.__debug:
            print 'DEBUG: follow href link ',params['href']
            print params
        url = (params['href'])
        #        urlquoted = urllib.quote_plus(url,safe=':/')
        urlquoted = urllib.quote_plus(url,safe='=&?:/')
        #print 'Using url ',url
        self.__curl.setUrl(urlquoted)
        return self.__curl.getData()

    def deletelink(self,link,servicebase=None):
        if self.__debug:
            print 'DEBUG: delete resource referred by href link ',link

        modurl = link.split("/")
        urllength = len(modurl)
        modurl.insert(urllength-2,'expert')
        url = ('/'.join(modurl))
        self.__curl.setUrl(url)
#        self.__curl.setHeader(['Content-Type:application/json', 'Accept:application/json'])
        return self.__curl.deleteData()

    def setUserPassword(self,user,passwd):
#        self.__curl.setUserPassword(user, passwd)
        print 'WARNING: Not implemented'
           
    def close(self):
        print 'WARNING: Not implemented ??'
#        self.__curl.close()

    def setdebug(self,value):
        self.__debug = value
        self.__curl.setdebug(value)

    def getFormFile(self):
        print 'WARNING: Not implemented'
        return self.__curl.getFormFile()
    
    def __init__(self, servicepath, usesocks=False):
        '''
        Constructor
        '''
        if self.__debug:
            print 'DEBUG: Access to PhysCondDB via REST services'
        self.__servicepath = servicepath
        self.__curl = PhysRestConnection()
        self.__curl.setBaseUrl('http://'+self.__servicepath)
        if usesocks == True:
            self.__curl.setSocks('localhost', 3129)


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
            print 'DEBUG: Function utils for command lines'
        self.__restserver = restserver
 
    def printmsg(self,msg,color):
        try:
          from clint.textui import colored
          if color == 'cyan':
          	print colored.cyan(msg)
          elif color == 'blue':
            print colored.blue(msg)
          elif color == 'red':
            print colored.red(msg)
          elif color == 'green':
            print colored.green(msg)
          elif color == 'yellow':
            print colored.yellow(msg)
          else:
          	print colored.cyan(msg)
        except:
          print msg

       
    def printItems(self, data):
    # Assume that data is a list of items
        for anobj in data:
            print anobj
            href = anobj['href']
            url = {}
            url['href'] = href
            if self.__debug:
               print 'DEBUG: Use url link ',url
            obj = self.__restserver.getlink(url)
            print obj

# load items from link
    def loadItems(self, data):
        # Check if data is a single object
        if 'href' in data:
            href = data['href']
            url = {}
            url['href'] = href
            if self.__debug:
                print 'DEBUG: Use url link ',url
            obj = self.__restserver.getlink(url)
            if self.__debug:
                print 'DEBUG: Retrieved object from link  ',obj
            
            return obj
            
        # Assume that data is a list of items
        # NOT SURE THIS WORKS...
#        for anobj in data:
#            #print anobj
#            href = anobj['href']
#            url = {}
#            url['href'] = href
#            if self.debug:
#                print 'Use url link from object ',anobj,' -> ',url
#            obj = self.restserver.getlink(url)
#            return obj


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


    def lockit(self, globaltagname,lockstatus):
        data={}
        # Search globaltagname in global tags
        if self.__debug:
           print 'Search for global tag name ',globaltagname
        data['lockstatus']=lockstatus
        gtag = self.__restserver.addJsonEntity(data,'/globaltags/'+globaltagname)
        print 'INFO: Updated status of global tag ', gtag
        return gtag

    def link(self, globaltagname,tagname,params):
        if self.__debug:
           print 'Link global tag ',globaltagname,' to tag ',tagname,' using params ',params
        params['globaltagname'] = globaltagname
        params['tagname'] = tagname
        maps = self.__restserver.addJsonEntity(params,'/maps')
        print 'INFO: Linked global tag to tag : ', maps
        return maps

    def calibtagandlink(self, systemsglobaltag,systemname):
        if self.__debug:
           print 'Create global tag ',systemsglobaltag,' and link it to all tags in package ',systemname
        params = {}
        params['globaltag'] = systemsglobaltag
        params['package'] = systemname
        response = self.__restserver.addPairs(params,'/calibration/tag')
        return response

# download calibration files for a given package and global tag
    def dumpgtag(self, globaltagname, package):
        if self.__debug:
           print 'Dump is using parameter ',globaltagname, package
        data={}
        data['name']=globaltagname
        data['package']=package
        data['trace']='off'
        data['expand']='false'
        # Search globaltagname in global tags
        msg = ('>>> Dump all files in GlobalTag %s') % (globaltagname)
#        print colored.cyan(msg)
        print msg
        self.__restserver.get(data,'/expert/calibration/dump')
        msg = ('    + Tree structure for GlobalTag %s was dump on file system') % (globaltagname)
#        print colored.green(msg)
        print msg
        
# collect a global tag
    def collect(self, globaltagname, asgglobaltagname):
        data={}
        data['packagetag']=globaltagname
        data['destgtag']=asgglobaltagname
        data['trace']='off'
        data['expand']='false'
        # Search globaltagname in global tags
        msg = ('>>> Merge all files in GlobalTag %s into ASG global tag %s') % (globaltagname,asgglobaltagname)
        self.printmsg(msg,'cyan')
        self.__restserver.addPairs(data,'/calibration/collect')
        msg = ('    + Tree structure for GlobalTag %s was dump on file system == FIX THIS MESSAGE') % (globaltagname)
        self.printmsg(msg,'green')
        
# get tar from a global tag
    def gettar(self, globaltagname, package):
        data={}
        data['name']=globaltagname
        data['package']=package
        # Search globaltagname in global tags
        msg = ('>>> Collect all files in GlobalTag %s and download tar file ') % (globaltagname)
        self.printmsg(msg,'cyan')
        self.__restserver.getfile(data,'/expert/calibration/tar')

    def linkall(self, tagname, action, globaltagname, record, label):
        if self.__debug:
           print 'Link global tag ',globaltagname,' to tag ',tagname,' using action ',action
        data = {}
        params = {}
        data['name'] = tagobject
        data['record'] = mapparams['record']
        data['label'] = mapparams['label']
        params['action'] = action             
        self.__restserver.addWithPairs(data,params,'/globaltags/'+object+'/'+gtagobject)
        return

    def unlink(self, globaltagname,tagname):
        if self.__debug:
           print 'Remove link for global tag ',globaltagname,' to tag ',tagname
        data = {}
        data['globaltag']=globaltagname
        data['tag']=tagname
        data['expand'] = 'true'        
        (entity, response) = self.__restserver.getmaps(data)
        url = entity['href']
        self.__restserver.deletelink(url)        
##maps = self.__restserver.deleteEntity(id,'/maps')
        print 'INFO: Removed link from global tag to tag'

    def gettagiovs(self, tag, globaltag, trace, expand, t0, tMax):
        objList = []
        data = {}
        data['trace']=trace
        data['expand']=expand
        data['tag']=tag
        data['globaltag']=globaltag
        data['since']=t0
        data['until']=tMax
##        print 'Select iovs using arguments ',data
        (objList, code) = self.__restserver.getiovs(data,'/iovs/find')
##        json_string = json.dumps(objList,sort_keys=True,indent=4, separators=(',', ': '))
##        print json_string
        return (objList,code)

    def storePayload(self, data):
        objList = []
        if self.__debug:
           print 'Store iov+payload using arguments ',data
        objList = self.__restserver.addPayload(data,'/iovs/payload')
        return objList

    def getPayload(self, hash, trace, expand):
        data = {}
        data['trace']=trace
        data['expand']=expand
        data['name']=hash
        if self.__debug:
           print 'Get payload using arguments ',data
        (obj, response) = self.__restserver.get(data,'/payload')
        return (obj, response)

    def getPayloadData(self, hash, outfile):
        data = {}
        data['name']=hash
        data['filename']=outfile
        if self.__debug:
           print 'Get payload data using arguments ',data
        self.__restserver.getpayload(data,'/payload/data')
        return

    def dump(self, hash):
        if self.__debug:
           print 'Dump payload using arguments ',hash
        data = {}
        data['name']=hash
        data['trace'] = 'off'
        data['expand'] = 'true'
        (obj, response) = self.__restserver.get(data,'/payload')
        if response > 220:
           print 'ERROR: get payload information has given error code ',response
           raise Exception('Request for getting payload failed')
        print 'Payload information retrieved: ',obj
        payloaddataname = obj['objectType']
        self.getPayloadData(hash,payloaddataname)
        return (payloaddataname, response)

    def getTag(self, tag, trace, expand):
        data = {}
        data['trace']=trace
        data['expand']=expand
        data['name']=tag
        if self.__debug:
           print 'Get tag using arguments ',data
        (obj, response) = self.__restserver.get(data,'/tags')
        return (obj, response)

    def addObject(self, type, data):
        if self.__debug:
           print 'Add object of type ', type, ' using arguments ',data
        response = self.__restserver.addJsonEntity(data,'/'+type)
        return (response)

    def deleteObject(self, type, data):
        if self.__debug:
           print 'Delete object of type ', type, ' using arguments ',data
        self.__restserver.deleteEntity(data,'/'+type)
        print 'INFO: Object removed'
        return
        
#### calibration methods
    def commit(self,filename,pkgname,destpath,since,sincedesc):
        params = {}
        params['file'] = filename
        params['package'] = pkgname
        params['path'] = destpath
        params['since'] = since
        params['description'] = sincedesc 
        if self.__debug:
           print 'commit using parameters ',params       
        response = self.__restserver.commitCalibration(params,'/calibration/commit')
        return response
    
    def getobject(self, name, trace, expand, object):
        data = {}
        data['trace']=trace
        data['expand']=expand
        data['name']=name
        obj = {}
        if self.__debug:
           print 'Select mappings using arguments ',data
        (obj, response) = self.__restserver.get(data,'/'+object)
        return (obj, response)

    def getobjectlist(self, by, expand, page, pagesize, object):
        data = {}
        data['by']=by
        data['expand']=expand
        data['page']=page
        data['size']=pagesize
        obj = {}
        if self.__debug:
            print 'Select mappings using arguments ',data
        (obj, response) = self.__restserver.get(data,'/'+object)
        return (obj, response)

    def getsystems(self, bytype, fieldname):
        systemdata = {}
        systemdata['by']=bytype
        systemdata['name']=fieldname
        systemdata['expand']='true'
        (systemobj, code) = self.__restserver.getsystems(systemdata,'/systems/find')
        return (systemobj, code)
        
