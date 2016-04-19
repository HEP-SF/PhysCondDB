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
import cStringIO
import os.path

#from pygments import highlight, lexers, formatters
from xml.dom import minidom
#from clint.textui import colored
from datetime import datetime

from PhysUrllib2SvcJersey import PhysCurl,PhysUtils,GlobalTag,Tag,Iov,GtagMap,SystemDesc,Payload,PayloadData

class PhysDBDriver():
    def __init__(self):
    # process command line options
        try:
            self.restserver = {}
            self.resttools = {}
            self._command = sys.argv[0]
            self.useSocks = False
            self.t0 = 0
            self.tMax = 'Inf'
            self.debug = False
            self.trace = 'off'
            self.expand = 'false'
            self.by = 'none'
            self.page = 0
            self.pagesize = 1000
            self.iovspan = 'time'
            self.jsondump=False
            self.dump=False
            self.user='none'
            self.passwd='none'
            self.outfilename=''
            self.urlsvc='localhost:8080/physconddb'
            longopts=['help','socks','out=','jsondump','t0=','tMax=','url=','debug','trace=','expand=','by=','page=','pagesize=','iovspan=','user=','pass=']
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
        print "        ex: --trace=on FIND globaltags MYGTAG-01-01 : retrieve one global tag and associated leaf tags"
        print "   remarks: --trace=on will fail in case multiple object (id=xx%) are retrieved"
        print " "
        print " - ADD <type> [tags, globaltags, systems] <parameters string> [column separated key=val list]"
        print "        ex: globaltags 'name=MYGTAG-01-01;description=A global tag; xxxx ' : create a global tag using the provided parameter list"
        print "        ex: tags 'name=MyTag-01-01;description=A test tag; xxxx ' : create a tag using the provided parameter list"
        print "        To dump the needed parameter list you can use the command DESCRIBE"
        print "        Constraints applied to tag name: ^([A-Z]+[a-zA-Z0-9-_]+)_([A-Za-z0-9-]+)_([0-9])++$"
        print "        Constraints applied to globaltag name: ^([A-Z]+[A-Za-z0-9]+)-([A-Z0-9]+)-([0-9])++$"
        print " "
        print " - STORE <tag> <filename> <iov parameters string> <payload parameters string> [column separated key=val list]"
        print "        ex: mytag01 local.file 'since=1000;sinceString=t1000; xxxx ' 'streamerInfo=an info; objectType=the object type; version=1.0': create an iov inside a tag using the provided parameter list and input file"
        print " "
        print " - LS <tag> <globaltag> :"
        print "        ex: mytag01 MYGTAG-01-01 : list iovs in tag/globaltag combination; use t0 and tMax for time boundaries"
        print " "
        print " - LINK <globaltagname> <tagname> 'record=xxx;label=yyy'"
        print "        ex: MYGTAG-01-01 TAG_01 record=none;label=none : link a global tag to a tag, both should exists."
        print " "
        print " - UNLINK <globaltagname> <tagname> "
        print "        ex: MYGTAG-01-01 TAG_01 : remove link from global tag to a tag."
        print " "
        print " - DELETE <type> <id> [tags, globaltags, systems]"
        print "        ex: globaltags MYGTAG-01-01 : remove the global tag MYGTAG."
        print " "
        print " - DESCRIBE <type>  [tags, globaltags, systems, iovs, payload]"
        print "        ex: globaltags : describe the content of the globaltag object."
        print " "
        print "Options: "
        print "  --socks activate socks proxy on localhost 3129 "
        print "  --debug activate debugging output "
        print "  --out={filename} activate dump on filename "
        print "  --jsondump activate a dump of output lines in json format "
        print "  --trace [on|off]: trace associations (ex: globaltag ->* tags or tag ->* iovs "
        print "  --expand [true|false]: expand result to complete obj, not only urls "
        print "  --by : comma separated list of conditions for filtering a query (e.g.: by=name:pippo,value<10)"
        print "  --page [0,...N]: page number to retrieve; use it in combination with page size, default is 0"
        print "  --pagesize [1000,30,...]: page size; use it in combination with page, default is 1000"
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
        elif type == 'payload':
            return Payload(data)
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
        elif type == 'payload':
            mobj = Payload({})

        print 'Help defined for ',type,' : ',mobj.help()
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
            if (o=='--by'):
                self.by=a
            if (o=='--page'):
                self.page=a
            if (o=='--pagesize'):
                self.pagesize=a
            if (o=='--pass'):
                self.passwd=a
            if (o=='--iovspan'):
                self.iovspan=a
                
        if (len(args)<2):
            raise getopt.GetoptError("Insufficient arguments - need at least 3, or try --help")
        self.action=args[0].upper()
        self.args=args[1:]
        
    def printmsg(self,msg,color):
        try:
          from clint.textui import colored
          if color == 'cyan':
          	print colored.cyan(msg)
          elif color == 'blue':
            print colored.blue(msg)
          elif color == 'red':
            print colored.red(msg)
            
        except:
          print msg

    def execute(self):
        msg = ('Execute the command for action %s and arguments : %s ' ) % (self.action, str(self.args))
        self.printmsg(msg,'cyan')
            
        start = datetime.now()
        self.restserver = PhysCurl(self.urlsvc, self.useSocks)
        self.resttools = PhysUtils(self.restserver)
        
        if self.debug:
            self.restserver.setdebug(True)
        
        if self.dump:
            outfile = open(self.outfilename,"w")
        _dict = {}
        params = {}
        
        
        if (self.action=='FIND'):
            try:
                print 'Action FIND is used to retrieve object from the DB'
                print 'Found N arguments ',len(self.args)
                object=self.args[0]
                msg = ('FIND: selected object is %s ') % (object)
                if object in [ 'globaltags', 'tags', 'systems' ]:
                    self.printmsg(msg,'cyan')
                else:
                    msg = ('FIND: cannot apply command to object %s ') % (object)
                    self.printmsg(msg,'red')
                    return -1        
                
                # load arguments and prepare the data structure for the GET request
                # optional parameters like trace and expand are added
                name = None
                if len(self.args) > 1:
                    name=self.args[1]
                    msg = ('FIND: selected id is %s ') % (name)
                    self.printmsg(msg,'cyan')
                        
                    if '%' in name:
                        print 'Cannot use pattern in this argument; if you want to filter a list, use option by=param[:<>]value,param1[:<>]value,...'
                        return -1
                
                    
                objList = []
                traceList = []
                msg = ('FIND: load data using trace %s, expand %s, by %s ') % (self.trace,self.expand,self.by)
                self.printmsg(msg,'cyan')


                if name is None:
                    print 'Query a list of objects....'
                    if objList is None or len(objList) == 0:
                        (objList,response) = self.resttools.getobjectlist(self.by,self.expand,self.page,self.pagesize,object)

                else:
                    print 'Query single object....'
                    (obj,response) = self.resttools.getobject(name,self.trace,self.expand,object)

                    if self.debug:
                        msg = ('FIND: retrieved object from database %s ') % (obj)
                        self.printmsg(msg,'cyan')
                        
                    if obj is None:
                        msg = ('FIND: error, cannot find any object in database for type %s ') % (object)
                        self.printmsg(msg,'red')

                    
                    objList.append(obj)
                    # If trace is active, perform a special dump for the trace
                    if object in [ 'globaltags', 'tags' ]:
                        if self.debug:
                            msg = ('FIND: create object of type %s from %s ') % (object,obj)
                            self.printmsg(msg,'cyan')

                        mpobj = self.createObj(object,obj)
                        if self.debug:
                            msg = ('FIND: created object %s ') % (mpobj)
                            self.printmsg(msg,'cyan')
                    
                        globaltagmaps = None
                        if 'globalTagMaps' in mpobj.getValues():
                            globaltagmaps = mpobj.getValues()['globalTagMaps']
                        if globaltagmaps is not None:
                            try:
                                maplist = globaltagmaps
###maplist = globaltagmaps['items']
                                if hasattr(maplist, '__iter__'):
                                    for amap in maplist:
                                        if object == 'globaltags':
                                        	globaltagname = mpobj.getValues()['name']
                                        	tagname = amap['systemTag']['name']
                                        else:
                                        	tagname = mpobj.getValues()['name']
                                        	globaltagname = amap['globalTag']['name']
                                            
                                        traceList.append(('GlobalTag %s => Tag %s') % (globaltagname,tagname))
                            except Exception, e:
                                sys.exit("failed looping over items: %s" % (str(e)))
                                raise e
                                
# Now dump the retrieved content
                if response != 200 and response != 201:
                    msg = ('FIND: error in data retrieval %s ') % (response)
                    self.printmsg(msg,'red')

                    return
                json_string = json.dumps(objList,sort_keys=True,indent=4, separators=(',', ': '))
                colorful_json = json_string
                try:
                    from pygments import highlight, lexers, formatters
                    colorful_json = highlight(unicode(json_string, 'UTF-8'), lexers.JsonLexer(), formatters.TerminalFormatter())
                except:
                    print 'Cannot use colored messages'
                
                #colorful_json = highlight(unicode(json_string, 'UTF-8'), lexers.JsonLexer(), formatters.TerminalFormatter())
                print colorful_json
                
                if len(traceList) > 0:
                    msg = ('FIND: found list of globaltags to tags associations')
                    self.printmsg(msg,'cyan')

                    for amsg in traceList:
                        self.printmsg(msg,'green') 
            
            except Exception, e:
                sys.exit("failed on action FIND: %s" % (str(e)))
                raise

        elif self.action == 'LS':
            try:
                print 'Action LS is used to iovs in a tag / globaltag pair; if globaltag info is missing do not use snapshottime'
                tag=self.args[0]
                globaltag = 'none'
                if len(self.args) == 2:
                    globaltag=self.args[1]
                msg = ('LS: use tag %s and global tag  %s !') % (tag,globaltag)
                self.printmsg(msg,'cyan')
      
                (objList, code)=self.resttools.gettagiovs(tag,globaltag,self.trace,self.expand,self.t0,self.tMax)
                if code > 220:
                	raise Exception('Wrong return code from server ',code)
                json_string = json.dumps(objList,sort_keys=True,indent=4, separators=(',', ': '))
                colorful_json = json_string
                try:
                    from pygments import highlight, lexers, formatters
                    colorful_json = highlight(unicode(json_string, 'UTF-8'), lexers.JsonLexer(), formatters.TerminalFormatter())
                except:
                    print 'Cannot use colored messages'
                print colorful_json
                
            except Exception, e:
                sys.exit("LS failed: %s" % (str(e)))
                raise e
            
        elif self.action == 'LOCK':
            try:
                calibargs=self.args
                msg = '>>> Call method %s using arguments %s ' % (self.action,calibargs)
                self.printmsg(msg,'cyan')
  
                if len(calibargs) < 2:
                    print 'Set default option for lockstatus to LOCKED (type -h for help)'
                    calibargs.append('LOCKED')
                self.resttools.lockit(calibargs[0],calibargs[1])
                    
            except Exception, e:
                sys.exit("failed: %s" % (str(e)))
                raise
            
        elif self.action == 'ADD':
            try:
                print 'Action ADD is used to insert a metadata object (globaltags,tags,maps,...) into the DB'
                object=self.args[0]
                msg = ('ADD: selected object is %s ') % (object)
                if object in [ 'globaltags', 'tags', 'systems' ]:
                    self.printmsg(msg,'cyan')
  
                else:
                    msg = ('ADD: cannot apply command to object %s ') % (object)
                    self.printmsg(msg,'red')
                    msg = ('ADD: to insert an IOV + Payload use STORE, see --help')
                    self.printmsg(msg,'cyan')
                    
                    return

                objparams = None
                if len(self.args) > 1:
                    objparams=self.args[1]
                    
                msg = ('ADD: object parameters %s ') % (objparams)
                self.printmsg(msg,'cyan')
                  
                data = {}
                if objparams is None:
                    msg = self.helpAdd(object)
                    self.printmsg(msg,'cyan') 
                    return
                # Parameters have been provided in command line, try to create the json entity
                data = self.createObjParsingArgs(object,objparams)
                print json.dumps(data)
        
                response = self.resttools.addObject(object,data)
                print 'Object ',object,' added to db and response is ',response
        		
            except Exception, e:
                sys.exit("ADD failed: %s" % (str(e)))
                raise

        elif self.action == 'STORE':
            try:
                print 'Action STORE is used to insert an iov object + its payload associated to a tag into the DB'
                print ' - <dest tag name>  : destination tag name; the tag should already exists in the DB.'
                print ' - <local file name>: local file name'
                print ' - <iov params>  : column separated list of parameters for iov [since=xxx;sinceString=yyy]'
                print ' - <payload params>  : column separated list of parameters for payload [version=zzz;objectType=aaa;streamerInfo=bbb.;backendInfo=ccc]'

                tag=self.args[0]
                msg = ('STORE: selected object is %s ') % (tag)
                (obj,response) = self.resttools.getTag(tag,"off","true")
                
                if self.debug:
                    msg = ('STORE: retrieved object from database %s ') % (obj)
                    self.printmsg(msg,'cyan') 
                      
                if obj is None:
                    msg = ('STORE: error, cannot find any tag in database for name %s ') % (tag)
                    self.printmsg(msg,'red')         
                
                objparams = None
                if len(self.args) != 4:
                    msg = ('STORE: error, cannot find enough parameters for completing the request')
                    self.printmsg(msg,'red')          
                    
                filename=self.args[1]
                iovobjparams=self.args[2]
                pyldobjparams=self.args[3]

                    
                msg = ('STORE: iov parameters %s and filename %s (%s)') % (iovobjparams, filename,pyldobjparams)
                self.printmsg(msg,'cyan')          
            
                data = {}
                if iovobjparams is None:
                    msg = self.helpAdd("iovs")
                    self.printmsg(msg,'cyan')            
                    return
                
                iovdata = {}
                iovdata = self.createObjParsingArgs("iovs",iovobjparams)
                pylddata = {}
                pylddata = self.createObjParsingArgs("payload",pyldobjparams)
##                print 'created payload data ',pylddata
                params = {}
                params['file'] = filename
                params['tag'] = tag
                params['since'] = iovdata['since']
                params['sinceString'] = iovdata['sinceString']
                params['backendInfo'] = pylddata['backendInfo']
                params['objectType'] = pylddata['objectType']
                params['streamerInfo'] = pylddata['streamerInfo']
                params['version'] = pylddata['version']

                self.resttools.storePayload(params)
        		
            except Exception, e:
                sys.exit("STORE failed: %s" % (str(e)))
                raise

        elif self.action == 'DESCRIBE':
            try:
                print 'Action DESCRIBE is used to list the keys needed for filling the object ',self.args[0]
                object=self.args[0]
                msg = ('DESCRIBE: selected object is %s ') % (object)
                if object in [ 'globaltags', 'tags', 'systems', 'iovs', 'payload' ]:
                    msg = self.helpAdd(object);
                    self.printmsg(msg,'green')
                else:
                    msg = ('DESCRIBE: cannot apply command to object %s ') % (object)
                    self.printmsg(msg,'red')
                    return                
            
            except Exception, e:
                sys.exit("DESCRIBE failed: %s" % (str(e)))
                raise

        elif self.action == 'DELETE':
            try:
                print 'Action DELETE is used to remove an object (globaltags,tags,systems) from the DB'
                object=self.args[0]
                msg = ('DELETE: selected object is %s ') % (object)
                if object in [ 'globaltags', 'tags', 'systems' ]:
                    self.printmsg(msg,'green')
                else:
                    msg = ('DELETE: cannot apply command to object %s ') % (object)
                    self.printmsg(msg,'red')
                    return                
                
                id = self.args[1]
                msg = ('DELETE: selected object id is %s ') % (id)
                self.printmsg(msg,'cyan')
                self.resttools.deleteObject(object,id)
            
            except Exception, e:
                sys.exit("DELETE failed: %s" % (str(e)))
                raise

        elif self.action == 'LINK':
            try:
                print 'Action LINK is used to map a tag into a global tag'
                gtagobject=self.args[0]
                tagobject=self.args[1]
                msg = ('LINK: perform association between %s and %s ') % (gtagobject,tagobject)
                self.printmsg(msg,'cyan')
                data = {}
                object = 'maps'
                objparams = None

                if len(self.args) > 2:
                    objparams=self.args[2]
                else:
                    msg = ('LINK: object parameters are missing %s ') % ("record=xxx;label=yyyy")
                    self.printmsg(msg,'red')
                    return

                msg = ('LINK: object parameters %s ') % (objparams)
                self.printmsg(msg,'cyan')

                data = self.createObjParsingArgs(object,objparams)
                (response) = self.resttools.link(gtagobject,tagobject,data)
                
                if response is None:
                    msg = ('Failed in linking the objects: unknown response, server not reached?')
                    self.printmsg(msg,'red')
                elif response['code']>220:
                    msg = ('Failed in linking the objects (HTTP code %d): %s ') % (response['code'],response['internalMessage'])
                    self.printmsg(msg,'red')
                else:
                    msg = ('LINK: performed association between %s and %s ') % (gtagobject,tagobject)
                    self.printmsg(msg,'green')
            
            except Exception, e:
                sys.exit("failed: %s" % (str(e)))
                raise

        elif self.action == 'UNLINK':
            try:
                print 'Action UNLINK is used to remove mapping from a tag to a global tag'
                gtagobject=self.args[0]
                tagobject=self.args[1]
                msg = ('UNLINK: remove association between %s and %s ') % (gtagobject,tagobject)
                self.printmsg(msg,'green')
                    
                self.resttools.unlink(gtagobject,tagobject)
                msg = ('UNLINK: removed association between %s and %s ') % (gtagobject,tagobject)
                self.printmsg(msg,'green')
            
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
                self.resttools.linkall(tagobject, action, gtagobject, mapparams['record'],mapparams['label'])
            
            except Exception, e:
                sys.exit("failed: %s" % (str(e)))
                raise


        else:
            print "Command not recognized: please type -h for help"


        tend=datetime.now()
        print 'Time spent (ms): ',tend-start
        
if __name__ == '__main__':
    PhysDBDriver()

