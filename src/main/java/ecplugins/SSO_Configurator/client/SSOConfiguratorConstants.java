
// SSOConfiguratorConstants.java --
//
// SSOConfiguratorConstants.java is part of ElectricCommander.
//
// Copyright (c) 2005-2019 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.SSO_Configurator.client;

class SSOConfiguratorConstants
{

    //~ Static fields/initializers ---------------------------------------------

    // Common
    static final String PROCEDURE_NAME         = "Launcher";
    static final String CONNECTION_PUBLIC_KEY  = "connectionPublicKey";
    static final String CONNECTION_PRIVATE_KEY = "connectionPrivateKey";
    static final String SSH_USER               = "sshUser";
    static final String INSTALL_DIRECTORY      = "installDirectory";
    static final String DRIVING_RESOURCE       = "drivingResource";
    static final String RESTART_REQUIRED       = "restartRequired";
    static final String CONNECTION_USER        = "connectionUser";
    static final String CONNECTION_CREDENTIAL  = "connectionCredential";
    static final String LINUX_EF_INSTALL_DIR   = "/opt/electriccloud/electriccommander";
    static final String WINDOWS_EF_INSTALL_DIR =
        "C:\\Program Files\\Electric Cloud\\ElectricCommander";

    // authentication screen fields
    static final String PUBLIC_KEY_PATH  = "sshPublicKeyPath";
    static final String PRIVATE_KEY_PATH = "sshPrivateKeyPath";
    static final String SSH_PWD          = "sshPwd";
    static final String PASSPHRASE       = "passPhrase";

    //
    static final String SSH_USER_CRED   = "SSH User";
    static final String SSH_USER_KEY    = "SSH Key";
    static final String WIN_DOMAIN_USER = "Domain User";

    //
    static final String CONNECTION_DOMAIN = "connectionDomain";
    static final String WINDOWS_PASSWORD  = "windowsPassword";
    static final String WINDOWS_USER      = "windowsUser";
    static final String WINDOWS_ACCOUNT   = "windowsAccount";

    //
    static final String SSH_USER_CRED_VALUE    = "sshUserCred";
    static final String SSH_USER_KEY_VALUE     = "sshKey";
    static final String WIN_BUILTIN_USER_VALUE = "winBuiltinUser";
    static final String WIN_LOCAL_USER_VALUE   = "winLocalUser";
    static final String WIN_DOMAIN_USER_VALUE  = "winDomainUser";
    static final String AUTH_SSH_PASSWORD      = "SSH_PASSWORD";
    static final String AUTH_SSH_KEY           = "SSH_KEY";
    static final String AUTH_WINDOWS           = "WINDOWS";
    static final String WINDOWS                = "Windows";
    static final String LINUX                  = "Linux";
    static final String TARGET_OS              = "trgOs";

    // Windows Built-in system accounts
    static final String LOCAL_SYSTEM = "LocalSystem";

    // connection type parameter
    static final String CONNECTION_TYPE_PARAMETER = "connectionType";
    static final String TARGET_SEVER_OS_PARAMETER = "targetServerOperationSystem";
    static final String CONNECTION_TYPE_SSH       = "SSH";
    static final String CONNECTION_TYPE_WMI       = "WMI";

    // different screens that user transitions to
    static final String AUTHENTICATION_SCREEN = "as";
    static final String SUMMARY_SCREEN        = "ss";
    static final String KEYTAB_SCREEN         = "ts";
    static final String HOSTS_LIST            = "hostPort";
}
