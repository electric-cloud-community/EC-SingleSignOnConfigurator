
// SSOConfiguratorManagementPanelFactory.java --
//
// SSOConfiguratorManagementPanelFactory.java is part of ElectricCommander.
//
// Copyright (c) 2005-2019 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.SSO_Configurator.client;

import org.jetbrains.annotations.NotNull;

import com.google.gwt.core.client.GWT;

import com.google.web.bindery.event.shared.EventBus;

import com.gwtplatform.mvp.client.DelayedBindRegistry;

import com.electriccloud.commander.client.CommanderRequestManager;
import com.electriccloud.commander.gwt.client.Component;
import com.electriccloud.commander.gwt.client.ComponentContext;

import ecinternal.client.InternalComponentBaseFactory;

public class SSOConfiguratorManagementPanelFactory
    extends InternalComponentBaseFactory
{

    //~ Instance fields --------------------------------------------------------

    public final SSOConfiguratorGinjector ginjector = GWT.create(SSOConfiguratorGinjector.class);

    //~ Methods ----------------------------------------------------------------

    @NotNull @Override public Component createComponent(ComponentContext jso)
    {
        return ginjector.getPresenter();
    }

    @Override public void onCommanderInit(
            CommanderRequestManager requestManager,
            String                  divId,
            ComponentContext        context,
            EventBus                eventBus)
    {
        SSOConfiguratorGinModule.setRequestManager(requestManager);
        super.onCommanderInit(requestManager, divId, context, eventBus);
    }

    @Override public void onModuleLoad()
    {

        // Finish binding GWTP objects
        DelayedBindRegistry.bind(ginjector);
        super.onModuleLoad();
    }
}
