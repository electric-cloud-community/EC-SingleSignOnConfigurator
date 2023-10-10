
// SSOConfiguratorManagementView.java --
//
// SSOConfiguratorManagementView.java is part of ElectricCommander.
//
// Copyright (c) 2005-2019 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.SSO_Configurator.client;

import java.util.Collection;
import java.util.Comparator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import com.google.inject.Inject;

import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import com.electriccloud.commander.client.domain.Resource;

import ecinternal.client.ui.InternalMessages;

import static ecplugins.SSO_Configurator.client.SSOConfiguratorConstants.KEYTAB_SCREEN;

import static com.electriccloud.commander.gwt.client.ComponentBaseFactory.getPluginName;

public class SSOConfiguratorManagementView
    extends ViewWithUiHandlers<SSOConfiguratorManagementUiHandlers>
    implements SSOConfiguratorManagementPresenter.MyView
{

    //~ Instance fields --------------------------------------------------------

    private final Widget m_widget;

    // Gin injected fields
    @UiField ScrollPanel         m_CAMPanel;
    @UiField HorizontalPanel     m_buttonPanel;
    @UiField Button              m_okButton;
    @UiField HorizontalPanel     m_nextPanel;
    @UiField Button              m_cancelButton;
    @UiField Button              m_previousButton;
    @UiField HorizontalPanel     m_previousPanel;
    @UiField Button              m_finishButton;
    @UiField HorizontalPanel     m_finishPanel;
    @UiField InternalMessages    i18N;
    @UiField DeckPanel           m_deck;
    @UiField Button              m_dismissButton;
    @UiField CellTable<Resource> m_errorTable;
    @UiField HorizontalPanel     m_errorPanel;

    //~ Constructors -----------------------------------------------------------

    @Inject public SSOConfiguratorManagementView(Binder binder)
    {
        m_widget = binder.createAndBindUi(this);
        showButtonPanel(false);
        m_deck.showWidget(0);
        m_deck.getWidget(1)
              .setVisible(false);

        // disable ok button till we don't know install / upgrade platform
        m_okButton.setEnabled(false);
    }

    @TestOnly SSOConfiguratorManagementView()
    {

        // No-op, used only in junit testing.
        m_widget = null;
    }

    //~ Methods ----------------------------------------------------------------

    @Override public Widget asWidget()
    {
        return m_widget;
    }

    @Override public void disableFinishButton()
    {
        m_finishButton.setEnabled(false);
    }

    @UiHandler(value = {"m_cancelButton", "m_dismissButton"})
    public void onCancel(ClickEvent event)
    {

        while (m_errorTable.getColumnCount() > 0) {
            m_errorTable.removeColumn(0);
        }

        m_errorPanel.clear();
        m_deck.showWidget(0);
        getUiHandlers().onCancel();
    }

    @UiHandler("m_finishButton")
    public void onFinish(ClickEvent event)
    {
        getUiHandlers().onFinish(getPluginName());
    }

    @UiHandler("m_okButton")
    public void onNext(ClickEvent event)
    {
        getUiHandlers().onNext();
    }

    @UiHandler("m_previousButton")
    public void onPrevious(ClickEvent event)
    {
        getUiHandlers().onPrevious();
    }

    @Override public void showButtonPanel(Boolean isShowing)
    {
        m_buttonPanel.setVisible(isShowing);
    }

    @Override public void showErrorPanel(
            Collection<Resource> resources,
            boolean              platformIssue,
            boolean              zonesIssue) { }

    private Comparator<Resource> getNameComparator()
    {
        return new Comparator<Resource>() {
            @Override public int compare(
                    Resource o1,
                    Resource o2)
            {

                if (o1 == o2) {
                    return 0;
                }

                return o1.getName()
                         .compareTo(o2.getName());
            }
        };
    }

    @NotNull private Comparator<Resource> getPlatformComparator()
    {
        return new Comparator<Resource>() {
            @Override public int compare(
                    Resource o1,
                    Resource o2)
            {

                if (o1 == o2) {
                    return 0;
                }

                String platform1 = o1.getHostPlatform();

                if (platform1 == null) {
                    return -1;
                }

                String platform2 = o2.getHostPlatform();

                if (platform2 == null) {
                    return 1;
                }

                return o1.getHostPlatform()
                         .compareTo(o2.getHostPlatform());
            }
        };
    }

    @Override public boolean isEnabledOkButton()
    {
        return m_okButton.isEnabled();
    }

    @Override public void setButtonVisible(
            String  screenName,
            boolean error)
    {
        String blueStyle = "blueButton";
        String greyStyle = "greyButton";

        // hide and show buttons - previous, next, cancel and finish based on
        // the active screen name
        if (error && screenName.equals(KEYTAB_SCREEN)) {

            // if there is error on welcome screen only show the cancel
            // button.hide the rest
            m_previousPanel.setVisible(false);
            m_finishPanel.setVisible(false);
            m_nextPanel.setVisible(false);
            m_cancelButton.setVisible(true);

            //
            m_cancelButton.setWidth("150px");
            m_cancelButton.removeStyleName(greyStyle);
            m_cancelButton.addStyleName(blueStyle);

            return;
        }

        m_cancelButton.setWidth("100%");
        m_cancelButton.removeStyleName(blueStyle);
        m_cancelButton.addStyleName(greyStyle);

        if (screenName.equals(KEYTAB_SCREEN)) {
            m_previousPanel.setVisible(false);
            m_finishPanel.setVisible(false);
            m_cancelButton.setVisible(true);
            m_nextPanel.setVisible(true);

            //
            m_nextPanel.addStyleName(blueStyle);
        }
        else if (screenName.equals(SSOConfiguratorConstants.SUMMARY_SCREEN)) {
            m_nextPanel.setVisible(false);
            m_previousPanel.setVisible(true);
            m_finishPanel.setVisible(true);
            m_finishButton.setEnabled(true);
            m_cancelButton.setVisible(true);

            //
            m_nextPanel.removeStyleName(blueStyle);
            m_finishPanel.addStyleName(blueStyle);
        }
        else {
            m_finishPanel.setVisible(false);
            m_previousPanel.setVisible(true);
            m_nextPanel.setVisible(true);
            m_cancelButton.setVisible(true);

            //
            m_nextPanel.addStyleName(blueStyle);
        }
    }

    @Override public void setEnabledOkButton(boolean isEnable)
    {
        m_okButton.setEnabled(isEnable);
    }

    @Override public void setInSlot(
            Object slot,
            Widget content)
    {

        // set the active screen in the popup slot
        if (slot.equals(SSOConfiguratorManagementPresenter.TYPE_SSOMainSlot)) {
            m_CAMPanel.setWidget(content);
            showButtonPanel(true);
        }
        else {
            super.setInSlot(slot, content);
        }
    }

    //~ Inner Interfaces -------------------------------------------------------

    public interface Binder
        extends UiBinder<Widget, SSOConfiguratorManagementView> { }

    public interface UIStyle
        extends CssResource
    {

        //~ Methods ------------------------------------------------------------

        String buttonPadding();

        String htmlBorder();
    }
}
