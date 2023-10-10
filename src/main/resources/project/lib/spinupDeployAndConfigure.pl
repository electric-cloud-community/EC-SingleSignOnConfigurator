# -*- Perl -*-
#
# spinupDeployAndConfigure.pl
#
# Copyright (c) 2005-2019 Electric Cloud, Inc.
# All rights reserved.

use warnings;
use strict;
use ElectricCommander;
use CAMHelper;
use Data::Dumper;

#------------------------------------------------------------------------------
# main
#
#------------------------------------------------------------------------------
sub main() {

    # Login to commander
    my $ec = new ElectricCommander({ format => "json" });

    # Create the batch API object
    my $batch = $ec->newBatch('parallel');

    # Create multiple requests
    my @reqIds = (
        $batch->getProperty('/myJob/hostnames'),
        $batch->getProperty('connectionCredential'),
        $batch->getProperty('connectionType'),
        $batch->getProperty('keytabFilePath'),
        $batch->getProperty('keytabName'),
        $batch->getProperty('/myJob/keytabType'),
        $batch->getProperty('/myJob/serverInstallationDirectory'),
        $batch->getProperty('/myJob/restartRequired'),
        $batch->getProperty('/myJob/targetServerOperationSystem'),
    );

    $batch->submit();

    # Retrieve the formal parameter to the "Install Agent" procedure.

    my $hostnames = $batch->findvalue($reqIds[0], 'property/value');
    my $connectionCredential = $batch->findvalue($reqIds[1], 'property/value');
    my $connectionType = $batch->findvalue($reqIds[2], 'property/value');
    my $keytabFilePath = $batch->findvalue($reqIds[3], 'property/value');
    my $keytabName = $batch->findvalue($reqIds[4], 'property/value');
    my $keytabType = $batch->findvalue($reqIds[5], 'property/value');
    my $serverInstallationDirectory = $batch->findvalue($reqIds[6], 'property/value');
    my $restartRequired = $batch->findvalue($reqIds[7], 'property/value');
    my $targetServerOperationSystem = $batch->findvalue($reqIds[8], 'property/value');

    # Logging
    $ec->setProperty("/kerberosKeytabs/$keytabName/ec_deployments/LastDeployment/TargetServerInstallationDirectory",
        {value => $serverInstallationDirectory});

    $ec->setProperty("/kerberosKeytabs/$keytabName/ec_deployments/LastDeployment/TargetServerOperationSystem",
        {value => $targetServerOperationSystem});

    $ec->setProperty("/kerberosKeytabs/$keytabName/ec_deployments/LastDeployment/TargetServerHosts",
        {value => $hostnames});


    # Building command for remote execution
    my $pathsep = "/";
    my $ext = "";
    my $arg = "";
    my @extCommands = ();
    my $ecconfigure =  "";

    if ($connectionType eq "WINDOWS"){

        $pathsep = "\\";
        $ext = ".exe";

        if ($keytabType eq "FLOW_SERVER"){

            $arg = "--serverCopyKeytabFile='" . $keytabName . "'";
            if ($restartRequired eq "0") {
                $arg .= " --skipServiceRestart"
            }

        }else {

            $arg = "--webCopyKeytabFile='" . $keytabName . "'";
            if ($restartRequired eq "1"){
                push (@extCommands, "Restart-Service -Name CommanderApache");
            }
        }
        #Remove keytab file
        push (@extCommands, "Remove-Item '$keytabName'");
        $ecconfigure .= "&";
    }else {

        if ($keytabType eq "FLOW_SERVER"){

            $arg = "--serverCopyKeytabFile='/tmp/" . $keytabName . "'";
            if ($restartRequired eq "0") {
                $arg .= " --skipServiceRestart"
            }

        }else {

            $arg = "--webCopyKeytabFile='/tmp/" . $keytabName . "'";
            if ($restartRequired eq "1"){
                push (@extCommands, "/etc/init.d/commanderApache restart");
            }
        }
        #Remove keytab file
        push (@extCommands, "rm '/tmp/$keytabName'");
    }

    $ecconfigure .= "'". $serverInstallationDirectory . $pathsep . "bin" . $pathsep . "ecconfigure" . $ext . "' " . $arg;

    my $extCommand = join(";", @extCommands);

    my $commandToRun = $ecconfigure . ";" . $extCommand;

    my $connectionUser = $ec->getFullCredential('connectionCredential', { value => 'userName' })->findvalue('//userName');

    my $password = "";
    if ("$connectionCredential" ne "") {

        $password =
            $ec->getFullCredential('connectionCredential', { value => 'password' })
                ->findvalue('//password');
    }

    mesg("[INFO] commandToRun: $commandToRun \n");
    # Put each host name into an array
    my @hostArray = getHostNames($hostnames);
    mesg("[INFO] Hosts pointed for keytab deploy: $hostnames \n");


    foreach my $targetHostName (@hostArray) {
        $ec->createJobStep(
            {
                jobStepName     => "Deploy Keytab - $targetHostName",
                parallel        => 1,
                subprocedure    => 'Deploy and Configure Keytab',
                resourceName    => '$[drivingResource]',
                actualParameter => [
                    {
                        actualParameterName => 'commandFile',
                        value               => "command-$targetHostName-"
                            . $ENV{COMMANDER_JOBSTEPID}
                    },
                    {
                        actualParameterName => 'ECProxyCfgFile',
                        value               => "cfg-$targetHostName-"
                            . $ENV{COMMANDER_JOBSTEPID}
                    }
                    ,
                    {
                        actualParameterName => 'targetHostName',
                        value               => $targetHostName
                    }
                    ,
                    {
                        actualParameterName => 'connectionType',
                        value               => $connectionType
                    },
                    {
                        actualParameterName => 'keytabFilePath',
                        value               => $keytabFilePath
                    },
                    {
                        actualParameterName => 'commandToRun',
                        value               => $commandToRun
                    },
                    {
                        actualParameterName => 'connectionCredential',
                        value               => $connectionCredential
                    },
                    {
                        actualParameterName => 'attemptingMessage',
                        value               => 'Attempting to run remove command(s)'
                    }
                ],
                credential      => [
                    {
                        credentialName => 'connectionCredential',
                        userName       => $connectionUser,
                        password       => $password
                    }
                ]
            }
        );
    }

}

main() unless $::gTESTING;

1;
