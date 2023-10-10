# -*- Perl -*-
#
# transferToLinux.pl
#
# Copyright (c) 2005-2019 Electric Cloud, Inc.
# All rights reserved.

use strict;
use warnings;
use Net::SSH2;
use Time::HiRes qw(time);
use ElectricCommander;
use CAMHelper;
# Turn off output buffering:
$| = 1;

#------------------------------------------------------------------------------
# main
#
# Performs batch
#
#------------------------------------------------------------------------------


sub main() {
    # Create SSH2 object
    my $ssh2 = Net::SSH2->new();

    my $ec = new ElectricCommander();

    # Create the batch API object
    my $batch = $ec->newBatch('parallel');

    # Create multiple requests
    my @reqIds = (
        $batch->getProperty('targetHostName'),
        $batch->getProperty('connectionCredential'),
        $batch->getProperty('/myJob/keytabFilePath'),
        $batch->getProperty('/myJob/keytabName'),
        $batch->getProperty('/myJob/connectionPublicKey'),
        $batch->getProperty('/myJob/connectionPrivateKey'),
        $batch->getProperty('/myJob/connectionType')
    );

    $batch->submit();

    # Fetch the values
    my $targetHostName = $batch->findvalue($reqIds[0], 'property/value')->value;
    my $connectionCredential = $batch->findvalue($reqIds[1], 'property/value')->value;
    my $keytabFilePath = $batch->findvalue($reqIds[2], 'property/value')->value;
    my $keytabName = $batch->findvalue($reqIds[3], 'property/value')->value;
    my $connectionPublicKey  = $batch->findvalue($reqIds[4], 'property/value')->value;
    my $connectionPrivateKey = $batch->findvalue($reqIds[5], 'property/value')->value;
    my $connectionType = $batch->findvalue($reqIds[6], 'property/value')->value;

    mesg("Credential is  $connectionCredential \n");
    my $targetPathToInstaller = "/tmp/" . $keytabName;

    my $password = "";
    if ("$connectionCredential" ne "") {
        mesg("Attempting to retrieve password\n");
        $password = $ec->getFullCredential('connectionCredential', { value => 'password' })->findvalue('//password')->value();
        mesg("SUCCESS: Retrieved password\n");
    }

    my $connectionUser = $ec->getFullCredential('connectionCredential', { value => 'userName' })->findvalue('//userName');
    if ("$connectionType" eq "SSH_PASSWORD") {
        $ssh2 = connectToHostSSHUserPass($ssh2, $targetHostName, $connectionUser, $password);
    }
    else {
        # Connect and authenticate with target host
        $ssh2 = connectToHostSSHKeys($ssh2, $targetHostName, $connectionUser, $connectionPublicKey, $connectionPrivateKey, $password);
    }

    # Perform file transfer
    transferFileToTarget($ssh2, $keytabFilePath, $targetPathToInstaller);

    # Cleanly disconnect ssh connection
    $ssh2->disconnect();
}

#------------------------------------------------------------------------------
# connectToHostSSHKeys
#
#       Connects to targetHost and performs authentication
#
# Arguments:
#       ssh2            - A Net::SSH2 instance.
#       targetHostName  - hostname we are SSH'ing into
#       sshUser         - The SSH user
#       sshPublicKey    - SSH user's public key.
#       sshPrivateKey   - SSH user's private key.
#       sshPassPhrase   - Passphrase protecting the SSH user's private key.
# Returns:
# Net::SSH2 object connected and authenticated to target host
#------------------------------------------------------------------------------

sub connectToHostSSHKeys($$$$$$) {
    my ($ssh2, $targetHostName, $sshUser, $sshPublicKey, $sshPrivateKey, $sshPassphrase) = @_;

    # Connect to host
    mesg("Attempting to connect via SSH to $targetHostName\n");
    $ssh2->connect("$targetHostName") or die "FAILED: Cannot connect to $targetHostName\n";
    mesg("SUCCESS: Connected via SSH to $targetHostName\n");

    mesg("Attempting to authenticate via SSH key based authentication to $targetHostName\n");
    mesg("Using private key: $sshPrivateKey\n");
    mesg("Using public key: $sshPublicKey\n");
    mesg("SSH User is: $sshUser\n");

    if ($ssh2->auth_publickey($sshUser, $sshPublicKey, $sshPrivateKey, $sshPassphrase)) {
        if ($ssh2->auth_ok) {
            mesg("SUCCESS: SSH Authorization successful\n");
            return $ssh2;
        }
    }
    $ssh2->disconnect();
    die "FAILED: SSH Authorization failed\n";
}

#------------------------------------------------------------------------------
# connectToHostSSHUserPass
#
#       Connects to targetHost and performs authentication
#
# Arguments:
#       ssh2            - A Net::SSH2 instance.
#       targetHostName  - hostname we are SSH'ing into
#       sshUser         - The SSH user
#       sshPassword     - Password for the SSH User
# Returns:
# Net::SSH2 object connected and authenticated to target host
#------------------------------------------------------------------------------

sub connectToHostSSHUserPass($$$$) {
    my ($ssh2, $targetHostName, $sshUser, $sshPassword) = @_;

    mesg("Attempting to connect via SSH to $targetHostName\n");
    # Connect to host
    $ssh2->connect("$targetHostName") or die "FAILED: Cannot connect to $targetHostName\n";
    mesg("SUCCESS: Connected via SSH to $targetHostName\n");

    mesg("Attempting to authenticate via password based authentication to $targetHostName\n");
    mesg("SSH User is: $sshUser\n");
    if ($ssh2->auth_password($sshUser, $sshPassword)) {
        if ($ssh2->auth_ok) {
            mesg("SUCCESS: SSH Authorization successful\n");
            return $ssh2;
        }
    }
    $ssh2->disconnect();
    die "FAILED: SSH Authorization failed\n";
}


#------------------------------------------------------------------------------
# transferFileToTarget
#
# Transfers file from local system to remote system using scp.
#
# Arguments:
#       ssh2 -                - A Net::SSH2 instance.
#       hostPathToInstaller   - A path including file name to the file to be transferred
#       targetPathToInstaller - The destination path including file name
#                               where the file should end up
#------------------------------------------------------------------------------

sub transferFileToTarget($$$) {
    my ($ssh2, $hostPathToInstaller, $targetPathToInstaller) = @_;

    # Sanity check, does the file exist?
    if (!-e "$hostPathToInstaller") {
        $ssh2->disconnect();
        die "FAILED: Could not find file: $hostPathToInstaller\n";
    }
    mesg("File: $hostPathToInstaller\n");
    mesg("Transferring on target server: $targetPathToInstaller\n");

    # Determine size of file in bytes
    my $sizeOfFile = -s "$hostPathToInstaller";

    # Convert sizeOfFile to megabytes (1024 * 1024) = 1048576
    $sizeOfFile = $sizeOfFile / 1048576;

    # Start stopwatch
    my $start = time();

    # Perform scp put operation to target machine
    $ssh2->scp_put("$hostPathToInstaller", "$targetPathToInstaller") or die ("Transferring error");

    # Stop stopwatch
    my $end = time();

    # Report statistics

    mesg("Transferred file $hostPathToInstaller" . ' of size %.2f MB in %.2f' . " seconds" . "\n", $sizeOfFile, $end - $start);

    # Compute mb/s
    if ($end - $start > 0) {
        my $mbPerSec = $sizeOfFile / ($end - $start);
        printf('File transfer rate: %.2f MB/s', $mbPerSec);
    }
}

main() unless $::gTESTING;

1;