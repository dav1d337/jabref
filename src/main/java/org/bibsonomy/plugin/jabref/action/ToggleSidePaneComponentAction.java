package org.bibsonomy.plugin.jabref.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import net.sf.jabref.gui.JabRefFrame;
import net.sf.jabref.gui.SidePaneManager;
import net.sf.jabref.logic.l10n.Localization;

import org.bibsonomy.plugin.jabref.BibSonomySidePaneComponent;
import org.bibsonomy.plugin.jabref.gui.BibSonomySidePanel;

/**
 * Display or hide the {@link BibSonomySidePanel}
 *
 * @author Waldemar Biller <biller@cs.uni-kassel.de>
 */
public class ToggleSidePaneComponentAction extends AbstractAction {

    private SidePaneManager manager;
    private JabRefFrame jabRefFrame;

    private BibSonomySidePaneComponent sidePaneComponent;

    public void actionPerformed(ActionEvent e) {
        if (!manager.hasComponent(BibSonomySidePaneComponent.class)) {
            manager.register(sidePaneComponent);
        }

        if (jabRefFrame.getTabbedPane().getTabCount() > 0) {
            manager.toggle(BibSonomySidePaneComponent.class);
        }

    }

    public ToggleSidePaneComponentAction(BibSonomySidePaneComponent sidePaneComponent) {
        super(Localization.lang("Search entries"), new ImageIcon(ToggleSidePaneComponentAction.class.getResource("/images/images/tag-label.png")));

        this.sidePaneComponent = sidePaneComponent;
        this.manager = sidePaneComponent.getSidePaneManager();
        this.jabRefFrame = sidePaneComponent.getJabRefFrame();
    }
}