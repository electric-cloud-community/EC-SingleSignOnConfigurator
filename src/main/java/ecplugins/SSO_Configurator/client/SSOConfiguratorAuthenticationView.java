
// SSOConfiguratorAuthenticationView.java --
//
// SSOConfiguratorAuthenticationView.java is part of ElectricCommander.
//
// Copyright (c) 2005-2019 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.SSO_Configurator.client;

import org.jetbrains.annotations.TestOnly;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.Widget;

import com.google.inject.Inject;

import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import com.electriccloud.commander.client.util.StringUtil;
import com.electriccloud.commander.gwt.client.ui.FormBuilder;
import com.electriccloud.commander.gwt.client.ui.ValuedListBox;

public class SSOConfiguratorAuthenticationView
    extends ViewWithUiHandlers<SSOConfiguratorAuthenticationUiHandlers>
    implements SSOConfiguratorAuthenticationPresenter.MyView
{

    //~ Instance fields --------------------------------------------------------

    // The following are all supplied by Gin injection:
    @UiField UIStyle     style;
    @UiField FormBuilder m_form;
    @UiField(provided = true)
    ValuedListBox        m_authType;
    @UiField(provided = true)
    PasswordTextBox      m_passwordBox;
    @UiField(provided = true)
    PasswordTextBox      m_windowsPasswordBox;
    @UiField(provided = true)
    ValuedListBox        m_windowsAccount;
    @UiField(provided = true)
    PasswordTextBox      m_passphraseBox;
    private final Widget m_widget;

    //~ Constructors -----------------------------------------------------------

    @Inject public SSOConfiguratorAuthenticationView(
            Binder          binder,
            ValuedListBox   listBox,
            PasswordTextBox passwordTextBox,
            PasswordTextBox windowsPasswordTextBox,
            ValuedListBox   windowsAccount,
            PasswordTextBox passphraseTextBox)
    {
        m_authType           = listBox;
        m_passwordBox        = passwordTextBox;
        m_windowsPasswordBox = windowsPasswordTextBox;
        m_windowsAccount     = windowsAccount;
        m_passphraseBox      = passphraseTextBox;

        // create the Authentication Type dropdown and populate it with options.
        setupAuthenticationListBox();

        // create the Service Account dropdown and populate it with options.
        setupServiceAccountListBox();
        m_widget = binder.createAndBindUi(this);
    }

    @TestOnly SSOConfiguratorAuthenticationView()
    {

        // No-op, used only in junit testing.
        m_widget = null;
    }

    //~ Methods ----------------------------------------------------------------

    @Override public Widget asWidget()
    {
        return m_widget;
    }

    @Override public void clearErrors()
    {
        m_form.clearAllErrors();
    }

    @Override public void clearFormFields()
    {
        m_form.setValue(SSOConfiguratorConstants.SSH_USER, "");
        m_form.setValue(SSOConfiguratorConstants.SSH_PWD, "");
        m_form.setValue(SSOConfiguratorConstants.PUBLIC_KEY_PATH, "");
        m_form.setValue(SSOConfiguratorConstants.PRIVATE_KEY_PATH, "");
        m_form.setValue(SSOConfiguratorConstants.PASSPHRASE, "");
        m_form.setValue(SSOConfiguratorConstants.WINDOWS_USER, "");
        m_form.setValue(SSOConfiguratorConstants.WINDOWS_PASSWORD, "");
        m_form.setValue(SSOConfiguratorConstants.CONNECTION_DOMAIN, "");
    }

    public void displayAuthenticationFields(String authVal)
    {
        m_form.setRowVisible(SSOConfiguratorConstants.PUBLIC_KEY_PATH, false);
        m_form.setRowVisible(SSOConfiguratorConstants.PRIVATE_KEY_PATH, false);
        m_form.setRowVisible(SSOConfiguratorConstants.PASSPHRASE, false);
        m_form.setRowVisible(SSOConfiguratorConstants.SSH_USER, false);
        m_form.setRowVisible(SSOConfiguratorConstants.SSH_PWD, false);
        m_form.setRowVisible(SSOConfiguratorConstants.WINDOWS_USER, false);
        m_form.setRowVisible(SSOConfiguratorConstants.WINDOWS_PASSWORD, false);
        m_form.setRowVisible(SSOConfiguratorConstants.CONNECTION_DOMAIN, false);
        m_form.setRowVisible(SSOConfiguratorConstants.WINDOWS_ACCOUNT, false);

        if (authVal.equals(SSOConfiguratorConstants.SSH_USER_CRED_VALUE)) {
            m_form.setRowVisible(SSOConfiguratorConstants.SSH_USER, true);
            m_form.setRowVisible(SSOConfiguratorConstants.SSH_PWD, true);
        }
        else if (authVal.equals(SSOConfiguratorConstants.SSH_USER_KEY_VALUE)) {
            m_form.setRowVisible(SSOConfiguratorConstants.SSH_USER, true);
            m_form.setRowVisible(SSOConfiguratorConstants.PUBLIC_KEY_PATH, true);
            m_form.setRowVisible(SSOConfiguratorConstants.PRIVATE_KEY_PATH, true);
            m_form.setRowVisible(SSOConfiguratorConstants.PASSPHRASE, true);
        }
        else if (authVal.equals(SSOConfiguratorConstants.WIN_DOMAIN_USER_VALUE)) {
            m_form.setRowVisible(SSOConfiguratorConstants.WINDOWS_USER, true);
            m_form.setRowVisible(SSOConfiguratorConstants.WINDOWS_PASSWORD, true);
            m_form.setRowVisible(SSOConfiguratorConstants.CONNECTION_DOMAIN, true);
        }
        else if (authVal.equals(SSOConfiguratorConstants.WIN_LOCAL_USER_VALUE)) {
            m_form.setRowVisible(SSOConfiguratorConstants.WINDOWS_USER, true);
            m_form.setRowVisible(SSOConfiguratorConstants.WINDOWS_PASSWORD, true);
        }
        else {
            m_form.setRowVisible(SSOConfiguratorConstants.WINDOWS_ACCOUNT, true);
        }
    }

    public void onAuthenticationChange(ValueChangeEvent stringValueChangeEvent)
    {
        displayAuthenticationFields((String) stringValueChangeEvent.getValue());
        clearFormFields();
        clearErrors();
    }

    @Override public void showPage(String connectionType)
    {

        // Control authentication fields visibility by connection type
        if (StringUtil.isEmpty(getAuthenticationType())
                || isConnectionTypeChanged(connectionType)) {
            m_authType.clear();

            if (SSOConfiguratorConstants.CONNECTION_TYPE_SSH.equals(connectionType)) {
                m_authType.addItem(SSOConfiguratorConstants.SSH_USER_CRED,
                    SSOConfiguratorConstants.SSH_USER_CRED_VALUE);
                m_authType.addItem(SSOConfiguratorConstants.SSH_USER_KEY,
                    SSOConfiguratorConstants.SSH_USER_KEY_VALUE);
                m_authType.setValue(SSOConfiguratorConstants.SSH_USER_CRED_VALUE);
            }
            else {
                m_authType.addItem(SSOConfiguratorConstants.WIN_DOMAIN_USER,
                    SSOConfiguratorConstants.WIN_DOMAIN_USER_VALUE);
                m_authType.setValue(SSOConfiguratorConstants.WIN_DOMAIN_USER_VALUE);
            }
        }

        displayAuthenticationFields(getAuthenticationType());
    }

    /**
     * Create the Service Account suggest box and populate it with options.
     */
    void setupServiceAccountListBox()
    {

        // populate the type dropdown with some static options
        m_windowsAccount.addItem(SSOConfiguratorConstants.LOCAL_SYSTEM,
            SSOConfiguratorConstants.LOCAL_SYSTEM);

        // set the default value to Local System
        m_windowsAccount.setValue(SSOConfiguratorConstants.LOCAL_SYSTEM);
    }

    /**
     * Create the Authentication Type suggest box and populate it with options.
     */
    private void setupAuthenticationListBox()
    {

        // populate the type dropdown with some static options
        m_authType.addItem(SSOConfiguratorConstants.SSH_USER_CRED,
            SSOConfiguratorConstants.SSH_USER_CRED_VALUE);
        m_authType.addItem(SSOConfiguratorConstants.SSH_USER_KEY,
            SSOConfiguratorConstants.SSH_USER_KEY_VALUE);
        m_authType.addItem(SSOConfiguratorConstants.WIN_DOMAIN_USER,
            SSOConfiguratorConstants.WIN_DOMAIN_USER_VALUE);
        // Hide local user authentication type in 7.0
        // m_authType.addItem(SSOConfiguratorConstants.WIN_LOCAL_USER,
        //        SSOConfiguratorConstants.WIN_LOCAL_USER_VALUE); Hide built-in authentication type
        // in 7.0 m_authType.addItem(SSOConfiguratorConstants.WIN_BUILTIN_USER,
        //       SSOConfiguratorConstants.WIN_BUILTIN_USER_VALUE);

        // Add the value changed handler to hide/show the fields based on the
        // option selected
        m_authType.addValueChangeHandler(new ValueChangeHandler<String>() {
                @Override public void onValueChange(
                        ValueChangeEvent<String> stringValueChangeEvent)
                {
                    onAuthenticationChange(stringValueChangeEvent);
                }
            });
    }

    @Override public String getAuthenticationType()
    {
        return m_authType.getValue();
    }

    @Override public String getPassPhrase()
    {
        return m_form.getValue(SSOConfiguratorConstants.PASSPHRASE);
    }

    @Override public String getPrivateKeyPath()
    {
        return m_form.getValue(SSOConfiguratorConstants.PRIVATE_KEY_PATH);
    }

    @Override public String getPublicKeyPath()
    {
        return m_form.getValue(SSOConfiguratorConstants.PUBLIC_KEY_PATH);
    }

    @Override public String getSSHPassword()
    {
        return m_form.getValue(SSOConfiguratorConstants.SSH_PWD);
    }

    @Override public String getSSHUser()
    {
        return m_form.getValue(SSOConfiguratorConstants.SSH_USER);
    }

    @Override public String getWindowsAccount()
    {
        return m_windowsAccount.getValue();
    }

    @Override public String getWindowsDomain()
    {
        return m_form.getValue(SSOConfiguratorConstants.CONNECTION_DOMAIN);
    }

    @Override public String getWindowsPassword()
    {
        return m_form.getValue(SSOConfiguratorConstants.WINDOWS_PASSWORD);
    }

    @Override public String getWindowsUser()
    {
        return m_form.getValue(SSOConfiguratorConstants.WINDOWS_USER);
    }

    /**
     * Verify is connection type changed and authenticated type is not actual anymore.
     *
     * @param   connectionType
     *
     * @return
     */
    boolean isConnectionTypeChanged(String connectionType)
    {
        return (SSOConfiguratorConstants.CONNECTION_TYPE_WMI.equals(connectionType)
                && (m_authType.getValue()
                              .equals(SSOConfiguratorConstants.SSH_USER_CRED_VALUE)
                    || m_authType.getValue()
                                 .equals(SSOConfiguratorConstants.SSH_USER_KEY_VALUE)))
            || (SSOConfiguratorConstants.CONNECTION_TYPE_SSH.equals(connectionType)
                && (m_authType.getValue()
                              .equals(SSOConfiguratorConstants.WIN_DOMAIN_USER_VALUE)
                    || m_authType.getValue()
                                 .equals(SSOConfiguratorConstants.WIN_LOCAL_USER_VALUE)
                    || m_authType.getValue()
                                 .equals(SSOConfiguratorConstants.WIN_BUILTIN_USER_VALUE)));
    }

    @Override public void setErrorOnForm(
            String fieldName,
            String errorMsg)
    {
        m_form.setErrorMessage(fieldName, errorMsg);
    }

    //~ Inner Interfaces -------------------------------------------------------

    public interface Binder
        extends UiBinder<Widget, SSOConfiguratorAuthenticationView> { }

    public interface UIStyle
        extends CssResource
    {

        //~ Methods ------------------------------------------------------------

        String formPadding();

        @ClassName("form-value")
        String formValue();

        String fullWidth();

        @ClassName("gwt-PasswordTextBox")
        String gwtPasswordTextBox();

        // String showBorder();
        @ClassName("gwt-PopupPanel")
        String gwtPopupPanel();

        @ClassName("gwt-TextBox")
        String gwtTextBox();

        String labelDesc();

        String labelHeader();

        String wizardHeader();
    }
}
