
#------------------------------------------------------------------------------
# Remove obsolete ACL entries
#
# Arguments:
#    xpath            - XPath object with ACL entries that should be checked
#    systemObjectName - A name of system object
#
#------------------------------------------------------------------------------
sub removeObsoleteACLEntries($$) {

    my $xpath            = shift;
    my $systemObjectName = shift;

    my $projectPrincipalPrefix = "project: @PLUGIN_KEY@";

    my @aclEntries = $xpath->findnodes('//aclEntry');
    foreach my $node (@aclEntries) {
        my $principalName = $node->findvalue("principalName");
        if (index($principalName, $projectPrincipalPrefix) != -1) {
            print "[DEBUG] Remove obsolete ACL entry $principalName for $systemObjectName \n";
            $commander->deleteAclEntry("user", $principalName,
                {"systemObjectName" => $systemObjectName});
        }
    }
}

if ($promoteAction eq 'promote') {
    # Prepare batch for requsts
    my $batch = $commander->newBatch();

    # Prepare creation ACL requests
    $batch->createAclEntry("user", "project: @PLUGIN_KEY@-@PLUGIN_VERSION@",
        {"systemObjectName"=>"resources",
            "readPrivilege"=>"deny",
            "modifyPrivilege"=>"deny",
            "executePrivilege"=>"deny",
            "changePermissionsPrivilege"=>"deny"
        });
    $batch->createAclEntry("user", "project: @PLUGIN_KEY@-@PLUGIN_VERSION@",
        {"systemObjectName"=>"zonesAndGateways",
            "readPrivilege"=>"deny",
            "modifyPrivilege"=>"deny",
            "executePrivilege"=>"deny",
            "changePermissionsPrivilege"=>"deny"
        });

    # Submit batch requests
    $batch->submit();

    1
} elsif ($promoteAction eq 'demote') {
    # Prepare batch for requsts
    my $batch = $commander->newBatch();

    # Prepare removing ACL requests
    $batch->deleteAclEntry("user", "project: @PLUGIN_KEY@-@PLUGIN_VERSION@",
        {"systemObjectName"=>"resources"});
    $batch->deleteAclEntry("user", "project: @PLUGIN_KEY@-@PLUGIN_VERSION@",
        {"systemObjectName"=>"zonesAndGateways"});

    # Submit batch requests
    $batch->submit();

    1;
}

if ($upgradeAction eq 'upgrade') {
    # Traverse all systemObject resources ACL entries and remove all related to @PLUGIN_KEY@
    my $resourcesXPath = $commander->getAccess({"systemObjectName"=>"resources"});
    removeObsoleteACLEntries($resourcesXPath, "resources");

    # Traverse all systemObject zonesAndGateways ACL entries and remove all related to @PLUGIN_KEY@
    my $zonesAndGatewaysXPath = $commander->getAccess({"systemObjectName"=>"zonesAndGateways"});
    removeObsoleteACLEntries($zonesAndGatewaysXPath, "zonesAndGateways");

    # Prepare batch for requsts
    my $batch = $commander->newBatch();

    # Prepare creation ACL requests
    $batch->createAclEntry("user", "project: @PLUGIN_KEY@-@PLUGIN_VERSION@",
        {"systemObjectName"=>"resources",
            "readPrivilege"=>"deny",
            "modifyPrivilege"=>"deny",
            "executePrivilege"=>"deny",
            "changePermissionsPrivilege"=>"deny"
        });
    $batch->createAclEntry("user", "project: @PLUGIN_KEY@-@PLUGIN_VERSION@",
        {"systemObjectName"=>"zonesAndGateways",
            "readPrivilege"=>"deny",
            "modifyPrivilege"=>"deny",
            "executePrivilege"=>"deny",
            "changePermissionsPrivilege"=>"deny"
        });

    # Submit batch requests
    $batch->submit();

    1;
}
