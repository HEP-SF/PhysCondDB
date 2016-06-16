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
from os import walk


#from pygments import highlight, lexers, formatters
from xml.dom import minidom
#from clint.textui import colored
from datetime import datetime

from swagger_client.apis import GlobaltagsApi, TagsApi, IovsApi, SystemsApi, ExpertApi, MapsApi, ExpertcalibrationApi
from swagger_client.models import GlobalTag, Tag, GlobalTagMap, SystemDescription
from swagger_client import ApiClient
from physdbutils import PhysDbUtils

class CalibDirDriver():
    def __init__(self):
    # process command line options
        try:
            self._command = sys.argv[0]
            self.useSocks = False
            self.snap = -1
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
            longopts=['help','socks','out=','jsondump','t0=','tMax=','snap=','url=','debug','trace=','expand=','by=','page=','pagesize=','iovspan=','user=','pass=']
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
        print " - TAG <package-name> <global tag name> [description]"
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
        print "  --snap={snapshot time in milli seconds since EPOCH}. DEFAULT=",self.snap
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
            if (o=='--snap'):
                self.snap=a
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
        default_calib_dir = '/Users/aformic/Public/Atlas/TESTS/CalibArea'
        if (self.action=='COMMIT'):
            try:
                print 'Action commit is used to list calibration files in a given package and create the commit scripts'
                print 'Found N arguments ',len(self.args)
                pkgname=self.args[0]
                subdir = ''
                subdir=self.args[1]
                subdir2=self.args[2]
                msg = ('LS: use package name %s and sub directory %s') % (pkgname,subdir)
                if len(self.args) >= 3 :
                    self.phtools.printmsg(msg,'cyan')
                else:
                    msg = ('LS: cannot apply command because number of arguments is not enough, type -h for help %')
                    self.phtools.printmsg(msg,'red')
                    return -1        
                

                fullpath = ('%s/%s/%s') % (default_calib_dir,pkgname,subdir)
                print 'Fullpath is ',fullpath
                locfilepath = ''
                f = []
                for (dirpath, dirnames, filenames) in walk(fullpath):
                    print (dirpath,dirnames)
                    if subdir2 in dirnames:
                        subdpath = ('%s/%s') % (dirpath,subdir2)
                        print 'Sub dir path is:', subdpath
                        for (sdirpath, sdirnames, fnames) in walk(subdpath):
                            locfilepath = sdirpath
                            f.extend(fnames)
                            break
                    
                
                for afile in f:        
                    fname = ('%s/%s') % (locfilepath,afile)
                    print 'File ',afile,' is link: ',os.path.islink(fname)
                    if os.path.islink(fname):
                        print 'Skip sym link ',afile
                    else:
                        print 'commit file :', afile
                        fname = afile
                        if '2015' in afile:
                            fname = fname.replace('2015','')
                        cmd = ('./calibcli.py --url=$PHYSCONDDB_SRV commit %s %s/%s /%s/%s %s %s') % (pkgname,locfilepath,afile,pkgname,subdir,subdir2,fname)
                        print cmd
                return 0
                        
                        
            except Exception, e:
                sys.exit("failed on action LS: %s" % (str(e)))
                raise
                
        elif (self.action=='ADD'):
            try:
                print 'Action add is used to list calibration files in a given package and create the add scripts'
                print 'Found N arguments ',len(self.args)
                pkgname=self.args[0]
                subdir = ''
                subdir=self.args[1]
                subdir2=self.args[2]
                msg = ('ADD: use package name %s and sub directory %s') % (pkgname,subdir)
                if len(self.args) >= 3 :
                    self.phtools.printmsg(msg,'cyan')
                else:
                    msg = ('ADD: cannot apply command because number of arguments is not enough, type -h for help %')
                    self.phtools.printmsg(msg,'red')
                    return -1        
                

                fullpath = ('%s/%s/%s') % (default_calib_dir,pkgname,subdir)
                print 'Fullpath is ',fullpath
                locfilepath = ''
                f = []
                for (dirpath, dirnames, filenames) in walk(fullpath):
                    print (dirpath,dirnames)
                    if subdir2 in dirnames:
                        subdpath = ('%s/%s') % (dirpath,subdir2)
                        print 'Sub dir path is:', subdpath
                        for (sdirpath, sdirnames, fnames) in walk(subdpath):
                            locfilepath = sdirpath
                            f.extend(fnames)
                            break
                    
                
                for afile in f:        
                    fname = ('%s/%s') % (locfilepath,afile)
                    print 'File ',afile,' is link: ',os.path.islink(fname)
                    if os.path.islink(fname):
                        print 'Skip sym link ',afile
                    else:
                        fnamelst = afile.split('.')
                        fname = fnamelst[0]
                        if '2015' in fname:
                            fname = fname.replace('2015','')
                        print 'add file :', fname
                        desc = ('New calibration file %s') % (fname)
                        cmd = ('./calibcli.py --url=$PHYSCONDDB_SRV add %s /%s/%s %s \"%s\"') % (pkgname,pkgname,subdir,fname,desc)
                        print cmd
                return 0
                        
                        
            except Exception, e:
                sys.exit("failed on action ADD: %s" % (str(e)))
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
    CalibDirDriver()

