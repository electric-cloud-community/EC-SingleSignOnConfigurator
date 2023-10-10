
// SSOConfiguratorSummaryPresenter.java --
//
// SSOConfiguratorSummaryPresenter.java is part of ElectricCommander.
//
// Copyright (c) 2005-2019 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.SSO_Configurator.client;

import java.util.Map;

import com.google.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;

import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import com.electriccloud.commander.client.responses.CommanderError;

public class SSOConfiguratorSummaryPresenter
    extends PresenterWidget<SSOConfiguratorSummaryPresenter.MyView>
    implements SSOConfiguratorSummaryUiHandlers
{

    //~ Instance fields --------------------------------------------------------

    private SSOConfiguratorModel m_model;

    //~ Constructors -----------------------------------------------------------

    @Inject public SSOConfiguratorSummaryPresenter(
            EventBus             eventBus,
            MyView               view,
            SSOConfiguratorModel model)
    {
        super(eventBus, view);
        m_model = model;
    }

    //~ Methods ----------------------------------------------------------------

    @Override public void onBind()
    {
        super.onBind();
        getView().setUiHandlers(this);
    }

    @Override public void onHide() { }

    @Override public void onReset()
    {
        super.onReset();
        getView().showPage(m_model.getParameters(), m_model.getSshPassword(),
            m_model.getWindowsPassword(), m_model.getSshPassphrase());
    }

    @Override public void onReveal()
    {
        super.onReveal();
    }

    public void showError(CommanderError error)
    {
        getView().handleError(error);
    }

    @Override public String getAuthenticationType()
    {
        return m_model.getAuthenticationType();
    }

    //~ Inner Interfaces -------------------------------------------------------

    public interface MyView
        extends View,
        HasUiHandlers<SSOConfiguratorSummaryUiHandlers>
    {

        //~ Methods ------------------------------------------------------------

        void handleError(CommanderError error);

        void showPage(
                Map<String, String> params,
                String              password,
                String              windowsPassword,
                String              passPhrase);
    }
}
