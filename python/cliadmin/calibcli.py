#!/usr/bin/env python
# encoding: utf-8
'''
cliadmin.calibcli -- shortdesc
    Calibration files management CLI
cliadmin.calibcli is a description

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
        
        
    def execute(self):
        msg = ('Execute the command for action %s and arguments : %s ' ) % (self.action, str(self.args))
        self.phtools.printmsg(msg,'cyan')
            
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
                    self.phtools.printmsg(msg,'cyan')
                else:
                    msg = ('COMMIT: cannot apply command because number of arguments is not enough, type -h for help %')
                    self.phtools.printmsg(msg,'red')
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
                self.phtools.printmsg(msg,'cyan')   
                expcalapi = ExpertcalibrationApi(self.api_client)
                iov = expcalapi.commit_file(pkgname,destpath,file=filename,since=since,description=sinceDescription)    
#                print 'Response: ',iov                
                msg = ('Store in path %s: since=%d, hash=%s, file=%s, size=%d [identified by tag=%s, url=%s]') %(destpath,int(iov.since),iov.hash,iov.payload.object_type,int(iov.payload.datasize),iov.tag.name,iov.payload.data.href)
                self.phtools.printmsg(msg,'green')                            
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
                self.phtools.printmsg(msg,'cyan')
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
                    self.phtools.dumpmodelobject(row,False,['name','time_type','record','label'])
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
                     
                    self.phtools.dumpmodellist(rowlist,True,['row','since','insertion_time','hash','path','file','size'])
                print '============= summary ============== '
                print 'Selected global tag is: '
                self.phtools.dumpmodelobject(gtag,True,['name','snapshot_time','validity','description','lockstatus'])
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
                self.phtools.printmsg(msg,'cyan')
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
                        self.phtools.dumpmodellist(rowlist,True,['row','since','insertion_time','hash','path','file','size'])
                    else:
                        self.phtools.dumpmodellist(rowlist,False,['row','since','insertion_time','hash','path','file','size'])

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
                self.phtools.printmsg(msg,'cyan')
                
                if len(lockargs) < 2:
                    print 'Set default option for lockstatus to LOCKED (type -h for help)'
                    lockargs.append('LOCKED')
                
                expapi = ExpertApi(self.api_client)
                objparams = ('name=%s;lockstatus=%s') % (lockargs[0],lockargs[1])
                gtag = self.phtools.createobject(objparams,'GlobalTag')
                gtagcreated = expapi.update_global_tag(gtag.name,body=gtag)
                print 'GlobalTag ',lockargs[0],' lockstatus modified and response is: '
                self.phtools.dumpmodelobject(gtagcreated,True,['name','lockstatus','validity','description','release'])
            
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
                self.phtools.dumpmodelobject(gtag,True,['name','lockstatus','validity','description','release'])
                    
            except Exception, e:
                sys.exit("DELETE failed: %s" % (str(e)))
                raise

        elif self.action == 'COLLECT':
            try:
                print 'Action COLLECT is used to associate package global tags into an ASG globaltag'
                globaltag=self.args[0]
                if 'ASG' not in globaltag:
                    msg = ('Cannot collect global tags different from ASG-xxxx : %s wrong name ') % globaltag
                    self.phtools.printmsg(msg,'red')
                    return -1
                packageglobaltag=self.args[1]
                msg = ('COLLECT: ASG global tag %s, collecting package global tag %s ') % (globaltag,packageglobaltag)
                self.phtools.printmsg(msg,'cyan')
                gtapis = GlobaltagsApi(self.api_client)
                gtag = gtapis.find_global_tag(packageglobaltag,expand=self.expand,trace=self.trace)
                if gtag.name is None:
                    msg = ('Cannot find package global tag %s') % (packageglobaltag)
                    return -1
                
                expcalapi = ExpertcalibrationApi(self.api_client)
                gtag = expcalapi.collect(globaltag,packageglobaltag)    
                self.phtools.fetchglobaltag(gtag.name)
                    
            except Exception, e:
                sys.exit("COLLECT failed: %s" % (str(e)))
                raise
                
        elif (self.action=='TAR'):
            try:
                print 'Action TAR is used to create a tar file from a global tag, in general an ASG...'
                globaltagname=self.args[0]
                msg = ('TAR: create tar from global tag %s') % (globaltagname)
                self.phtools.printmsg(msg,'cyan')  
                
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

