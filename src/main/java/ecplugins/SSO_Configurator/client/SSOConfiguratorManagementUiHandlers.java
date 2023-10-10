
// SSOConfiguratorManagementUiHandlers.java --
//
// SSOConfiguratorManagementUiHandlers.java is part of ElectricCommander.
//
// Copyright (c) 2005-2019 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.SSO_Configurator.client;

import com.gwtplatform.mvp.client.UiHandlers;

public interface SSOConfiguratorManagementUiHandlers
    extends UiHandlers
{

    //~ Methods ----------------------------------------------------------------

    void onCancel();

    void onFinish(String pluginName);

    void onNext();

    void onPrevious();
}
