# -*- Perl -*-

# CAMHelper.pm --
#
# Parses the result of a uname invocation on a remote system
# to determine the platform.
#
# Copyright (c) 2005-2012 Electric Cloud, Inc.
# All rights reserved.
package CAMHelper;

use strict;
use warnings;

use Exporter 'import';
use ElectricCommander;
use XML::Simple;

our @EXPORT = qw(mesg getHostNames rollLog addLogOutput startLogging);

# ------------------------------------------------------------------------
# mesg
#
#      Print debug info to stdout.
#
# Arguments:
#      message     - message to print
# ------------------------------------------------------------------------

sub mesg{

    my $ec = new ElectricCommander({ format => "json" });
    my $xpath = $ec->getProperty('/myJob/keytabName');
    my $keytabName = $xpath->findvalue('//value') if $xpath;

    addLogOutput($keytabName, @_);

    print( join( " ", @_ ) ) unless $::gTESTING;
}

# ------------------------------------------------------------------------
# getHostNames
#
#      Returns array containing hostnames
#
# Arguments:
#  input - string containing the hosts whitespace or comma delimited
# ------------------------------------------------------------------------

sub getHostNames($) {
    my $input = shift;

    my @result = ();
    my @matches = $input =~ /"([^",;]+)"|([^\s",;]+)/g;

    foreach my $matched (@matches) {
        if ($matched) {
            push(@result, $matched);
        }
    }

    if (scalar @matches == 0) {
        mesg("[WARNING] Can't parse input string $input. \n");
    }

    return @result;
}

sub addLogOutput($$) {
    my $ec = new ElectricCommander({ format => "json", abortOnError => '0' });
    my ($keytab, $message)  = @_;

    my $xpath = $ec->getProperty("/kerberosKeytabs/$keytab/ec_deployments/LastDeployment/LogOutput");
    my $logOutput = $xpath->findvalue('//value') if $xpath;

    if ($logOutput) {
        $logOutput .= "\n";
        $logOutput .= $message;
        $ec->modifyProperty("/kerberosKeytabs/$keytab/ec_deployments/LastDeployment/LogOutput",
            {value => $logOutput});
    }
    else {
        $ec->createProperty("/kerberosKeytabs/$keytab/ec_deployments/LastDeployment/LogOutput",
            {value => $message});
    }
}

sub startLogging($) {
    my $ec = new ElectricCommander({ format => "json", abortOnError => '0' });
    my ($keytab) = @_;

    my $xpath = $ec->getProperty("/kerberosKeytabs/$keytab/ec_deployments/LastDeployment");
    my $value = $xpath->findvalue('//propertyId') if $xpath;
    if ($value) {


        $xpath = $ec->getProperty("/kerberosKeytabs/$keytab/ec_deployments/LastDeployment/DeploymentTimestamp");
        my $deploymentTimestamp = $xpath->findvalue('//value') if $xpath;

        $ec->modifyProperty("/kerberosKeytabs/$keytab/ec_deployments/LastDeployment", {newName => $deploymentTimestamp});
        startLogging($keytab)
    }
    else {
        my $deploymentTimestamp = $ec->timestamp();
        $ec->setProperty("/kerberosKeytabs/$keytab/ec_deployments/LastDeployment/DeploymentTimestamp",
            {value => $deploymentTimestamp});
    }

}
1;
