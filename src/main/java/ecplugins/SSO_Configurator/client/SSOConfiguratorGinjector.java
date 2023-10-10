
// SSOConfiguratorGinjector.java --
//
// SSOConfiguratorGinjector.java is part of ElectricCommander.
//
// Copyright (c) 2005-2019 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.SSO_Configurator.client;

import com.google.gwt.inject.client.GinModules;

import com.google.inject.Provider;

import com.gwtplatform.mvp.client.proxy.PlaceManager;

import com.electriccloud.commander.gwt.client.Component;
import com.electriccloud.commander.gwt.client.ui.ErrorPanel;
import com.electriccloud.commander.gwt.client.ui.FormBuilder;

import ecinternal.client.gin.CommanderGinjector;

@GinModules({SSOConfiguratorGinModule.class})
@SuppressWarnings({"InterfaceNeverImplemented"})
public interface SSOConfiguratorGinjector
    extends CommanderGinjector
{

    //~ Methods ----------------------------------------------------------------

    Provider<SSOConfiguratorManagementPresenter> getCAManagementPresenter();

    ErrorPanel getErrorPanel();

    FormBuilder getFormBuilder();

    PlaceManager getPlaceManager();

    Component getPresenter();
}
