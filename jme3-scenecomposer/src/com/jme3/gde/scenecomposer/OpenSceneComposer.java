/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.scenecomposer;

import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.assets.BinaryModelDataObject;
import com.jme3.scene.Spatial;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Confirmation;

public final class OpenSceneComposer implements ActionListener {

    private final BinaryModelDataObject context;

    public OpenSceneComposer(BinaryModelDataObject context) {
        this.context = context;
    }

    public void actionPerformed(ActionEvent ev) {
        final ProjectAssetManager manager = context.getLookup().lookup(ProjectAssetManager.class);
        if (manager == null) {
            return;
        }
        Runnable call = () -> {
            ProgressHandle progressHandle = ProgressHandle.createHandle("Opening in SceneComposer");
            progressHandle.start();
            try {
                manager.clearCache();
                final Spatial asset = context.loadAsset();
                if (asset != null) {
                    java.awt.EventQueue.invokeLater(() -> {
                        SceneComposerTopComponent composer = SceneComposerTopComponent.findInstance();
                        composer.openScene(asset, context, manager);
                    });
                } else {
                    Confirmation msg = new NotifyDescriptor.Confirmation(
                            "Error opening " + context.getPrimaryFile().getNameExt(),
                            NotifyDescriptor.OK_CANCEL_OPTION,
                            NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(msg);
                }
            } finally {
                progressHandle.finish();
            }
        };
        new Thread(call).start();
    }
}
