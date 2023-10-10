#!/bin/bash

projectName=EC-SingleSignOnConfigurator-1.0.5.0

camHelper=`cat CAMHelper.pm`
ectool setProperty "/projects/$projectName/CAMHelper.pm" --value "$camHelper"

prepareDeployment=`cat prepareDeployment.pl`
ectool setProperty "/projects/$projectName/prepareDeployment" --value "$prepareDeployment"

spinupDeployAndConfigure=`cat spinupDeployAndConfigure.pl`
ectool setProperty "/projects/$projectName/spinupDeployAndConfigure" --value "$spinupDeployAndConfigure"

transferToLinux=`cat transferToLinux.pl`
ectool setProperty "/projects/$projectName/transferToLinux" --value "$transferToLinux"

transferToWindows=`cat transferToWindows.pl`
ectool setProperty "/projects/$projectName/transferToWindows" --value "$transferToWindows"

transferToWindows=`cat transferToWindows.pl`
ectool setProperty "/projects/$projectName/transferToWindows" --value "$transferToWindows"

setupEcProxy=`cat setupEcProxy.pl`
ectool setProperty "/projects/$projectName/setupEcProxy" --value "$setupEcProxy"

constructArgs=`cat constructArgs.pl`
ectool setProperty "/projects/$projectName/constructArgs" --value "$constructArgs"

