# -*- Perl -*-
#
# transferToWindows.pl
#
# Copyright (c) 2005-2019 Electric Cloud, Inc.
# All rights reserved.

use strict;
use warnings;
use File::Temp;
use Time::HiRes qw( time );
use ElectricCommander;
use CAMHelper;
use File::Basename;

# Turn off output buffering:
$| = 1;

my $powershell_exe = "powershell.exe";

# ------------------------------------------------------------------------
# powershell
#
#       Runs the provided script under Windows PowerShell.
#
# Arguments:
#       script -    Script to run.
#
# Result:
#       Exit status of PowerShell.
# ------------------------------------------------------------------------

sub powershell($)
{
    my ($script) = @_;

    if ($^O ne "MSWin32") {
        die "WMI support is only available on Windows.\n";
    }

    my $scriptFile = File::Temp->new(SUFFIX => '.ps1');
    $scriptFile->autoflush;
    $scriptFile->unlink_on_destroy(0);
    my $scriptFilename = $scriptFile->filename;
    print $scriptFile $script;
    close $scriptFile;

    # When we switch to Perl 5.16.0, this might be more elegant
    # with IPC::Run instead of IPC::Open3.

    system "$powershell_exe set-executionpolicy Bypass"
        || die "Couldn't run $powershell_exe set-executionpolicy Bypass: $!";

    # Using IPC::Open2 and IPC::Open3 to talk to the child process
    # somehow fails to close the input correctly, leaving the process
    # to hang, even when I explicitly close(CHILDIN).

    my $pid = open(CHILDOUT,
        "$powershell_exe -NonInteractive -File $scriptFilename |")
        || die "Couldn't invoke $powershell_exe: $!";

    while (<CHILDOUT>) {
        print $_;
    }
    waitpid($pid, 0);

    my $exitStatus;

    #PowerShell uses bigendian???
    #and perl have only one byte for exit code
    if($? & 255) {
        $exitStatus = 1;
    }
    else {
        $exitStatus = $? >> 8;
    }

    unlink $scriptFilename;

    return $exitStatus;
}

# ------------------------------------------------------------------------
# transferFileToTarget
#
#       Copy installer to the specified remote host.
#
# Arguments:
#       hostPathToInstaller - Path to installer on running host.
#       targetHostName      - Name of the target machine to which
#                             the installer should be transfered.
#       targetInstallerName - Installer name for remote host.
#       username            - If nonempty, this will be used
#                             to generate a credential to use
#                             when talking to the remote machine.
#       password            - Password to use with username.
#
# Result:
#       Exit status from PowerShell.
# ------------------------------------------------------------------------

sub transferFileToTarget($$$$$)
{
    my ($hostPathToInstaller, $targetHostName, $targetInstallerName, $username, $password) = @_;

    # This is the template PowerShell script that gets invoked.
    my $invoker = <<'SCRIPT';
## Initial required parameters
$username = 'FNORDUSERNAMEFNORD';
$password = 'FNORDPASSWORDFNORD';

$hostPathToInstaller = 'FNORDHOSTPATHTOINSTALLERFNORD'
$targetInstallerName = 'FNORDTARGETINSTALLERNAMEFNORD'

## Prepare secure password and credential
$securePassword = ConvertTo-SecureString -AsPlainText -Force $password;
$credential = New-Object System.Management.Automation.PSCredential($username, $securePassword);

Try {
    ## Determine tmp folder on remote host for pointed user
    $session = New-PSSession -ComputerName FNORDTARGETHOSTNAMEFNORD -Credential $credential
} Catch {
    echo '[ERROR] Failed to create connection session to remote host with error:';
    echo $_;
    exit 1;
}

Try {
    $remoteTmpFolder = Invoke-Command -ErrorAction Stop -Session $session -Scriptblock {$ENV:tmp}
} Catch {
    echo '[ERROR] Failed to determine path of temporary folder on remote host with error:';
    echo $_;
    exit 1;
} Finally {
    # Cleanup session to remote host
    Remove-PSSession -Session $session
}

## Determine version of powershell
$psVersion = ((Get-Host).Version).Major;
echo "PowerShell version is: $psVersion";
if ( $psVersion -le 2 ) {
    echo '[ERROR] At least v.3 of PowerShell is required.';
    exit 1;
}

## Mount remote network share
Try {
    $tmpEFDrive = 'tmp_EF_drive'
    echo "Drive name is: $tmpEFDrive";

    ## Mount PowerShell tmp network drive
    New-PSDrive -Credential $credential -Name $tmpEFDrive -PSProvider filesystem -Root \\FNORDTARGETHOSTNAMEFNORD\C$
} Catch {
    echo '[ERROR] Failed to mount network share from remote host with error:';
    echo $_;
    exit 1;
}

Try {
  ## Prepare destination path
  $targetPathToInstaller = $tmpEFDrive + $remoteTmpFolder.Substring(1) + '\' + $targetInstallerName
  $targetPathToInstaller

  ## Transfer installer
  echo "Copy-Item $hostPathToInstaller $targetPathToInstaller"
  Copy-Item $hostPathToInstaller $targetPathToInstaller
} Catch {
    echo '[ERROR] Failed to transfer installer to remote host with error:';
    echo $_;
    exit 1;
} Finally {
    ## Unmount PowerShell tmp network drive
    Get-PSDrive $tmpEFDrive | Remove-PSDrive
}
SCRIPT

    # Do all the substitutions for the script.
    $invoker =~ s/FNORDUSERNAMEFNORD/$username/g;
    $invoker =~ s/FNORDHOSTPATHTOINSTALLERFNORD/$hostPathToInstaller/g;
    $invoker =~ s/FNORDTARGETINSTALLERNAMEFNORD/$targetInstallerName/g;
    $invoker =~ s/FNORDTARGETHOSTNAMEFNORD/$targetHostName/g;

    mesg("PowerShell script is:\n$invoker");

    # Escape characters ' in password because it quoted in ''
    $password =~s/'/''/g;
    $invoker =~ s/FNORDPASSWORDFNORD/$password/g;

    return powershell($invoker);
}

#------------------------------------------------------------------------------
# main
#
# Performs batch
#
#------------------------------------------------------------------------------

sub main(){
    my $ec = new ElectricCommander();

    # Create the batch API object
    my $batch = $ec->newBatch('parallel');

    # Create multiple requests
    my @reqIds = (
        $batch->getProperty('targetHostName'),
        $batch->getProperty('/myJob/connectionDomain'),
        $batch->getProperty('connectionCredential'),
        $batch->getProperty('/myJob/keytabFilePath')
    );

    # Send off the requests
    $batch->submit();

    # Fetch the values
    my $targetHostName               = $batch->findvalue($reqIds[0], 'property/value')->value;
    my $domain               = $batch->findvalue($reqIds[1], 'property/value')->value;
    my $connectionCredential = $batch->findvalue($reqIds[2], 'property/value')->value;
    my $keytabFilePath = $batch->findvalue($reqIds[3], 'property/value')->value;


    my $fileName = basename($keytabFilePath);

    my $connectionUser = $ec->getFullCredential('connectionCredential', {value=>'userName'})->findvalue('//userName');

    $targetHostName =~ s/^\s+|\s+$//g;

    mesg ("Target host name: $targetHostName \n");

    if ("$domain" ne "") {
        $connectionUser = $domain . "\\" . $connectionUser;
    }

    my $password = "";
    if("$connectionCredential" ne ""){
        mesg ("Attempting to retrieve password\n");
        $password = $ec->getFullCredential('connectionCredential', {value=>'password'})->findvalue('//password')->value();
        mesg ("SUCCESS: Retrieved password\n");
    }

    # Perform file transfer
    return transferFileToTarget($keytabFilePath, $targetHostName, $fileName, $connectionUser, $password);
}
exit main() unless $::gTESTING;

1;
