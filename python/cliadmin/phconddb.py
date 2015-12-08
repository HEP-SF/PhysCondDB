#!/usr/bin/env python
# encoding: utf-8
'''
cliadmin.phconddb -- shortdesc

cliadmin.phconddb is a description

It defines classes_and_methods

@author:     user_name

@copyright:  2015 organization_name. All rights reserved.

@license:    license

@contact:    user_email
@deffield    updated: Updated
'''

import sys, os, pickle, getopt,re
import json
import pycurl
import cStringIO
import os.path

from pygments import highlight, lexers, formatters
from xml.dom import minidom
from clint.textui import colored
from datetime import datetime

from PhysCurlSvcJersey import PhysCurl,GlobalTag,Tag,Iov,GtagMap,SystemDesc,Payload,PayloadData

class PhysDBDriver():
    def __init__(self):
    # process command line options
        try:
            self.restserver = {}
            self._command = sys.argv[0]
            self.useSocks = False
            self.t0 = 0
            self.tMax = 'Inf'
            self.debug = False
            self.trace = 'off'
            self.expand = 'false'
            self.iovspan = 'time'
            self.jsondump=False
            self.dump=False
            self.user='none'
            self.passwd='none'
            self.outfilename=''
            self.urlsvc='localhost:8080/physconddb'
            longopts=['help','socks','out=','jsondump','t0=','tMax=','url=','debug','trace=','expand=','iovspan=','user=','pass=']
            opts,args=getopt.getopt(sys.argv[1:],'',longopts)
            print opts, args
        except getopt.GetoptError,e:
            print e
            self.usage()
            sys.exit(-1)
        self.procopts(opts,args)
        self.execute()

    def usage(self):
        print
        print "usage: CliJerseyAdmin.py {<options>} <action> {<args>}"
        print "Search conditions DB content using args"
        print "Server url is needed in order to provide the system with the base url; defaults to localhost (see below)"
        print "Action determines which rest method to call"
        print "Actions list:"
        print " - FIND <type> <id> [tags, globaltags, systems]"
        print "        ex: globaltags MY% : retrieve list of global tags following pattern name"
        print "        ex: --trace=on FIND globaltags MYTAG : retrieve one global tag and associated leaf tags"
        print "   remarks: --trace=on will fail in case multiple object (id=xx%) are retrieved"
        print " "
        print " - ADD <type> [tags, globaltags, systems] <parameters string> [column separated key=val list]"
        print "        ex: globaltags 'name=MYGTAG;description=A global tag; xxxx ' : create a global tag using the provided parameter list"
        print " "
        print " - LINK <globaltagname> <tagname> 'record=xxx;label=yyy'"
        print "        ex: MYGTAG TAG_01 record=none;label=none : link a global tag to a tag, both should exists."
        print " "
        print " - UNLINK <globaltagname> <tagname> "
        print "        ex: MYGTAG TAG_01 : remove link from global tag to a tag."
        print " "
        print " - DELETE <type> <id> [tags, globaltags, systems]"
        print "        ex: globaltags MYGTAG : remove the global tag MYGTAG."
        print " "
        print "Options: "
        print "  --socks activate socks proxy on localhost 3129 "
        print "  --debug activate debugging output "
        print "  --out={filename} activate dump on filename "
        print "  --jsondump activate a dump of output lines in json format "
        print "  --trace [on|off]: trace associations (ex: globaltag ->* tags or tag ->* iovs "
        print "  --expand [true|false]: expand result to complete obj, not only urls "
        print "  --url [localhost:8080/physconddb]: use a specific server "
        print "  --t0={t0 for iovs} "
        print "  --tMax={tMax for iovs} "
        print "  --iovspan={time|date|runlb|timerun|daterun} "
        print "         time: iov in COOL format, allows Inf for infinity"
        print "         date: iov in yyyyMMddHHmmss format"
        print "         runlb: iov in run-lb format, only for run based folders "
        print "         the others make the conversion to run number, does not allow Inf for infinity "
        print "Examples: "

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
        return None

    def createObjParsingArgs(self, type, data):
        
        # Assumes that data is a string using column separated field
        # Build a dictionary out of it then call createObj
        #print data
        argsarr = data.split(';')
        obj = {}
        outdata = {}
        for anarg in argsarr:
            key = anarg.split('=')[0]
            val = anarg.split('=')[1]
            obj[key]=val
        
        #print 'Created python dictionary ',obj
        objinst = self.createObj(type,{})
        keys = objinst.getKeys()
        for akey in keys:
            #print 'Search value for ',akey
            if akey in obj:
                outdata[akey] = obj[akey]
            else:
                outdata[akey]=None
                #print 'No value is defined for ',akey
        return outdata


    def helpAdd(self, type):
        mobj = None
        if type == 'globaltags':
            mobj = GlobalTag({})
        elif type == 'tags':
            mobj = Tag({})
        elif type == 'iovs':
            mobj = Iov({})
        elif type == 'systems':
            mobj = SystemDesc({})

        print 'Help defined for ',mobj
        return mobj.help()

    def procopts(self,opts,args):
        "Process the command line parameters"
        for o,a in opts:
            print 'Analyse options ' + o + ' ' + a
            if (o=='--help'):
                self.usage()
                sys.exit(0)
            if (o=='--socks'):
                self.useSocks=True
            if (o=='--out'):
                self.dump=True
                self.outfilename=a
            if (o=='--debug'):
                self.debug=True
            if (o=='--jsondump'):
                self.jsondump=True
            if (o=='--url'):
                self.urlsvc=a
            if (o=='--t0'):
                self.t0=a
            if (o=='--tMax'):
                self.tMax=a
            if (o=='--user'):
                self.user=a
            if (o=='--trace'):
                self.trace=a
            if (o=='--expand'):
                self.expand=a
            if (o=='--pass'):
                self.passwd=a
            if (o=='--iovspan'):
                self.iovspan=a
                
        if (len(args)<2):
            raise getopt.GetoptError("Insufficient arguments - need at least 3, or try --help")
        self.action=args[0].upper()
        self.args=args[1:]
        
    
    def loadItems(self, data):
    # Assume that data is a list of items
        for anobj in data:
            print anobj
            href = anobj['href']
            url = {}
            url['href'] = href
            print 'Use url link ',url
            obj = self.restserver.getlink(url)
            print obj

    def lockit(self, params):
        globaltagname=params[0]
        lockstatus=params[1]
        data={}
        # Search globaltagname in global tags
        print 'Search for global tag name ',globaltagname
        data['lockstatus']=lockstatus
        gtag = self.restserver.addJsonEntity(data,'/globaltags/'+globaltagname)
        print 'Updated status of global tag ', gtag


    def gettagiovs(self, data):
        objList = []
        print 'Select iovs using arguments ',data
        objList = self.restserver.getiovs(data,'/iovs/find')
        json_string = json.dumps(objList,sort_keys=True,indent=4, separators=(',', ': '))
        print json_string

    def getgtagtags(self, data):
        obj = {}
        print 'Select mappings using arguments ',data
        obj = self.restserver.get(data,'/globaltags')
        mpobj = self.createObj('globaltags',obj)
        maplist=[]
        if mpobj.getValues()['globalTagMaps'] is not None:
            maplist = mpobj.getValues()['globalTagMaps']
            for amap in maplist:
                atag = Tag(amap['systemTag'])
                gtag = GlobalTag(amap['globalTag'])
                print atag.toJson()
        return maplist

    def execute(self):
        print colored.blue(('Execute the command for action %s and arguments : %s ' ) % (self.action, str(self.args)))
        start = datetime.now()
        self.restserver = PhysCurl(self.urlsvc, self.useSocks)
        if self.debug:
            self.restserver.setdebug(True)
        
        if self.dump:
            outfile = open(self.outfilename,"w")
        _dict = {}
        dictarr = []
        dictkeys = []
        dictvalues = []
        params = {}
        
        if (self.action=='SEARCHIOVS'):
            try:
                print 'Action SEARCHIOVS is used to retrieve iovs from the DB, filtering by tag and time range'
                print 'Found N arguments ',len(self.args)
                print 'The searchiovs uses type ID {optional: globaltag, since, until, page, size}'
                type=self.args[0]
                tagname=self.args[1]
                globaltag='none'
                print 'Selecting tag  ', tagname
                if len(self.args)>2:
                    globaltag=self.args[2]
                    print 'Use insertion time mode ', globaltag
                since = 0
                until = 'INF'
                page = 0
                size = 1000
                if len(self.args) > 3:
                    since = self.args[2]
                    until = self.args[3]
                print since, until
                if len(self.args) > 5:
                    page = self.args[4]
                    size = self.args[5]
                print page, size
                
                if type == 'globaltag':
                    print 'Select a list of tags using the global tag ',tagname
                    data = {}
                    data['trace']='on'
                    data['name']=tagname
                    maplist = self.getgtagtags(data)
                    for amap in maplist:
                        atag = Tag(amap['systemTag'])
                        data = {}
                        data['tag']=atag.getParameter('name')
                        data['globaltag']=globaltag
                        data['since']=since
                        data['until']=until
                        data['page']=page
                        data['size']=size
                        data['expand']=self.expand
                        objList = self.gettagiovs(data)
                elif type == 'tag':
                        data = {}
                        data['tag']=tagname
                        data['globaltag']=globaltag
                        data['since']=since
                        data['until']=until
                        data['page']=page
                        data['size']=size
                        data['expand']=self.expand
                        objList = self.gettagiovs(data)


            except Exception, e:
                sys.exit("failed: %s" % (str(e)))
                raise
        
        if (self.action=='FIND'):
            try:
                print 'Action FIND is used to retrieve object from the DB'
                print 'Found N arguments ',len(self.args)
                object=self.args[0]
                msg = ('FIND: selected object is %s ') % (object)
                if object in [ 'globaltags', 'tags', 'systems' ]:
                    print colored.cyan(msg)
                else:
                    msg = ('FIND: cannot apply command to object %s ') % (object)
                    print colored.red(msg) 
                    return
                # load arguments and prepare the data structure for the GET request
                # optional parameters like trace and expand are added
                name = None
                if len(self.args) > 1:
                    name=self.args[1]
                    msg = ('FIND: selected id is %s ') % (name)
                    print colored.cyan(msg)
                data = {}
                data['trace']=self.trace
                data['expand']=self.expand
                data['name']=name
                objList = []
                traceList = []
                msg = ('FIND: load data using trace %s ') % (self.trace)
                print colored.cyan(msg)                   

                if self.trace == 'off':   
                    if objList is None or len(objList) == 0:
                        (objList,response) = self.restserver.get(data,'/'+object)

                else:
                    (obj,response) = self.restserver.get(data,'/'+object)
                    if self.debug:
                        msg = ('FIND: retrieved object from database %s ') % (obj)
                        print colored.cyan(msg)                        
                    if obj is None:
                        msg = ('FIND: error, cannot find any object in database for type %s ') % (object)
                        print colored.red(msg)
                        raise
                    objList.append(obj)
                    # If trace is active, perform a special dump for the trace
                    if object in [ 'globaltags', 'tags' ]:
                        mpobj = self.createObj(object,obj)
                        globaltagmaps = mpobj.getValues()['globalTagMaps']
                        if globaltagmaps is not None:
                            try:
                                maplist = globaltagmaps['items']
                                if hasattr(maplist, '__iter__'):
                                    for amap in maplist:
                                        traceList.append(('GlobalTag %s => Tag %s') % (amap['globalTagName'],amap['tagName']))
                            except Exception, e:
                                sys.exit("failed on looping over items: %s" % (str(e)))
                                raise
                                
# Now dump the retrieved content
                if response != 200 and response != 201:
                    msg = ('FIND: error in data retrieval %s ') % (response)
                    print colored.red(msg)
                    return
                json_string = json.dumps(objList,sort_keys=True,indent=4, separators=(',', ': '))
                colorful_json = highlight(unicode(json_string, 'UTF-8'), lexers.JsonLexer(), formatters.TerminalFormatter())
                print colorful_json
                
                if len(traceList) > 0:
                    msg = ('FIND: found list of globaltags to tags associations')
                    print colored.cyan(msg)
                    for amsg in traceList:
                        print colored.green(amsg)
   
            
            except Exception, e:
                sys.exit("failed on action FIND: %s" % (str(e)))
                raise

        elif self.action == 'ADD':
            try:
                print 'Action ADD is used to insert a metadata object (globaltags,tags,maps,...) into the DB'
                object=self.args[0]
                msg = ('ADD: selected object is %s ') % (object)
                if object in [ 'globaltags', 'tags', 'systems' ]:
                    print colored.cyan(msg)
                else:
                    msg = ('ADD: cannot apply command to object %s ') % (object)
                    print colored.red(msg) 
                    msg = ('ADD: to insert an IOV + Payload use STORE, see --help')
                    print colored.cyan(msg) 
                    return

                objparams = None
                if len(self.args) > 1:
                    objparams=self.args[1]
                    
                msg = ('ADD: object parameters %s ') % (objparams)
                print colored.cyan(msg)
                data = {}
                if objparams is None:
                    msg = self.helpAdd(object)
                    print colored.cyan(msg)
                    return
                # Parameters have been provided in command line, try to create the json entity
                data = self.createObjParsingArgs(object,objparams)
                print json.dumps(data)
        
                self.restserver.addJsonEntity(data,'/'+object)
        
            except Exception, e:
                sys.exit("ADD failed: %s" % (str(e)))
                raise

        elif self.action == 'DELETE':
            try:
                print 'Action DELETE is used to remove an object (globaltags,tags,systems) from the DB'
                object=self.args[0]
                msg = ('DELETE: selected object is %s ') % (object)
                if object in [ 'globaltags', 'tags', 'systems' ]:
                    print colored.cyan(msg)
                else:
                    msg = ('DELETE: cannot apply command to object %s ') % (object)
                    print colored.red(msg) 
                    return                
                
                id = self.args[1]
                msg = ('DELETE: selected object id is %s ') % (id)
                print colored.cyan(msg)
                
                self.restserver.deleteEntity(id,'/'+object)
            
            except Exception, e:
                sys.exit("DELETE failed: %s" % (str(e)))
                raise

        elif self.action == 'STORE':
            try:
                print 'Action STORE is used to insert a data object [iov+payload] into the DB. '
                print ' - <local file name>: first argument is the local file name'
                print ' - <dest tag name>  : second argument is the destination tag name; the tag should already exists in the DB.'
                print ' - <since>  : third argument is the time of the iov from which the payload is valid.'
                print ' - <sincestr> : forth arg is a stringified version of the since'
                print ' - <column separated meta-data list>  : fifth argument is a list of parameters: version=zzz;objectType=aaa;streamerInfo=bbb.;backendInfo=ccc'
                ifile=self.args[0]
                print 'Selecting local file ', ifile
                tagid = self.args[1]
                print '   use tag id : ', tagid
                since = self.args[2]
                print '   use since : ', since
                sincestr = self.args[3]
                print '   use sincestr : ', sincestr
                params = {}
                if len(self.args) > 4:
                    params = self.args[4]
                argsarr = params.split(';')
                iovdata = {}
                iovdata['since']=since
                iovdata['sinceString']=sincestr
                pylddata = {}
                for anarg in argsarr:
                    key = anarg.split('=')[0]
                    val = anarg.split('=')[1]
                    pylddata[key]=val
                pylddata['since'] = since
                pylddata['sinceString'] = sincestr
                pylddata['file'] = ifile
                pylddata['tag'] = tagid
                self.restserver.addPayload(pylddata,'/iovs/payload')
            
            except Exception, e:
                sys.exit("failed: %s" % (str(e)))
                raise

        elif self.action == 'LOCK':
            try:
                print 'Action LOCK is used to lock a global tag (a locked global tag cannot be unlocked by users)'
                gtagobject=self.args[0]
                print 'Locking global tag',gtagobject
                data = {}
                object = 'globaltags'
                data['lockstatus']='locked'
    
                self.restserver.addJsonEntity(data,'/'+object+'/'+gtagobject)
            
            except Exception, e:
                sys.exit("failed: %s" % (str(e)))
                raise



        elif self.action == 'LINK':
            try:
                print 'Action LINK is used to map a tag into a global tag'
                gtagobject=self.args[0]
                tagobject=self.args[1]
                msg = ('LINK: perform association between %s and %s ') % (gtagobject,tagobject)
                print colored.cyan(msg)
                data = {}
                object = 'maps'
                objparams = None

                if len(self.args) > 2:
                    objparams=self.args[2]
                else:
                    msg = ('LINK: object parameters are missing %s ') % ("record=xxx;label=yyyy")
                    print colored.red(msg)
                    return
                msg = ('LINK: object parameters %s ') % (objparams)
                print colored.cyan(msg)

                data = self.createObjParsingArgs(object,objparams)
                data['globaltagname']=gtagobject
                data['tagname']=tagobject

                (response) = self.restserver.addJsonEntity(data,'/'+object)
                if response is None:
                    print colored.red('Failed in linking the objects: may be link already exists ?')
                else:
                    msg = ('LINK: performed association between %s and %s ') % (gtagobject,tagobject)
                    print colored.green(msg)
            
            except Exception, e:
                sys.exit("failed: %s" % (str(e)))
                raise

        elif self.action == 'UNLINK':
            try:
                print 'Action UNLINK is used to remove maping from a tag to a global tag'
                gtagobject=self.args[0]
                tagobject=self.args[1]
                msg = ('UNLINK: remove association between %s and %s ') % (gtagobject,tagobject)
                print colored.cyan(msg)
                data = {}
                object = 'maps'

                data['globaltag']=gtagobject
                data['tag']=tagobject
                data['expand'] = 'true'
                (entity, response) = self.restserver.getmaps(data)
                mappingidlink = entity['href'] 
                self.restserver.deletelink(mappingidlink)
                msg = ('UNLINK: removed association between %s and %s ') % (gtagobject,tagobject)
                print colored.green(msg)
            
            except Exception, e:
                sys.exit("failed: %s" % (str(e)))
                raise

        elif self.action == 'LINKALL':
            try:
                print 'Action LINKALL is used to map a tag pattern into a global tag'
                gtagobject=self.args[0]
                tagobject=self.args[1]
                action='addtags'
                if len(self.args)>2:
                    action=self.args[2]
                else:
                    print 'can define action as "addtags" or "merge"; in the latter case it will intepret ',tagobject,' as a global tag name '
                print 'Linking tag',tagobject,' into global tag ', gtagobject, ' using action ',action
                data = {}
                object = 'maps'
                objparams = None
                if len(self.args) > 3:
                    objparams=self.args[3]
                    print '   use object parameters: ', objparams
                else:
                    print '   missing arguments: "record=xxx;label=yyyy"'
                    return
                mapparams = self.createObjParsingArgs(object,objparams)

                data['name'] = tagobject
                data['record'] = mapparams['record']
                data['label'] = mapparams['label']
                params['action'] = action
                
                self.restserver.addWithPairs(data,params,'/globaltags/'+object+'/'+gtagobject)

#         elif self.action == 'DESCRIBE':
#             try:
#                 print 'Action LINKALL is used to map a tag pattern into a global tag'
#                 gtagobject=self.args[0]
#                 tagobject=self.args[1]
#                 action='addtags'
#                 if len(self.args)>2:
#                     action=self.args[2]
#                 else:
#                     print 'can define action as "addtags" or "merge"; in the latter case it will intepret ',tagobject,' as a global tag name '
#                 print 'Linking tag',tagobject,' into global tag ', gtagobject, ' using action ',action
#                 data = {}
#                 object = 'maps'
#                 objparams = None
#                 if len(self.args) > 3:
#                     objparams=self.args[3]
#                     print '   use object parameters: ', objparams
#                 else:
#                     print '   missing arguments: "record=xxx;label=yyyy"'
#                     return
#                 mapparams = self.createObjParsingArgs(object,objparams)
# 
#                 data['name'] = tagobject
#                 data['record'] = mapparams['record']
#                 data['label'] = mapparams['label']
#                 params['action'] = action
#                 
#                 self.restserver.addWithPairs(data,params,'/globaltags/'+object+'/'+gtagobject)
            
            except Exception, e:
                sys.exit("failed: %s" % (str(e)))
                raise


        else:
            print "Command not recognized: please type -h for help"


if __name__ == '__main__':
    PhysDBDriver()

