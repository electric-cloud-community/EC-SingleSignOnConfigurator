
// SSOConfiguratorSummaryView.java --
//
// SSOConfiguratorSummaryView.java is part of ElectricCommander.
//
// Copyright (c) 2005-2019 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.SSO_Configurator.client;

import java.util.Map;

import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import com.google.inject.Inject;

import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import com.electriccloud.commander.client.responses.CommanderError;
import com.electriccloud.commander.client.util.StringUtil;
import com.electriccloud.commander.gwt.client.ui.ErrorPanel;

import ecinternal.client.ui.InternalMessages;

import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.CONNECTION_DOMAIN;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.CONNECTION_PRIVATE_KEY;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.CONNECTION_PUBLIC_KEY;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.CONNECTION_USER;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.INSTALL_DIRECTORY;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.RESTART_REQUIRED;

public class SSOConfiguratorSummaryView
    extends ViewWithUiHandlers<SSOConfiguratorSummaryUiHandlers>
    implements SSOConfiguratorSummaryPresenter.MyView
{

    //~ Instance fields --------------------------------------------------------

    private final Widget m_widget;

    // The following are all supplied by Gin injection:
    @UiField protected Grid   m_form;
    @UiField UIStyle          style;
    @UiField Label            m_sshUser;
    @UiField Label            m_sshPwd;
    @UiField Label            m_windowsUser;
    @UiField Label            m_windowsDomain;
    @UiField Label            m_windowsPassword;
    @UiField Label            m_passPhrase;
    @UiField Label            m_sshPublicKey;
    @UiField Label            m_sshPrivateKey;
    @UiField Label            m_installDir;
    @UiField Label            m_title;
    @UiField Label            m_subtitle;
    @UiField Label            m_runLabel;
    @UiField InternalMessages i18N;
    @UiField ErrorPanel       m_error;

    //
    @UiField Label               m_keytabName;
    @UiField Label               m_keytabType;
    @UiField Label               m_targetOperationSystem;
    @UiField Label               m_targetHosts;
    @UiField Label               m_drivingResource;
    @UiField Label               m_restartServer;
    private SSOConfiguratorModel m_model;

    //~ Constructors -----------------------------------------------------------

    @Inject public SSOConfiguratorSummaryView(
            Binder               binder,
            SSOConfiguratorModel model)
    {
        m_widget = binder.createAndBindUi(this);
        m_error.asWidget()
               .setVisible(false);
        m_model = model;
    }

    //~ Methods ----------------------------------------------------------------

    @Override public Widget asWidget()
    {
        return m_widget;
    }

    @Override public void handleError(CommanderError error)
    {
        m_error.asWidget()
               .setVisible(true);
        m_error.clearErrorMessages();
        m_error.addErrorMessage(error.getCode() + ": " + error.getMessage());
    }

    public void populateForm(
            Map<String, String> map,
            String              password,
            String              windowsPassword,
            String              passPhrase)
    {
        m_error.asWidget()
               .setVisible(false);

        String  authenticationType = getUiHandlers().getAuthenticationType();
        boolean sshType            = authenticationType.equals(
                SSOConfiguratorConstants.SSH_USER_CRED_VALUE)
                || authenticationType.equals(SSOConfiguratorConstants.SSH_USER_KEY_VALUE);
        boolean winType            = authenticationType.equals(
                SSOConfiguratorConstants.WIN_DOMAIN_USER_VALUE)
                || authenticationType.equals(SSOConfiguratorConstants.WIN_LOCAL_USER_VALUE);

        m_keytabName.setText(m_model.getKeytabName());
        m_form.getRowFormatter()
              .setVisible(0, true);
        m_keytabType.setText(m_model.getKeytabTypeValue(m_model.getKeytabType()));
        m_form.getRowFormatter()
              .setVisible(1, true);

        if (!StringUtil.isEmpty(map.get(SSOConfiguratorConstants.TARGET_OS))) {
            m_targetOperationSystem.setText(map.get(SSOConfiguratorConstants.TARGET_OS));
            m_form.getRowFormatter()
                  .setVisible(2, true);
        }

        if (!StringUtil.isEmpty(map.get(SSOConfiguratorConstants.HOSTS_LIST))) {
            m_targetHosts.setText((map.get(SSOConfiguratorConstants.HOSTS_LIST)));
            m_form.getRowFormatter()
                  .setVisible(3, true);
        }

        if (!StringUtil.isEmpty(map.get(SSOConfiguratorConstants.DRIVING_RESOURCE))) {
            m_drivingResource.setText((map.get(SSOConfiguratorConstants.DRIVING_RESOURCE)));
            m_form.getRowFormatter()
                  .setVisible(4, true);
        }

        if (!StringUtil.isEmpty(map.get(INSTALL_DIRECTORY))) {
            m_installDir.setText(map.get(INSTALL_DIRECTORY));
            m_form.getRowFormatter()
                  .setVisible(5, true);
        }

        if (!StringUtil.isEmpty(map.get(CONNECTION_USER)) && winType) {
            m_windowsUser.setText(map.get(CONNECTION_USER));
            m_form.getRowFormatter()
                  .setVisible(6, true);
        }

        String PROTECTED = "[PROTECTED]";

        if (!StringUtil.isEmpty(windowsPassword)) {
            m_windowsPassword.setText(PROTECTED);
            m_form.getRowFormatter()
                  .setVisible(7, true);
        }

        if (!StringUtil.isEmpty(map.get(CONNECTION_DOMAIN))) {
            m_windowsDomain.setText(map.get(CONNECTION_DOMAIN));
            m_form.getRowFormatter()
                  .setVisible(8, true);
        }

        if (!StringUtil.isEmpty(map.get(CONNECTION_USER)) && sshType) {
            m_sshUser.setText(map.get(CONNECTION_USER));
            m_form.getRowFormatter()
                  .setVisible(9, true);
        }

        if (!StringUtil.isEmpty(password)) {
            m_sshPwd.setText(PROTECTED);
            m_form.getRowFormatter()
                  .setVisible(10, true);
        }

        if (!StringUtil.isEmpty(map.get(CONNECTION_PUBLIC_KEY))) {
            m_sshPublicKey.setText(map.get(CONNECTION_PUBLIC_KEY));
            m_form.getRowFormatter()
                  .setVisible(11, true);
        }

        if (!StringUtil.isEmpty(map.get(CONNECTION_PRIVATE_KEY))) {
            m_sshPrivateKey.setText(map.get(CONNECTION_PRIVATE_KEY));
            m_form.getRowFormatter()
                  .setVisible(12, true);
        }

        if (!StringUtil.isEmpty(passPhrase)) {
            m_passPhrase.setText(PROTECTED);
            m_form.getRowFormatter()
                  .setVisible(13, true);
        }

        if (!StringUtil.isEmpty(map.get(RESTART_REQUIRED))) {
            String value = "1".equals(map.get(RESTART_REQUIRED))
                ? "Yes"
                : "No";

            m_restartServer.setText(value);
            m_form.getRowFormatter()
                  .setVisible(14, true);
        }
    }

    @Override public void showPage(
            Map<String, String> params,
            String              password,
            String              windowsPassword,
            String              passPhrase)
    {

        //
        hideAllRows();
        populateForm(params, password, windowsPassword, passPhrase);
    }

    private void hideAllRows()
    {

        // by default hide all rows. Show only those which have values
        for (int i = 0; i < m_form.getRowCount(); i++) {
            m_form.getRowFormatter()
                  .setVisible(i, false);
        }
    }

    //~ Inner Interfaces -------------------------------------------------------

    public interface Binder
        extends UiBinder<Widget, SSOConfiguratorSummaryView> { }

    public interface UIStyle
        extends CssResource
    {

        //~ Methods ------------------------------------------------------------

        String finalMessage();

        String fullWidth();

        String gridPadding();

        String gridTextAlign();

        String gridTextValueAlign();

        String labelContent();

        String labelDesc();

        String labelFontMedium();

        String labelHeader();

        String wizardHeader();
    }
}
