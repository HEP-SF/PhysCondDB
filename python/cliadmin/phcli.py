#!/usr/bin/env python
# encoding: utf-8
'''
cliadmin.phcli -- shortdesc
    General conditions DB management CLI.
cliadmin.phcli is a description

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

from swagger_client.apis import GlobaltagsApi, TagsApi, IovsApi, SystemsApi, ExpertApi, MapsApi, PayloadApi
from swagger_client.models import GlobalTag, Tag, GlobalTagMap, SystemDescription
from swagger_client import ApiClient
from physdbutils import PhysDbUtils

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
            self.phtools=None
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
        self.phtools = PhysDbUtils(host=self.urlsvc,expand=self.expand,trace=self.trace,debug=self.debug,page=self.page,pagesize=self.pagesize)
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
        print " - STORE <tag> <filename> <iov parameters string> <payload parameters string> [column separated key=val list]"
        print "        ex: mytag01 local.file 'since=1000;sinceString=t1000; xxxx ' 'streamerInfo=an info; objectType=the object type; version=1.0': create an iov inside a tag using the provided parameter list and input file"
        print " "
        print " - LS <tag name> : list iovs in a tag."
        print "        ex: MYTAG-00-01  : list the iovs"
        print " "
        print " - GET <hash> : download blob on disk."
        print " "
        print " - LOCK <global tag name> <lock status> [LOCKED|UNLOCKED] : lock a global tag, default lock status is LOCKED."
        print "        ex: LOCK MYGTAG-01-01 LOCKED : Lock a global tag."
        print " "
        print " "
        print " - LINK <global tag name> <tag name> <record=xxx;label=xxx> : link a global tag to a tag."
        print "        ex: LINK MYGTAG-01-01 TAG-01-01 'record=a record;label=a label' : Link a global tag."
        print " "
        print " - UNLINK <global tag name> <tag name> : unlink a global tag from a tag."
        print "        ex: UNLINK MYGTAG-01-01 TAG-01-01 : unlink a global tag."
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

    def execute(self):
        msg = ('Execute the command for action %s and arguments : %s ' ) % (self.action, str(self.args))
        self.phtools.printmsg(msg,'cyan')
            
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
                    self.phtools.printmsg(msg,'cyan')
                else:
                    msg = ('FIND: cannot apply command to object %s ') % (object)
                    self.phtools.printmsg(msg,'red')
                    return -1        
                
                # load arguments and prepare the data structure for the GET request
                # optional parameters like trace and expand are added
                name = None
                if len(self.args) > 1:
                    name=self.args[1]
                    msg = ('FIND: selected id is %s ') % (name)
                    self.phtools.printmsg(msg,'cyan')
                        
                    if '%' in name:
                        print 'Cannot use pattern in this argument; if you want to filter a list, use option by=param[:<>]value,param1[:<>]value,...'
                        return -1
      
                    if object == 'globaltags':
                        self.phtools.fetchglobaltag(name)
                    elif object == 'tags':
                        self.phtools.fetchtag(name)
                    elif object == 'systems':
                        self.phtools.fetchsystem(name)
                else:
                    if object == 'globaltags':
                        self.phtools.getglobaltags(self.by)
                    elif object == 'tags':
                        self.phtools.gettags(self.by)
                    elif object == 'systems':
                        self.phtools.getsystems(self.by)

            except Exception, e:
                sys.exit("failed on action FIND: %s" % (str(e)))
                raise

        elif (self.action=='ADD'):
            try:
                print 'Action ADD is used to insert a metadata object (globaltags,tags,systems,...) into the DB'
                object=self.args[0]
                msg = ('ADD: selected object is %s ') % (object)
                if object in [ 'globaltags', 'tags', 'systems' ]:
                    self.phtools.printmsg(msg,'cyan')
                else:
                    msg = ('ADD: cannot apply command to object %s ') % (object)
                    self.phtools.printmsg(msg,'red')
                    msg = ('ADD: to insert an IOV + Payload use STORE, see --help')
                    self.phtools.printmsg(msg,'cyan')
                    
                    return
                
                objparams = None
                if len(self.args) > 1:
                    objparams=self.args[1]
            
                msg = ('ADD: object parameters %s ') % (objparams)
                self.phtools.printmsg(msg,'cyan')
                
                data = {}
                if objparams is None:
                    msg = 'Cannot create object without a list of parameters, which should be separated by ;'
                    self.phtools.printmsg(msg,'cyan')
                    return
        # Parameters have been provided in command line, try to create the json entity
                expapi = ExpertApi(self.api_client)
                if object == 'globaltags':
                    objparams = ('%s;lockstatus=UNLOCKED') % (objparams)
                    gtag = self.phtools.createobject(objparams,'GlobalTag')
                    gtagcreated = expapi.create_global_tag(body=gtag)
                    print 'Object ',object,' added to db and response is :'
                    self.phtools.dumpmodelobject(gtagcreated,True,['name','snapshot_time','validity','description','lockstatus'])

                elif object == 'tags':
                    tag = self.phtools.createobject(objparams,'Tag')
                    tagcreated = expapi.create_tag(body=tag)
                    print 'Object ',object,' added to db and response is :'
                    self.phtools.dumpmodelobject(tagcreated,True,['name','time_type','object_type','description','last_validated_time'])

                elif object == 'systems':
                    system = self.phtools.createobject(objparams,'SystemDescription')
                    syscreated = expapi.create_system_description(body=system)
                    print 'Object ',object,' added to db and response is :'
                    self.phtools.dumpmodelobject(syscreated,True,['schema_name','node_fullpath','node_description','tag_name_root'])

                else:
                    print 'Cannot create object of type ',object

            except Exception, e:
                sys.exit("ADD failed: %s" % (str(e)))
                raise

        elif (self.action=='STORE'):
            try:
                print 'Action STORE is used to insert an iov object into the DB'
                print ' - <dest tag name>  : destination tag name; the tag should already exists in the DB.'
                print ' - <local file name>: local file name'
                print ' - <iov params>  : column separated list of parameters for iov [since=xxx;sinceString=yyy]'
                print ' - <payload params>  : column separated list of parameters for payload [version=zzz;objectType=aaa;streamerInfo=bbb.;backendInfo=ccc]'

                tagname=self.args[0]
                msg = ('STORE: use tag %s ') % (tagname)
                tapis = TagsApi(self.api_client)
                tag = tapis.find_tag(tagname,expand=self.expand,trace=self.trace)
                if tag.name is None:
                    msg = ('STORE: error, cannot find tag with name %s') % tagname
                    self.phtools.printmsg(msg,'red')
                    return -1
                    
                msg = ('STORE: retrieved tag from database %s ') % (tag.name)
                self.phtools.printmsg(msg,'cyan') 
                
                if len(self.args) != 4:
                    msg = ('STORE: error, cannot find enough parameters for completing the request')
                    self.phtools.printmsg(msg,'red')          
                    
                filename=self.args[1]
                iovobjparams=self.args[2]
                pyldobjparams=self.args[3]

                iov = self.phtools.createobject(iovobjparams,'Iov')
                print 'parsed arguments for iov :',iov
                pyld = self.phtools.createobject(pyldobjparams,'Payload')
                print 'parsed arguments for pyld :',pyld
                since=0
                sinceDescription=''
                if (iov.since is None):
                    since = self.t0
                else:
                    since = iov.since
                    
                if (iov.since_string is None):
                    sinceDescription = ('t%d') % int(since)
                else:
                    sinceDescription = iov.since_string
                    
                msg = ('STORE: input parameters are file=%s, streamer_info=%s, object_type=%s, backend_info=%s, version=%s, since=%s, tag=%s') % (filename,pyld.streamer_info,pyld.object_type,pyld.backend_info,pyld.version,int(since),tag.name)
                self.phtools.printmsg(msg,'cyan')
                
                expapi = ExpertApi(self.api_client)
                retiov = expapi.create_iov_with_payload(filename,pyld.streamer_info,pyld.object_type,pyld.backend_info,pyld.version,int(since),sinceDescription,tag.name)
            
                msg = ('STORE: new payload has been stored in tag %s, since=%d, hash=%s') % (tag.name,int(retiov.since),retiov.hash)
                self.phtools.printmsg(msg,'blue')

            except Exception, e:
                sys.exit("STORE failed: %s" % (str(e)))
                raise

        elif self.action == 'LS':
            try:
                print 'Action LS is used to retrieve iovs in a tag: an optional argument can be added, indicating the snapshot time for the IOVs.'
                tag=self.args[0]
                snapt = 'none'
                if len(self.args) == 2:
                    snapt=self.args[1]
                msg = ('LS: use tag %s and snapshot time %s !') % (tag,snapt)
                self.phtools.printmsg(msg,'cyan')
      
                iovapi = IovsApi(self.api_client)
                iovlist = iovapi.get_iovs_in_tag(tag,payload=True,expand=self.expand,since=self.t0,until=self.tMax)
                rowlist = []
                j=0
                for aniov in iovlist.items:
                    j+=1
                    row = {'row':j,'since' : int(aniov.since), 'insertion_time': aniov.insertion_time, 'hash' : aniov.hash, 'file' : aniov.payload.object_type,'size' : int(aniov.payload.datasize)}
                    rowlist.append(row)
                        
                    if j <= 1:
                        self.phtools.dumpmodellist(rowlist,True,['row','since','insertion_time','hash','file','size'])
                    else:
                        self.phtools.dumpmodellist(rowlist,False,['row','since','insertion_time','hash','file','size'])


#            [ self.phtools.printmsg((' >>> since=%d hash=%s') % (int(aniov.since),aniov.hash),'cyan') for aniov in iovlist.items]
#                print 'Retrieved iov list of length ',len(iovlist.items)
                
            except Exception, e:
                sys.exit("LS failed: %s" % (str(e)))
                raise e
                
        elif self.action == 'GET':
            try:
                print 'Action GET is used to retrieve a blob using the hash.'
                hashstr=self.args[0]
                msg = ('GET: use hash %s !') % (hashstr)
                self.phtools.printmsg(msg,'cyan')
      
                pyldapi = PayloadApi(self.api_client)
                resp = pyldapi.get_blob(hashstr)
                outfname = ('%s.blob') % hashstr
                locfile = open(outfname,'wb')
                locfile.write(resp)
                print 'Dump blob on disk: ',locfile

            except Exception, e:
                sys.exit("GET failed: %s" % (str(e)))
                raise e
                
        elif self.action == 'LOCK':
            try:
                print 'Action LOCK is used to lock or unlock a global tag'
                lockargs=self.args
                msg = '>>> Call method %s using arguments %s ' % (self.action,lockargs)
                self.phtools.printmsg(msg,'cyan')
                
                if len(lockargs) < 2:
                    print 'Set default option for lockstatus to LOCKED (type -h for help)'
                    lockargs.append('LOCKED')
                
                expapi = ExpertApi(self.api_client)
                objparams = ('name=%s;lockstatus=%s') % (lockargs[0],lockargs[1])
                gtag = self.phtools.createobject(objparams,'GlobalTag')
                gtagcreated = expapi.update_global_tag(gtag.name,body=gtag)
                print 'GlobalTag ',lockargs[0],' lockstatus modified and response is ',gtagcreated
                self.phtools.dumpmodelobject(gtagcreated,True,['name','lockstatus','validity','description','release'])
            
            except Exception, e:
                sys.exit("failed: %s" % (str(e)))
                raise

        elif self.action == 'DELETE':
            try:
                print 'Action DELETE is used to remove an object (globaltags,tags,systems) from the DB'
                object=self.args[0]
                msg = ('DELETE: selected object is %s ') % (object)
                if object in [ 'globaltags', 'tags', 'systems' ]:
                    self.phtools.printmsg(msg,'green')
                else:
                    msg = ('DELETE: cannot apply command to object %s ') % (object)
                    self.phtools.printmsg(msg,'red')
                    return
                
                expapi = ExpertApi(self.api_client)
                
                id = self.args[1]
                print 'Removing element ',id
                if object == 'globaltags':
                    gtagremoved = expapi.delete_global_tag(id)
                    print 'Object ',object,' removed and response is '
                    self.phtools.dumpmodelobject(gtagremoved,True,['name','snapshot_time','validity','description','lockstatus'])

                elif object == 'tags':
                    tagremoved = expapi.delete_tag(id)
                    print 'Object ',object,' removed and response is '
                    self.phtools.dumpmodelobject(tagremoved,True,['name','time_type','object_type','description','last_validated_time'])

                elif object == 'systems':
                    sysremoved = expapi.delete_system_description(id)
                    print 'Object ',object,' removed and response is '
                    self.phtools.dumpmodelobject(sysremoved,True,['schema_name','node_fullpath','node_description','tag_name_root'])

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
                self.phtools.printmsg(msg,'cyan')
                
                objparams = '';
                if len(self.args)>2: 
                    objparams = ('globalTagName=%s;tagName=%s;%s') % (gtagobject,tagobject,self.args[2])
                else:
                    objparams = ('globalTagName=%s;tagName=%s') % (gtagobject,tagobject)
                expapi = ExpertApi(self.api_client)
                gtagmap = self.phtools.createobject(objparams,'GlobalTagMap')
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
                self.phtools.printmsg(msg,'cyan')
                
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
                    self.phtools.printmsg(msg,'cyan')
                else:
                    msg = ('UPD: cannot apply command to object %s ') % (object)
                    self.phtools.printmsg(msg,'red')
                    return
                
                objparams = None
                if len(self.args) > 1:
                    objparams=self.args[1]
            
                msg = ('UPD: object parameters %s ') % (objparams)
                self.phtools.printmsg(msg,'cyan')
                expapi = ExpertApi(self.api_client)

                data = {}
                if objparams is None:
                    msg = 'Cannot create object without a list of parameters, which should be separated by ;'
                    self.phtools.printmsg(msg,'cyan')
                    return
            # Parameters have been provided in command line, try to create the json entity
                if object == 'globaltags':
                    gtag = self.phtools.createobject(objparams,'GlobalTag')
                    gtagcreated = expapi.update_global_tag(gtag.name,body=gtag)
                    print 'Object ',object,' updated and response is '
                    self.phtools.dumpmodelobject(gtagcreated,True,['name','snapshot_time','validity','description','lockstatus'])
                elif object == 'tags':
                    tag = self.phtools.createobject(objparams,'Tag')
                    tagcreated = expapi.update_tag(tag.name,body=tag)
                    print 'Object ',object,' updated to db and response is '
                    self.phtools.dumpmodelobject(tagcreated,True,['name','time_type','object_type','description','last_validated_time'])
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
                self.phtools.printmsg(msg,'cyan')
                
                if objecttype == 'globaltag':
                    gtag = GlobalTag()
                    self.phtools.helpmodel(gtag,['name','validity','description','release'])
                elif objecttype == 'tag':
                    tag = Tag()
                    self.phtools.helpmodel(tag,['name','time_type','object_type','description','last_validated_time','synchronization','end_of_validity'])
                elif objecttype == 'system':
                    system = SystemDescription()
                    self.phtools.helpmodel(system,['schema_name','tag_name_root','node_fullpath','node_description'])
                elif objecttype == 'map':
                    maps = GlobalTagMap()
                    self.phtools.helpmodel(maps,['record','label'])
                else:
                    print 'Missing help on ',objecttype
                
                    
            except Exception, e:
                sys.exit("DESCRIBE failed: %s" % (str(e)))
                raise
        else:
            print "Command not recognized: please type -h for help"


        tend=datetime.now()
        print 'Time spent (ms): ',tend-start


if __name__ == '__main__':
    PhysDBDriver()

