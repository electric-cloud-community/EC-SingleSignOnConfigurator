
// SSOConfiguratorManagementPlaceManager.java --
//
// SSOConfiguratorManagementPlaceManager.java is part of ElectricCommander.
//
// Copyright (c) 2005-2019 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.SSO_Configurator.client;

import com.google.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;

import com.gwtplatform.mvp.client.proxy.PlaceManagerImpl;
import com.gwtplatform.mvp.client.proxy.TokenFormatter;

public class SSOConfiguratorManagementPlaceManager
    extends PlaceManagerImpl
{

    //~ Constructors -----------------------------------------------------------

    @Inject public SSOConfiguratorManagementPlaceManager(
            EventBus       eventBus,
            TokenFormatter tokenFormatter)
    {
        super(eventBus, tokenFormatter);
    }

    //~ Methods ----------------------------------------------------------------

    @Override public void revealDefaultPlace()
    {
        // noop because the main page is actually handling the overall place.
    }
}
