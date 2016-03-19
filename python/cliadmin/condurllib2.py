#!/usr/bin/env python
# encoding: utf-8
'''
cliadmin.CalibAdmin -- shortdesc

cliadmin.CalibAdmin is a description

It defines classes_and_methods

@author:     formica

@copyright:  2015 CEA. All rights reserved.

@license:    license

@contact:    andrea.formica@cea.fr
@deffield    updated: Updated
'''

import sys, os, pickle, getopt,re
import json
import cStringIO
import os.path

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
            self.taxextension = '_HEAD_00'
            self._command = sys.argv[0]
            self.useSocks = False
            self.t0 = 0
            self.debug = False
            self.t0desc = 't0'
            self.tMax = 'INF'
            self.trace = 'off'
            self.expand = 'false'
            self.iovspan = 'time'
            self.snapshot = '2050-01-01T00:00:00+02:00'
            self.jsondump=False
            self.dump=False
            self.user='none'
            self.passwd='none'
            self.outfilename=''
            self.urlsvc='localhost:8080/physconddb'
            longopts=['help','socks','out=','jsondump','debug','url=','t0desc=','t0=','tMax=','snapshot=','trace=','expand=','iovspan=','user=','pass=']
            opts,args=getopt.getopt(sys.argv[1:],'',longopts)
        #print opts, args
        except getopt.GetoptError,e:
#            print colored.red(e)
            print e
            self.usage()
            sys.exit(-1)
        self.procopts(opts,args)
        self.execute()

    def usage(self):
        print
        print "usage: CalibAdmin.py {<options>} <action> {<args>}"
        print "Manage calibration files in conditions DB"
        print " "
        print "<Action> determines which rest method to call"
        print "Subcommands are:"
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
        print " - SHOW  <folder path> <tag-name> [tag-name defaults to _HEAD_00]"
        print "        ex: /MY/FOLDER/PATH : retrieve list of files in the folder showing their insertion time and datasize."
        print "    List files under a given folder (and tag)."
        print " "
        print " - COLLECT  <global tag name> <ASG global tag>"
        print "        ex: MyNewPkg-00-01 ASG-00-01: Dump the full directory structure for global tag in server file system."
        print "    The purpose is to trigger the copy to afs of all calibration files under a global tag."
        print " "
        print "Options: "
        print "  --socks activate socks proxy on localhost 3129 "
        print "  --out={filename} activate dump on filename "
        print "  --debug activate debugging output "
        print "  --jsondump activate a dump of output lines in json format "
        print "  --trace [on|off]: trace associations (ex: globaltag ->* tags or tag ->* iovs "
        print "  --url [localhost:8080/physconddb]: use a specific server "
        print "  --snapshot={the snapshot time for global tag, default is 2050-01-01T00:00:00+02:00} "
        print "  --t0={t0 for iovs} "
        print "  --t0desc={description string for t0, can be used for payload file name} "
        print "  --tMax={tMax for iovs} "
        print "  --iovspan={time|date|runlb|timerun|daterun} "
        print "         time: iov in COOL format, allows Inf for infinity"
        print "         date: iov in yyyyMMddHHmmss format"
        print "         runlb: iov in run-lb format, only for run based folders "
        print "         the others make the conversion to run number, does not allow Inf for infinity "
        print "Examples: "

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

#print 'Help defined for ',mobj
        return mobj.help()

    def procopts(self,opts,args):
        "Process the command line parameters"
        for o,a in opts:
            #           print 'Analyse options ' + o + ' ' + a
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
            if (o=='--snapshot'):
                self.snapshot=a
            if (o=='--t0'):
                self.t0=a
            if (o=='--t0desc'):
                self.t0desc=a
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


######## Utility functions

     
# Lock a global tag
    def lockit(self, params):
        globaltagname=params[0]
        lockstatus=params[1]
        package="none"
        if len(params)>2:
            package=params[2]
        data={}
        # Search globaltagname in global tags
        msg = ('>>> Set lock for GlobalTag %s to %s using package name %s') % (globaltagname,lockstatus,package)
        #print colored.cyan(msg)
        print msg
        gtag = self.resttools.lockit(globaltagname,lockstatus)
        print gtag, ' status ',gtag['lockstatus']
        msg = ('    + Lock status has been set to %s for GlobalTag %s : snapshot time is %s') % (gtag['lockstatus'],gtag['href'],gtag['snapshotTime'])
        print msg
        if 'LOCKED' == lockstatus:
            print 'Lock status is LOCKED, then you can dump the global tag...',globaltagname
            print 'Calling dump method using parameters ',gtag['name'],package
            self.resttools.dumpgtag(gtag['name'],package)
        else:
            print 'No dumping on directory is performed when unlocking....'
#        print colored.green(msg)    

# List files under global tag
    def listcalib(self, params):
        globaltagname=params[0]
        filenamepattern=params[1]
        data={}
        # Search for all files associated to global tag
        msg = ('>>> Search files in GlobalTag %s using pattern %s') % (globaltagname,filenamepattern)
        #print colored.cyan(msg)
        print msg
        (maplist, response) = self.resttools.getgtagtags(globaltagname,'on','true')
        print 'Response is ',response
        if self.debug:
            print 'Dump the retrieved map list'
            print maplist
        if len(maplist) == 0:
            msg = (' ===> GlobalTag %s has empty list of associated tags....') % (globaltagname)
            #print colored.cyan(msg)
            print msg
            
        globaltagmaps = maplist['globalTagMaps']
###        print 'Use list of mappings ',globaltagmaps
        for amap in globaltagmaps:
            #print 'Map in loop is ',amap
            atag = Tag(amap['systemTag'])
            #print 'Tag in loop is ', atag

            atagname = atag.getParameter('name')
            if filenamepattern not in atagname and filenamepattern != '*' :
                msg = (' --- skip file for tag %s ') % (atagname)
                #print colored.cyan(msg)
                print msg
                continue
            (objList, code) = self.resttools.gettagiovs(atag.getParameter('name'), globaltagname, self.trace, self.expand, self.t0, self.tMax)
            tagnameroot = atagname.split(self.taxextension)[0]
            msg = ' ==> Search for system by tag name root %s ' % tagnameroot
            #print colored.cyan(msg)
            print msg
            (systemobj, code) = self.resttools.getsystems('tag',tagnameroot)
            nodepath = systemobj['nodeFullpath']
            #print 'Node path for system is ',nodepath
            filename = atag.getParameter('objectType')
            msg = '     + (path,file) %s, %s : ' % (nodepath,filename)
            #coloredmsg1 = colored.cyan(msg)
            coloredmsg1 = msg
                        
            #printed = False
            counter=0
            print coloredmsg1
            for aniov in objList['items']:
                counter = counter+1
##                print ' The object link is ', aniov
                (iovobjlink, code) = self.resttools.loadItems(aniov)
##                print 'IOV db link is  ',iovobjlink
                since = iovobjlink['since']
                sincestr = iovobjlink['sinceString']
                instime = iovobjlink['insertionTime']
                payloadobj = iovobjlink['payload']
                pyldhash = payloadobj['hash']
                pyldsize = payloadobj['datasize']
                msg = '        (%d) [size] %s [since] %s [%s] @ %s' % (counter,pyldsize,since,sincestr,instime)
                #coloredmsg2 = colored.green(msg)
                coloredmsg2 = msg

                (datapyld, code) = self.resttools.getPayload(pyldhash,'false','off')
                datapyldhref = datapyld['href']
                
                msg = ': [url] %s' % (datapyldhref)
                ymsg = msg
#                print coloredmsg2, colored.yellow(msg)
                print coloredmsg2, msg


    #print ' The object retrieved is ', iovobjlink
    #                msg = ('    + ') % ()
#        print colored.green(msg)


    def execute(self):
        #print colored.blue(('Execute the command for action %s and arguments : %s ' ) % (self.action, str(self.args)))
        print ('Execute the command for action %s and arguments : %s ' ) % (self.action, str(self.args))
        start = datetime.now()
        self.restserver = PhysCurl(self.urlsvc, self.useSocks)
        self.resttools = PhysUtils(self.restserver)
        
        if self.debug:
            self.restserver.setdebug(True)
        
        if self.dump:
            outfile = open(self.outfilename,"w")
        _dict = {}
        dictarr = []
        dictkeys = []
        dictvalues = []
        params = {}
        
        if (self.action=='COMMIT'):
            try:
                calibargs=self.args
                msg = '>>> Call method %s using arguments %s ' % (self.action,calibargs)
                #print colored.cyan(msg)
                print msg
                pkgname=calibargs[0]
                filename=calibargs[1]
                destpath=calibargs[2]
                since=0
                sinceDescription="t0"
                if (len(calibargs) > 3):
                    since = calibargs[3]
                if (len(calibargs) > 4):
                    sinceDescription = calibargs[4]
                print 'calling commit: ',filename,' ',pkgname,' ',destpath,' ',since,' ',sinceDescription
                response = self.resttools.commit(filename,pkgname,destpath,since,sinceDescription)    
##                print 'Response: ',response                
                msg = 'Response Code: %s ' % (response['code'])
                #print colored.cyan(msg)
                print msg
                if response['code'] != 200:
                    msg = 'Error in commit: %s ' % (response['code'])
                    #print colored.red(msg)
                    print msg
                    return -1
                payload = response['payload']
                tag = response['tag']
                msg = 'Stored file %s of size %s in path %s -> tag %s @ time %s ' % (tag['objectType'],payload['datasize'],destpath,tag['name'],response['since'])
                #print colored.green(msg)
                print msg
                return 0
                    
            except Exception, e:
                sys.exit("failed: %s" % (str(e)))
                raise
        
        elif (self.action=='TAG'):
            try:
                calibargs=self.args
                msg = '>>> Call method %s using arguments %s ' % (self.action,calibargs)
                #print colored.cyan(msg)
                print msg
                systemname=calibargs[0]
                systemsglobaltag=calibargs[1]
                response = self.resttools.calibtagandlink(systemsglobaltag,systemname)
                msg = 'Response Code: %s ' % (response['code'])
                #print colored.cyan(msg)
                print msg
                if response['code'] != 200:
                    msg = 'Error in commit: %s ' % (response['code'])
                    #print colored.red(msg)
                    print msg
                    return -1
                if self.debug:
                    print 'Received response ',response
                maplist = response['globalTagMaps']
                for amap in maplist:
                    tag = amap['systemTag']
                    msg = 'Associate tag %s for file %s to global tag %s' % (tag['name'], tag['objectType'],systemsglobaltag)
                    #print colored.green(msg)
                    print msg
                return 0
            
            except Exception, e:
                sys.exit("failed: %s" % (str(e)))
                raise
            
        elif (self.action=='LOCK'):
            try:
                calibargs=self.args
                msg = '>>> Call method %s using arguments %s ' % (self.action,calibargs)
                #print colored.cyan(msg)
                print msg
                if len(calibargs) < 2:
                    print 'Set default option for lockstatus to LOCKED (type -h for help)'
                    calibargs.append('LOCKED')
                self.lockit(calibargs)
                    
            except Exception, e:
                sys.exit("failed: %s" % (str(e)))
                raise
        elif (self.action=='LS'):
            try:
                calibargs=self.args
                msg = '>>> Call method %s using arguments %s ' % (self.action,calibargs)
                #print colored.cyan(msg)
                print msg
                if len(calibargs) < 2:
                    print 'Set default option for filename pattern (type -h for help)'
                    calibargs.append('*')
                self.listcalib(calibargs)
            
            except Exception, e:
                sys.exit("failed: %s" % (str(e)))
                raise

        elif (self.action=='SHOW'):
            try:
                calibargs=self.args
                msg = '>>> Call method %s using arguments %s ' % (self.action,calibargs)
                #print colored.cyan(msg)
                print msg
                if len(calibargs) < 1:
                    print 'Not enough arguments, type -h for help'
                nodefullpath = calibargs[0]
                msg = ' ==> Search for system by nodefullpath %s ' % nodefullpath
                #print colored.cyan(msg)
                print msg
                (systemobjlist, code) = self.resttools.getsystems('node',nodefullpath)
                print 'Retrieved ',systemobjlist,' with code ',code
                if code != 200:
                    msg = 'Error in show while retrieving systems: %s ' % (code)
                    #print colored.red(msg)
                    print msg
                    return -1
      
                for systemobj in systemobjlist['items']:
                    #print 'Retrieved system ',systemobj
                    tagnameroot = systemobj['tagNameRoot']
                    tagname = tagnameroot+self.taxextension
                    msg = '>>> Check content for system %s using tag name root %s ' % (systemobj['nodeFullpath'],tagnameroot)
                    #print colored.cyan(msg)
                    print msg
                    params = {}
                    params['tag']=tagname
                    params['expand']=True
                    (iovlist, code) = self.restserver.getiovs(params, '/iovs/find')
                    if code != 200:
                        msg = 'Error in show while retrieving iovs for %s : %s ' % (tagname,code)
                        #print colored.red(msg)
                        print msg
                    else:
                        #print 'Retrieved iov list ',iovlist
                        iovitems = iovlist['items']
                        for iov in iovitems:
                            hash = iov['hash']
                            (payload,code)=self.resttools.getPayload(hash,'false','true')
                            msg = '>>> Found entry for file %s with size %s inserted %s @ since %s [%s] ' % (payload['objectType'],payload['datasize'],payload['insertionTime'],iov['since'],payload['href'])
                            #print colored.green(msg)
                            print msg
                            
                
            except Exception, e:
                sys.exit("failed: %s" % (str(e)))
                raise
    
        elif (self.action=='COLLECT'):
            try:
                calibargs=self.args
                msg = '>>> Call method %s using arguments %s ' % (self.action,calibargs)
                #print colored.cyan(msg)
                print msg
                globaltagname = calibargs[0]
                asgglobaltagname = calibargs[1]
                self.resttools.collect(globaltagname, asgglobaltagname)
            
            except Exception, e:
                sys.exit("failed: %s" % (str(e)))
                raise

        elif (self.action=='TAR'):
            try:
                calibargs=self.args
                msg = '>>> Call method %s using arguments %s ' % (self.action,calibargs)
                #print colored.cyan(msg)
                print msg
                globaltagname = calibargs[0]
                package="none"
                if len(calibargs)>1:
                   package=calibargs[1]
                self.resttools.gettar(globaltagname,package)
            
            except Exception, e:
                sys.exit("failed: %s" % (str(e)))
                raise

        else:
            msg = ('Command %s not recognized, type -h for help') % self.action
            #print colored.red(msg)
            print msg
            return -1

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
        print data
        argsarr = data.split(';')
        obj = {}
        outdata = {}
        for anarg in argsarr:
            key = anarg.split('=')[0]
            val = anarg.split('=')[1]
            obj[key]=val
        
        print 'Created python dictionary ',obj
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



if __name__ == '__main__':
    PhysDBDriver()

