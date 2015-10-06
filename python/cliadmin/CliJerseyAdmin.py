#!/usr/bin/env python
# encoding: utf-8
'''
cliadmin.CliAdmin -- shortdesc

cliadmin.CliAdmin is a description

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
            self.trace = 'off'
            self.expand = 'false'
            self.iovspan = 'time'
            self.jsondump=False
            self.dump=False
            self.user='none'
            self.passwd='none'
            self.outfilename=''
            self.urlsvc='localhost:8080/physconddb'
            longopts=['help','socks','out=','jsondump','t0=','tMax=','url=','trace=','expand=','iovspan=','user=','pass=']
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
        print "usage: CalibAdmin.py {<options>} <dburl> <action> {<args>}"
        print "Search conditions DB content using args"
        print "DBurl is needed in order to provide the system with the base url"
        print "Action determines which rest method to call"
        print "Subcommands are:"
        print " - FIND <type> <id> <opt> [payload, iovs, tags, globaltags]"
        print "        ex: globaltags MY% : retrieve list of global tags following pattern name"
        print "        ex: globaltags MYTAG trace=on : retrieve one global tag and associated leaf tags"
        print " "
        print "Options: "
        print "  --socks activate socks proxy on localhost 3129 "
        print "  --out={filename} activate dump on filename "
        print "  --jsondump activate a dump of output lines in json format "
        print "  --trace [on|off]: trace associations (ex: globaltag ->* tags or tag ->* iovs "
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
            print 'Search value for ',akey
            if akey in obj:
                outdata[akey] = obj[akey]
            else:
                outdata[akey]=None
                print 'No value is defined for ',akey
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
                
        print "Using options [socks, iovspan,jsondump]"
        print self.useSocks
        print self.iovspan
        print self.jsondump
        
        if (len(args)<2):
            raise getopt.GetoptError("Insufficient arguments - need at least 3, or try --help")
        self.action=args[0].upper()
        self.args=args[1:]
        print self.args
    
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

    def commit(self, params):
        filename=params[0]
        destpath=params[1]
        data={}
    # Search tagRootName in systems
        print 'Search for system given the node full path ',destpath
        data['by']='node'
        data['name']=destpath
        obj = self.restserver.getsystems(data,'/systems/find')
        print obj
        if 'tagNameRoot' in obj:
            print 'Found system...use the tagNameRoot ',obj['tagNameRoot']
        # Now search for HEAD tag, if does exists create it
            tagdata={}
            tagdata['expand']='true'
            tagdata['trace']= 'off'
            tagdata['name']=obj['tagNameRoot']+'-HEAD'
            print 'Search for tag HEAD using ',tagdata
            tagobj = self.restserver.get(tagdata,'/tags')
            print 'Retrieved ',tagobj
            if tagobj is None:
                print 'Create the tag HEAD...'
                objparams={}
                objparams['name']=tagdata['name']
                objparams['timeType']='time'
                objparams['objectType']=filename
                objparams['synchronization']='none'
                objparams['description']='main tag for calibration of '+destpath
                objparams['lastValidatedTime']=0
                objparams['endOfValidity']=0 # This means that it is always valid
                print '    use parameters ',objparams
                response = self.restserver.addJsonEntity(objparams,'/tags')
                if response['response'] == 201:
                    print 'Tag inserted, retrieve to verify'
                else:
                    print 'Error in inserting tag...'
                    return
                tagobj = self.restserver.get(tagdata,'/tags')

# Now store file in IOV[since]=0
            print 'Found tag ',tagobj
            since=0
            sincestr='0'
            print 'Upload iov in tag ',tagdata['name']
            tagid=tagobj['name']
            pylddata = {}
            filetype=filename.split('.')[1]
            argsforpyld='version=1.0;objectType=file;streamerInfo=extension;backendInfo=database'
            argsarr = argsforpyld.split(';')
            for anarg in argsarr:
                key = anarg.split('=')[0]
                val = anarg.split('=')[1]
                pylddata[key]=val
            pylddata['objectType']=filetype
            pylddata['since'] = since
            pylddata['sinceString'] = sincestr
            pylddata['file'] = filename
            pylddata['tag'] = tagid
            response=self.restserver.addPayload(pylddata,'/iovs/payload')
            if response['response'] == 201:
                print 'Iov and payload inserted, retrieve to verify'

        else:
            print 'System does not exists...create it before using commit!'
        
        print 'End of commit...'
 
    def tagit(self, params):
        systemname=params[0]
        systemsglobaltag=params[1]
        data={}
        # Search tagRootName in systems
        print 'Search for system given the provided system name ',systemname
        data['by']='tag'
        data['name']=systemname+'%'
        data['trace']='on'
        systemlist = self.restserver.getsystems(data,'/systems/find')
        print systemlist
        # check if destination global tag exists: if not, create it
        gtagparams = {}
        gtagparams['name']=systemsglobaltag
        gtagparams['trace']='off'
        gtagparams['expand']='false'
        gtag = self.restserver.get(gtagparams,'/globaltags')
        print 'Found global tag ',gtag
        if gtag is None:
            print 'Create the tag HEAD...'
            objparams={}
            objparams['name']=systemsglobaltag
            objparams['lockstatus']='unlocked'
            objparams['validity']='0'
            objparams['release']='1.0'
            objparams['description']='first global tag for calibration of '+systemname
            objparams['snapshotTime']='2050-01-01T00:00:00+02:00'   # Very large snapshot time
            
            print '    use parameters ',objparams
            response = self.restserver.addJsonEntity(objparams,'/globaltags')
            if response['response'] == 201:
                print 'Tag inserted, retrieve to verify'
            else:
                print 'Error in inserting global tag...'
                return
            gtag = self.restserver.get(gtagparams,'/globaltags')
        # Now make the associations
        for asys in systemlist:
            # get the tagNameRoot, search for the -HEAD and associate it to the global tag
            print 'Analyse system ',asys['tagNameRoot']
            tagname = asys['tagNameRoot']
            tagparams = {}
            tagparams['name']=tagname+'%HEAD'
            tagparams['trace']='off'
            tagparams['expand']='true'
            taglist = self.restserver.get(tagparams,'/tags')
            for atag in taglist:
            # link it to the global tag
                print 'Associate tag ',atag['name']
                mapdata={}
                mapdata['globaltagname']=gtag['name']
                mapdata['tagname']=atag['name']
                mapdata['label']='no label'
                mapdata['record']='none'
                self.restserver.addJsonEntity(mapdata,'/maps')

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
        "Execute the command for action "+self.action+" and using arguments : "
        print self.args
        start = datetime.now()
        self.restserver = PhysCurl(self.urlsvc, self.useSocks)
        
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
        
        if (self.action=='CALIB'):
            try:
                print 'Action CALIB is used to simulate calibration actions: commit, tag, lock ... '
                print 'Found N arguments ',len(self.args)
                calaction=self.args[0]
                print 'Method ', calaction, ' will be called '
                calibargs=self.args[1:]
                print '    using arguments: ',calibargs
                if calaction == 'commit':
                    self.commit(calibargs)
                elif calaction=='tag':
                    self.tagit(calibargs)
                elif calaction=='lock':
                    self.lockit(calibargs)
                else:
                    print 'Cannot perform action...'
            except Exception, e:
                sys.exit("failed: %s" % (str(e)))
                raise

        
        if (self.action=='FIND'):
            try:
                print 'Action FIND is used to retrieve object from the DB'
                print 'Found N arguments ',len(self.args)
                object=self.args[0]
                print 'Selecting object type ', object
                name = None
                if len(self.args) > 1:
                    name=self.args[1]
                    print '   use id ', name
                data = {}
                data['trace']=self.trace
                data['expand']=self.expand
                data['name']=name
                objList = []
                if name is None:
                    #print 'Load all ',object
                    linkList = self.restserver.get(data,'/'+object)
                    objList = self.loadItems(linkList['items'])

                if self.trace == 'off':
                    if objList is None or len(objList) == 0:
                        objList = self.restserver.get(data,'/'+object)

                    json_string = json.dumps(objList,sort_keys=True,indent=4, separators=(',', ': '))
                    print json_string
#for obj in objList:
                        #mpobj = self.createObj(object,obj)
                        #print 'found object in list ', obj
                else:
                    obj = self.restserver.get(data,'/'+object)
                    if obj is None:
                        print 'Cannot find any object in database ',object
                        raise
                    json_string = json.dumps(obj,sort_keys=True,indent=4, separators=(',', ': '))
                    print json_string
                    
                    mpobj = self.createObj(object,obj)
                    if mpobj.getValues()['globalTagMaps'] is not None:
                        maplist = mpobj.getValues()['globalTagMaps']
                        for amap in maplist:
                            atag = Tag(amap['systemTag'])
                            gtag = GlobalTag(amap['globalTag'])
                            print atag.toJson()
            
            
            except Exception, e:
                sys.exit("failed: %s" % (str(e)))
                raise

        elif self.action == 'ADD':
            try:
                print 'Action ADD is used to insert a metadata object (globaltags,tags,maps,...) into the DB'
                object=self.args[0]
                print 'Selecting object type ', object
                if object not in ['globaltags','tags','maps','systems']:
                    print 'To add an iov+payload use action STORE'
                    return
                objparams = None
                if len(self.args) > 1:
                    objparams=self.args[1]
                print '   use object parameters: ', objparams
                data = {}
                if objparams is None:
                    print self.helpAdd(object)
                    return
                data = self.createObjParsingArgs(object,objparams)
                print json.dumps(data)
        
                self.restserver.addJsonEntity(data,'/'+object)
        
            except Exception, e:
                sys.exit("failed: %s" % (str(e)))
                raise

        elif self.action == 'DELETE':
            try:
                print 'Action DELETE is used to remove an object (globaltags,tags,maps) from the DB'
                object=self.args[0]
                print 'Selecting object type ', object
                if object not in ['globaltags','tags','maps']:
                    print 'To remove an iov+payload ask an administrator'
                    return
                id = self.args[1]
                print '   use id : ', id
                
                self.restserver.deleteEntity(id,'/'+object)
            
            except Exception, e:
                sys.exit("failed: %s" % (str(e)))
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
                print 'Linking tag',tagobject,' into global tag ', gtagobject
                data = {}
                object = 'maps'
                objparams = None
                if len(self.args) > 2:
                    objparams=self.args[2]
                    print '   use object parameters: ', objparams
                else:
                    print '   missing arguments: "record=xxx;label=yyyy"'
                    return
                data = self.createObjParsingArgs(object,objparams)
                data['globaltagname']=gtagobject
                data['tagname']=tagobject

                self.restserver.addJsonEntity(data,'/'+object)
            
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
            
            except Exception, e:
                sys.exit("failed: %s" % (str(e)))
                raise


        else:
            print "Command not recognized: please type -h for help"


if __name__ == '__main__':
    PhysDBDriver()

