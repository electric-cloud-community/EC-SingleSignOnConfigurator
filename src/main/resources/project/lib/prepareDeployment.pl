# -*- Perl -*-
#
# prepareDeployment.pl
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


    my $xpath = $ec->getProperty('/myJob/drivingResource');
    my $drivingResource = $xpath->findvalue('//value') if $xpath;


    $xpath = $ec->getProperty('/myJob/keytabName');
    my $keytabName = $xpath->findvalue('//value') if $xpath;

    startLogging($keytabName);

    $ec->setProperty("/kerberosKeytabs/$keytabName/ec_deployments/LastDeployment/DeploymentStatus",
        {value => "FAILURE"});

    $ec->setProperty("/kerberosKeytabs/$keytabName/ec_deployments/LastDeployment/DrivingResource",
        {value => $drivingResource});


    $xpath = $ec->getProperty('connectionCredential');
    my $connectionCredential = $xpath->findvalue('//value') if $xpath;

    my $connectionUser = $ec->getFullCredential('connectionCredential', {value=>'userName'})->findvalue('//userName');
    my $password =       $ec->getFullCredential( 'connectionCredential', { value => 'password' } )          ->findvalue('//password');

    print(Dumper($connectionCredential));

    $ec->createJobStep(
        {
            jobStepName     => "Keytab Deployment From - $drivingResource",
            parallel        => 1,
            subprocedure    => 'Keytab Management',
            resourceName    => '$[drivingResource]',

            actualParameter => [
                {
                    actualParameterName => 'connectionCredential',
                    value               => $connectionCredential
                } ],

            credential => [
                {
                    credentialName => 'connectionCredential',
                    userName       => $connectionUser,
                    password       => $password
                },
            ]

        }
    );
}

main() unless $::gTESTING;

1;
