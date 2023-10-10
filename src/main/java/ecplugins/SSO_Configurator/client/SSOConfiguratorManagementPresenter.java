
// SSOConfiguratorManagementPresenter.java --
//
// SSOConfiguratorManagementPresenter.java is part of ElectricCommander.
//
// Copyright (c) 2005-2019 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.SSO_Configurator.client;

import java.util.Collection;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

import com.google.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;

import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

import com.electriccloud.commander.client.CommanderRequestManager;
import com.electriccloud.commander.client.domain.Resource;
import com.electriccloud.commander.client.responses.CommanderError;
import com.electriccloud.commander.client.responses.CommanderErrorHandler;
import com.electriccloud.commander.client.responses.RunProcedureResponse;
import com.electriccloud.commander.client.responses.RunProcedureResponseCallback;
import com.electriccloud.commander.gwt.client.Component;
import com.electriccloud.commander.gwt.client.ComponentContext;
import com.electriccloud.commander.gwt.client.HasPresenterHandlers;
import com.electriccloud.commander.gwt.client.ui.UIFactory;
import com.electriccloud.commander.gwt.client.util.CommanderUrlBuilder;
import com.electriccloud.commander.gwt.client.util.Log;

import ecinternal.client.presenter.RootComponentPresenter;

public class SSOConfiguratorManagementPresenter
    extends Presenter<SSOConfiguratorManagementPresenter.MyView,
    SSOConfiguratorManagementPresenter.MyProxy>
    implements SSOConfiguratorManagementUiHandlers,
        Component,
        HasPresenterHandlers,
        RunProcedureResponseCallback
{

    //~ Static fields/initializers ---------------------------------------------

    // the popup slot in which the SSO Configurator screen should be set up
    @ContentSlot static final GwtEvent.Type<RevealContentHandler<?>> TYPE_SSOMainSlot =
        new GwtEvent.Type<RevealContentHandler<?>>();

    //~ Instance fields --------------------------------------------------------

    private final RootComponentPresenter           m_rootPresenter;
    private CommanderRequestManager                m_requestManager;
    private final Log                              m_log;
    private ComponentContext                       m_componentContext;
    private UIFactory                              m_uiFactory;
    private SSOConfiguratorModel                   m_model;
    private SSOConfiguratorAuthenticationPresenter m_SSOConfiguratorAuthenticationPresenter;
    private SSOConfiguratorSummaryPresenter        m_SSOConfiguratorSummaryPresenter;
    private SSOConfiguratorTargetPresenter         m_SSOConfiguratorTargetPresenter;

    //~ Constructors -----------------------------------------------------------

    @Inject public SSOConfiguratorManagementPresenter(
            RootComponentPresenter                 rootPresenter,
            EventBus                               eventBus,
            MyView                                 view,
            MyProxy                                proxy,
            Log                                    log,
            SSOConfiguratorModel                   model,
            SSOConfiguratorAuthenticationPresenter SSOConfiguratorAuthenticationPresenter,
            SSOConfiguratorSummaryPresenter        SSOConfiguratorSummaryPresenter,
            SSOConfiguratorTargetPresenter         SSOConfiguratorTargetPresenter)
    {
        super(eventBus, view, proxy);
        m_rootPresenter                          = rootPresenter;
        m_model                                  = model;
        m_log                                    = log;
        m_SSOConfiguratorAuthenticationPresenter = SSOConfiguratorAuthenticationPresenter;
        m_SSOConfiguratorSummaryPresenter        = SSOConfiguratorSummaryPresenter;
        m_SSOConfiguratorTargetPresenter         = SSOConfiguratorTargetPresenter;
    }

    //~ Methods ----------------------------------------------------------------

    @Override public Widget doInit()
    {
        setIncomingParameters();
        onTransitionScreen(SSOConfiguratorConstants.KEYTAB_SCREEN);
        setInSlot(TYPE_SSOMainSlot, m_SSOConfiguratorTargetPresenter);
        getView().setEnabledOkButton(true);

        return getView().asWidget();
    }

    @Override public void handleError(@NotNull CommanderError error)
    {
        m_log.error(error.getCode());
        m_SSOConfiguratorSummaryPresenter.showError(error);
        getView().setButtonVisible(SSOConfiguratorConstants.SUMMARY_SCREEN, false);
    }

    @Override public void handleResponse(@Nullable RunProcedureResponse response)
    {

        // Open JOb's page with the SSO job running.
        assert response != null;

        String         jobId = response.getJobId();
        @NonNls String url   = "link/jobDetails/jobs/" + jobId;

        Window.Location.assign(CommanderUrlBuilder.createUrl(url)
                                                  .buildString());
    }

    @Override public void onBind()
    {
        super.onBind();
        getView().setUiHandlers(this);
    }

    @Override public void onCancel()
    {

        // clear the model data
        m_model.clearModel();

        // clear all forms in all screens
        clearForms();
        Window.Location.assign(Window.Location.getHref());
    }

    @Override public void onFinish(String pluginName)
    {
        m_model.runDeployKeytabProcedure(pluginName, this);
        getView().disableFinishButton();
    }

    @Override public void onHide() { }

    @Override public void onNext()
    {

        // When transitioning to the next screen the following happen
        // 1. set the new screen as Active Screen
        // 2. validate te values entered by user and show error msg if any
        // 3. Save the old screen (user entered) values
        // 4. Show the new screen
        // 5. setInSlot
        // 6. Show the related buttons
        if (m_model.getActiveScreen()
                   .equals(SSOConfiguratorConstants.KEYTAB_SCREEN)) {

            if (m_SSOConfiguratorTargetPresenter.isFormValid()) {
                onTransitionScreen(SSOConfiguratorConstants.AUTHENTICATION_SCREEN);
                setInSlot(TYPE_SSOMainSlot, m_SSOConfiguratorAuthenticationPresenter);
            }
        }
        else if (m_model.getActiveScreen()
                        .equals(SSOConfiguratorConstants.AUTHENTICATION_SCREEN)) {

            if (m_SSOConfiguratorAuthenticationPresenter.isFormValid()) {
                onTransitionScreen(SSOConfiguratorConstants.SUMMARY_SCREEN);
                setInSlot(TYPE_SSOMainSlot, m_SSOConfiguratorSummaryPresenter);
            }
        }

        if (m_model.isConnectionTypeChanged()) {

            // clear forms and parameters map if connection type was changed
            clearForms();
            m_model.setConnectionTypeChanged(false);
            m_model.getParameters()
                   .clear();
        }
    }

    @Override public void onPrevious()
    {

        if (m_model.getActiveScreen()
                   .equals(SSOConfiguratorConstants.AUTHENTICATION_SCREEN)) {
            onTransitionScreen("ts");
            setInSlot(TYPE_SSOMainSlot, m_SSOConfiguratorTargetPresenter);
        }
        else if (m_model.getActiveScreen()
                        .equals(SSOConfiguratorConstants.SUMMARY_SCREEN)) {
            onTransitionScreen(SSOConfiguratorConstants.AUTHENTICATION_SCREEN);
            setInSlot(TYPE_SSOMainSlot, m_SSOConfiguratorAuthenticationPresenter);
            getView().setButtonVisible(m_model.getActiveScreen(), false);
        }
    }

    @Override public void onReset()
    {
        super.onReset();
    }

    @Override public void onReveal()
    {
        super.onReveal();
    }

    @Override public void onUnbind() { }

    @Override protected void revealInParent()
    {
        m_rootPresenter.setInSlot(false, this);
    }

    private void clearForms()
    {

        // clear all the fields and set them to "" on each screen
        m_SSOConfiguratorAuthenticationPresenter.clearForm();
    }

    private void onTransitionScreen(String activeScreen)
    {
        m_model.setActiveScreen(activeScreen);
        getView().setButtonVisible(activeScreen, false);
    }

    @Override public CommanderErrorHandler getCommanderErrorHandler()
    {
        return null;
    }

    @Override public ComponentContext getComponentContext()
    {
        return m_componentContext;
    }

    @Override public Log getLog()
    {
        return m_log;
    }

    @Override public CommanderRequestManager getRequestManager()
    {
        return m_requestManager;
    }

    @Override public UIFactory getUIFactory()
    {
        return m_uiFactory;
    }

    @Override public void setCommanderErrorHandler(CommanderErrorHandler handler) { }

    @Override public void setComponentContext(ComponentContext componentContext)
    {
        m_componentContext = componentContext;
    }

    private void setIncomingParameters()
    {
        String keytabName = m_componentContext.getParameter("keytabName");

        m_model.setKeytabName(keytabName);

        String keytabType = m_componentContext.getParameter("keytabType");

        m_model.setKeytabType(keytabType);
    }

    @Override public void setLog(Log log) { }

    @Override public void setRequestManager(CommanderRequestManager manager)
    {
        m_requestManager = manager;
    }

    @Override public void setUIFactory(UIFactory uiFactory)
    {
        m_uiFactory = uiFactory;
    }

    //~ Inner Interfaces -------------------------------------------------------

    @ProxyStandard public interface MyProxy
        extends Proxy<SSOConfiguratorManagementPresenter> { }

    public interface MyView
        extends View,
        HasUiHandlers<SSOConfiguratorManagementUiHandlers>
    {

        //~ Methods ------------------------------------------------------------

        void disableFinishButton();

        void showButtonPanel(Boolean boolVal);

        void showErrorPanel(
                Collection<Resource> resources,
                boolean              platformsIssue,
                boolean              zonesIssue);

        boolean isEnabledOkButton();

        void setButtonVisible(
                String  screenName,
                boolean error);

        void setEnabledOkButton(boolean isEnable);
    }
}
