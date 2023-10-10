# -*- Perl -*-
#
# setupEcProxy.pl --
#
# Create the ECProxy configuration file
#
# Copyright (c) 2005-2012 Electric Cloud, Inc.
# All rights reserved.

use strict;
use warnings;
use ElectricCommander;
use CAMHelper;
use XML::Simple;
use Socket;

# Turn off output buffering:
$| = 1;

# ------------------------------------------------------------------------
# writeConfigFile
#
#      Writes the configuration file that is passed to ecproxy.pl
#
#
# Arguments:
#      targetHostName, the target machine's host name.
#      customization, the credentials needed to log into the target machine
#      workingDirectory, the working directory on the target machine
#      command file
#      args
#      fileName, name of config file.
#      agentDomain
# ------------------------------------------------------------------------

sub writeConfigFile($$$$$$$$) {

    my ($targetHostName, $customization, $wd, $shellCAM, $cmdFile, $fileName, $connectionType, $targetHostDomain)
        = @_;
    my $config;
    if ($connectionType ne "WINDOWS" && $connectionType ne "PSEXEC") {
        $config = {
            targetHostName   => $targetHostName,
            customization    => $customization,
            workingDirectory => $wd,
            agentCommandFile => $cmdFile,
            commandLine      => { arg => [ $shellCAM, $cmdFile ] }
        };
    }

    if ($connectionType eq "WINDOWS") {

        # NMB-27631  Some issue with Deploy Keytab on windows with Authentication Credssp
        # NMB-24295 we need CredSSP authentication only to have access to Windows AD via WinRM connection
        my $drivingHostDomain = $ENV{'USERDOMAIN'};

        my $wmiConnectExtras = '';
        if (defined($targetHostDomain) &&  defined($drivingHostDomain) && (uc($targetHostDomain) eq uc($drivingHostDomain))) {
            $wmiConnectExtras .= " -Authentication Credssp ";
        }

        $config = {
            protocol         => lc 'WMI',
            targetHostName   => $targetHostName,
            customization    => $customization,
            workingDirectory => $wd,
            agentCommandFile => $cmdFile,
            commandLine      => { arg => [ $cmdFile ] },
            wmiConnectExtras => $wmiConnectExtras

        };
    }


    # Parse the port number if it exists
    my @result = split(':', $targetHostName);

    # A port was specified
    if (@result > 1) {

        # Put the ip/host (stripped off port) in targetHostName
        $config->{targetHostName} = $result[0];

        # Put the port in targetPort
        $config->{targetPort} = $result[1];
    }

    open(FILE, '>', $fileName) || die "Unable to write $fileName: $!";
    binmode(FILE);
    print FILE XMLout(
        $config,
        NoAttr   => 1,
        RootName => "proxyConfig",
        XMLDecl  => 1
    );
    close(FILE);
}

#------------------------------------------------------------------------------
# main
#
#------------------------------------------------------------------------------

sub main() {
    my $ec = new ElectricCommander({ format => 'json' });
    my $batch = $ec->newBatch();
    my @reqIds = (
        $batch->expandString(q{$[targetHostName]}),
        $batch->expandString(q{$[/myJob/proxyCustomization]}),
        $batch->expandString(q{$[commandFile]}),
        $batch->expandString(q{$[shellCAM]}),
        $batch->expandString(q{$[ECProxyCfgFile]}),
        $batch->expandString(q{$[/myJob/connectionType]}),
        $batch->expandString(q{$[/myJob/connectionDomain]}),
    );
    $batch->submit();

    my ($targetHostName, $proxyCustomization, $commandFile, $shellCAM,
        $ECProxyCfgFile, $connectionType, $connectionDomain)
        = map {$batch->findvalue($_, "value");} @reqIds;

    if ($connectionType ne "WINDOWS") {
        writeConfigFile($targetHostName, $proxyCustomization, "/tmp",
            $shellCAM, $commandFile, $ECProxyCfgFile, $connectionType, $connectionDomain);
    }
    elsif ($connectionType eq "WINDOWS") {
        # Prepare tmp folder on remote host
        my $tempPath = '$Env:temp';
        print "Using temp dir: $tempPath for temp files.\n";

        my $targetHostNameWindows = '';

        $targetHostNameWindows = gethostbyaddr(inet_aton($targetHostName), AF_INET) or do {
            mesg("[WARNING] Failed to resolve address $targetHostName, trying to use provided hostname.");
            $targetHostNameWindows = $targetHostName;
        };

        $targetHostNameWindows =~ s/^\s+|\s+$//g;

        print "Using discoverd Windows hostname for connection: $targetHostNameWindows instead of $targetHostName.\n";

        writeConfigFile($targetHostNameWindows, $proxyCustomization, $tempPath,
            $shellCAM, $commandFile, $ECProxyCfgFile, $connectionType, $connectionDomain);
    }
    else {
        print "Invalid connection type.";
    }
}

main() unless $::gTESTING;

1;
