
// SSOConfiguratorModel.java --
//
// SSOConfiguratorModel.java is part of ElectricCommander.
//
// Copyright (c) 2005-2019 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.SSO_Configurator.client;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

import com.google.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;

import com.electriccloud.commander.client.CommanderRequestManager;
import com.electriccloud.commander.client.requests.RunProcedureRequest;
import com.electriccloud.commander.client.responses.RunProcedureResponseCallback;
import com.electriccloud.commander.client.util.StringUtil;

import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.AUTH_SSH_KEY;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.AUTH_SSH_PASSWORD;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.AUTH_WINDOWS;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.CONNECTION_CREDENTIAL;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.CONNECTION_DOMAIN;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.CONNECTION_PRIVATE_KEY;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.CONNECTION_PUBLIC_KEY;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.CONNECTION_TYPE_PARAMETER;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.CONNECTION_TYPE_SSH;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.CONNECTION_TYPE_WMI;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.CONNECTION_USER;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.DRIVING_RESOURCE;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.HOSTS_LIST;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.INSTALL_DIRECTORY;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.LINUX;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.PROCEDURE_NAME;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.RESTART_REQUIRED;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.TARGET_SEVER_OS_PARAMETER;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.WINDOWS;

public class SSOConfiguratorModel
    implements HasHandlers
{

    //~ Instance fields --------------------------------------------------------

    private String                        keytabName;
    private String                        keytabType;
    private final EventBus                m_bus;
    private final CommanderRequestManager m_requestManager;
    private String                        m_activeScreen;
    private String                        m_connectionType        = "";
    private String                        m_sshPassword           = "";
    private String                        m_sshPassphrase         = "";
    private String                        m_windowsPassword       = "";
    private Map<String, String>           m_parameters            = new HashMap<String, String>();
    private boolean                       m_connectionTypeChanged;
    private String                        m_authenticationType;
    private HashMap<String, String>       m_keytabTypes           = new HashMap<String, String>();

    //~ Constructors -----------------------------------------------------------

    @Inject public SSOConfiguratorModel(
            CommanderRequestManager requestManager,
            EventBus                bus)
    {
        m_keytabTypes.put("FLOW_SERVER", "CloudBees CD Server");
        m_keytabTypes.put("FLOW_WEB_SERVER", "CloudBees CD Web Server");
        m_requestManager = requestManager;
        m_bus            = bus;
    }

    //~ Methods ----------------------------------------------------------------

    @Override public void fireEvent(GwtEvent<?> event)
    {
        m_bus.fireEvent(event);
    }

    void addParameter(
            String key,
            String value)
    {
        m_parameters.put(key, value);
    }

    void clearModel()
    {
        m_activeScreen    = "";
        m_sshPassword     = "";
        m_sshPassphrase   = "";
        m_windowsPassword = "";

        m_parameters.clear();
        m_connectionType        = "";
        m_connectionTypeChanged = false;
    }

    void runDeployKeytabProcedure(
            String                                pluginName,
            @NotNull RunProcedureResponseCallback callback)
    {

        // Create the runProcedure request
        RunProcedureRequest request = m_requestManager.getRequestFactory()
                                                      .createRunProcedureRequest();

        // Set the procedure name for the runProcedure request
        // TODO get using appropriate way
        request.setProjectName(pluginName);
        request.setProcedureName(PROCEDURE_NAME);

        String connectionType = m_parameters.get(CONNECTION_TYPE_PARAMETER);

        request.addActualParameter(CONNECTION_TYPE_PARAMETER, connectionType);

        if (connectionType.equals(AUTH_WINDOWS)) {
            request.addActualParameter(TARGET_SEVER_OS_PARAMETER, WINDOWS);
        }
        else {
            request.addActualParameter(TARGET_SEVER_OS_PARAMETER, LINUX);
        }

        // handle credentials
        String connectionUser = m_parameters.get(CONNECTION_USER);
        String publicKeyPath  = m_parameters.get(CONNECTION_PUBLIC_KEY);

        // Pass the ssh password if specified.
        if (m_connectionType.equals(CONNECTION_TYPE_SSH) && StringUtil.isEmpty(publicKeyPath)) {
            request.addCredentialParameter(CONNECTION_CREDENTIAL, connectionUser, m_sshPassword);

            // Set connection type
            request.addActualParameter(CONNECTION_TYPE_PARAMETER, AUTH_SSH_PASSWORD);
        }
        else if (!StringUtil.isEmpty(publicKeyPath)) {
            request.addCredentialParameter(CONNECTION_CREDENTIAL, connectionUser, m_sshPassphrase);

            // Set connection type
            request.addActualParameter(CONNECTION_TYPE_PARAMETER,
                SSOConfiguratorConstants.AUTH_SSH_KEY);
        }
        else if (m_connectionType.equals(CONNECTION_TYPE_WMI)) {
            request.addCredentialParameter(CONNECTION_CREDENTIAL, connectionUser,
                m_windowsPassword);

            // Set connection type
            request.addActualParameter(CONNECTION_TYPE_PARAMETER,
                SSOConfiguratorConstants.AUTH_WINDOWS);
        }

        request.addActualParameter("connectionDomain", m_parameters.get(CONNECTION_DOMAIN));

        String publicKey = m_parameters.get(CONNECTION_PUBLIC_KEY);

        if (!StringUtil.isEmpty(publicKey)) {
            request.addActualParameter("connectionPublicKey", publicKey);
        }

        String privateKey = m_parameters.get(CONNECTION_PRIVATE_KEY);

        if (!StringUtil.isEmpty(privateKey)) {
            request.addActualParameter("connectionPrivateKey", privateKey);
        }

        request.addActualParameter("drivingResource", m_parameters.get(DRIVING_RESOURCE));
        request.addActualParameter("hostnames", m_parameters.get(HOSTS_LIST));
        request.addActualParameter("serverInstallationDirectory",
            m_parameters.get(INSTALL_DIRECTORY));
        request.addActualParameter("restartRequired", m_parameters.get(RESTART_REQUIRED));
        request.addActualParameter("keytabName", getKeytabName());
        request.addActualParameter("keytabType", getKeytabType());
        request.setCallback(callback);
        m_requestManager.doRequest(request);
    }

    void saveAuthenticationDetails(
            String user,
            String password,
            String domain,
            String passphrase,
            String privateKey,
            String publicKey,
            String authType)
    {
        m_authenticationType = authType;
        m_parameters.put(CONNECTION_USER, "");
        m_parameters.put(CONNECTION_DOMAIN, "");
        m_sshPassword = "";
        m_parameters.put(CONNECTION_PUBLIC_KEY, "");
        m_parameters.put(CONNECTION_PRIVATE_KEY, "");
        m_sshPassphrase   = "";
        m_windowsPassword = "";

        if (authType.equals(SSOConfiguratorConstants.SSH_USER_CRED_VALUE)) {
            m_parameters.put(CONNECTION_USER, user);
            m_parameters.put(CONNECTION_TYPE_PARAMETER, AUTH_SSH_PASSWORD);
            m_sshPassword = password;
        }
        else if (authType.equals(SSOConfiguratorConstants.SSH_USER_KEY_VALUE)) {
            m_parameters.put(CONNECTION_USER, user);
            m_parameters.put(CONNECTION_PUBLIC_KEY, publicKey);
            m_parameters.put(CONNECTION_PRIVATE_KEY, privateKey);
            m_parameters.put(CONNECTION_TYPE_PARAMETER, AUTH_SSH_KEY);
            m_sshPassphrase = passphrase;
        }
        else if (authType.equals(SSOConfiguratorConstants.WIN_DOMAIN_USER_VALUE)) {
            m_parameters.put(CONNECTION_USER, user);
            m_parameters.put(CONNECTION_DOMAIN, domain);
            m_windowsPassword = password;
            m_parameters.put(CONNECTION_TYPE_PARAMETER, AUTH_WINDOWS);
        }
        else if (authType.equals(SSOConfiguratorConstants.WIN_LOCAL_USER_VALUE)) {
            m_parameters.put(CONNECTION_USER, user);
            m_windowsPassword = password;
            m_parameters.put(CONNECTION_TYPE_PARAMETER, AUTH_WINDOWS);
        }
        else {
            m_parameters.put(CONNECTION_USER, user);
        }
    }

    String getActiveScreen()
    {
        return m_activeScreen;
    }

    String getAuthenticationType()
    {
        return m_authenticationType;
    }

    String getConnectionType()
    {
        return m_connectionType;
    }

    String getKeytabName()
    {
        return keytabName;
    }

    String getKeytabType()
    {
        return keytabType;
    }

    String getKeytabTypeValue(String key)
    {
        return m_keytabTypes.get(key);
    }

    Map<String, String> getParameters()
    {
        return m_parameters;
    }

    String getSshPassphrase()
    {
        return m_sshPassphrase;
    }

    String getSshPassword()
    {
        return m_sshPassword;
    }

    String getWindowsPassword()
    {
        return m_windowsPassword;
    }

    boolean isConnectionTypeChanged()
    {
        return m_connectionTypeChanged;
    }

    void setActiveScreen(String screen)
    {
        m_activeScreen = screen;
    }

    void setConnectionTypeChanged(boolean connectionTypeChanged)
    {
        m_connectionTypeChanged = connectionTypeChanged;
    }

    void setKeytabName(String keytabName)
    {
        this.keytabName = keytabName;
    }

    void setKeytabType(String keytabType)
    {
        this.keytabType = keytabType;
    }

    void setTargetOS(String targetOS)
    {

        if (targetOS.equals(WINDOWS)) {
            m_connectionType = CONNECTION_TYPE_WMI;
        }
        else {
            m_connectionType = CONNECTION_TYPE_SSH;
        }

        m_parameters.put(SSOConfiguratorConstants.TARGET_OS, targetOS);
    }
}
