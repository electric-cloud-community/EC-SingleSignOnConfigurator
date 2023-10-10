
// SSOConfiguratorAuthenticationPresenter.java --
//
// SSOConfiguratorAuthenticationPresenter.java is part of ElectricCommander.
//
// Copyright (c) 2005-2019 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.SSO_Configurator.client;

import com.google.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;

import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import com.electriccloud.commander.client.util.StringUtil;

import ecinternal.client.ui.InternalUIFactory;

public class SSOConfiguratorAuthenticationPresenter
    extends PresenterWidget<SSOConfiguratorAuthenticationPresenter.MyView>
    implements SSOConfiguratorAuthenticationUiHandlers
{

    //~ Instance fields --------------------------------------------------------

    private SSOConfiguratorModel m_model;
    private InternalUIFactory    m_factory;

    //~ Constructors -----------------------------------------------------------

    @Inject public SSOConfiguratorAuthenticationPresenter(
            EventBus             eventBus,
            MyView               view,
            SSOConfiguratorModel model,
            InternalUIFactory    uiFactory)
    {
        super(eventBus, view);
        m_model   = model;
        m_factory = uiFactory;
    }

    //~ Methods ----------------------------------------------------------------

    @Override public void onBind()
    {
        super.onBind();
        getView().setUiHandlers(this);
    }

    @Override public void onHide()
    {

        // call to save the authentication page user entered values after validations.
        if (m_model.getConnectionType()
                   .equals(SSOConfiguratorConstants.CONNECTION_TYPE_WMI)) {
            String windowsUser = StringUtil.isEmpty(getView().getWindowsUser())
                ? getView().getWindowsAccount()
                : getView().getWindowsUser();

            m_model.saveAuthenticationDetails(windowsUser, getView().getWindowsPassword(),
                getView().getWindowsDomain(), "", "", "", getView().getAuthenticationType());
        }
        else {
            m_model.saveAuthenticationDetails(getView().getSSHUser(), getView().getSSHPassword(),
                "", getView().getPassPhrase(), getView().getPrivateKeyPath(),
                getView().getPublicKeyPath(), getView().getAuthenticationType());
        }
    }

    @Override public void onReset()
    {
        super.onReset();
        getView().showPage(m_model.getConnectionType());
    }

    @Override public void onReveal()
    {
        super.onReveal();
    }

    void clearForm()
    {
        getView().clearErrors();
        getView().clearFormFields();
    }

    boolean isFormValid()
    {

        // before validating clear all the previous errors if any
        getView().clearErrors();

        // perform validation based on user input if any of the required fields
        // are empty then show an error message on the form
        String authenticationType = getView().getAuthenticationType();

        if (authenticationType.equals(SSOConfiguratorConstants.SSH_USER_CRED_VALUE)) {

            // if authentication type == User Crendentials then User Name and
            // Password are required
            if (StringUtil.isEmpty(getView().getSSHUser())) {
                getView().setErrorOnForm(SSOConfiguratorConstants.SSH_USER,
                    m_factory.getInternalMessages()
                             .sshUserEmpty());

                return false;
            }

            if (StringUtil.isEmpty(getView().getSSHPassword())) {
                getView().setErrorOnForm(SSOConfiguratorConstants.SSH_PWD,
                    m_factory.getInternalMessages()
                             .sshPasswordEmpty());

                return false;
            }
        }
        else if (authenticationType.equals(SSOConfiguratorConstants.SSH_USER_KEY_VALUE)) {

            // if authentication type == SSH KEY then User Name, Public Key
            // Path and Private Key Path are required
            if (StringUtil.isEmpty(getView().getSSHUser())) {
                getView().setErrorOnForm(SSOConfiguratorConstants.SSH_USER,
                    m_factory.getInternalMessages()
                             .sshUserEmpty());

                return false;
            }

            if (StringUtil.isEmpty(getView().getPublicKeyPath())) {
                getView().setErrorOnForm(SSOConfiguratorConstants.PUBLIC_KEY_PATH,
                    m_factory.getInternalMessages()
                             .sshPublicKeyPathEmpty());

                return false;
            }

            if (StringUtil.isEmpty(getView().getPrivateKeyPath())) {
                getView().setErrorOnForm(SSOConfiguratorConstants.PRIVATE_KEY_PATH,
                    m_factory.getInternalMessages()
                             .sshPrivateKeyPathEmpty());

                return false;
            }
        }
        else if (authenticationType.equals(SSOConfiguratorConstants.WIN_LOCAL_USER_VALUE)) {

            // if authentication type == WIN_LOCAL_USER then User Name and
            // Password are required
            if (StringUtil.isEmpty(getView().getWindowsUser())) {
                getView().setErrorOnForm(SSOConfiguratorConstants.WINDOWS_USER,
                    m_factory.getInternalMessages()
                             .windowsUserEmpty());

                return false;
            }

            if (StringUtil.isEmpty(getView().getWindowsPassword())) {
                getView().setErrorOnForm(SSOConfiguratorConstants.WINDOWS_PASSWORD,
                    m_factory.getInternalMessages()
                             .windowsPasswordEmpty());

                return false;
            }
        }
        else if (authenticationType.equals(SSOConfiguratorConstants.WIN_DOMAIN_USER_VALUE)) {

            // if authentication type == WIN_DOMAIN_USER then User Name,
            // Password and Domain are required
            if (StringUtil.isEmpty(getView().getWindowsUser())) {
                getView().setErrorOnForm(SSOConfiguratorConstants.WINDOWS_USER,
                    m_factory.getInternalMessages()
                             .windowsUserEmpty());

                return false;
            }

            if (StringUtil.isEmpty(getView().getWindowsPassword())) {
                getView().setErrorOnForm(SSOConfiguratorConstants.WINDOWS_PASSWORD,
                    m_factory.getInternalMessages()
                             .windowsPasswordEmpty());

                return false;
            }

            if (StringUtil.isEmpty(getView().getWindowsDomain())) {
                getView().setErrorOnForm(SSOConfiguratorConstants.CONNECTION_DOMAIN,
                    m_factory.getInternalMessages()
                             .windowsDomainEmpty());

                return false;
            }
        }

        return true;
    }

    //~ Inner Interfaces -------------------------------------------------------

    public interface MyView
        extends View,
        HasUiHandlers<SSOConfiguratorAuthenticationUiHandlers>
    {

        //~ Methods ------------------------------------------------------------

        void clearErrors();

        void clearFormFields();

        void showPage(String connectionType);

        String getAuthenticationType();

        String getPassPhrase();

        String getPrivateKeyPath();

        String getPublicKeyPath();

        String getSSHPassword();

        String getSSHUser();

        String getWindowsAccount();

        String getWindowsDomain();

        String getWindowsPassword();

        String getWindowsUser();

        void setErrorOnForm(
                String fieldName,
                String errorMsg);
    }
}
