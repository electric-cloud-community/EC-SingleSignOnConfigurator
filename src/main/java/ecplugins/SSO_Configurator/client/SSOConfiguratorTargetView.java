
// SSOConfiguratorTargetView.java --
//
// SSOConfiguratorTargetView.java is part of ElectricCommander.
//
// Copyright (c) 2005-2019 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.SSO_Configurator.client;

import org.jetbrains.annotations.NotNull;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;

import com.google.inject.Inject;

import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import com.electriccloud.commander.client.responses.CommanderError;
import com.electriccloud.commander.client.responses.CommanderErrorHandler;
import com.electriccloud.commander.gwt.client.ui.FormBuilder;
import com.electriccloud.commander.gwt.client.ui.ValuedListBox;

import ecinternal.client.FormValue;
import ecinternal.client.FormValueWrapper;
import ecinternal.client.ResourcePicker;
import ecinternal.client.ui.OptionListLoader;
import ecinternal.client.ui.SuggestBoxWrapper;

import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.INSTALL_DIRECTORY;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.LINUX;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.LINUX_EF_INSTALL_DIR;
import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.WINDOWS_EF_INSTALL_DIR;

public class SSOConfiguratorTargetView
    extends ViewWithUiHandlers<SSOConfiguratorTargetUiHandlers>
    implements SSOConfiguratorTargetPresenter.MyView,
        CommanderErrorHandler
{

    //~ Instance fields --------------------------------------------------------

    private final Widget         m_widget;
    private Label                m_keytabName;
    private Label                m_ketabType;
    private SSOConfiguratorModel m_model;
    @UiField FormBuilder         m_form;
    @UiField(provided = true)
    ValuedListBox                m_resourceNameTemplate;
    @UiField(provided = true)
    ResourcePicker               m_resource;

    //~ Constructors -----------------------------------------------------------

    @Inject public SSOConfiguratorTargetView(
            Binder               binder,
            ValuedListBox        listBox,
            ResourcePicker       resource,
            SSOConfiguratorModel model)
    {
        m_resourceNameTemplate = listBox;
        m_resource             = resource;

        // Initialize the ResourcePicker.
        SuggestBox resourceSuggest = new SuggestBox();

        resourceSuggest.setLimit(20);

        FormValue<String> resourceBox    = new FormValueWrapper(resourceSuggest);
        OptionListLoader  resourceLoader = new SuggestBoxWrapper(resourceSuggest);

        m_resource.configure(resourceBox, resourceLoader, this);
        m_resource.refreshOptions();
        m_widget     = binder.createAndBindUi(this);
        m_model      = model;
        m_keytabName = new Label();
        m_ketabType  = new Label();

        addCustormLabel(m_keytabName, "keytabName", "Keytab Name: ",
            "Name of the keytab that will be deployed", 6);
        addCustormLabel(m_ketabType, "keytabType", "Server Type: ",
            "Type of the server to which the keytab applies to. Valid values are CD Web Server and CD Server",
            5);

        m_form.getRow("keytabType")
                .get(0)
                .getElement()
                .setAttribute("style", "padding-top:18px");
        m_form.getRow("keytabType")
                .get(1)
                .getElement()
                .setAttribute("style", "padding-top:18px");

        setupTargetOS();
        m_resourceNameTemplate.addValueChangeHandler(new ValueChangeHandler<String>() {
                @Override public void onValueChange(ValueChangeEvent<String> event)
                {

                    if (event.getValue()
                             .equals(LINUX)) {
                        m_form.setValue(INSTALL_DIRECTORY, LINUX_EF_INSTALL_DIR);
                    }
                    else {
                        m_form.setValue(INSTALL_DIRECTORY, WINDOWS_EF_INSTALL_DIR);
                    }
                }
            });

        // Default selected dir
        m_form.setValue(INSTALL_DIRECTORY, LINUX_EF_INSTALL_DIR);
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

    @Override public void handleError(@NotNull CommanderError error) { }

    @Override public void showPage()
    {
        m_keytabName.setText(m_model.getKeytabName());
        m_ketabType.setText(m_model.getKeytabTypeValue(m_model.getKeytabType()));
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    private void addActionsForDrivingResource() { }

    private void addCustormLabel(
            IsWidget labelWidget,
            String   propertyName,
            String   labelText,
            String   helpText,
            int      moveUpTimes)
    {
        m_form.addFormRow(propertyName, labelText, labelWidget, false, helpText);
        moveUp(propertyName, moveUpTimes);
        m_form.getRow(propertyName)
              .get(1)
              .getElement()
              .getParentElement()
              .setAttribute("class", "form-value");
        m_form.getRow(propertyName)
              .get(0)
              .getElement()
              .getParentElement()
              .setAttribute("class", "formBuilder-label");
    }

    private void moveUp(
            String elemName,
            int    times)
    {

        for (int i = 0; i < times; i++) {
            m_form.moveUp(elemName);
        }
    }

    private void setupTargetOS()
    {
        m_resourceNameTemplate.addItem(SSOConfiguratorConstants.WINDOWS,
            SSOConfiguratorConstants.WINDOWS);
        m_resourceNameTemplate.addItem(SSOConfiguratorConstants.LINUX,
            SSOConfiguratorConstants.LINUX);

        // Set the default value
        m_resourceNameTemplate.setValue(SSOConfiguratorConstants.LINUX);
    }

    @Override public String getDrivingHost()
    {
        return m_form.getValue("drivingResource");
    }

    @Override public String getInstallDirectory()
    {
        return m_form.getValue(INSTALL_DIRECTORY);
    }

    @Override public String getTargetHosts()
    {
        return m_form.getValue("hostPort");
    }

    @Override public String getTargetOS()
    {
        return m_resourceNameTemplate.getValue();
    }

    @Override public String isRestartRequired()
    {
        return m_form.getValue("restartServer");
    }

    @Override public void setTargetHostsError(
            String key,
            String errorMessage)
    {
        m_form.setErrorMessage(key, errorMessage);
    }

    //~ Inner Interfaces -------------------------------------------------------

    public interface Binder
        extends UiBinder<Widget, SSOConfiguratorTargetView> { }

    @SuppressWarnings("GwtCssResourceErrors")
    public interface UIStyle
        extends CssResource
    {

        //~ Methods ------------------------------------------------------------

        String fullWidth();

        String labelDesc();

        String labelHeader();

        String loading();

        String wizardHeader();
    }
}
