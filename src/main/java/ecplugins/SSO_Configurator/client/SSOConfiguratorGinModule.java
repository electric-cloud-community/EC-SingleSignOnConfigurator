
// SSOConfiguratorGinModule.java --
//
// SSOConfiguratorGinModule.java is part of ElectricCommander.
//
// Copyright (c) 2005-2019 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.SSO_Configurator.client;

import java.util.logging.Logger;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import com.google.web.bindery.event.shared.EventBus;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.gwtplatform.mvp.client.proxy.ParameterTokenFormatter;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TokenFormatter;

import com.electriccloud.commander.client.CommanderRequestManager;
import com.electriccloud.commander.gwt.client.Component;
import com.electriccloud.commander.gwt.client.GlobalEventBus;
import com.electriccloud.commander.gwt.client.protocol.xml.WrappedCommanderRequestManagerImpl;
import com.electriccloud.commander.gwt.client.ui.ErrorPanel;
import com.electriccloud.commander.gwt.client.ui.FormBuilder;
import com.electriccloud.commander.gwt.client.ui.RadioButtonGroup;
import com.electriccloud.commander.gwt.client.ui.UIFactory;
import com.electriccloud.commander.gwt.client.ui.ValuedListBox;
import com.electriccloud.commander.gwt.client.util.Log;

import ecinternal.client.ComponentController;
import ecinternal.client.ComponentControllerImpl;
import ecinternal.client.LoggerLog;
import ecinternal.client.ui.InternalUIFactory;
import ecinternal.client.ui.impl.InternalUIFactoryImpl;

public class SSOConfiguratorGinModule
    extends AbstractPresenterModule
{

    //~ Static fields/initializers ---------------------------------------------

    private static LoggerLog               s_loggerLog      = null;
    private static CommanderRequestManager s_requestManager;

    //~ Methods ----------------------------------------------------------------

    @Override protected void configure()
    {
        bind(TokenFormatter.class).to(ParameterTokenFormatter.class);

        // bind classes needed for loading the ParameterPanel and MVP
        // classes in place
        bind(com.google.gwt.event.shared.EventBus.class).to(GlobalEventBus.class)
                                                        .in(Singleton.class);
        bind(EventBus.class).to(com.google.gwt.event.shared.EventBus.class);
        bind(Log.class).to(LoggerLog.class);
        bind(UIFactory.class).to(InternalUIFactory.class);
        bind(InternalUIFactory.class).to(InternalUIFactoryImpl.class);
        bind(ComponentController.class).to(ComponentControllerImpl.class)
                                       .asEagerSingleton();
        bind(InternalUIFactoryImpl.class).in(Singleton.class);
        bind(PlaceManager.class).to(SSOConfiguratorManagementPlaceManager.class)
                                .in(Singleton.class);
        bind(SSOConfiguratorModel.class).in(Singleton.class);
        bindPresenter(SSOConfiguratorManagementPresenter.class,
            SSOConfiguratorManagementPresenter.MyView.class, SSOConfiguratorManagementView.class,
            SSOConfiguratorManagementPresenter.MyProxy.class);
        bind(Component.class).to(SSOConfiguratorManagementPresenter.class);
        bindPresenterWidget(SSOConfiguratorTargetPresenter.class,
            SSOConfiguratorTargetPresenter.MyView.class, SSOConfiguratorTargetView.class);
        bindPresenterWidget(SSOConfiguratorAuthenticationPresenter.class,
            SSOConfiguratorAuthenticationPresenter.MyView.class,
            SSOConfiguratorAuthenticationView.class);
        bindPresenterWidget(SSOConfiguratorSummaryPresenter.class,
            SSOConfiguratorSummaryPresenter.MyView.class, SSOConfiguratorSummaryView.class);
    }

    @Provides CommanderRequestManager provideRequestManager()
    {

        if (s_requestManager == null) {
            setRequestManager(new WrappedCommanderRequestManagerImpl());
        }

        return s_requestManager;
    }

    @Provides ErrorPanel providerErrorPanel(InternalUIFactory uiFactory)
    {
        return uiFactory.createErrorPanel();
    }

    @Provides FormBuilder providerFormBuilder(InternalUIFactory uiFactory)
    {
        return uiFactory.createFormBuilder();
    }

    @Provides LoggerLog providerLoggerLog()
    {

        if (s_loggerLog == null) {
            final Logger logger = Logger.getLogger("ec.CoreComponent");

            s_loggerLog = new LoggerLog(logger);
        }

        return s_loggerLog;
    }

    @Provides RadioButtonGroup providerRadioButtonGroup(InternalUIFactory uiFactory)
    {
        return uiFactory.createRadioButtonGroup("cam");
    }

    @Provides ValuedListBox providerValuedListBox(InternalUIFactory uiFactory)
    {
        return uiFactory.createValuedListBox();
    }

    //~ Methods ----------------------------------------------------------------

    public static void setRequestManager(CommanderRequestManager requestManager)
    {

        if (s_requestManager == null) {
            s_requestManager = requestManager;
        }
    }
}
