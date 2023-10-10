
// SSOConfiguratorTargetPresenter.java --
//
// SSOConfiguratorTargetPresenter.java is part of ElectricCommander.
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

public class SSOConfiguratorTargetPresenter
    extends PresenterWidget<SSOConfiguratorTargetPresenter.MyView>
{

    //~ Instance fields --------------------------------------------------------

    private SSOConfiguratorModel m_model;

    //~ Constructors -----------------------------------------------------------

    @Inject public SSOConfiguratorTargetPresenter(
            EventBus             eventBus,
            SSOConfiguratorModel model,
            MyView               view)
    {
        super(eventBus, view);
        m_model = model;
    }

    //~ Methods ----------------------------------------------------------------

    @Override protected void onHide()
    {
        m_model.setTargetOS(getView().getTargetOS());
        m_model.addParameter(SSOConfiguratorConstants.HOSTS_LIST, getView().getTargetHosts());
        m_model.addParameter(SSOConfiguratorConstants.INSTALL_DIRECTORY,
            getView().getInstallDirectory());
        m_model.addParameter(SSOConfiguratorConstants.DRIVING_RESOURCE, getView().getDrivingHost());
        m_model.addParameter(SSOConfiguratorConstants.RESTART_REQUIRED,
            getView().isRestartRequired());
    }

    @Override protected void onReset()
    {
        super.onReset();
        getView().showPage();
    }

    boolean isFormValid()
    {

        // before validating clear all the previous errors if any
        getView().clearErrors();

        // perform validation based on user input if any of the required fields
        // are empty then show an error message on the form
        if (StringUtil.isEmpty(getView().getTargetHosts()
                                        .trim())) {
            getView().setTargetHostsError("hostPort", "Target hosts cannot be empty");

            return false;
        }
        else if (StringUtil.isEmpty(getView().getDrivingHost()
                                             .trim())) {
            getView().setTargetHostsError("drivingResource", "Driving resource cannot be empty");

            return false;
        }
        else if (StringUtil.isEmpty(getView().getInstallDirectory()
                                             .trim())) {
            getView().setTargetHostsError(SSOConfiguratorConstants.INSTALL_DIRECTORY,
                "Server install directory cannot be empty");

            return false;
        }

        return true;
    }

    //~ Inner Interfaces -------------------------------------------------------

    public interface MyView
        extends View,
        HasUiHandlers<SSOConfiguratorTargetUiHandlers>
    {

        //~ Methods ------------------------------------------------------------

        void clearErrors();

        void showPage();

        String getDrivingHost();

        String getInstallDirectory();

        String getTargetHosts();

        String getTargetOS();

        String isRestartRequired();

        void setTargetHostsError(
                String key,
                String errorMessage);
    }
}
