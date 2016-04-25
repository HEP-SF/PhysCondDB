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

from swagger_client.apis import GlobaltagsApi, TagsApi, IovsApi, SystemsApi, ExpertApi, MapsApi
from swagger_client.models import GlobalTag, Tag, GlobalTagMap, SystemDescription
from swagger_client import ApiClient

class PhysDBDriver():
    def __init__(self):
    # process command line options
        try:
            self._command = sys.argv[0]
            self.useSocks = False
            self.t0 = 0
            self.tMax = 'Inf'
            self.debug = False
            self.trace = 'on'
            self.expand = 'true'
            self.by = 'none'
            self.page = 0
            self.pagesize = 1000
            self.iovspan = 'time'
            self.jsondump=False
            self.dump=False
            self.user='none'
            self.passwd='none'
            self.outfilename=''
            self.api_client=None
            self.urlsvc='http://localhost:8080/physconddb/api/rest'
            longopts=['help','socks','out=','jsondump','t0=','tMax=','url=','debug','trace=','expand=','by=','page=','pagesize=','iovspan=','user=','pass=']
            opts,args=getopt.getopt(sys.argv[1:],'',longopts)
            print opts, args
            self.procopts(opts,args)
        except getopt.GetoptError,e:
            print e
            self.usage()
            sys.exit(-1)
            
        self.api_client = ApiClient(host=self.urlsvc)
        self.execute()

    def usage(self):
        print
        print "usage: phcli.py {<options>} <action> {<args>}"
        print "Search conditions DB content using args"
        print "Server url is needed in order to provide the system with the base url; defaults to localhost (see below)"
        print "Action determines which rest method to call"
        print "Actions list:"
        print " - FIND <type> [id] [type = tags, globaltags, systems] : retrieve list of resources of the selected type, use option <by> to filter it"
        print "        ex: FIND globaltags : retrieve full list of global tags"
        print "        ex: --by=name:BLKPA globaltags : retrieve list of global tags where name contains BLKPA"
        print "        ex: --expand=true --by=name:BLKPA FIND globaltags : retrieve list of global tags where name contains BLKPA and show all fields, not only links"
        print "        ex: --trace=on FIND globaltags MYGTAG-01-01 : retrieve one global tag and associated leaf tags"
        print "   remarks: --trace=on will not work when a list of items is retrieved"
        print " "
        print " - ADD <type> <column separated list of arguments> [type = tags, globaltags, systems] : add a new resource of the selected type."
        print "        ex: ADD globaltags 'name=MYGTAG-01-01;description=A global tag;release=some release;validity=0' : create a global tag using the provided parameter list"
        print "        ex: ADD tags 'name=MyTag-01-01;description=A test tag; ... ' : create a tag using the provided parameter list"
        print " "
        print "        WARN:Constraints applied to tag name: ^([A-Z]+[a-zA-Z0-9-_]+)_([A-Za-z0-9-]+)_([0-9])++$"
        print "        WARN:Constraints applied to globaltag name: ^([A-Z]+[A-Za-z0-9]+)-([A-Z0-9]+)-([0-9])++$"
        print " "
        print " - UPD <type> <column separated list of arguments> [type = tags, globaltags, systems] : update an existing resource of the selected type."
        print "        ex: globaltags 'name=MYGTAG-01-01;description=A global tag;release=some release;validity=0' : create a global tag using the provided parameter list"
        print "        ex: tags 'name=MyTag-01-01;description=A test tag; ... ' : create a tag using the provided parameter list"
        print " "
        print " - LOCK <global tag name> <lock status> [LOCKED|UNLOCKED] : lock a global tag, default lock status is LOCKED."
        print "        ex: LOCK MYGTAG-01-01 LOCKED : Lock a global tag."
        print " "
        print " "
        print " - LINK <global tag name> <tag name> <record=xxx;label=xxx> : link a global tag to a tag."
        print "        ex: LINK MYGTAG-01-01 TAG-01-01 'record=a record;label=a label' : Link a global tag."
        print " "
        print " - DESCRIBE <object> [globaltag | tag | system | map ] : describe fields in the given object type."
        print " "
        print "Options: "
        print "  --socks activate socks proxy on localhost 3129 "
        print "  --debug activate debugging output "
        print "  --out={filename} activate dump on filename "
        print "  --jsondump activate a dump of output lines in json format "
        print "  --trace [on|off]: trace associations (ex: globaltag ->* tags or tag ->* globaltags. DEFAULT=",self.trace
        print "  --by : comma separated list of conditions for filtering a query (e.g.: by=name:pippo,value<10). DEFAULT=",self.by
        print "  --page [0,...N]: page number to retrieve; use it in combination with page size. DEFAULT=",self.page
        print "  --pagesize [1000,30,...]: page size; use it in combination with page. DEFAULT=",self.pagesize
        print "  --expand [true|false]: expand result to complete obj, not only urls. DEFAULT=",self.expand
        print "  --url [localhost:8080/physconddb]: use a specific server "
        print "  --t0={t0 for iovs}. DEFAULT=",self.t0
        print "  --tMax={tMax for iovs}. DEFAULT=",self.tMax
        print "  --iovspan={time|date|runlb|timerun|daterun}. DEFAULT=",self.iovspan
        print "         time: iov in COOL format, allows Inf for infinity"
        print "         date: iov in yyyyMMddHHmmss format"
        print "         runlb: iov in run-lb format, only for run based folders "
        print "         the others make the conversion to run number, does not allow Inf for infinity "
        print "Examples: "

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

    def getsystems(self,by):
        sysapis = SystemsApi(self.api_client)
        coll = sysapis.list_systems(by,page=self.page,size=self.pagesize)
        syslist = coll.items
        self.dumpmodellist(syslist,True,['schema_name','node_fullpath','node_description','tag_name_root'])
        print 'List of retrieved systems : ',len(syslist)

    def fetchsystem(self,name):
        sysapis = SystemsApi(self.api_client)
        system = sysapis.find_system(name,expand=self.expand,trace=self.trace)
        self.dumpmodelobject(system,True,['schema_name','node_fullpath','node_description','tag_name_root'])

    def getglobaltags(self,by):
        gtapis = GlobaltagsApi(self.api_client)
        coll = gtapis.list_global_tags(by,expand=self.expand,page=self.page,size=self.pagesize)
        gtlist = coll.items
        self.dumpmodellist(gtlist,True,['name','validity','release','snapshot_time','description','lockstatus'])
        print 'List of retrieved global tags : ',len(gtlist)

    def createobject(self,objparams,objtype):
        params = {}
        args = objparams.split(";")
        for _arg in args:
            print 'Filling argument ',_arg
            (key, value) = _arg.split("=")
            params[key] = value
        print 'created parameter list: ',params
        resp = RESTResponse()
        resp.data = json.dumps(params)
        instance = {}
        instance = self.api_client.deserialize(resp, objtype)
        print 'created model object : ',instance, ' for type ',objtype
        return instance

    def helpmodel(self,obj,keylist=[]):
        attrs = obj.attribute_map
        msg = ''
        for key in keylist:
            msg = ('%s%s=...;') % (msg,attrs[key])
        self.printmsg(msg[:-1],'cyan')
        
    def dumpmodellist(self,objlist,withheader=False,keylist=[]):
        i=0
        for obj in objlist:
            i=(i+1)
            if i == 1:
                self.dumpmodelobject(obj,True,keylist)
            self.dumpmodelobject(obj,False,keylist)

    def dumpmodelobject(self,obj,withheader=False,keylist=[]):
        objdict = obj.to_dict()
        headermsg = ''
        msg = ''
        if len(keylist)>0:
            for key in keylist:
                if isinstance(objdict[key],list):
                    print 'skip list...'
                elif key in ['href','res_id']:
                    print 'skip fields'
                else:
                    headermsg = ('%s | %15s') % (headermsg,key)
                    msg = ('%s | %s') % (msg,objdict[key])

        else:
            for key in objdict:
                if isinstance(objdict[key],list):
                    print 'skip list...'
                elif key in ['href','res_id']:
                    print 'skip fields'
                else:
                    headermsg = ('%s | %15s') % (headermsg,key)
                    msg = ('%s | %s') % (msg,objdict[key])

        if withheader is True:
            self.printmsg(headermsg,'blue')
        self.printmsg(msg,'cyan')


    def fetchglobaltag(self,name):
        gtapis = GlobaltagsApi(self.api_client)
        gtag = gtapis.find_global_tag(name,expand=self.expand,trace=self.trace)
        coll = gtag.global_tag_maps
        if coll is None:
            print 'associated tags not found: may be trace is disabled ? (--trace=on)'
            return
        for map in coll:
            msg = ('%s | %s | %s | %s ')%(map.system_tag.name, map.system_tag.time_type, map.record, map.label)
            self.printmsg(msg,'cyan')
        print 'Selected global tag is: '
        self.dumpmodelobject(gtag,True,['name','snapshot_time','validity','description','lockstatus'])
        print 'List of associated tag : ',len(coll)

    def gettags(self,by):
        tapis = TagsApi(self.api_client)
        coll = tapis.list_tags(by,expand=self.expand,page=self.page,size=self.pagesize)
        tlist = coll.items
        self.dumpmodellist(tlist,True,['name','time_type','object_type','description','last_validated_time'])
        print 'List of retrieved tags : ',len(tlist)

    def fetchtag(self,name):
        tapis = TagsApi(self.api_client)
        tag = tapis.find_tag(name,expand=self.expand,trace=self.trace)
        print tag.name,tag.time_type,tag.object_type,tag.description,tag.last_validated_time
        coll = tag.global_tag_maps
        if coll is None:
            print 'associated global tags not found: may be trace is disabled ? (--trace=on)'
        for map in coll:
            msg = ('%s | %s | %s | %s ')%(map.global_tag.name, map.global_tag.description, map.record, map.label)
            self.printmsg(msg,'cyan')

    def execute(self):
        msg = ('Execute the command for action %s and arguments : %s ' ) % (self.action, str(self.args))
        self.printmsg(msg,'cyan')
            
        start = datetime.now()
        
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
      
                    if object == 'globaltags':
                        self.fetchglobaltag(name)
                    elif object == 'tags':
                        self.fetchtag(name)
                    elif object == 'systems':
                        self.fetchsystem(name)
                else:
                    if object == 'globaltags':
                        self.getglobaltags(self.by)
                    elif object == 'tags':
                        self.gettags(self.by)
                    elif object == 'systems':
                        self.getsystems(self.by)

            except Exception, e:
                sys.exit("failed on action FIND: %s" % (str(e)))
                raise

        elif (self.action=='ADD'):
            try:
                print 'Action ADD is used to insert a metadata object (globaltags,tags,systems,...) into the DB'
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
                    msg = 'Cannot create object without a list of parameters, which should be separated by ;'
                    self.printmsg(msg,'cyan')
                    return
        # Parameters have been provided in command line, try to create the json entity
                expapi = ExpertApi(self.api_client)
                if object == 'globaltags':
                    objparams = ('%s;lockstatus=UNLOCKED') % (objparams)
                    gtag = self.createobject(objparams,'GlobalTag')
                    gtagcreated = expapi.create_global_tag(body=gtag)
                    print 'Object ',object,' added to db and response is :'
                    self.dumpmodelobject(gtagcreated,True,['name','snapshot_time','validity','description','lockstatus'])

                elif object == 'tags':
                    tag = self.createobject(objparams,'Tag')
                    tagcreated = expapi.create_tag(body=tag)
                    print 'Object ',object,' added to db and response is :'
                    self.dumpmodelobject(tagcreated,True,['name','time_type','object_type','description','last_validated_time'])

                elif object == 'systems':
                    system = self.createobject(objparams,'SystemDescription')
                    syscreated = expapi.create_system_description(body=system)
                    print 'Object ',object,' added to db and response is :'
                    self.dumpmodelobject(syscreated,True,['schema_name','node_fullpath','node_description','tag_name_root'])

                else:
                    print 'Cannot create object of type ',object

            except Exception, e:
                sys.exit("ADD failed: %s" % (str(e)))
                raise

        elif self.action == 'LS':
            try:
                print 'Action LS is used to retrieve iovs in a tag: an optional argument can be added, indicating the snapshot time for the IOVs.'
                tag=self.args[0]
                snapt = 'none'
                if len(self.args) == 2:
                    snapt=self.args[1]
                msg = ('LS: use tag %s and snapshot time %s !') % (tag,snapt)
                self.printmsg(msg,'cyan')
      
                iovapi = IovsApi()
                iovlist = iovapi.get_iovs_in_tag(tag,expand=self.expand,since=self.t0,until=self.tMax)
                print 'Retrieved iov list: ',iovlist
                
            except Exception, e:
                sys.exit("LS failed: %s" % (str(e)))
                raise e
                
        elif self.action == 'LOCK':
            try:
                print 'Action LOCK is used to lock or unlock a global tag'
                lockargs=self.args
                msg = '>>> Call method %s using arguments %s ' % (self.action,lockargs)
                self.printmsg(msg,'cyan')
                
                if len(lockargs) < 2:
                    print 'Set default option for lockstatus to LOCKED (type -h for help)'
                    lockargs.append('LOCKED')
                
                expapi = ExpertApi(self.api_client)
                objparams = ('name=%s;lockstatus=%s') % (lockargs[0],lockargs[1])
                gtag = self.createobject(objparams,'GlobalTag')
                gtagcreated = expapi.update_global_tag(gtag.name,body=gtag)
                print 'GlobalTag ',lockargs[0],' lockstatus modified and response is ',gtagcreated
                self.dumpmodelobject(gtagcreated,True,['name','lockstatus','validity','description','release'])
            
            except Exception, e:
                sys.exit("failed: %s" % (str(e)))
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
                
                expapi = ExpertApi(self.api_client)
                
                id = self.args[1]
                print 'Removing element ',id
                if object == 'globaltags':
                    gtagremoved = expapi.delete_global_tag(id)
                    print 'Object ',object,' removed and response is '
                    self.dumpmodelobject(gtagremoved,True,['name','snapshot_time','validity','description','lockstatus'])

                elif object == 'tags':
                    tagremoved = expapi.delete_tag(id)
                    print 'Object ',object,' removed and response is '
                    self.dumpmodelobject(tagremoved,True,['name','time_type','object_type','description','last_validated_time'])

                elif object == 'systems':
                    sysremoved = expapi.delete_system_description(id)
                    print 'Object ',object,' removed and response is '
                    self.dumpmodelobject(sysremoved,True,['schema_name','node_fullpath','node_description','tag_name_root'])

                else:
                    print 'Cannot remove object of type ',object
                
                    
            except Exception, e:
                sys.exit("DELETE failed: %s" % (str(e)))
                raise

        elif self.action == 'LINK':
            try:
                print 'Action LINK is used to link a global tag to a tag'
                gtagobject=self.args[0]
                tagobject=self.args[1]
                msg = ('LINK: perform association between %s and %s ') % (gtagobject,tagobject)
                self.printmsg(msg,'cyan')
                
                objparams = '';
                if len(self.args)>2: 
                    objparams = ('globalTagName=%s;tagName=%s;%s') % (gtagobject,tagobject,self.args[2])
                else:
                    objparams = ('globalTagName=%s;tagName=%s') % (gtagobject,tagobject)
                expapi = ExpertApi(self.api_client)
                gtagmap = self.createobject(objparams,'GlobalTagMap')
                gtagmapcreated = expapi.create_global_tag_map(body=gtagmap)
                print 'Object globaltagmap created and response is ',gtagmapcreated
                    
            except Exception, e:
                sys.exit("LINK failed: %s" % (str(e)))
                raise

        elif self.action == 'UNLINK':
            try:
                print 'Action UNLINK is used to remove link from a global tag to a tag'
                gtagobject=self.args[0]
                tagobject=self.args[1]
                msg = ('UNLINK: perform association removal between %s and %s ') % (gtagobject,tagobject)
                self.printmsg(msg,'cyan')
                
                objparams = ('globalTag_name:%s,systemTag_name:%s') % (gtagobject,tagobject);
                mapapi = MapsApi(self.api_client)
                print 'use arguments ',objparams
                gtagmaplist = mapapi.list_global_tag_maps('record:',objparams,expand='true')
                for item in gtagmaplist.items:
                    expapi = ExpertApi()
                    print 'Removing ID = ',item.id
                    id = item.id
                    gtagmapremoved = expapi.delete_global_tag_map(id)
                    
            except Exception, e:
                sys.exit("UNLINK failed: %s" % (str(e)))
                raise

        elif (self.action=='UPD'):
            try:
                print 'Action UPD is used to update a metadata object (globaltags,tags,systems,...) into the DB'
                object=self.args[0]
                msg = ('UPD: selected object is %s ') % (object)
                if object in [ 'globaltags', 'tags', 'systems' ]:
                    self.printmsg(msg,'cyan')
                else:
                    msg = ('UPD: cannot apply command to object %s ') % (object)
                    self.printmsg(msg,'red')
                    return
                
                objparams = None
                if len(self.args) > 1:
                    objparams=self.args[1]
            
                msg = ('UPD: object parameters %s ') % (objparams)
                self.printmsg(msg,'cyan')
                expapi = ExpertApi(self.api_client)

                data = {}
                if objparams is None:
                    msg = 'Cannot create object without a list of parameters, which should be separated by ;'
                    self.printmsg(msg,'cyan')
                    return
            # Parameters have been provided in command line, try to create the json entity
                if object == 'globaltags':
                    gtag = self.createobject(objparams,'GlobalTag')
                    gtagcreated = expapi.update_global_tag(gtag.name,body=gtag)
                    print 'Object ',object,' updated and response is '
                    self.dumpmodelobject(gtagcreated,True,['name','snapshot_time','validity','description','lockstatus'])
                elif object == 'tags':
                    tag = self.createobject(objparams,'Tag')
                    tagcreated = expapi.update_tag(tag.name,body=tag)
                    print 'Object ',object,' updated to db and response is '
                    self.dumpmodelobject(tagcreated,True,['name','time_type','object_type','description','last_validated_time'])
                elif object == 'systems':
                    #system = self.createobject(objparams,'SystemDescription')
                    #syscreated = expapi.update_system_description(body=system)
                    print 'Object ',object,' does not have update method implemented yet'
                else:
                    print 'Cannot update object of type ',object

            except Exception, e:
                sys.exit("UPD failed: %s" % (str(e)))
                raise
        elif self.action == 'DESCRIBE':
            try:
                print 'Action DESCRIBE is used to get help on fields for the given object type'
                objecttype=self.args[0]
                msg = ('DESCRIBE: get help on %s') % (objecttype)
                self.printmsg(msg,'cyan')
                
                if objecttype == 'globaltag':
                    gtag = GlobalTag()
                    self.helpmodel(gtag,['name','validity','description','release'])
                elif objecttype == 'tag':
                    tag = Tag()
                    self.helpmodel(tag,['name','time_type','object_type','description','last_validated_time','synchronization','end_of_validity'])
                elif objecttype == 'system':
                    system = SystemDescription()
                    self.helpmodel(system,['schema_name','tag_name_root','node_fullpath','node_description'])
                elif objecttype == 'map':
                    maps = GlobalTagMap()
                    self.helpmodel(maps,['record','label'])
                else:
                    print 'Missing help on ',objecttype
                
                    
            except Exception, e:
                sys.exit("DESCRIBE failed: %s" % (str(e)))
                raise
        else:
            print "Command not recognized: please type -h for help"


        tend=datetime.now()
        print 'Time spent (ms): ',tend-start


class RESTResponse(object):
    def __init__(self):
        print 'create response object'
        self.__data = None

    def data(self):
        return self.__data

    def data(self, data):
        self.__data = data
        print 'create data field ',self.__data
# process command line options


if __name__ == '__main__':
    PhysDBDriver()

