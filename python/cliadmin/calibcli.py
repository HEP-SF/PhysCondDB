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

from swagger_client.apis import GlobaltagsApi, TagsApi, IovsApi, SystemsApi, ExpertApi, MapsApi, ExpertcalibrationApi
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
        print "usage: calibcli.py {<options>} <action> {<args>}"
        print "Search conditions DB content using args"
        print "Server url is needed in order to provide the system with the base url; defaults to localhost (see below)"
        print "Action determines which rest method to call: list of possible actions is given below"
        print " - COMMIT <package-name> <filename> <destination-path> "
        print "        ex: MyNewPkg local.file /MyNewPkg/ASubPkg/SomeDir/: store file local.file into conditions DB "
        print "    Add a file from local system to destination path, storing it with iov t0 [see option --t0]"
        print "    Creates an entry for the new file in the systemdescription table if not already there."
        print "    Creates a tag using package name and filename (appending _HEAD_00) if not already there."
        print " "
        print " - TAG <package-name> <global tag name>"
        print "        ex: MyNewPkg MyNewPkg-00-01 : creates new global tag and associate all tags (files) found in package MyNewPkg."
        print "     Creates new global tag <global tag name> associated to all files in package <package-name>."
        print " "
        print " - LOCK <global tag name> <lockstatus> [locked|unlocked]"
        print "        ex: MyNewPkg-00-01 locked : lock the global tag and change its snapshot time to now."
        print "     Lock or unlock existing global tag <global tag name>."
        print " "
        print " - LS  <global tag name> <filter file name> [pattern | *(DEFAULT)] "
        print "        ex: MyNewPkg-00-01 myfile : retrieve list of files containing 'myfile' string in global tag MyNewPkg-00-01."
        print "    List files under a given global tag, filtering by file name."
        print " "
        print " - DIR  <folder path>"
        print "        ex: MYFOLDERPATH (cannot use slashes here) : retrieve list of files in the folder showing their insertion time and datasize."
        print "    List files under a given folder."
        print " "
        print " - DUMP  hash [hash of a file can be retrieved using LS and SHOW commands]"
        print "        ex: anaash : dump the file in local directory."
        print "    Dump the file in the local directory."
        print " "
        print " - COLLECT  <global tag name> <ASG global tag>"
        print "        ex: MyNewPkg-00-01 ASG-00-01: Dump the full directory structure for global tag in server file system."
        print "    The purpose is to trigger the copy to afs of all calibration files under a global tag."
        print " "

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
          elif color == 'green':
            print colored.green(msg)
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
            if self.debug:
                print 'Filling argument ',_arg
            (key, value) = _arg.split("=")
            params[key] = value
        if self.debug:
            print 'created parameter list: ',params
        resp = RESTResponse()
        resp.data = json.dumps(params)
        instance = {}
        instance = self.api_client.deserialize(resp, objtype)
        if self.debug:
            print 'created model object : ',instance, ' for type ',objtype
        return instance

    def helpmodel(self,obj,keylist=[]):
        attrs = obj.attribute_map
        msg = ''
        for key in keylist:
            msg = ('%s%s=...;') % (msg,attrs[key])
        self.printmsg(msg[:-1],'cyan')
        
    def printrow(self, objdict, keys=[]):
        ##print objdict
        (head_format, row_format) = self.getformat(objdict,keys)
        for akey in keys:
            val = objdict[akey]
            if '_time' in akey:
                objdict[akey] = str(objdict[akey])
        ##print 'using row format ',row_format
        return row_format.format(**objdict)

    def getformat(self, objdict, keys=[]):
        ##print objdict
        head_format = ''
        row_format = ''
        i=0
        for akey in keys:
            val = objdict[akey]
            num = 30
            if (akey == 'name'):
                num=60
            elif ('_time' in akey):
                num=35
            elif ('node_' in akey):
                num=50
            elif ('status' in akey):
                num=15
            elif ('hash' in akey):
                num=65
            elif ('_type' in akey):
                num=30
            elif ('description' in akey):
                num=70
                
            akey_format='{%s:<%d.%d} | ' % (akey,num,num)
            if (isinstance(val,(int,float,long))):
                num=12
                if (akey == 'row'):
                    num=5
                akey_format='{%s:<%d} | ' % (akey,num)

            head_format += '{0[%d]:<%d} |' % (int(i),num)
            row_format += akey_format
            i+=1
            
        return head_format,row_format
    
    def printheader(self, objdict, keys=[]):
        ##print objdict
        (head_format, row_format) = self.getformat(objdict,keys)
        return head_format.format(keys)


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
                self.dumpmodelobject(obj,withheader,keylist)
            else:
                self.dumpmodelobject(obj,False,keylist)

    def dumpmodelobject(self,obj,withheader=False,keylist=[]):
        objdict = {}
        if not isinstance(obj,dict):
            objdict = obj.to_dict()
        else:
            objdict = obj
        headermsg = ''
        msg = ''
        reducedlist=[]
        if len(keylist)>0:
            for key in keylist:
                if isinstance(objdict[key],list):
                    print 'skip list...'
                elif key in ['href','res_id']:
                    print 'skip fields'
                else:
                    reducedlist.append(key)
#                    headermsg = ('%s | %15s') % (headermsg,key)
#                    msg = ('%s | %s') % (msg,objdict[key])

        else:
            for key in objdict:
                if isinstance(objdict[key],list):
                    print 'skip list...'
                elif key in ['href','res_id']:
                    print 'skip fields'
                else:
                    reducedlist.append(key)
                    print 'Appending key ',key
#                    headermsg = ('%s | %15s') % (headermsg,key)
#                    msg = ('%s | %s') % (msg,objdict[key])
                    

        if withheader is True:
            headermsg = self.printheader(objdict,reducedlist)
            self.printmsg(headermsg,'blue')
            sep_format = '{:=^%d}' % int(len(headermsg))
            self.printmsg(sep_format.format('='),'blue')
        
        msg = self.printrow(objdict,reducedlist)
        self.printmsg(msg,'cyan')

    def fetchglobaltag(self,name):
        gtapis = GlobaltagsApi(self.api_client)
        gtag = gtapis.find_global_tag(name,expand=self.expand,trace=self.trace)
        coll = gtag.global_tag_maps
        if coll is None:
            print 'associated tags not found: may be trace is disabled ? (--trace=on)'
            return
        i = 0
        rowlist = []
        for map in coll:
            i+=1
            row = { 'row' : i, 'name' : map.system_tag.name, 'time_type' : map.system_tag.time_type, 'record' : map.record, 'label' : map.label}
            rowlist.append(row)
            
        self.dumpmodellist(rowlist,True,['row','name','time_type','record','label'])
        print 'Selected global tag is: '
        self.dumpmodelobject(gtag,True,['name','snapshot_time','validity','description','lockstatus'])
        if self.debug:
            print gtag
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
        rowlist = []
        i=0
        for map in coll:
            i+=1
            row = { 'row' : i, 'name' : map.global_tag.name, 'description' : map.global_tag.description, 'record' : map.record, 'label' : map.label}
            rowlist.append(row)
            
        self.dumpmodellist(rowlist,True,['row','name','description','record','label'])

        
    def execute(self):
        msg = ('Execute the command for action %s and arguments : %s ' ) % (self.action, str(self.args))
        self.printmsg(msg,'cyan')
            
        start = datetime.now()
        
        if self.dump:
            outfile = open(self.outfilename,"w")
        _dict = {}
        params = {}
        
        default_tag_extension = '_HEAD_00'
        
        if (self.action=='COMMIT'):
            try:
                print 'Action COMMIT is used to store a file in the DB; mandatory arguments are the package name, the file name and the destination path'
                print 'Found N arguments ',len(self.args)
                pkgname=self.args[0]
                msg = ('COMMIT: use package name %s ') % (pkgname)
                if len(self.args) >= 3 :
                    self.printmsg(msg,'cyan')
                else:
                    msg = ('COMMIT: cannot apply command because number of arguments is not enough, type -h for help %')
                    self.printmsg(msg,'red')
                    return -1        
 
                filename=self.args[1]
                destpath=self.args[2]
                since=0
                sinceDescription=''
                if (len(self.args) > 3):
                    since = int(self.args[3])
                if (len(self.args) > 4):
                    sinceDescription = self.args[4]
                else:
                    sinceDescription = ('t%d') % since
                # load arguments and prepare the data structure for the GET request
                # optional parameters like trace and expand are added

                msg = ('COMMIT: calling commit with args: %s %s %s %s %s') % (filename,pkgname,destpath,str(since),sinceDescription)
                self.printmsg(msg,'cyan')   
                expcalapi = ExpertcalibrationApi(self.api_client)
                iov = expcalapi.commit_file(pkgname,destpath,file=filename,since=since,description=sinceDescription)    
#                print 'Response: ',iov                
                msg = ('Store in path %s: since=%d, hash=%s, file=%s, size=%d [identified by tag=%s, url=%s]') %(destpath,int(iov.since),iov.hash,iov.payload.object_type,int(iov.payload.datasize),iov.tag.name,iov.payload.data.href)
                self.printmsg(msg,'green')                            
                return 0
                        
                        
            except Exception, e:
                sys.exit("failed on action COMMIT: %s" % (str(e)))
                raise

        elif (self.action=='TAG'):
            try:
                print 'Action TAG is used to provide a global tag for a package, associating all subfolders to it.'
                systemname=self.args[0]
                systemsglobaltag=self.args[1]
                msg = ('TAG: calling tag with args: %s %s') % (systemname,systemsglobaltag)
                self.printmsg(msg,'cyan')   
                expcalapi = ExpertcalibrationApi(self.api_client)
                gtag = expcalapi.tag_file(systemsglobaltag,systemname)
                ## print 'Response: ',gtag
                msg = ('Tag package %s: name=%s, lockstatus=%s') %(systemname,gtag.name,gtag.lockstatus)
                self.printmsg(msg,'cyan') 
                rowlist = []
                for maps in gtag.global_tag_maps:
                    row = {}
                    row = {'file' : maps.system_tag.object_type, 'name': maps.system_tag.name }
                    rowlist.append(row)
                self.dumpmodellist(rowlist,True,['file','name'])
                
            except Exception, e:
                sys.exit("TAG failed: %s" % (str(e)))
                raise

        elif self.action == 'LS':
            try:
                print 'Action LS is used to retrieve files in a global tag.'
                gtagname=self.args[0]
                
                msg = ('LS: use global tag %s !') % (gtagname)
                self.printmsg(msg,'cyan')
                gtapis = GlobaltagsApi(self.api_client)
                gtag = gtapis.find_global_tag(gtagname,expand=self.expand,trace=self.trace)
                coll = gtag.global_tag_maps
                if coll is None:
                    print 'associated tags not found: may be trace is disabled ? (--trace=on)'
                    return
                
                iovapi = IovsApi(self.api_client)
                sysapis = SystemsApi(self.api_client)
                for map in coll:
                    tagname = map.system_tag.name
                    tagnameroot = tagname.replace(default_tag_extension,'')
                    system = sysapis.find_system(tagnameroot,expand=self.expand)
                    if self.debug:
                        print 'found tag ',tagname
                    row = { 'name' : map.system_tag.name, 'time_type' : map.system_tag.time_type, 'record' : map.record, 'label' : map.label}
                    print ''
                    self.dumpmodelobject(row,False,['name','time_type','record','label'])
                    print '>>>>>>>>>> associated files :'
                    iovlist = iovapi.get_iovs_in_tag(map.system_tag.name,payload=True,expand=self.expand,since=self.t0,until=self.tMax)
                    if self.debug:
                        print iovlist
                    #print 'Retrieved file list of length: ',len(iovlist.items)
                    rowlist = []
                    i=0
                    for aniov in iovlist.items:
                        i+=1
                        row = {'row': i, 'since' : int(aniov.since), 'insertion_time': aniov.insertion_time, 'hash' : aniov.hash, 'path': system.node_fullpath, 'file' : aniov.payload.object_type,'size' : int(aniov.payload.datasize)}
                        rowlist.append(row)
                     
                    self.dumpmodellist(rowlist,True,['row','since','insertion_time','hash','path','file','size'])
                print '============= summary ============== '
                print 'Selected global tag is: '
                self.dumpmodelobject(gtag,True,['name','snapshot_time','validity','description','lockstatus'])
                print '>>>>>>>>>> '
                print 'List of associated files : ',len(coll)
      
                
            except Exception, e:
                sys.exit("LS failed: %s" % (str(e)))
                raise e
                
        elif self.action == 'DIR':
            try:
                print 'Action DIR is used to list files using the folder path'
                nodefullpath = self.args[0]
                msg = 'Search for files in path %s' % (nodefullpath)
                self.printmsg(msg,'cyan')
                iovapi = IovsApi(self.api_client)
                sysapis = SystemsApi(self.api_client)
                by = ('nodeFullpath:%s') % (nodefullpath)
                syslist = sysapis.list_systems(by) 
                i=0
                for asys in syslist.items:
                    #print asys
                    tagnameroot = asys.tag_name_root
                    tagname = ('%s%s') % (tagnameroot,default_tag_extension)
                    iovlist = iovapi.get_iovs_in_tag(tagname,payload=True,expand=self.expand,since=self.t0,until=self.tMax)
                    rowlist = []
                    j=0
                    for aniov in iovlist.items:
                        j+=1
                        row = {'row':j,'since' : int(aniov.since), 'insertion_time': aniov.insertion_time, 'hash' : aniov.hash, 'path': asys.node_fullpath, 'file' : aniov.payload.object_type,'size' : int(aniov.payload.datasize)}
                        rowlist.append(row)
                        
                    i = (i+1)
                    if i <= 1:
                        self.dumpmodellist(rowlist,True,['row','since','insertion_time','hash','path','file','size'])
                    else:
                        self.dumpmodellist(rowlist,False,['row','since','insertion_time','hash','path','file','size'])

                print '============= summary ============== '
                print 'Selected system list with path %s has length : %d ' % (nodefullpath,len(syslist.items))
                
                    
            except Exception, e:
                sys.exit("DIR failed: %s" % (str(e)))
                raise

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
                print 'GlobalTag ',lockargs[0],' lockstatus modified and response is: '
                self.dumpmodelobject(gtagcreated,True,['name','lockstatus','validity','description','release'])
            
            except Exception, e:
                sys.exit("failed: %s" % (str(e)))
                raise

        elif self.action == 'DUMP':
            try:
                print 'Action DUMP is used to dump a whole package global tag from the DB'
                globaltag=self.args[0]
                msg = ('DUMP: selected global tag is %s ') % (globaltag)
                expcalapi = ExpertcalibrationApi(self.api_client)
                gtag = expcalapi.dump_content(globaltag)
                self.dumpmodelobject(gtag,True,['name','lockstatus','validity','description','release'])
                    
            except Exception, e:
                sys.exit("DELETE failed: %s" % (str(e)))
                raise

        elif self.action == 'COLLECT':
            try:
                print 'Action COLLECT is used to associate package global tags into an ASG globaltag'
                globaltag=self.args[0]
                if 'ASG' not in globaltag:
                    msg = ('Cannot collect global tags different from ASG-xxxx : %s wrong name ') % globaltag
                    self.printmsg(msg,'red')
                    return -1
                packageglobaltag=self.args[1]
                msg = ('COLLECT: ASG global tag %s, collecting package global tag %s ') % (globaltag,packageglobaltag)
                self.printmsg(msg,'cyan')
                gtapis = GlobaltagsApi(self.api_client)
                gtag = gtapis.find_global_tag(packageglobaltag,expand=self.expand,trace=self.trace)
                if gtag.name is None:
                    msg = ('Cannot find package global tag %s') % (packageglobaltag)
                    return -1
                
                expcalapi = ExpertcalibrationApi(self.api_client)
                gtag = expcalapi.collect(globaltag,packageglobaltag)    
                self.fetchglobaltag(gtag.name)
                    
            except Exception, e:
                sys.exit("COLLECT failed: %s" % (str(e)))
                raise
                
        elif (self.action=='TAR'):
            try:
                print 'Action TAR is used to create a tar file from a global tag, in general an ASG...'
                globaltagname=self.args[0]
                msg = ('TAR: create tar from global tag %s') % (globaltagname)
                self.printmsg(msg,'cyan')  
                
                packagename="none"
                if len(self.args)>1:
                    packagename=self.args[1]
                expcalapi = ExpertcalibrationApi(self.api_client)
                data = expcalapi.get_tar_from_global_tag(globaltagname,package=packagename)
                outfname = ('%s.tar') % globaltagname
                locfile = open(outfname,'wb')
                locfile.write(data)
                print 'Dump tar on disk: ',locfile
                
            except Exception, e:
                sys.exit("TAR: failed %s" % (str(e)))
                raise

        else:
            print "Command not recognized: please type -h for help"


        tend=datetime.now()
        print 'Time spent (ms): ',tend-start


class RESTResponse(object):
    def __init__(self):
        #print 'create response object'
        self.__data = None

    def data(self):
        return self.__data

    def data(self, data):
        self.__data = data
        print 'create data field ',self.__data
# process command line options


if __name__ == '__main__':
    PhysDBDriver()

