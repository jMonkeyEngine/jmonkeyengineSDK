/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.jme3.gde.assetbrowser.widgets;

import com.jme3.gde.assetbrowser.dnd.AssetPreviewWidgetMouseListener;
import com.jme3.gde.materials.dnd.TextureMoveHandler;
import com.jme3.gde.core.icons.IconList;
import com.jme3.gde.core.scene.PreviewRequest;
import com.jme3.gde.core.scene.SceneListener;
import com.jme3.gde.core.scene.SceneRequest;
import com.jme3.gde.core.dnd.AssetNameHolder;
import javax.swing.Icon;

/**
 * Displays an asset as an image in the AssetBrowser
 * @author rickard
 */
public class AssetPreviewWidget extends javax.swing.JPanel implements SceneListener, AssetNameHolder {

    /**
     * Creates new form AssetPreviewWidget
     */
    public AssetPreviewWidget() {
        initComponents();
        setTransferHandler(TextureMoveHandler.createFor(this));
    }
    
    public AssetPreviewWidget(PreviewInteractionListener listener) {
        this();
        addMouseListener(new AssetPreviewWidgetMouseListener(this, listener));
    }

    public void setPreviewImage(Icon icon) {
        assetPreviewLabel.setIcon(icon);
    }

    public void setPreviewName(String name) {
        assetNameLabel.setText(name);
    }
    
    public String getPreviewName() {
        return assetNameLabel.getText();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        assetNameLabel = new javax.swing.JLabel();
        assetPreviewLabel = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(170, 180));

        org.openide.awt.Mnemonics.setLocalizedText(assetNameLabel, org.openide.util.NbBundle.getMessage(AssetPreviewWidget.class, "AssetPreviewWidget.assetNameLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(assetPreviewLabel, org.openide.util.NbBundle.getMessage(AssetPreviewWidget.class, "AssetPreviewWidget.assetPreviewLabel.text")); // NOI18N
        assetPreviewLabel.setPreferredSize(new java.awt.Dimension(150, 150));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(assetNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(assetPreviewLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(assetPreviewLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(assetNameLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        assetNameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AssetPreviewWidget.class, "AssetPreviewWidget.assetNameLabel.AccessibleContext.accessibleName")); // NOI18N
        assetPreviewLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AssetPreviewWidget.class, "AssetPreviewWidget.assetPreviewLabel.AccessibleContext.accessibleName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel assetNameLabel;
    private javax.swing.JLabel assetPreviewLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void sceneOpened(SceneRequest request) {
    }

    @Override
    public void sceneClosed(SceneRequest request) {
    }

    @Override
    public void previewCreated(PreviewRequest request) {
        if (request.getRequester() == this) {
            System.out.println("preview generated " + this.hashCode() + " " + getParent());
            java.awt.EventQueue.invokeLater(() -> {
                System.out.println("setting icon " + this.hashCode() + " " + getParent());
                assetPreviewLabel.setIcon(IconList.asset);
//                    invalidate();
                revalidate();
                repaint();
//                    updateUI();
            });
        }
    }

    @Override
    public String getAssetName() {
        return assetNameLabel.getText();
    }

    @Override
    public void setAssetName(String name) {
        assetNameLabel.setText(name);
    }
    

}
