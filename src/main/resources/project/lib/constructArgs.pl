# -*- Perl -*-
#
# constructArgs.pl --
#
# Transforms user input into a format ecproxy understands.
#
#
# Copyright (c) 2005-2012 Electric Cloud, Inc.
# All rights reserved.

use strict;
use warnings;
use File::Temp qw/ tempfile tempdir /;
use ElectricCommander;

# Turn off output buffering:
$| = 1;

#------------------------------------------------------------------------------
# main
#
#------------------------------------------------------------------------------

sub main() {

    my $ec = new ElectricCommander();

    # Create the batch API object
    my $batch = $ec->newBatch('parallel');

    # Create multiple requests
    my @reqIds = (
        $batch->getProperty('/myJob/connectionPublicKey'),
        $batch->getProperty('/myJob/connectionPrivateKey'),
        $batch->getProperty('connectionCredential'),
        $batch->getProperty('/myJob/connectionType'),
        $batch->getProperty('/myJob/connectionDomain')
    );

    # Send off the requests
    $batch->submit();

    # Fetch the values
    my $connectionPublicKey     = $batch->findvalue( $reqIds[0], 'property/value' )->value;
    my $connectionPrivateKey    = $batch->findvalue( $reqIds[1], 'property/value' )->value;
    my $connectionCredential    = $batch->findvalue( $reqIds[2], 'property/value' )->value;
    my $connectionType          = $batch->findvalue( $reqIds[3], 'property/value' )->value;
    my $connectionDomain        = $batch->findvalue( $reqIds[4], 'property/value' )->value;

    my $connectionUser          = $ec->getFullCredential('connectionCredential', {value=>'userName'})->findvalue('//userName');

    #Unix platform actions
    if ( "$connectionType" ne "WINDOWS" ) {

        # Construct the ecproxy proxy customization values
        my $ecproxySSHUser       = "setSSHUser(\'$connectionUser\');";
        my $ecproxySSHKeyFiles   = "";
        my $ecproxySSHPassword   = "";
        my $ecproxySSHPassphrase = "";

        if ( $connectionPublicKey && $connectionPrivateKey ) {
            $ecproxySSHKeyFiles =
              "setSSHKeyFiles(\'$connectionPublicKey\',\'$connectionPrivateKey\');";

            # If there is a passphrase set, pass it to ecproxy
            if ( "$connectionType" eq "SSH_KEY" ) {

                $ecproxySSHPassphrase =
                  "setSSHPassphraseCredential(\'$connectionCredential\')";

            }

        }
        else {
            $ecproxySSHPassword = "setSSHPasswordCredential(\'$connectionCredential\')";
        }

        my $ecproxyFunctions =
            "usePty();"
          . $ecproxySSHUser . ' '
          . $ecproxySSHKeyFiles . ' '
          . $ecproxySSHPassword . ' '
          . $ecproxySSHPassphrase;

        # Set them as a property on the job for other steps to use
        $ec->setProperty( "/myJob/proxyCustomization", $ecproxyFunctions );

        # Create a tempfile and save it for referencing later on
        # When running an install or upgrade check for the existence before proceeding to avoid
        # upgrading the host resource
        if(!(-d '/tmp')) {
            mkdir '/tmp';
        }

        my ( $fh, $filename ) = tempfile( DIR => '/tmp' );
        $ec->setProperty( "/myJob/tempfile/filename", $filename );

        # End of Unix specific block
    }
    else {
        if ("$connectionDomain" ne "") {
            $connectionUser = $connectionDomain . "\\" . $connectionUser;
        }

        $ec->setProperty( "/myJob/proxyCustomization",
            "setSSHUser(\'$connectionUser\');" . "setSSHPasswordCredential(\'$connectionCredential\')" );

        # Create a tempfile and save it for referencing later on
        # When running an install or upgrade check for the existence before proceeding to avoid
        # upgrading the host resource
        my ( $fh, $filename ) = tempfile();
        $ec->setProperty( "/myJob/tempfile/filename", $filename );
    }

    # There is no need to do anything for WMI. It will be connected by the same credentials as to the agent.

}

main() unless $::gTESTING;

1;
